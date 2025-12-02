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

import java.util.concurrent.TimeUnit;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public final class HttpClientProvider {

    private static final int MAX_REQUESTS = 64;
    private static final int MAX_REQUESTS_PER_HOST = 16;
    private static final int CONNECT_TIMEOUT_SEC = 2;
    private static final int WRITE_TIMEOUT_SEC = 10;
    private static final int READ_TIMEOUT_SEC = 16;
    private static final int CALL_TIMEOUT_SEC = 32;

    private static final OkHttpClient CLIENT;

    static {
        final Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(MAX_REQUESTS_PER_HOST);

        CLIENT = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .callTimeout(CALL_TIMEOUT_SEC, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .addInterceptor(new MetricsInterceptor())
                .build();

        // Ensure we release resources on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CLIENT.dispatcher().executorService().shutdownNow();
            } catch (Throwable ignore) {
            }
            try {
                CLIENT.connectionPool().evictAll();
            } catch (Throwable ignore) {
            }
            try {
                if (CLIENT.cache() != null)
                    CLIENT.cache().close();
            } catch (Throwable ignore) {
            }
        }));

        HttpMetrics.init();
    }

    private HttpClientProvider() {
    }

    static OkHttpClient getClient() {
        return CLIENT;
    }

    // Expose metrics shutdown to other packages without making HttpMetrics public
    public static void shutdownMetrics() {
        // Emit a final summary before shutting metrics down
        HttpMetrics.logSummary("manual-shutdown");
        HttpMetrics.shutdown();
        // Reset counters so the next run starts clean
        HttpMetrics.reset();
    }

    public static void updateDownloadProgress(int totalItems, int downloaded, int downloadErrors) {
        HttpMetrics.updateDownloadProgress(totalItems, downloaded, downloadErrors);
    }

    public static void updateProcessorErrors(int processorErrors) {
        HttpMetrics.updateProcessorErrors(processorErrors);
    }

    public static void updateProcessed(int processed) {
        HttpMetrics.updateProcessed(processed);
    }

    // Reset and (re)start metrics for a new run
    public static void resetAndStartMetrics() {
        HttpMetrics.reset();
        HttpMetrics.init();
    }

    static void setConcurrency(int maxRequests, int maxRequestsPerHost) {
        if (maxRequests > 0)
            CLIENT.dispatcher().setMaxRequests(maxRequests);
        if (maxRequestsPerHost > 0)
            CLIENT.dispatcher().setMaxRequestsPerHost(maxRequestsPerHost);
    }
}
