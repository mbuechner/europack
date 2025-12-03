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
package de.ddb.labs.europack.processor;

import de.ddb.labs.europack.filter.FilterInterface;
import de.ddb.labs.europack.sink.SinkInterface;
import de.ddb.labs.europack.source.ddbapi.CacheManager;
import de.ddb.labs.europack.source.ddbapi.HttpClientProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class EuropackFilterProcessor {
    
    private final static Logger LOG = LoggerFactory.getLogger(EuropackFilterProcessor.class);
    private static final Marker FILE_MARKER = MarkerFactory.getMarker("FILE");
    private final String cacheId;
    private final List<String> queueIds;
    private final int threads;
    private final int queueCapacity;
    private static final String CACHED_POOL = "EuropackEDMProcessor";
    private final ExecutorService exe;
    private final List<String> filter;
    private final List<SinkInterface> sinks;
    private int addedJobs, processedJobs;
    private boolean canceled;
    private int errors;
    
    public EuropackFilterProcessor(String cacheId, List<String> filter, List<SinkInterface> sinks) {
        this.queueIds = new ArrayList<>();
        this.threads = Integer.getInteger("europack.processor.threads", Runtime.getRuntime().availableProcessors());
        this.queueCapacity = Integer.getInteger("europack.processor.queueSize", Math.max(threads * 2, 64));
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueCapacity);
        this.exe = new ThreadPoolExecutor(
            threads,
            threads,
            30L,
            TimeUnit.SECONDS,
            workQueue,
            new AppThreadFactory(CACHED_POOL),
            new ThreadPoolExecutor.CallerRunsPolicy());
        LOG.info("Processor threads='{}', queueCapacity='{}'", threads, queueCapacity);
        this.cacheId = cacheId;
        this.filter = filter;
        this.addedJobs = 0;
        this.processedJobs = 0;
        this.errors = 0;
        this.sinks = sinks;
        this.canceled = false;
    }
    
    public void reset() {
        this.queueIds.clear();
        this.addedJobs = 0;
        this.processedJobs = 0;
        this.errors = 0;
        this.canceled = false;
    }
    
    public synchronized void addJob(String id) {
        if (canceled) {
            return;
        }
        queueIds.add(id);
        try {
            exe.submit(new MyRunnable(id));
//          final Future handler = exe.submit(new MyRunnable(id));
            // cancel after 10 Sek
//          exe.schedule(new Runnable() {
//              public void run() {
//                  handler.cancel(true);
//                  LOG.error("{}: was canceled after 10 sec. no response", id);
//              }
//          }, 10, TimeUnit.SECONDS);
            ++addedJobs;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            LOG.error(FILE_MARKER, "{}: Cannot filter item. {}", id, ex.getMessage(), ex);
        }
        if (addedJobs % 1000 == 0) {
            LOG.info("{} added jobs, {} processed jobs", addedJobs, processedJobs);
        }
    }
    
    public void dispose() {
        for (SinkInterface sink : sinks) {
            sink.dispose();
        }
        exe.shutdown();
        try {
            if (!exe.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.warn("Processor did not terminate in time; forcing shutdownNow()");
                exe.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            exe.shutdownNow();
        }
    }
    
    public synchronized boolean isDone() {
        return addedJobs <= processedJobs;
    }
    
    private class MyRunnable implements Runnable {
        
        private final String id;
        private final List<FilterInterface> filterInstance;
        
        public MyRunnable(String id) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.filterInstance = new ArrayList<>();
            this.id = id;
            
            for (String f : filter) {
                final Class<?> act = Class.forName("de.ddb.labs.europack.filter." + f);
                final Constructor<?> constr = act.getConstructor();
                final FilterInterface fi = (FilterInterface) constr.newInstance();
                filterInstance.add(fi);
            }
        }
        
        @Override
        public void run() {
            if (isCanceled()) {
                return;
            }
            final EuropackDoc ed = CacheManager.getInstance().get(cacheId, id);

            // debugging (1%)
//            if (new Random().nextInt(100) < 1) {
//                ed.setStatus(EuropackDoc.Status.INVALID_FILTER_FAILED);
//                CacheManager.getInstance().addError(cacheId, ed);
//                incErrors();
//                LOG.error("{}: Statistical sort out for debugging", id);
//            }
            for (FilterInterface f : filterInstance) {
                if (isCanceled()) {
                    return;
                }
                try {
                    if (ed.getStatus() == EuropackDoc.Status.VALID) {
                        f.init();
                        f.filter(ed);
                    }
                } catch (Exception | StackOverflowError ex) {
                    ed.setStatus(EuropackDoc.Status.INVALID_FILTER_FAILED);
                    CacheManager.getInstance().addError(cacheId, ed);
                    incErrors();
                    LOG.error(FILE_MARKER, "{}: {} said {}", id, f.getName(), ex.getMessage());
                }
            }
            if (ed.getStatus() == EuropackDoc.Status.VALID) {
                for (SinkInterface sink : sinks) {
                    if (isCanceled()) {
                        return;
                    }
                    try {
                        sink.filter(ed);
                    } catch (Exception | StackOverflowError ex) {
                        ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
                        CacheManager.getInstance().addError(cacheId, ed);
                        incErrors();
                        LOG.error(FILE_MARKER, "{}: {} said {}", id, sink.getName(), ex.getMessage());
                    }
                }
            }
            incProcessedJobs();
        }
    }

    /**
     * @param errors
     */
    public void setErrors(int errors) {
        this.errors = errors;
    }

    /**
     * @return the errors
     */
    public int getErrors() {
        return errors;
    }
    
    public synchronized boolean hadErrors() {
        return errors > 0;
    }

    /**
     * @return the addedJobs
     */
    public synchronized int getAddedJobs() {
        return addedJobs;
    }

    /**
     */
    public synchronized int incProcessedJobs() {
        int v = ++processedJobs;
        // refresh processor error snapshot along with progress
        HttpClientProvider.updateProcessorErrors(errors);
        HttpClientProvider.updateProcessed(v);
        return v;
    }
    
    public synchronized int getProcessedJobs() {
        return processedJobs;
    }

    /**
     */
    public synchronized void incErrors() {
        ++errors;
        HttpClientProvider.updateProcessorErrors(errors);
    }

    /**
     * @return the canceled
     */
    public synchronized boolean isCanceled() {
        return canceled;
    }

    /**
     * @param canceled the canceled to set
     */
    public synchronized void setCanceled(boolean canceled) {
        this.canceled = canceled;
        if (canceled) {
            LOG.warn("{} canceled", EuropackFilterProcessor.class.getSimpleName());
        }
    }
}


