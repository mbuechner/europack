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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class EdmDataProviderFilter implements FilterInterface {

    public EdmDataProviderFilter() {
    }

    @Override
    public void init() throws Exception {
    }
    
    /**
     * ore:Aggregation/edm:dataProvider - Nur Elementinstanzen mit dem Wert
     * löschen, der mit "http" beginnt.Die Organization-URIs verweisen auf eine
     * Agent-Class.Die jeweilige Agent-Instanz für den URI mit allen zugehörigen
     * Properties muss auch gelöscht werden, einschließlich der
     * edm:isPartOf-Relation, die wiederum auf einen übergeordneten Agent
     * verweist.Es sind drei Hierarchieebenen möglich.
     *
     * @param ed
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void filter(EuropackDoc ed) throws IOException, ParserConfigurationException, SAXException {
        final NodeList nl = ed.getDoc().getElementsByTagNameNS(EdmNamespaces.getNsUri().get("ore"), "Aggregation");
        if (nl != null && nl.getLength() > 0) {
            final List<Node> l = getElementsByName(nl.item(0), EdmNamespaces.getNsUri().get("edm"), "dataProvider", false);
            for (Node n : l) {
                final Node attribute = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                if (attribute != null) {
                    final String s = attribute.getTextContent();
                    if (s.startsWith("http") || Pattern.matches("[0-9A-Z]{32}", s)) {
                        n.getParentNode().removeChild(n);
                        removeEdmAgent(ed.getDoc().getElementsByTagNameNS(EdmNamespaces.getNsUri().get("rdf"), "RDF").item(0), s, true);
                    }
                }

            }
        }
    }

    /**
     * Gets all edm:Agent in a node and it's children with an specific URI in
     * rdf:about and removes them.
     *
     * @param node
     * @param uri Identifier in rdf:about
     * @param withPartOf If True all edm:Agent linked in dcterms:isPartOf will
     * be removes as well
     */
    private void removeEdmAgent(Node node, String uri, boolean withPartOf) {
        final List<Node> agentList = getElementsByNameAndAttribute(
                node,
                EdmNamespaces.getNsUri().get("edm"),
                "Agent",
                EdmNamespaces.getNsUri().get("rdf"),
                "about",
                uri,
                false);

        for (Node an : agentList) {
            if (withPartOf) {
                // get ifPartOf
                final NodeList nl = an.getChildNodes();
                for (int i = 0; i < nl.getLength(); ++i) {
                    final Node n = nl.item(i);
                    if (n != null && n.getNodeType() == Node.ELEMENT_NODE && n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("dcterms")) && n.getLocalName().equals("isPartOf")) {
                        final Node rn = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                        if (rn != null && !rn.getTextContent().isEmpty() && !rn.getTextContent().equals(uri)) {
                            removeEdmAgent(node, rn.getTextContent(), withPartOf);
                        }
                    } else if (n != null && n.getNodeType() == Node.ELEMENT_NODE && n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("rdf")) && n.getLocalName().equals("type")) {
                        final Node rn = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                        if (rn != null && !rn.getTextContent().isEmpty()) {
                            removeElementsByNameAndAttribute(node, EdmNamespaces.getNsUri().get("skos"), "Concept", EdmNamespaces.getNsUri().get("rdf"), "about", rn.getTextContent(), false);
                        }
                    }
                }
            }
            an.getParentNode().removeChild(an);
        }
    }

    /**
     *
     *
     * @param node
     * @param ns
     * @param elementName
     * @param rekursiv
     * @return
     */
    protected static List<Node> getElementsByName(Node node, String ns, String elementName, boolean rekursiv) {
        final NodeList nodeList = node.getChildNodes();
        final List<Node> newList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node n = nodeList.item(i);
            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } else if (n.hasChildNodes() && rekursiv) {
                newList.addAll(getElementsByName(n, ns, elementName, rekursiv));
            }

            if (n.getNamespaceURI().equals(ns) && n.getLocalName().equals(elementName)) {
                // found element
                newList.add(n);
            }
        }
        return newList;
    }

    /**
     *
     *
     * @param node
     * @param ns
     * @param elementName
     * @param attrNS
     * @param attrName
     * @param attriValue
     * @param rekursiv
     * @return
     */
    protected static List<Node> getElementsByNameAndAttribute(Node node, String ns, String elementName, String attrNS, String attrName, String attriValue, boolean rekursiv) {
        final NodeList nodeList = node.getChildNodes();
        final List<Node> newList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node n = nodeList.item(i);
            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } else if (n.hasChildNodes() && rekursiv) {
                newList.addAll(getElementsByName(n, ns, elementName, rekursiv));
            }

            if (n.getNamespaceURI().equals(ns) && n.getLocalName().equals(elementName)) {
                final Node a = n.getAttributes().getNamedItemNS(attrNS, attrName);
                if (a != null && (attriValue == null || a.getTextContent().equals(attriValue))) {
                    // found element
                    newList.add(n);
                }
            }
        }
        return newList;
    }

    /**
     *
     *
     * @param node
     * @param ns
     * @param elementName
     * @param attrNS
     * @param attrName
     * @param attriValue
     * @param rekursiv
     */
    protected static void removeElementsByNameAndAttribute(Node node, String ns, String elementName, String attrNS, String attrName, String attriValue, boolean rekursiv) {
        List<Node> list = getElementsByNameAndAttribute(node, ns, elementName, attrNS, attrName, attriValue, rekursiv);
        if (list != null) {
            for (Node n : list) {
                n.getParentNode().removeChild(n);
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
        return "ore:Aggregation/edm:dataProvider - Nur Elementinstanzen mit dem Wert löschen, der mit \"http\" beginnt. Die Organization-URIs verweisen auf eine Agent-Class. Die jeweilige Agent-Instanz für den URI mit allen zugehörigen Properties muss auch gelöscht werden, einschließlich der edm:isPartOf-Relation, die wiederum auf einen übergeordneten Agent verweist. Es sind drei Hierarchieebenen möglich.";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return EdmDataProviderFilter.class.getSimpleName();
    }

}
