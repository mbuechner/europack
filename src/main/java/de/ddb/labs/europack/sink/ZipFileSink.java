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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
public class ZipFileSink implements SinkInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ZipFileSink.class);
    // files per Zip archive
    private final int filesPerZip;
    // files already wrote to archive
    private int filesWrote;
    // Zip Output Stream
    private ZipOutputStream zos;
    private final List<String> zipFilenames;
    private int fileCounter;

    private final Normalizer.Form normalizerForm;

    /**
     * ZIP file writer
     *
     * @param filenames
     * @param filesPerZip Number of XML files within the ZIP file (0 means
     * all-on-one)
     * @param normalizerForm Normalize XML files. NULL for NOT.
     */
    public ZipFileSink(List<String> filenames, int filesPerZip, Normalizer.Form normalizerForm) {
        for (String filename : filenames) {
            final File file = new File(filename);
            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException(filename + " already exists");
                }
                if (!file.delete()) {
                    throw new IllegalStateException("Could not delete " + filename);
                }
            } catch (SecurityException | IOException ex) {
                throw new IllegalStateException("Cannot write to " + filename);
            }
        }
        this.zipFilenames = filenames;
        this.filesPerZip = filesPerZip;
        this.normalizerForm = normalizerForm;
        this.fileCounter = 0;
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
        writeToZipArchive(doc.getId() + ".xml", normalizedString);        
        return true;
    }

    /**
     * Writes an Stream to an ZIP-File
     *
     * @param in InputStream with data
     * @param xmlFile Designated file name in ZIP file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private synchronized void writeToZipArchive(String xmlFileName, String xmlFileContent) throws FileNotFoundException, IOException {

        if (filesWrote % filesPerZip == 0) {
            if (zos != null) {
                zos.close();
            }

            final String newFilename = zipFilenames.get(fileCounter++);
            zos = new ZipOutputStream(new FileOutputStream(newFilename));
            zos.setLevel(9);
            LOG.info("Created new ZIP package: " + newFilename);
        }

        zos.putNextEntry(new ZipEntry(xmlFileName));
        final byte[] data = xmlFileContent.getBytes(Charset.forName("UTF-8"));
        zos.write(data, 0, data.length);

        zos.closeEntry();
        ++filesWrote;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Speichert die Daten als in eine oder mehrere ZIP-Dateien mit einer bestimmten UTF-8 Normalisierung ab";
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return ZipFileSink.class.getName();
    }

    @Override
    public synchronized void dispose() {
        try {
            if (zos != null) {
                zos.close();
            }
        } catch (IOException ex) {
        }
    }

}
