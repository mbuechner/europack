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

import de.ddb.labs.europack.processor.EdmNamespaces;
import de.ddb.labs.europack.processor.EuropackDoc;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class HierarchieFilter implements FilterInterface {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchieFilter.class);
    private final XPathFactory factory;

    public HierarchieFilter() {

        factory = XPathFactory.newInstance();
        if (factory == null) {
            throw new IllegalStateException("XPathFactory is null. This filter won't work.");
        }
    }

    private boolean ruleOne(Document doc) throws XPathExpressionException {
        final String ex
                = // "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("ddb-cortex") + "' and local-name()='edm']"
                // + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("rdf") + "' and local-name()='RDF']"
                "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='ProvidedCHO']"
                + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("dcterms") + "' and local-name()='isPartOf']";

        final XPathExpression expr = factory.newXPath().compile(ex);
        final Object result = expr.evaluate(doc, XPathConstants.NODESET);
        final NodeList nodeList = (NodeList) result;
        return nodeList.getLength() > 0;
    }

    private boolean ruleTwo(Document doc) throws XPathExpressionException {
        final String ex
                = // "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("ddb-cortex") + "' and local-name()='edm']"
                // + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("rdf") + "' and local-name()='RDF']"
                "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='ProvidedCHO']"
                + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("ddb-edm") + "' and local-name()='hierarchyType']";
        final XPathExpression expr = factory.newXPath().compile(ex);
        final Object result = expr.evaluate(doc, XPathConstants.NODE);
        final Node node = (Node) result;
        final String nodeText = node.getTextContent();
        return nodeText.equalsIgnoreCase("htype_007");
    }

    private boolean ruleThree(Document doc) throws XPathExpressionException {
        final String ex
                = // "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("ddb-cortex") + "' and local-name()='edm']"
                // + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("rdf") + "' and local-name()='RDF']"
                "//*[namespace-uri()='" + EdmNamespaces.getNsUri().get("edm") + "' and local-name()='ProvidedCHO']"
                + "/*[namespace-uri()='" + EdmNamespaces.getNsUri().get("ddb-edm") + "' and local-name()='hierarchyType']";
        final XPathExpression expr = factory.newXPath().compile(ex);
        final Object result = expr.evaluate(doc, XPathConstants.NODE);
        final Node node = (Node) result;
        final String nodeText = node.getTextContent();
        return nodeText.equalsIgnoreCase("htype_020") || nodeText.equalsIgnoreCase("htype_023");
    }

    /**
     * Filter Hierarchien heraus (so wie es Francesca definiert hat)
     *
     * @param ed
     */
    @Override
    public void filter(EuropackDoc ed) {
        final Document doc = ed.getDoc();

        boolean ruleOne, ruleTwo, ruleThree;

        try {
            ruleOne = ruleOne(doc);
        } catch (Exception ex) {
            // path not there
            ruleOne = false;
        }

        try {
            ruleTwo = ruleTwo(doc);
        } catch (Exception ex) {
            // path not there
            ruleTwo = false;
        }

        try {
            ruleThree = ruleThree(doc);
        } catch (Exception ex) {
            // path not there
            ruleThree = false;
        }

        // isPartOf vorhanden -> herausfiltern
        if (ruleOne) {
            // hierarchyType=htype_007, dann NICHT herausfiltern 
            if (!ruleTwo) {
                LOG.warn("{}: Filtered out because: with 'isPartOf' and NOT 'hierarchyType=htype_007'.", ed.getId());
                ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
            }
            // isPartOf NICHT vorhanden
        } else {
            // und: hierarchyType=htype_020|htype_023, dann herausfiltern
            if (ruleThree) {
                LOG.warn("{}: Filtered out because: without 'isPartOf' and 'hierarchyType=htype_020|htype_023'.", ed.getId());
                ed.setStatus(EuropackDoc.Status.INVALID_SAVE);
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
        return "Filter Hierarchien heraus (so wie es Francesca definiert hat)";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return HierarchieFilter.class.getSimpleName();
    }

}
