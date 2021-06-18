/*
 * Copyright 2019-2021 Michael Büchner <m.buechner@dnb.de>.
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

import de.ddb.labs.europack.processor.EdmNamespaces;
import de.ddb.labs.europack.processor.EuropackDoc;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class VgBildKunstFilter implements FilterInterface {

    private final static Logger LOG = LoggerFactory.getLogger(VgBildKunstFilter.class);
    private final static String EX0 = "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='WebResource']/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("dc") + "' and local-name()='rights']";
    private final static String EX1 = "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='ProvidedCHO']/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("dc") + "' and local-name()='rights']";

    private XPathFactory factory;

    public VgBildKunstFilter() {
    }

    @Override
    public void init() throws Exception {
        factory = XPathFactory.newInstance();
        if (factory == null) {
            throw new IllegalStateException("XPathFactory is null. This filter won't work.");
        }
    }

    /**
     * edm:WebResource/dc:type - Property und zugehörige skos:Concept-Class
     * löschen
     *
     * @param ed
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Override
    public void filter(EuropackDoc ed) throws XPathExpressionException {
        final Document doc = ed.getDoc();

        final XPathExpression expr0 = factory.newXPath().compile(EX0);
        final Object result0 = expr0.evaluate(doc, XPathConstants.NODESET);
        final NodeList nodeList0 = (NodeList) result0;

        final XPathExpression expr1 = factory.newXPath().compile(EX1);
        final Object result1 = expr1.evaluate(doc, XPathConstants.NODESET);
        final NodeList nodeList1 = (NodeList) result1;

        if (nodeList0 != null) {
            for (int i = 0; i < nodeList0.getLength(); ++i) {
                final String s = nodeList0.item(i).getTextContent().trim();
                if (s.equals("VG Bild-Kunst, Bonn")) {
                    LOG.warn("{}: Filtered out because: \"VG Bild-Kunst, Bonn\".", ed.getId());
                    ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
                    return;
                }
                if (s.equals("Berlinische Galerie / VG Bild-Kunst, Bonn")) {
                    LOG.warn("{}: Filtered out because: \"Berlinische Galerie / VG Bild-Kunst, Bonn\".", ed.getId());
                    ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
                    return;
                }
            }
        }

        if (nodeList1 != null) {
            for (int i = 0; i < nodeList1.getLength(); ++i) {
                final String s = nodeList1.item(i).getTextContent().trim();
                if (s.equals("VG Bild-Kunst, Bonn")) {
                    LOG.warn("{}: Filtered out because: \"VG Bild-Kunst, Bonn\".", ed.getId());
                    ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
                    return;
                }
                if (s.equals("Berlinische Galerie / VG Bild-Kunst, Bonn")) {
                    LOG.warn("{}: Filtered out because: \"Berlinische Galerie / VG Bild-Kunst, Bonn\".", ed.getId());
                    ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
                    return;
                }
            }
        }
    }

    /**
     * Description of this Filter
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Filtert alle DDB-Objekte heraus, die in edm:WebResource/dc:rights oder edm:ProvidedCHO/dc:rights entweder „VG Bild-Kunst, Bonn“ oder „Berlinische Galerie / VG Bild-Kunst, Bonn“. stehen haben";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return VgBildKunstFilter.class.getSimpleName();
    }

}
