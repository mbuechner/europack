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

import de.ddb.labs.europack.processor.EuropackFilterProcessor;
import de.ddb.labs.europack.processor.EuropackDoc;
import java.io.IOException;
import java.net.ConnectException;
import javax.xml.parsers.ParserConfigurationException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.xml.sax.SAXException;

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class EdmDownloader {

    private final static Logger LOG = LoggerFactory.getLogger(EdmDownloader.class);
    private static final Marker FILE_MARKER = MarkerFactory.getMarker("FILE");

    private final OkHttpClient client;
    private final String cacheId;
    private final EuropackFilterProcessor epfp;
    private int itemsToDownload, itemsDowloaded;
    private boolean done, canceled;
    private int errors;

    public EdmDownloader(String cacheId, EuropackFilterProcessor epfp) throws InterruptedException, IOException {
        client = HttpClientProvider.getClient();
        this.cacheId = cacheId;
        this.epfp = epfp;
        this.itemsDowloaded = 0;
        this.itemsToDownload = Integer.MAX_VALUE;
        this.done = true;
        this.canceled = false;
        this.errors = 0;
        LOG.info("Download ID is {}. Cache opened..", cacheId);
    }

    public synchronized void addDownloadJob(String ddbId, Request request, boolean removeFromErrors) {
        if (canceled) {
            return;
        }
        try {
            client.newCall(request).enqueue(new MyCallback(ddbId));
        } catch (Exception e) {
            LOG.error(FILE_MARKER, "{}", e.getMessage(), e);
        }
        this.done = false;
        if (removeFromErrors) {
            final boolean removed = CacheManager.getInstance().removeErrorById(cacheId, ddbId);
            if (removed) {
                if (errors > 0) {
                    --errors;
                }
                final int epfpErrors = epfp.getErrors();
                epfp.setErrors(Math.max(0, epfpErrors - 1));
            }
        }
    }

    /**
     * Current processor backlog (added - processed).
     * Used by producers (e.g., DDBIdGetter) to throttle submissions.
     */
    public synchronized int getBacklog() {
        return epfp.getAddedJobs() - epfp.getProcessedJobs();
    }

    public void reset() {
        this.itemsDowloaded = 0;
        this.itemsToDownload = Integer.MAX_VALUE;
        this.done = true;
        this.canceled = false;
        this.errors = 0;
    }

    public void dispose() {
        client.dispatcher().cancelAll();
        client.dispatcher().executorService().shutdownNow();
        client.connectionPool().evictAll();
        try {
            if (client.cache() != null) {
                client.cache().close();
            }
        } catch (IOException ex) {
            // nothing
        }
    }

    class MyCallback implements Callback {

        private final String id;

        public MyCallback(String id) {
            this.id = id;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            if (isCanceled()) {
                return;
            }
            LOG.error(FILE_MARKER, "{}: {}", id, e.getLocalizedMessage(), e);
            CacheManager.getInstance().addError(cacheId, new EuropackDoc(id));
            incErrors();
            finishing();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (isCanceled()) {
                return;
            }
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) {
                    throw new ConnectException(response.toString());
                } else {

                // debugging
                // if (new Random().nextInt(100) < 1) {
                //    throw new ConnectException("Statistical error for debugging thrown. " + response.toString());
                // }
                    final EuropackDoc ed = new EuropackDoc(id, responseBody.byteStream());
                    CacheManager.getInstance().put(cacheId, ed);
                    epfp.addJob(id);
                }

            } catch (ConnectException | IllegalArgumentException | SAXException | ParserConfigurationException | NullPointerException ex) {
                LOG.error(FILE_MARKER, "{}: {}", id, ex.getMessage());
                CacheManager.getInstance().addError(cacheId, new EuropackDoc(id));
                incErrors();
            } finally {
                finishing();
            }
        }

        private void finishing() {
            final int getItemsDowloaded = incItemsDowloaded();
            final int getItemsToDownload = getItemsToDownload();
            if (getItemsDowloaded % 1000 == 0 || getItemsDowloaded >= getItemsToDownload) {
                LOG.info(FILE_MARKER, "{} of {} downloaded", getItemsDowloaded, (getItemsToDownload == Integer.MAX_VALUE ? "?" : getItemsToDownload));
            }
            if (getItemsDowloaded >= getItemsToDownload) {
                setDone(true);
            }
            // Update domain metrics snapshot
            HttpClientProvider.updateDownloadProgress(getItemsToDownload, getItemsDowloaded, errors);
        }

    }

    /**
     * @return the itemsToDownload
     */
    public synchronized int getItemsToDownload() {
        return itemsToDownload;
    }

    /**
     * @param itemsToDownload the itemsToDownload to set
     */
    public synchronized void setItemsToDownload(int itemsToDownload) {
        this.itemsToDownload = itemsToDownload;
    }

    /**
     * @return the itemsDowloaded
     */
    public synchronized int getItemsDowloaded() {
        return itemsDowloaded;
    }

    /**
     * @return 
     */
    public synchronized int incItemsDowloaded() {
        return ++itemsDowloaded;
    }

    /**
     * @return the isDone
     */
    public synchronized boolean isDone() {
        return done;
    }

    private synchronized void setDone(boolean isDone) {
        this.done = isDone;
    }

    /**
     * @return the hadErrors
     */
    public synchronized boolean hadErrors() {
        return (errors > 0);
    }

    /**
     * @return the errors
     */
    public synchronized int getErrors() {
        return errors;
    }

    public synchronized void incErrors() {
        ++errors;
    }

    /**
     * @param itemsDowloaded the itemsDowloaded to set
     */
    public synchronized void setItemsDowloaded(int itemsDowloaded) {
        this.itemsDowloaded = itemsDowloaded;
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
            LOG.warn("{} canceled", EdmDownloader.class.getSimpleName());
        }
    }
}



