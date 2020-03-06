/*
 * Copyright 2019, 2020 Michael Büchner <m.buechner@dnb.de>.
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
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class CacheManager {

    private final static Logger LOG = LoggerFactory.getLogger(CacheManager.class);
    private final static CacheConfigurationBuilder<String, EuropackDoc> CCB = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, EuropackDoc.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(2048, MemoryUnit.MB)
                    .disk(10, MemoryUnit.GB, false));
    private final org.ehcache.CacheManager CM;
    private final Path tmpPath;
    private final Map<String, List<EuropackDoc>> errors;

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

    public synchronized void addError(String cacheId, EuropackDoc ed) {
        errors.get(cacheId).add(ed);
    }

    public synchronized List<EuropackDoc> getErrors(String cacheId) {
        return errors.get(cacheId);
    }

    public synchronized List<String> getErrorIds(String cacheId) {
        final List<String> list = new ArrayList<>();
        for (EuropackDoc e : errors.get(cacheId)) {
            list.add(e.getId());
        }
        return list;
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
}

