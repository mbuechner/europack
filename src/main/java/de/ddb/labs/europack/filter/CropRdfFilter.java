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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class CropRdfFilter implements FilterInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CropRdfFilter.class);
    private DocumentBuilder builder;

    public CropRdfFilter() {
    }

    @Override
    public void init() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        if (builder == null) {
            throw new IllegalStateException("XMLBuilder is null. This filter won't work.");
        }
    }

    /**
     * Macht das rdf:RDF-Element zum Wurzelelement
     *
     * @param ed
     * @throws Exception
     */
    @Override
    public void filter(EuropackDoc ed) throws Exception {

        builder.reset();

        final Document doc = ed.getDoc();
        final NodeList nl = doc.getChildNodes();

        Element root = null;
        for (int i = 0; i < nl.getLength(); ++i) {
            final Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                root = (Element) n;
                break;
            }
        }

        if (root == null) {
            LOG.error("{}: Has no XML root element.", ed.getId());
            return;
        }

        // remove cortex wrapper if RDF is not root
        LOG.debug("{}: getNamespaceURI: {}; getLocalName: {}; getNodeName: {}", ed.getId(), root.getNamespaceURI(), root.getLocalName(), root.getNodeName());
        if (root.getNamespaceURI().equals(EdmNamespaces.getNsUri().get("rdf")) && root.getLocalName().equals("RDF")) {
            // LOG.info("{}: rdf:RDF is already the root element. Nothing to do here.", ed.getId());
            return;
        }

        final NodeList nlst = root.getElementsByTagNameNS(EdmNamespaces.getNsUri().get("rdf"), "RDF");
        if (nlst == null || nlst.getLength() < 1) {
            LOG.warn("{}: There's no rdf:RDF element in this document.", ed.getId());
            ed.setStatus(EuropackDoc.Status.INVALID_FILTER_FAILED);
            return;
        }
        final Node n = nlst.item(0);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            final Document newDoc = builder.newDocument();
            final Node importedNode = newDoc.importNode((Element) n, true);
            newDoc.appendChild(importedNode);
            ed.setDoc(newDoc);
        }
    }

    /**
     * Description of this Filter
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Macht das rdf:RDF-Element zum Wurzelelement";
    }

    /**
     * Name of this Filter
     *
     * @return
     */
    @Override
    public String getName() {
        return CropRdfFilter.class.getSimpleName();
    }

}
