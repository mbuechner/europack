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
package de.ddb.labs.europack.gui.helper;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public final class IconLabel extends JLabel {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(IconLabel.class);
    private int size;
    private String helpText;

    public enum Status {
        NONE, OK, ERROR, PROCESS;
    }

    public IconLabel(int size) {
        this(size, Status.NONE, "");
    }

    public IconLabel(int size, String helpText) {
        this(size, Status.NONE, helpText);
    }

    public IconLabel(int size, Status status, String helpText) {
        this.size = size;
        this.helpText = helpText;
        setSize(size, size);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setToolTipText(helpText);
        setIcon(status);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
    }
    
    public void resetToolTipText() {
        super.setToolTipText(helpText);
    }

    public void setIcon(Status status) {
        String img;

        switch (status) {
            case ERROR:
                img = "icons8-fehler-50.png";
                break;
            case PROCESS:
                img = "icons8-ausgang-50.png";
                break;
            case OK:
                img = "icons8-mag-ich-50.png";
                break;
            default:
                img = "icons8-ios-anwendung-platzhalter-50.png";
                break;
        }
        final BufferedImage myIco;
        try {
            myIco = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(img));
            final Image dimg = myIco.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(dimg));
        } catch (IOException ex) {
            LOG.warn("{}", ex.getMessage());
        }

    }
}








