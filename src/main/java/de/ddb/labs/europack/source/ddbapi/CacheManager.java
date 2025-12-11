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

import de.ddb.labs.europack.processor.EuropackDoc;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ehcache.Cache;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class CacheManager {

    private final static Logger LOG = LoggerFactory.getLogger(CacheManager.class);
    // Keep a small, entry-based on-heap store to minimize GC pressure,
    // move bulk data off-heap and to disk for large runs.
    private final static CacheConfigurationBuilder<String, EuropackDoc> CCB = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, EuropackDoc.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                            .heap(1000, EntryUnit.ENTRIES) // only a small number of hot entries on-heap
                            .offheap(512, MemoryUnit.MB) // bulk in off-heap to reduce GC impact
                            .disk(10, MemoryUnit.GB, false)); // persistent disk tier
    private final org.ehcache.CacheManager CM;
    private final Path tmpPath;
    // Track only IDs for errors to avoid retaining full documents in memory
    private final Map<String, List<String>> errors;

    private static final class InstanceHolder {

        static final CacheManager INSTANCE = new CacheManager();
    }

    private CacheManager() {

        Path pt;
        try {
            pt = Files.createTempDirectory("europack");
        } catch (IOException e1) {
            pt = Path.of("cache");
            pt.toFile().mkdir();
        }
        this.tmpPath = pt;
        this.errors = new HashMap<>();

        // download temporary files
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    /* Delete folder on exit. */
                    Files.walkFileTree(tmpPath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ex) {
                    // noting
                }
            }
        });

        this.CM = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(tmpPath.toFile()))
                .build(true);
    }

    public static CacheManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public synchronized void addCache(String cacheId) {
        try {
            CM.createCache(cacheId, CCB);
            errors.put(cacheId, new ArrayList<>());
        } catch (IllegalArgumentException e) {
            LOG.warn("{}", e.getMessage());
        }
    }

    public synchronized void removeCache(String cacheId) {
        CM.removeCache(cacheId);
        errors.remove(cacheId);
    }

    public synchronized void addError(String cacheId, String id) {
        if (id != null && !id.isBlank()) {
            errors.get(cacheId).add(id);
        }
    }

    public synchronized List<EuropackDoc> getErrors(String cacheId) {
        // Build lightweight wrappers on demand to preserve API without retaining docs
        final List<EuropackDoc> out = new ArrayList<>();
        final List<String> ids = errors.get(cacheId);
        if (ids != null) {
            for (String id : ids) {
                out.add(new EuropackDoc(id));
            }
        }
        return out;
    }

    /**
     * Remove first error entry with the given id from the error list of the cache.
     * 
     * @param cacheId cache identifier
     * @param id      document id to remove
     * @return true if an entry was removed, false otherwise
     */
    public synchronized boolean removeErrorById(String cacheId, String id) {
        final List<String> list = errors.get(cacheId);
        if (list == null || id == null) {
            return false;
        }
        final boolean removed = list.remove(id);
        if (removed)
            return true;
        return false;
    }

    public synchronized List<String> getErrorIds(String cacheId) {
        final List<String> ids = errors.get(cacheId);
        return (ids == null) ? new ArrayList<>() : new ArrayList<>(ids);
    }

    public synchronized void destroy() {
        if (CM.getStatus() == Status.AVAILABLE) {
            CM.close();
        }
    }

    public synchronized EuropackDoc get(String cacheId, String id) {
        final Cache<String, EuropackDoc> cacheLocal = CM.getCache(cacheId, String.class, EuropackDoc.class);
        return (EuropackDoc) cacheLocal.get(id);
    }

    public synchronized void put(String cacheId, EuropackDoc element) {
        final Cache<String, EuropackDoc> cacheLocal = CM.getCache(cacheId, String.class, EuropackDoc.class);
        cacheLocal.put(element.getId(), element);
    }

    /**
     * Remove a single document from the cache to free memory after processing.
     */
    public synchronized void remove(String cacheId, String id) {
        final Cache<String, EuropackDoc> cacheLocal = CM.getCache(cacheId, String.class, EuropackDoc.class);
        if (cacheLocal != null && id != null) {
            try {
                cacheLocal.remove(id);
            } catch (Exception ignore) {
            }
        }
    }
}
