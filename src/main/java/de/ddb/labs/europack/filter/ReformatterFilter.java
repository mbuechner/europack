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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class ReformatterFilter implements FilterInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ReformatterFilter.class);

    private final Transformer transformer;

    public ReformatterFilter() throws IOException, TransformerConfigurationException {
        final String xsltFileName = "filters/" + ReformatterFilter.class.getSimpleName() + ".xsl";
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream(xsltFileName);
        final InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        final BufferedReader br = new BufferedReader(isr);
        transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(br));

    }

    /**
     * Re-formatiert XML
     * <ol>
     * <li>Alle Namespace-Deklarationen im Root-Elemnt deklarieren.</li>
     * <li>Entfernen von allen unnötigen Namespace-Deklarationen.</li>
     * <li>XML-Einrücken, Formatieren und Aufhübschen.</li>
     * </ol>
     *
     * @param ed
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    @Override
    public void filter(EuropackDoc ed) throws TransformerException {
        transformer.reset();

        final Document doc = ed.getDoc();
        final Node root = doc.getFirstChild();

        final Map<String, String> ns = new HashMap<>();
        findAllNamespaces(root, ns);

        // consolidate ns list
        for (Map.Entry<String, String> e : ns.entrySet()) {
            if (EdmNamespaces.getUriNs().containsKey(e.getKey())) {
                ns.put(e.getKey(), EdmNamespaces.getUriNs().get(e.getKey()));
            }
        }

        readChildNodes(root, root, ns);

        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();
        transformer.transform(source, result);
        ed.setDoc((Document) result.getNode());
    }

    /**
     * Find all namespaces in a node and its children and put them in list.
     *
     * @param node
     * @param list
     */
    protected static void findAllNamespaces(Node node, Map<String, String> list) {
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                final String ns = currentNode.getNamespaceURI();
                final String prefix = currentNode.getPrefix();
                list.put(ns, prefix); // add ns of all nodes
                final NamedNodeMap nnm = currentNode.getAttributes();
                for (int j = 0; j < nnm.getLength(); ++j) {
                    // list.put(nnm.item(j).getNamespaceURI(), nnm.item(j).getPrefix()); // add ns of all attr
                    list.put(nnm.item(j).getNamespaceURI(), EdmNamespaces.getUriNs().get(nnm.item(j).getNamespaceURI()));
                }
                findAllNamespaces(currentNode, list);
            }
        }
    }

    /**
     * Correct namespace prefixes by given list
     *
     * @param node
     * @param root
     * @param ns
     */
    protected static void readChildNodes(Node node, Node root, Map<String, String> ns) {
        if (ns.containsKey(node.getNamespaceURI())) {
            final String prefix = ns.get(node.getNamespaceURI());
            node.setPrefix(prefix);
            ((Element) root).setAttribute("xmlns:" + prefix, node.getNamespaceURI());
        }

        final NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); ++i) {
                final Node attribute = nnm.item(i);
                if (ns.containsKey(attribute.getNamespaceURI())) {
                    final String prefix = ns.get(attribute.getNamespaceURI());
                    attribute.setPrefix(prefix);
                    ((Element) root).setAttribute("xmlns:" + prefix, attribute.getNamespaceURI());
                }
            }
        }
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                readChildNodes(currentNode, root, ns);
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
        return "Re-formatiert XML: (1) Alle Namespace-Deklarationen im Root-Elemnt deklarieren. (2) Entfernen von allen unnötigen Namespace-Deklarationen. (3) XML-Einrücken, Formatieren und Aufhübschen.";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return ReformatterFilter.class.getSimpleName();
    }

}
