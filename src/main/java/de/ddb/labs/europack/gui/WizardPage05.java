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
package de.ddb.labs.europack.gui;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import de.ddb.labs.europack.gui.helper.IconLabel;
import de.ddb.labs.europack.gui.helper.LogStreamAppender;
import de.ddb.labs.europack.gui.helper.TextAreaOutputStream;
import de.ddb.labs.europack.processor.EuropackFilterProcessor;
import de.ddb.labs.europack.sink.SinkInterface;
import de.ddb.labs.europack.source.ddbapi.CacheManager;
import de.ddb.labs.europack.source.ddbapi.DDBIdGetter;
import de.ddb.labs.europack.source.ddbapi.EdmDownloader;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class WizardPage05 extends WizardPageEuropack {

    /**
     *
     */
    private static final long serialVersionUID = -2802186541163942232L;
    private static final Logger LOG = LoggerFactory.getLogger(WizardPage05.class);
    private DDBIdGetter ddbidgetter;
    private EuropackFilterProcessor epfp;
    private EdmDownloader edmdown;

    private final static List<String> QUOTES = new ArrayList<String>() {
        {
            final Properties properties = new Properties();
            try (final BufferedReader is = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(".properties"), Charset.forName("UTF-8")))) {
                properties.load(is);
                final String pv = properties.getProperty("europack.quotes", "");
                final String[] pva = pv.split("\\|");
                addAll(Arrays.asList(pva));
            } catch (Exception e) {
                LOG.warn("Could not load quotes from properies. {}", e.getMessage());
            }
        }
    };

    private final Random rand = new Random();
    private Timer progressBarTimer;

    /**
     * Creates new form WizardLoad
     *
     * @param title
     * @param description
     */
    public WizardPage05(String title, String description) {
        super(title, description);
        initComponents();
        jTextArea1.setText(QUOTES.get(rand.nextInt(QUOTES.size())));
        new Timer(15000, (ActionEvent e) -> {
            jTextArea1.setText(QUOTES.get(rand.nextInt(QUOTES.size())));
        }).start();
        this.ddbidgetter = null;
        this.edmdown = null;
        this.epfp = null;
    }

    public void cancel() {
        if (ddbidgetter != null) {
            ddbidgetter.setCanceled(true);
        }
        if (edmdown != null) {
            edmdown.setCanceled(true);
        }
        if (epfp != null) {
            epfp.setCanceled(true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.progressBarTimer != null) {
            this.progressBarTimer.stop();
        }
        if (ddbidgetter != null) {
            ddbidgetter.dispose();
        }
        if (edmdown != null) {
            ddbidgetter.dispose();
        }
        if (epfp != null) {
            ddbidgetter.dispose();
        }
    }

    /**
     *
     * @param path
     * @param settings
     */
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        try {
            super.rendering(path, settings);
            jTextPane1.setText(""); //clear output
            setPrevEnabled(false);
            setNextEnabled(false);
            setFinishEnabled(false);
            setCancelEnabled(true);

            // Logging stuff
            final OutputStream os = new TextAreaOutputStream(jTextPane1, jCheckBoxMenuItem1);
            LogStreamAppender.setStaticOutputStream(os);

            // Start download
            // build cacge first
            final String cacheId = UUID.randomUUID().toString();
            CacheManager.getInstance().addCache(cacheId);

            final List<String> filters = (List<String>) settings.get("filters");
            final List<SinkInterface> sinks = (List<SinkInterface>) settings.get("sink");
            ddbidgetter = (DDBIdGetter) settings.get(DDBIdGetter.class.getSimpleName());
            epfp = new EuropackFilterProcessor(cacheId, filters, sinks);
            edmdown = new EdmDownloader(cacheId, epfp);
            ddbidgetter.setDownloader(edmdown); // yes, important!

            this.progressBarTimer = new Timer(500, (ActionEvent e) -> {
                try {
                    final int noOfObjects = ddbidgetter.getNumberOfResults();
                    final int noOfProcessed = edmdown.getItemsDowloaded();
                    final int v = Math.min((int) Math.ceil(100f / noOfObjects * noOfProcessed), 100);
                    jProgressBar1.setValue(v);
                    updateIcons();
                } catch (IOException ex) {
                    LOG.error("{}", ex.getMessage());
                }
            });
            // start
            progressBarTimer.start(); //timer

            // processor
            final Thread t = new Thread(() -> {
                try {
                    ddbidgetter.run();
                    // wait until everyone's finished
                    while (!ddbidgetter.isDone() || !edmdown.isDone() || !epfp.isDone()) {
                        if(ddbidgetter.isCanceled() || edmdown.isCanceled() || epfp.isCanceled()) break;
                    }

                    List<String> errors = CacheManager.getInstance().getErrorIds(cacheId);
                    while (!errors.isEmpty()) {
                        LOG.error("There were {} errors: {}", errors.size(), errors);
                        final int confirm = JOptionPane.showOptionDialog(null,
                                "There were " + errors.size() + " errors.\nShould I try to download and process them again?",
                                "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (confirm == JOptionPane.YES_OPTION) {
                            ddbidgetter.addAdditionalJobs(errors, true);
                            // wait again
                            while (!ddbidgetter.isDone() || !edmdown.isDone() || !epfp.isDone()) {
                                if(ddbidgetter.isCanceled() || edmdown.isCanceled() || epfp.isCanceled()) break;
                            }
                        } else {
                            break; //while (!errors.isEmpty())
                        }
                        errors = CacheManager.getInstance().getErrorIds(cacheId);
                    }
                    progressBarTimer.stop();
                    setFinishEnabled(true);
                    setCancelEnabled(false);
                    setNextEnabled(true);
                    updateIcons(); // a last time
                    ddbidgetter.dispose();
                    edmdown.dispose();
                    epfp.dispose();
                    CacheManager.getInstance().removeCache(cacheId);
                } catch (IOException ex) {
                    LOG.error("{}", ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            t.start();

        } catch (NullPointerException | InterruptedException | IOException ex) {
            LOG.error("{}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateIcons() {
        try {
            final Wizard frame = (Wizard) SwingUtilities.getRoot(this);
            // set icons
            if (ddbidgetter.isDone() && !ddbidgetter.hadErrors()) {
                frame.getCicon01().setIcon(IconLabel.Status.OK);

                frame.getCicon01().setToolTipText(Integer.toString(ddbidgetter.getNumberOfResults()));

            } else if (ddbidgetter.isDone() && ddbidgetter.hadErrors()) {
                frame.getCicon01().setIcon(IconLabel.Status.ERROR);
                frame.getCicon01().setToolTipText(ddbidgetter.getErrors() + " error(s)");
            } else {
                frame.getCicon01().setIcon(IconLabel.Status.PROCESS);
                frame.getCicon01().resetToolTipText();
            }

            if (edmdown.isDone() && !edmdown.hadErrors()) {
                frame.getCicon02().setIcon(IconLabel.Status.OK);
                frame.getCicon02().setToolTipText(Integer.toString(edmdown.getItemsDowloaded()));
            } else if (edmdown.isDone() && edmdown.hadErrors()) {
                frame.getCicon02().setIcon(IconLabel.Status.ERROR);
                frame.getCicon02().setToolTipText(edmdown.getErrors() + " error(s)");
            } else {
                frame.getCicon02().setIcon(IconLabel.Status.PROCESS);
                frame.getCicon02().setToolTipText(edmdown.getItemsDowloaded() + " / " + edmdown.getItemsToDownload());
            }

            if (epfp.isDone() && !epfp.hadErrors()) {
                frame.getCicon03().setIcon(IconLabel.Status.OK);
                frame.getCicon03().setToolTipText(Integer.toString(epfp.getProcessedJobs()));
            } else if (epfp.isDone() && epfp.hadErrors()) {
                frame.getCicon03().setIcon(IconLabel.Status.ERROR);
                frame.getCicon03().setToolTipText(epfp.getErrors() + " error(s)");
            } else {
                frame.getCicon03().setIcon(IconLabel.Status.PROCESS);
                frame.getCicon03().setToolTipText(epfp.getProcessedJobs() + " / " + epfp.getAddedJobs());
            }
        } catch (IOException ex) {
            LOG.warn("Could not update status icons. {}", ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }

        };

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Autoscroll");
        jPopupMenu1.add(jCheckBoxMenuItem1);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wizard.gif"))); // NOI18N
        jLabel1.setToolTipText("");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(UIManager.getColor("Label.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(UIManager.getFont("Label.font").deriveFont(UIManager.getFont("Label.font").getStyle() | Font.BOLD));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        jProgressBar1.setToolTipText("");
        jProgressBar1.setStringPainted(true);

        jTextPane1.setEditable(false);
        jTextPane1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        jTextPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextPane1MousePressed(evt);
            }
        });
        jScrollPane3.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(156, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap(157, Short.MAX_VALUE))
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextPane1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPane1MousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jTextPane1MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
