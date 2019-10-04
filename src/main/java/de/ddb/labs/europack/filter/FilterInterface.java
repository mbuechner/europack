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
package de.ddb.labs.europack.filter;

import de.ddb.labs.europack.processor.EuropackDoc;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public interface FilterInterface {

    /**
     * Run the filter on an EuropackDoc
     *
     * @param ed
     * @throws Exception
     */
    public void filter(EuropackDoc ed) throws Exception;

    /**
     * Description of this Filter
     *
     * @return
     */
    public String getDescription();

    /**
     * Name of this Filter
     *
     * @return
     */
    public String getName();

}
