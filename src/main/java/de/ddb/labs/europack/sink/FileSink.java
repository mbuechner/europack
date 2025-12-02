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
package de.ddb.labs.europack.sink;

import de.ddb.labs.europack.processor.EuropackDoc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.Normalizer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class FileSink implements SinkInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FileSink.class);

    private final Normalizer.Form normalizerForm;
    private File file;

    /**
     *
     * @param normalizerForm
     */
    public FileSink(Normalizer.Form normalizerForm) {
        this.normalizerForm = normalizerForm;
        this.file = null;
    }

    /**
     *
     * @param file
     */
    public FileSink(File file) {
        this.normalizerForm = null;
        this.file = file;
    }

    /**
     *
     * @param file
     * @param normalizerForm
     */
    public FileSink(File file, Normalizer.Form normalizerForm) {
        this.normalizerForm = normalizerForm;
        this.file = file;
    }

    /**
     *
     * @param doc
     * @return
     * @throws Exception
     */
    @Override
    public synchronized boolean filter(EuropackDoc doc) throws Exception {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc.getDoc()), new StreamResult(writer));

        String normalizedString;
        if (normalizerForm == null) {
            normalizedString = writer.toString();
        } else {
            normalizedString = Normalizer.normalize(writer.toString(), normalizerForm);
        }

        if (file == null) {
            file = new File(doc.getId() + ".xml");
        } else if (file.isDirectory()) {
            file = new File(file.getPath() + File.separator + doc.getId() + ".xml");
        }

        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            final byte[] strBytes = normalizedString.getBytes(Charset.forName("UTF-8"));
            outputStream.write(strBytes);

        } catch (IOException e) {
            LOG.error("Datei '{}' konnte nicht gespeichert werden. {}", file.getAbsoluteFile(), e.getMessage());
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Speichert die Daten als Datei mit einer bestimmten UTF-8 Normalisierung ab";
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return FileSink.class.getName();
    }

    @Override
    public void dispose() {
        // nothing todo
    }

}

