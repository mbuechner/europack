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
package de.ddb.labs.europack;

import de.ddb.labs.europack.gui.Wizard;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.WindowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    private final Wizard wizard;

    public static void main(String[] args) {
        LOG.info("##########################################");
        LOG.info("Europack started. Hello! ;-)");
        new Main().run();
    }
    
    

    public Main() {
        wizard = new Wizard();
    }

    /**
     * Europack GUI
     */
    private void run() {
        try {
            EventQueue.invokeLater(() -> {
                final Properties properties = new Properties();
                try (final BufferedReader is = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(".properties"), Charset.forName("UTF-8")));) {
                    properties.load(is);
                } catch (IOException ex) {
                    LOG.warn("Could not get properties in file .properties");
                }

                try {
                    final BufferedImage myIco = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("icon.png"));
                    wizard.setIconImage(myIco);
                } catch (IOException e) {
                    // nothing
                }
                wizard.setTitle(properties.getProperty("europack.title", "Europack") + " " + properties.getProperty("europack.version", "").trim());
                wizard.setMinimumSize(new Dimension(800, 600));
                wizard.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                wizard.setLocationRelativeTo(null);
                wizard.setVisible(true);
                wizard.addWindowListener(new MyWindowAdapter());
            });

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * Closing Window Listener
     */
    private class MyWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            LOG.info("Europack closed. Bye! :-(");
            LOG.info("##########################################");
            wizard.dispose();
        }

    }
}




