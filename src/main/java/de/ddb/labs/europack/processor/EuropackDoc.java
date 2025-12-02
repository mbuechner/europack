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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class EuropackDoc implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5407191277647184206L;
    private final static Logger LOG = LoggerFactory.getLogger(EuropackDoc.class);

    public enum Status {
        VALID_NOTDOWNLOADED, VALID, INVALID_SAVE, INVALID_XMLPARSE, INVALID_DOWNLOAD, INVALID_FILTER_FAILED;
    }
    private Status status;
    private String id;
    private Document doc;

    /**
     *
     *
     * @param id
     * @param doc
     */
    public EuropackDoc(String id, Document doc) throws IllegalArgumentException {
        this.status = Status.VALID;
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("DDB ID can't be null");
        }
        this.id = id;
        if (doc == null) {
            throw new IllegalArgumentException("XML document can't be null");
        }
        this.doc = doc;
    }

    public EuropackDoc(String id) {
        this.status = Status.VALID_NOTDOWNLOADED;
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("DDB ID can't be null");
        }
        this.id = id;
        this.doc = null;
    }

    public EuropackDoc(String id, InputStream is) throws IllegalArgumentException, SAXException, IOException, ParserConfigurationException {
        this.status = Status.VALID;
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("DDB ID can't be null");
        }
        this.id = id;
        if (is == null) {
            throw new IllegalArgumentException("InputStream with document can't be null");
        }

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        final DocumentBuilder db = dbf.newDocumentBuilder();
        this.doc = db.parse(is);

        this.status = Status.VALID;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the doc
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * @param doc the doc to set
     */
    public void setDoc(Document doc) throws IllegalArgumentException {
        if (doc == null) {
            LOG.warn("Someone tried to set an empty (null) xml document. That's illegal and not allowed.");
            return;
        }
        this.doc = doc;

    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return toString(getDoc());
    }

    private static String toString(Document doc) {
        try {
            final StringWriter sw = new StringWriter();
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (IllegalArgumentException | TransformerException ex) {
            return null;
        }
    }
}
