/*
 * Copyright 2019 Michael Büchner <m.buechner@dnb.de>.
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
package de.ddb.labs.europack.source;

import de.ddb.labs.europack.processor.EuropackDoc;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public interface SourceInterface {

    /**
     *
     * @throws Exception
     */
    public void load() throws Exception;

    /**
     *
     * @return
     */
    public EuropackDoc getNext();

    /**
     *
     * @return
     */
    public boolean hasNext();

    /**
     *
     */
    public void dispose();

    /**
     *
     * @return
     */
    public int count();

    /**
     *
     * @return
     */
    public int pos();

    /**
     *
     * @return
     */
    public String getDescription();

    /**
     *
     * @return
     */
    public String getName();

}

