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

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class AppThread extends Thread {

    private static final String POOL_DELIMITER = "-";

    /**
     * Application thread
     *
     * @param group
     * @param runnable, tasks to be processed
     * @param pool, name of the pool
     * @param id, Identifier for the thread
     */
    public AppThread(ThreadGroup group, Runnable runnable, String pool, int id) {
        super(group, runnable, String.format("%s%s%d", pool, POOL_DELIMITER, id));
    }
}


