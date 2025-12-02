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
package de.ddb.labs.europack;

import de.ddb.labs.europack.source.ddbapi.EdmDownloader;
import de.ddb.labs.europack.processor.EuropackFilterProcessor;
import de.ddb.labs.europack.source.ddbapi.CacheManager;
import de.ddb.labs.europack.source.ddbapi.DDBIdGetter;
import de.ddb.labs.europack.sink.SinkInterface;
import de.ddb.labs.europack.sink.ZipFileSink;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.swing.Timer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class TestMain {

    private final static Logger LOG = LoggerFactory.getLogger(TestMain.class);

    public static void main(String[] args) throws InterruptedException, IOException, TransformerException {

        final String cacheId = UUID.randomUUID().toString();
        CacheManager.getInstance().addCache(cacheId);

        final List<String> filter = new ArrayList<String>() {
            private static final long serialVersionUID = -4920951508743312615L;

            {
                add("CropRdfFilter");
                add("HierarchieFilter");
                add("VgBildKunstFilter");
                add("DcDescriptionFilter");
                add("DctermsLanguageFilter");
                add("DctermsLinguisticSystemFilter");
                add("DctermsRightsFilter");
                add("DctermsSubjectFilter");
                add("DcTypeFilter");
                add("DdbAggregationEntityFilter");
                add("DdbAggregatorFilter");
                add("DdbHierarchyPositionFilter");
                add("DdbHierarchyTypeFilter");
                add("EdmDataProviderFilter");
                add("EdmHasMetFilter");
                add("EdmHasTypeFilter");
                add("ReformatterFilter");
            }
        };

        final List<SinkInterface> sink = new ArrayList<SinkInterface>() {
            private static final long serialVersionUID = 1417566736194055279L;

            {
                add(new ZipFileSink(Arrays.asList(new String[]{"sink.zip"}), Integer.MAX_VALUE, null));
            }
        };

        final EuropackFilterProcessor epfp = new EuropackFilterProcessor(cacheId, filter, sink);
        final EdmDownloader ehg = new EdmDownloader(cacheId, epfp);
        // final DDBIdGetter sd = new DDBIdGetter("https://api.deutsche-digitale-bibliothek.de", "provider_id:WOGJQYZO42L7ZIZQHKFIGHG3D6XQJGYW AND type_fct:mediatype_003", ehg, "");
        final DDBIdGetter sd = new DDBIdGetter("https://api-q1.deutsche-digitale-bibliothek.de", "dataset_id:34753197757685558nKKn", ehg, "");

        final Timer t = new Timer(10000, null);
        t.addActionListener((ActionEvent e) -> {
            LOG.info("DDBIdGetter: {}, EDMDownloader: {}, EuropackFilterProcessor: {}", sd.isDone(), ehg.isDone(), epfp.isDone());
            if (sd.isDone() && ehg.isDone() && epfp.isDone()) {
                sd.dispose();
                ehg.dispose();
                epfp.dispose();
                CacheManager.getInstance().destroy();
                t.stop();
            }
        });
        t.start();

        sd.run();

        //ehg.close();
    }

    public static void save(Document doc, String file) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            final byte[] strBytes = writer.toString().getBytes(Charset.forName("UTF-8"));
            outputStream.write(strBytes);

        } catch (IOException e) {
            LOG.error("Datei '{}' konnte nicht gespeichert werden. {}", e);
        }
    }

    public static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (IllegalArgumentException | TransformerException ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

}
