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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class EdmNamespaces {

    private static final Map<String, String> URI_NS, NS_URI;

    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("http://www.w3.org/2004/02/skos/core#", "skos");
        aMap.put("http://www.europeana.eu/schemas/edm/", "edm");
        aMap.put("http://www.openarchives.org/ore/terms/", "ore");
        aMap.put("http://www.deutsche-digitale-bibliothek.de/edm/", "ddb");
        aMap.put("http://purl.org/dc/terms/", "dcterms");
        aMap.put("http://www.w3.org/2004/02/skos/core#", "skos");
        aMap.put("http://purl.org/dc/elements/1.1/", "dc");
        aMap.put("http://www.w3.org/2003/01/geo/wgs84_pos#", "wgs84_pos");
        aMap.put("http://ddb.vocnet.org/sparte/", "ddbsector");
        aMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        aMap.put("http://xmlns.com/foaf/0.1/", "foaf");
        aMap.put("http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#", "crm");
        aMap.put("http://www.w3.org/XML/1998/namespace", "xml");
        aMap.put("http://www.w3.org/2000/xmlns/", "xmlns");
        URI_NS = Collections.unmodifiableMap(aMap);

        aMap = new HashMap<>();
        for (Entry<String, String> e : URI_NS.entrySet()) {
            aMap.put(e.getValue(), e.getKey());
        }

        NS_URI = Collections.unmodifiableMap(aMap);
    }

    /**
     *
     * @return
     */
    public static Map<String, String> getUriNs() {
        return URI_NS;
    }

    /**
     *
     * @return
     */
    public static Map<String, String> getNsUri() {
        return NS_URI;
    }
}
