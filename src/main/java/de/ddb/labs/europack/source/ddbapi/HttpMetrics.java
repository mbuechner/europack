/*
 * Copyright 2019, 2025 Michael Büchner <m.buechner@dnb.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ddb.labs.europack.source.ddbapi;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import javax.net.ssl.SSLException;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HttpMetrics {

    private static final Logger LOG = LoggerFactory.getLogger(HttpMetrics.class);

    private static final LongAdder total = new LongAdder();
    private static final LongAdder s2xx = new LongAdder();
    private static final LongAdder s4xx = new LongAdder();
    private static final LongAdder s5xx = new LongAdder();
    private static final LongAdder s406 = new LongAdder();

    private static final LongAdder exTimeout = new LongAdder();
    private static final LongAdder exConnect = new LongAdder();
    private static final LongAdder exSSL = new LongAdder();
    private static final LongAdder exOther = new LongAdder();

    // Removed latency queue to avoid memory growth under very long runs

    private static volatile int intervalSec = 30;

    private static ScheduledExecutorService scheduler;
    // Domain (EDM pipeline) snapshot counters
    private static volatile int domainTotalItems = 0;
    private static volatile int domainDownloaded = 0; // includes successes + download errors
    private static volatile int domainDownloadErrors = 0;
    private static volatile int domainProcessErrors = 0;
    private static volatile int domainProcessed = 0;

    // Logging reduction helpers
    private static final int LOG_DELTA_THRESHOLD = 1000; // log only if change >= 1000
    private static final int LOG_EVERY_N_TICKS = 10;    // or every 10th tick as a heartbeat
    private static int lastProcessed = 0;
    private static int lastRunning = 0;
    private static int lastQueued = 0;
    private static int tickCounter = 0;

    private HttpMetrics() {
    }

    static void init() {
        if (scheduler != null)
            return;
        // Use a daemon scheduler thread so it won't block JVM shutdown
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EuropackHttpMetrics");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(HttpMetrics::tick, intervalSec, intervalSec, TimeUnit.SECONDS);
        LOG.info("HTTP metrics enabled (interval={}s)", intervalSec);

        // Emit a summary when the JVM shuts down
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logSummary("jvm-shutdown");
                } catch (Throwable ignored) {
                }
            }, "EuropackHttpMetricsSummary"));
        } catch (Throwable ignored) {
            // ignore if hooks not allowed
        }
    }

    static void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }
        } catch (Exception ignored) {
            // noop
        } finally {
            scheduler = null;
        }
    }

    // Reset all counters and snapshots. Call between runs.
    static void reset() {
        try {
            total.reset();
            s2xx.reset();
            s4xx.reset();
            s5xx.reset();
            s406.reset();
            exTimeout.reset();
            exConnect.reset();
            exSSL.reset();
            exOther.reset();
            // no latency buffer to clear
            domainTotalItems = 0;
            domainDownloaded = 0;
            domainDownloadErrors = 0;
            domainProcessErrors = 0;
            domainProcessed = 0;
            lastProcessed = 0;
            lastRunning = 0;
            lastQueued = 0;
            tickCounter = 0;
        } catch (Throwable ignored) {
        }
    }

    // Domain update methods (forwarded via HttpClientProvider)
    static void updateDownloadProgress(int totalItems, int downloaded, int downloadErrors) {
        if (totalItems >= 0)
            domainTotalItems = totalItems;
        if (downloaded >= 0)
            domainDownloaded = downloaded;
        if (downloadErrors >= 0)
            domainDownloadErrors = downloadErrors;
    }

    static void updateProcessorErrors(int procErrors) {
        if (procErrors >= 0)
            domainProcessErrors = procErrors;
    }

    static void updateProcessed(int processed) {
        if (processed >= 0)
            domainProcessed = processed;
    }

    static void recordStatus(int code, long durMsVal) {
        total.increment();
        if (code >= 200 && code <= 299)
            s2xx.increment();
        else if (code >= 400 && code <= 499)
            s4xx.increment();
        else if (code >= 500 && code <= 599)
            s5xx.increment();
        if (code == 406)
            s406.increment();
        // omit latency recording to stay memory-frugal
    }

    static void recordException(IOException ex, long durMsVal) {
        total.increment();
        if (ex instanceof SocketTimeoutException)
            exTimeout.increment();
        else if (ex instanceof ConnectException)
            exConnect.increment();
        else if (ex instanceof SSLException)
            exSSL.increment();
        else
            exOther.increment();
        // omit latency recording to stay memory-frugal
    }

    private static void tick() {
        try {
            tickCounter++;
            // Cumulative totals since start
            final long t = total.sum();
            final long c2 = s2xx.sum();
            final long c4 = s4xx.sum();
            final long c5 = s5xx.sum();
            final long c406 = s406.sum();
            final long xt = exTimeout.sum();
            final long xc = exConnect.sum();
            final long xs = exSSL.sum();
            final long xo = exOther.sum();

            // Latency percentiles are omitted here to avoid confusion with cumulative
            // counts

            final OkHttpClient client = HttpClientProvider.getClient();
            final Dispatcher d = client.dispatcher();
            final int running = d.runningCallsCount();
            final int queued = d.queuedCallsCount();

                // Decide if we should log this tick
                boolean heartbeat = (tickCounter % LOG_EVERY_N_TICKS) == 0;
                int deltaProcessed = Math.abs(domainProcessed - lastProcessed);
                int deltaRunning = Math.abs(running - lastRunning);
                int deltaQueued = Math.abs(queued - lastQueued);
                boolean significantChange = (deltaProcessed >= LOG_DELTA_THRESHOLD)
                    || (deltaRunning >= LOG_DELTA_THRESHOLD)
                    || (deltaQueued >= LOG_DELTA_THRESHOLD);

                if ((domainTotalItems > 0 || t > 0 || running > 0 || queued > 0) && (heartbeat || significantChange)) {
                // Show HTTP code counts as percentage of total ITEMS (requested behavior)
                final double h2pct = pct(c2, domainTotalItems);
                final double h4pct = pct(c4, domainTotalItems);
                final double h5pct = pct(c5, domainTotalItems);

                int domainErrors = domainDownloadErrors + domainProcessErrors;
                final double domDownloadedPct = pct(domainDownloaded, domainTotalItems);
                final double domProcessedPct = pct(domainProcessed, domainTotalItems);
                final double domErrorsPct = pct(domainErrors, domainTotalItems);
                final int remaining = Math.max(domainTotalItems - domainProcessed, 0);
                final int success = Math.max(domainProcessed - domainProcessErrors, 0);
                final double successPct = pct(success, domainTotalItems);

                LOG.info(
                        "stats: items total={} processed={} ({}%) remaining={} success={} ({}%) downloaded={} ({}%) errors={} ({}%) [download={}, process={}] | http: requests={} inflight={} queued={} 2xx={} ({}% of items) 4xx={} ({}% of items) 406={} 5xx={} ({}% of items) exceptions: timeouts={} connect={} ssl={} other={}",
                        domainTotalItems,
                        domainProcessed, fmt(domProcessedPct),
                        remaining,
                        success, fmt(successPct),
                        domainDownloaded, fmt(domDownloadedPct),
                        domainErrors, fmt(domErrorsPct), domainDownloadErrors, domainProcessErrors,
                        t, running, queued,
                        c2, fmt(h2pct), c4, fmt(h4pct), c406, c5, fmt(h5pct),
                        xt, xc, xs, xo);

                // Update last snapshots after logging
                lastProcessed = domainProcessed;
                lastRunning = running;
                lastQueued = queued;
            }
        } catch (Throwable e) {
            LOG.warn("metrics tick failed: {}", e.toString());
        }
    }

    // Final one-shot summary. Kept package-private so provider can call on
    // cancel/exit.
    static void logSummary(String reason) {
        try {
            final long t = total.sum();
            final long c2 = s2xx.sum();
            final long c4 = s4xx.sum();
            final long c5 = s5xx.sum();
            final long c406 = s406.sum();
            final long xt = exTimeout.sum();
            final long xc = exConnect.sum();
            final long xs = exSSL.sum();
            final long xo = exOther.sum();

            final OkHttpClient client = HttpClientProvider.getClient();
            final Dispatcher d = client.dispatcher();
            final int running = d.runningCallsCount();
            final int queued = d.queuedCallsCount();

            final int domainErrors = domainDownloadErrors + domainProcessErrors;
            final int remaining = Math.max(domainTotalItems - domainProcessed, 0);
            final int success = Math.max(domainProcessed - domainProcessErrors, 0);

            LOG.info(
                    "summary ({}): items total={} processed={} remaining={} success={} downloaded={} errors={} [download={}, process={}] | http: requests={} inflight={} queued={} 2xx={} 4xx={} 406={} 5xx={} exceptions: timeouts={} connect={} ssl={} other={}",
                    reason,
                    domainTotalItems, domainProcessed, remaining, success,
                    domainDownloaded, domainErrors, domainDownloadErrors, domainProcessErrors,
                    t, running, queued,
                    c2, c4, c406, c5,
                    xt, xc, xs, xo);
        } catch (Throwable ignored) {
        }
    }

    private static double pct(long part, long total) {
        if (total <= 0)
            return 0.0;
        return (part * 100.0) / total;
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.1f", v);
    }
}
