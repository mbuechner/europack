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
import java.io.StringWriter;
import java.text.Normalizer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class OutputSink implements SinkInterface {

    private final Normalizer.Form normalizerForm;

    /**
     *
     */
    public OutputSink() {
        this.normalizerForm = null;
    }

    /**
     *
     * @param normalizerForm
     */
    public OutputSink(Normalizer.Form normalizerForm) {
        this.normalizerForm = normalizerForm;
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
        System.out.println("### " + doc.getId() + " ####################################");
        System.out.println(normalizedString);
        System.out.println("#########################################################################");
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Gibt die Daten in einer bestimmten UTF-8 Normalisierung auf der Console aus";
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return OutputSink.class.getName();
    }

    @Override
    public void dispose() {
    }
}
