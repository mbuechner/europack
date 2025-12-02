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
package de.ddb.labs.europack.filter;

import de.ddb.labs.europack.processor.EdmNamespaces;
import de.ddb.labs.europack.processor.EuropackDoc;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class DctermsSubjectFilter implements FilterInterface {

    // private final static Logger LOG = LoggerFactory.getLogger(DctermsSubjectFilter.class);
    // select <dcterms:subject ns4:resource="PL35QIAPCMLUV7AJYKP2HCK4IUADKTKD" />
    private final static String EX0 = "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='ProvidedCHO']\n"
            + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("dcterms") + "' and local-name()='subject']";
    private XPathFactory factory;

    public DctermsSubjectFilter() {
    }

    @Override
    public void init() {
        factory = XPathFactory.newInstance();
        if (factory == null) {
            throw new IllegalStateException("XPathFactory is null. This filter won't work.");
        }
    }

    /**
     * edm:ProvidedCHO/dcterms:subject - Property und zugehörige
     * skos:Concept-Class muss gelöscht werden.
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
        if (nodeList0 == null || nodeList0.getLength() < 1) {
            return;
        }
        for (int i = 0; i < nodeList0.getLength(); ++i) {
            final Node n0 = nodeList0.item(i);
            final String uri = n0.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource").getTextContent();
            n0.getParentNode().removeChild(n0);

            // select <skos:Concept ns4:about="MQFVGIECAADKGVWAHMCKP7OZ3TIJCPAD" xmlns:dcterms="http://purl.org/dc/terms/" 
            // xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:foaf="http://xmlns.com/foaf/0.1/" 
            // xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:skos="http://www.w3.org/2004/02/skos/core#"> ...
            final String ex1 = "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("skos") + "' and local-name()='Concept']\n"
                    + "[@*[namespace-uri()='" + EdmNamespaces.getNsUri().get("rdf") + "' and local-name()='about'] = '" + uri + "']";

            final XPathExpression expr1 = factory.newXPath().compile(ex1);
            final Object result1 = expr1.evaluate(doc, XPathConstants.NODESET);
            final NodeList nodeList1 = (NodeList) result1;
            for (int j = 0; j < nodeList1.getLength(); ++j) {
                final Node n1 = nodeList1.item(j);
                n1.getParentNode().removeChild(n1);
            }
        }

        ed.setDoc(doc);
    }

    /**
     * Description of this Filter
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "edm:ProvidedCHO/dcterms:subject - Property und zugehörige skos:Concept-Class muss gelöscht werden.";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return DctermsSubjectFilter.class.getSimpleName();
    }

}
