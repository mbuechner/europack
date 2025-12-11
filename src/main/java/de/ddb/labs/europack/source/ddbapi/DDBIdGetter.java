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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ddb.labs.europack.gui.Preferences;
import de.ddb.labs.europack.gui.helper.PreferencesUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class DDBIdGetter {

    private final static Logger LOG = LoggerFactory.getLogger(DDBIdGetter.class);
    private static final Marker FILE_MARKER = MarkerFactory.getMarker("FILE");
    private final static int ENTITYCOUNT = 1000; // count of entities per query
    // Throttle at the source to prevent downloader/OkHttp queue explosion
    // when filters are off or processor is saturated.
    final int backlogLimit = 1024; // conservative fixed limit
    private final String api;
    private final String apiKey;
    private final ObjectMapper m;
    private final OkHttpClient client;
    private final String query;
    private EdmDownloader downloader;
    private boolean done, canceled;
    private List<String> firstIds;
    private int numberOfResults = -1;
    private int errors;
    private String edmProfile;

    private final static List<String> TEST_IDS = new ArrayList<String>() {
        private static final long serialVersionUID = 7913459012651874859L;

        {
            final Properties properties = new Properties();
            try (final BufferedReader is = new BufferedReader(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(".properties"),
                    Charset.forName("UTF-8")))) {
                properties.load(is);
                final String pv = properties.getProperty("europack.testdata", "");
                final String[] pva = pv.split("\\|");
                addAll(Arrays.asList(pva));
            } catch (Exception e) {
                LOG.warn("Could not load test data IDs from properies. {}", e.getMessage());
            }
        }
    };

    public DDBIdGetter(String api, String query) throws InterruptedException, IOException {
        this(api, query, null, "");
    }

    public DDBIdGetter(String api, String query, EdmDownloader downloader, String edmProfile)
            throws InterruptedException, IOException {
        this.errors = 0;
        this.m = new ObjectMapper();
        this.downloader = downloader;
        this.client = HttpClientProvider.getClient();
        this.done = false;
        this.canceled = false;
        this.api = api;
        this.edmProfile = edmProfile;

        final HashMap<String, String> hm = PreferencesUtil.getMap(Preferences.getPREFS(), "ddbapikeys");
        this.apiKey = hm.get(api);

        if (query.equals("ddbtest")) {
            final StringBuilder sbQ = new StringBuilder();
            for (String s : TEST_IDS) {
                sbQ.append("id:");
                sbQ.append(s);
                sbQ.append(" OR ");
            }

            this.query = URLEncoder.encode(sbQ.substring(0, sbQ.length() - 4), "UTF-8");
        } else {
            this.query = URLEncoder.encode(query, "UTF-8");
        }
        this.firstIds = new ArrayList<>();
        LOG.info("API: '{}'", this.api);
        LOG.info("Search query: '{}'", this.query);
        LOG.info("EDM profile: '{}'", this.edmProfile);
    }

    public void run() throws IOException {
        if (downloader == null) {
            LOG.error(FILE_MARKER, "EdmDownloader is null");
            return;
        }
        done = false;
        setCanceled(false);

        final int itemsToDownload = getNumberOfResults();
        LOG.info("There are {} DDB objects to download.", itemsToDownload);
        downloader.setItemsToDownload(itemsToDownload);

        String nextCursorMark = "*";
        int count = 0;
        while (nextCursorMark != null && !nextCursorMark.isBlank() && !isCanceled()) {
            final List<String> list = new ArrayList<>();
            final String nextCursorMarkTmp = findDdbIds(list, nextCursorMark);
            count += list.size();
            LOG.info(FILE_MARKER, "{} items added, it's {} all in all now.", list.size(), count);

            for (String ddbId : list) {
                if (isCanceled()) {
                    break;
                }
                try {
                    while (!isCanceled() && downloader.getBacklog() > backlogLimit) {
                        Thread.sleep(50);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                Request request;

                if (getEdmProfile().isBlank()) {
                    request = new Request.Builder()
                            .url(api + "/items/" + ddbId + "/edm")
                            .addHeader("Accept", "application/xml")
                            .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                            .build();
                } else {
                    request = new Request.Builder()
                            .url(api + "/items/" + ddbId + "/edm")
                            .addHeader("Accept", "application/xml")
                            .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                            .addHeader("Accept-Profile", getEdmProfile())
                            .build();
                }

                downloader.addDownloadJob(ddbId, request, false);
            }

            if (nextCursorMark.equals(nextCursorMarkTmp)) {
                break;
            }
            nextCursorMark = nextCursorMarkTmp;
        }
        done = true;
        errors = itemsToDownload - count; // should be 0 if successfull
    }

    public void addAdditionalJobs(List<String> ddbIds, boolean removeFromErrors) {
        for (String ddbId : ddbIds) {
            if (isCanceled()) {
                break;
            }
            try {
                while (!isCanceled() && downloader.getBacklog() > backlogLimit) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            Request request;
            if (getEdmProfile().isBlank()) {
                request = new Request.Builder()
                        .url(api + "/items/" + ddbId + "/edm")
                        .addHeader("Accept", "application/xml")
                        .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                        .build();
            } else {
                request = new Request.Builder()
                        .url(api + "/items/" + ddbId + "/edm")
                        .addHeader("Accept", "application/xml")
                        .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                        .addHeader("Accept-Profile", getEdmProfile())
                        .build();
            }
            downloader.addDownloadJob(ddbId, request, removeFromErrors);
        }
    }

    public void dispose() {
        // nothing todo
    }

    public synchronized boolean hadErrors() {
        return errors > 0;
    }

    public synchronized int getErrors() {
        return errors;
    }

    /**
     *
     * @return @throws IOException
     */
    public synchronized int getNumberOfResults() throws IOException {
        if (numberOfResults != -1) {
            return numberOfResults;
        }

        final String urltmp = api + "/search?" + (firstIds.isEmpty() ? "" : "count=0&") + "query=" + query;
        final Request request = new Request.Builder()
                .url(urltmp)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                .build();
        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final ResponseBody rb = response.body();
                if (rb != null) {
                    try (final InputStream searchResult = rb.byteStream()) {
                        final JsonNode jnrt = m.readTree(searchResult);
                        if (firstIds.isEmpty()) {
                            final List<JsonNode> resultsNode = jnrt.findValues("id");
                            for (JsonNode jn : resultsNode) {
                                if (jn.isTextual() && jn.asText("").length() == 32) {
                                    firstIds.add(jn.asText());
                                }
                            }
                        }
                        numberOfResults = jnrt.get("numberOfResults").asInt(-1);
                    }
                }
            } else {
                throw new ConnectException("Response for " + response.request().url().toString() + " is "
                        + response.code() + " (" + response.message() + ")");
            }
        }
        return numberOfResults;
    }

    public synchronized List<String> getFirstDdbIds() {
        if (firstIds.isEmpty()) {
            findDdbIds(firstIds, null);
        }
        return firstIds;
    }

    /**
     *
     * @param list       List to add DDB URIs to.
     * @param cursorMark Current cursor mark
     * @return Next cursor marl
     */
    private String findDdbIds(List<String> list, String cursorMark) {
        try {
            if (cursorMark == null || cursorMark.isBlank()) {
                cursorMark = "*";
            }
            String nextCursorMark = null;
            final String urltmp = api + "/search?query=" + query
                    + "&cursorMark=" + URLEncoder.encode(cursorMark, Charset.forName("UTF-8"))
                    + "&rows=" + ENTITYCOUNT
                    + "&sort=" + URLEncoder.encode("id asc", Charset.forName("UTF-8"));

            final Request request = new Request.Builder()
                    .url(urltmp)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "OAuth oauth_consumer_key=\"" + apiKey + "\"")
                    .build();
            try (final Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    final ResponseBody rb = response.body();
                    if (rb != null) {
                        try (final InputStream searchResult = rb.byteStream()) {
                            final JsonNode jnrt = m.readTree(searchResult);
                            final List<JsonNode> resultsNode = jnrt.findValues("id");
                            nextCursorMark = jnrt.get("nextCursorMark").asText("");
                            LOG.debug("cursorMark in/out: {} -> {}", cursorMark, nextCursorMark);

                            for (JsonNode jn : resultsNode) {
                                if (jn.isTextual() && jn.asText("").length() == 32) {
                                    list.add(jn.asText());
                                }
                            }
                        }
                    }
                } else {
                    throw new ConnectException(response.toString());
                }
            }
            return nextCursorMark;
        } catch (IOException ex) {
            LOG.error(FILE_MARKER, "{}", ex.getMessage());
            return null;
        }
    }

    /**
     * @param downloader the downloader to set
     */
    public void setDownloader(EdmDownloader downloader) {
        this.downloader = downloader;
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
            LOG.warn("{} canceled", DDBIdGetter.class.getSimpleName());
        }
    }

    /**
     * @return the done
     */
    public synchronized boolean isDone() {
        return done;
    }

    /**
     * @return the edmProfile
     */
    public String getEdmProfile() {
        return edmProfile;
    }

    /**
     * @param edmProfile the edmProfile to set
     */
    public void setEdmProfile(String edmProfile) {
        this.edmProfile = edmProfile;
        LOG.info("EDM profile set to '{}'", this.edmProfile);
    }
}
