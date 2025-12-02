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
package de.ddb.labs.europack.gui;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import de.ddb.labs.europack.gui.helper.MessageConsole;
import de.ddb.labs.europack.sink.OutputSink;
import de.ddb.labs.europack.sink.SinkInterface;
import de.ddb.labs.europack.sink.ZipFileSink;
import de.ddb.labs.europack.source.ddbapi.DDBIdGetter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class WizardPage04 extends WizardPageEuropack {

    /**
     *
     */
    private static final long serialVersionUID = 1715394000221547185L;
    private static final Logger LOG = LoggerFactory.getLogger(WizardPage04.class);
    private final static java.util.prefs.Preferences PREFS = java.util.prefs.Preferences.userRoot().node("de/ddb/labs/europack");
    private JFrame f;
    private int countOfObjects;
    private List<SinkInterface> sinkList;
    private final Properties properties;

    /**
     * Creates new form WizardLoad
     *
     * @param title
     * @param description
     */
    public WizardPage04(String title, String description) {
        super(title, description);
        this.f = null;
        this.sinkList = null;
        initComponents();
        jTextField1.setText(PREFS.get("zipoutputfolder", new File(".").getAbsolutePath()));
        final JComponent comp = jSpinner1.getEditor();
        final JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        final DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        jSpinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateList();
            }

        });

        this.properties = new Properties();
        try (final BufferedReader is = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(".properties"), Charset.forName("UTF-8")));) {
            properties.load(is);
        } catch (IOException ex) {
            LOG.warn("Could not get properties in file .properties");
        }
    }

    /**
     *
     * @param path
     * @param settings
     */
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        setPrevEnabled(true);
        setNextEnabled(true);
        setFinishEnabled(false);
        setCancelEnabled(false);
        try {
            final DDBIdGetter ddbid = (DDBIdGetter) settings.get(DDBIdGetter.class.getSimpleName());
            countOfObjects = ddbid.getNumberOfResults();
        } catch (Exception e) {
            countOfObjects = 1;
        }
        jSpinner1.setModel(new SpinnerNumberModel(countOfObjects, 1, countOfObjects, 1));
        updateList();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (f != null) {
            f.dispose();
        }
        if (sinkList != null) {
            for (SinkInterface s : sinkList) {
                s.dispose();
            }
        }
    }

    /**
     *
     * @param settings
     * @return
     */
    @Override
    public boolean onNext(WizardSettings settings) {
        final int confirm = JOptionPane.showOptionDialog(this,
                "Really start to download and process the data?",
                "Continue", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        Form normalization = null;
        if (jRadioButton1.isSelected()) {
            normalization = Form.NFC;
        } else if (jRadioButton2.isSelected()) {
            normalization = Form.NFD;
        }

        int numberOfObectsInFile;
        try {
            numberOfObectsInFile = (int) jSpinner1.getValue();
        } catch (Exception e) {
            numberOfObectsInFile = 0;
        }

        final DefaultListModel<String> listModel = (DefaultListModel<String>) jList1.getModel();
        final List<String> filenameList = Collections.list(listModel.elements());

        try {
            sinkList = new ArrayList<>();
            if (jCheckBox1.isSelected()) {
                sinkList.add(new OutputSink());
            }
            if (jCheckBox2.isSelected()) {
                sinkList.add(new ZipFileSink(filenameList, numberOfObectsInFile, normalization));
            }
            settings.put("sink", sinkList);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanel5 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();

        jCheckBox2.setText("enabled");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Output folder"));

        jTextField1.setEditable(false);

        jButton1.setText("...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(6, 6, 6))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("ZIP file name(s)"));

        jTextField2.setText("Europack-[C].zip");
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 0, 1));
        jSpinner1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSpinner1KeyReleased(evt);
            }
        });

        jLabel1.setText("Number of files in a ZIP archive:");

        jLabel2.setText("[C] - add a counter to the filename");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField2)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jList1.setModel(new DefaultListModel<>());
        jScrollPane1.setViewportView(jList1);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("UTF-8 Composed Normalization");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("UTF-8 Decomposed Normalization");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("None");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("ZIP file output", jPanel2);

        jCheckBox1.setText("enabled");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(377, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Text output", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (jCheckBox1.isSelected()) {
            if (f == null) {
                f = new JFrame(properties.getProperty("europack.title", "Europack") + " " + properties.getProperty("europack.version", "").trim() + ": Output");
                final JPopupMenu jPopupMenu1 = new JPopupMenu();
                final JCheckBoxMenuItem jCheckBoxMenuItem1 = new JCheckBoxMenuItem();
                jCheckBoxMenuItem1.setSelected(true);
                jCheckBoxMenuItem1.setText("Autoscroll");

                final JTextPane jTextPane1 = new JTextPane();
                jTextPane1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                jTextPane1.setEditable(false);

                final MessageConsole mc = new MessageConsole(jTextPane1, true, jCheckBoxMenuItem1);
                mc.redirectOut();
                mc.redirectErr(Color.RED, null);

                jCheckBoxMenuItem1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (jCheckBoxMenuItem1.isSelected()) {
                            jTextPane1.setCaretPosition(jTextPane1.getStyledDocument().getLength());
                        }
                    }
                });
                jPopupMenu1.add(jCheckBoxMenuItem1);
                jTextPane1.add(jPopupMenu1);

                jTextPane1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        if (evt.getButton() == MouseEvent.BUTTON3) {
                            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
                        }
                    }
                });

                f.add(new JScrollPane(jTextPane1));
                f.pack();
            }
            f.setMinimumSize(new Dimension(800, 600));
            f.setLocationRelativeTo((JFrame) SwingUtilities.getWindowAncestor(this));
            f.setLocation(f.getLocation().x, f.getLocation().y + (int) ((getHeight() / 3f)));
            f.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            f.setVisible(true);
            f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            f.toBack();
        } else {
            f.setVisible(false);
            f.dispose();
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if (jCheckBox2.isSelected()) {
            final DefaultListModel<String> listModel = (DefaultListModel<String>) jList1.getModel();
            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ZIP output is not configured valid", "Error", JOptionPane.ERROR_MESSAGE);
                jCheckBox2.setSelected(false);
            } else {
                jPanel3.setEnabled(false);
                jPanel4.setEnabled(false);
                jPanel5.setEnabled(false);
                jTextField1.setEnabled(false);
                jButton1.setEnabled(false);
                jTextField2.setEnabled(false);
                jSpinner1.setEnabled(false);
                jLabel1.setEnabled(false);
                jLabel2.setEnabled(false);
                jRadioButton1.setEnabled(false);
                jRadioButton2.setEnabled(false);
                jRadioButton3.setEnabled(false);
                jList1.setEnabled(false);
            }
        } else {
            jPanel3.setEnabled(true);
            jPanel4.setEnabled(true);
            jPanel5.setEnabled(true);
            jTextField1.setEnabled(true);
            jButton1.setEnabled(true);
            jTextField2.setEnabled(true);
            jSpinner1.setEnabled(true);
            jLabel1.setEnabled(true);
            jLabel2.setEnabled(true);
            jRadioButton1.setEnabled(true);
            jRadioButton2.setEnabled(true);
            jRadioButton3.setEnabled(true);
            jList1.setEnabled(true);
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(PREFS.get("zipoutputfolder", ".")));
        chooser.setDialogTitle("Set output folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("All folders", "*.*"));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            final String s = chooser.getSelectedFile().getAbsolutePath();
            final File lf = new File(s);
            if (!lf.isDirectory() || !lf.canWrite()) {
                JOptionPane.showMessageDialog(this, "This folder is not write-able.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            jTextField1.setText(s);
            PREFS.put("zipoutputfolder", s);
        }
        updateList();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
        updateList();

    }//GEN-LAST:event_jTextField2KeyReleased

    private void jSpinner1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSpinner1KeyReleased
        updateList();
    }//GEN-LAST:event_jSpinner1KeyReleased

    private void updateList() {
        int numberOfObectsInFile = 0;
        try {
            numberOfObectsInFile = (int) jSpinner1.getValue();
        } catch (Exception e) {
            numberOfObectsInFile = 0;
        }

        final DefaultListModel<String> listModel = (DefaultListModel<String>) jList1.getModel();
        listModel.removeAllElements();

        if (countOfObjects < 1 || numberOfObectsInFile < 1 || !jTextField2.getText().contains("[C]")) {
            return;
        }

        for (int i = 0; i < Math.ceil((float) countOfObjects / (float) numberOfObectsInFile); ++i) {
            final String path = jTextField1.getText();
            final int noOfZeros = (int) Math.ceil(Math.log10((float) countOfObjects / (float) numberOfObectsInFile));
            final String counter = String.format("%0" + (noOfZeros < 1 ? 1 : noOfZeros) + "d", i);
            String filename = jTextField2.getText();
            filename = filename.replaceAll("\\[C\\]", counter);
            listModel.addElement(path + File.separator + filename);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
