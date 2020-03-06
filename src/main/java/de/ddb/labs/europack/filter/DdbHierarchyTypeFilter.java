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
package de.ddb.labs.europack.filter;

import de.ddb.labs.europack.processor.EuropackDoc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class DdbHierarchyTypeFilter implements FilterInterface {

    private Transformer transformer;

    public DdbHierarchyTypeFilter() {
    }

    @Override
    public void init() throws IOException, TransformerConfigurationException {
        final String xsltFileName = "filters/" + DdbHierarchyTypeFilter.class.getSimpleName() + ".xsl";
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream(xsltFileName);
        final InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        final BufferedReader br = new BufferedReader(isr);
        transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(br));
    }

    /**
     * edm:ProvidedCHO/ddb:hierarchyType - Löschen
     *
     * @param ed
     * @throws Exception
     */
    @Override
    public void filter(EuropackDoc ed) throws Exception {
        transformer.reset();
        final Document doc = ed.getDoc();
        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();
        transformer.transform(source, result);
        ed.setDoc((Document) result.getNode());
    }

    /**
     * Description of this Filter
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "edm:ProvidedCHO/ddb:hierarchyType - Löschen";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return DdbHierarchyTypeFilter.class.getSimpleName();
    }

}
