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

import de.ddb.labs.europack.processor.EdmNamespaces;
import de.ddb.labs.europack.processor.EuropackDoc;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class EdmHasMetFilter implements FilterInterface {

    private static final Logger LOG = LoggerFactory.getLogger(EdmHasMetFilter.class);

    public EdmHasMetFilter() {
    }

    @Override
    public void init() throws Exception {
    }

    /**
     * edm:WebResource/edm:hasMet - Properties und zugehörige Event-Class und
     * von dort wieder zugehörige Agent-, TimeSpan- und Place-Class müssen
     * gelöscht. Agents und Places können wiederum (rein theoretisch) wieder
     * über isPart-Of-Relationen mit weiteren Instanzen dieser Classes verknüpft
     * sein.
     *
     * @param ed
     */
    @Override
    public void filter(EuropackDoc ed) {
        final NodeList nl = ed.getDoc().getElementsByTagNameNS(EdmNamespaces.getNsUri().get("edm"), "ProvidedCHO");
        if (nl != null && nl.getLength() > 0) {
            final List<Node> l = getElementsByName(nl.item(0), EdmNamespaces.getNsUri().get("edm"), "hasMet", false);
            for (Node n : l) {
                final Node attribute = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                if (attribute != null) {
                    final String s = attribute.getTextContent();
                    n.getParentNode().removeChild(n);
                    removeEdmEvent(ed.getDoc(), s);
                }
            }
        }
    }

    /**
     * Removes a specific edm:Event
     *
     * @param doc
     * @param uri
     */
    private static void removeEdmEvent(Document doc, String uri) {
        final Node node = doc.getElementsByTagNameNS(EdmNamespaces.getNsUri().get("rdf"), "RDF").item(0);
        final List<Node> agentList = getElementsByNameAndAttribute(
                node,
                EdmNamespaces.getNsUri().get("edm"),
                "Event",
                EdmNamespaces.getNsUri().get("rdf"),
                "about",
                uri,
                false);

        for (Node an : agentList) {
            an.getParentNode().removeChild(an);
            final NodeList nl = an.getChildNodes();
            for (int i = 0; i < nl.getLength(); ++i) {
                final Node n = nl.item(i);
                if (n == null || n.getNodeType() != Node.ELEMENT_NODE || n.getAttributes() == null) {
                    continue;
                }
                final Node a = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                if (a == null) {
                    continue;
                }

                // if (n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("edm")) && n.getLocalName().equals("hasType")) {
                //    // remove hasType: no class inside EDM
                // }
                if (n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("crm")) && n.getLocalName().equals("P11_had_participant")) {
                    removeAgent(doc, a.getTextContent(), uri);
                }

                if (n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("edm")) && n.getLocalName().equals("happenedAt")) {
                    removePlace(doc, a.getTextContent());
                }

                if (n.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("edm")) && n.getLocalName().equals("occuredAt")) {
                    removeTimeSpan(doc, a.getTextContent());
                }
            }
        }
    }

    /**
     * Removes a specific edm:Agent
     *
     * @param doc
     * @param aboutUri
     * @param wasPresentAtUri
     */
    private static void removeAgent(Document doc, String aboutUri, String wasPresentAtUri) {
        final NodeList nl = doc.getElementsByTagNameNS(EdmNamespaces.getNsUri().get("edm"), "Agent");
        for (int i = 0; i < nl.getLength(); ++i) {
            final Node n = nl.item(i);

            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final Node a = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "about");
            if (a == null || !a.getTextContent().equals(aboutUri)) {
                continue;
            }

            final NodeList nlc = n.getChildNodes();
            for (int j = 0; j < nlc.getLength(); ++j) {
                final Node nc = nlc.item(j);

                if (nc == null
                        || nc.getNodeType() != Node.ELEMENT_NODE
                        || !nc.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("edm"))
                        || !nc.getLocalName().equals("wasPresentAt")) {
                    continue;
                }
                final Node nca = nc.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "resource");
                if (nca != null && nca.getTextContent().equals(wasPresentAtUri)) {
                    n.getParentNode().removeChild(n);
                    return;
                }
            }
        }
    }

    /**
     * Removes a specific edm:TimeSpan
     *
     * @param doc
     * @param aboutUri
     */
    private static void removeTimeSpan(Document doc, String aboutUri) {
        final NodeList nl = doc.getElementsByTagNameNS(EdmNamespaces.getNsUri().get("edm"), "TimeSpan");
        for (int i = 0; i < nl.getLength(); ++i) {
            final Node n = nl.item(i);

            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final Node a = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "about");
            if (a == null || !a.getTextContent().equals(aboutUri)) {
                continue;
            }

            n.getParentNode().removeChild(n);
            return;
        }
    }

    /**
     * Removes a specific edm:Place
     *
     * @param doc
     * @param aboutUri
     */
    private static void removePlace(Document doc, String aboutUri) {
        final NodeList nl = doc.getElementsByTagNameNS(EdmNamespaces.getNsUri().get("edm"), "Place");
        for (int i = 0; i < nl.getLength(); ++i) {
            final Node n = nl.item(i);

            if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            final Node a = n.getAttributes().getNamedItemNS(EdmNamespaces.getNsUri().get("rdf"), "about");
            if (a == null || !a.getTextContent().equals(aboutUri)) {
                continue;
            }
            n.getParentNode().removeChild(n);
            return;
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
     * @param attriValue If null attribute value can be anything
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
     * Description of this Filter
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "edm:WebResource/edm:hasMet - Properties und zugehörige Event-Class und von dort wieder zugehörige Agent-, TimeSpan- und Place-Class müssen gelöscht. Agents und Places können wiederum (rein theoretisch) wieder über isPart-Of-Relationen mit weiteren Instanzen dieser Classes verknüpft sein.";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return EdmHasMetFilter.class.getSimpleName();
    }

}
