/*
 * Copyright 2019 Deutsche Digitale Bibliothek.
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
package de.ddb.labs.europack.gui.helper;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class TextAreaOutputStream extends OutputStream {

    private final static Logger LOG = LoggerFactory.getLogger(TextAreaOutputStream.class);
    private final JTextPane textPane;
    private final JCheckBoxMenuItem checkBox;
    private byte[] line;
    private final StyledDocument doc;

    public TextAreaOutputStream(JTextPane textPane, JCheckBoxMenuItem checkBox) {
        this.textPane = textPane;
        this.checkBox = checkBox;
        this.line = new byte[0];
        this.doc = textPane.getStyledDocument();
    }

    @Override
    public void write(int b) throws IOException {
        //this.line = Bytes.concat(this.line, new byte[]{(byte) b});
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(this.line);
            outputStream.write(b);
            this.line = outputStream.toByteArray();
        }

        if ((char) b == '\n') {
            final String newLine = new String(this.line, Charset.defaultCharset());
            final SimpleAttributeSet keyWord = new SimpleAttributeSet();
            if (newLine.contains("ERROR")) {
                StyleConstants.setBackground(keyWord, Color.RED);
            } else if (newLine.contains("WARN")) {
                StyleConstants.setBackground(keyWord, Color.YELLOW);
            }
            try {
                doc.insertString(doc.getLength(), newLine, keyWord);
            } catch (BadLocationException ex) {
                LOG.warn(ex.getLocalizedMessage());
            }

            this.line = new byte[0];
            if (checkBox.isSelected()) {
                textPane.setCaretPosition(doc.getLength());
            }
        }

    }
}

