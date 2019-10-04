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
import de.ddb.labs.europack.gui.helper.PreferencesUtil;
import de.ddb.labs.europack.source.ddbapi.DDBIdGetter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class WizardPage02 extends WizardPageEuropack {

    private static final long serialVersionUID = 5462344889448546593L;

    private static final Logger LOG = LoggerFactory.getLogger(WizardPage02.class);
    private final static java.util.prefs.Preferences PREFS = java.util.prefs.Preferences.userRoot().node("de/ddb/labs/europack");
    private DDBIdGetter ddbidgetter;

    /**
     * Creates new form WizardLoad
     *
     * @param title
     * @param description
     */
    public WizardPage02(String title, String description) {
        super(title, description);
        initComponents();
        jTextField1.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                event.getComponent().requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
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
        setNextEnabled(false);
        setFinishEnabled(false);
        setCancelEnabled(false);
        this.ddbidgetter = null;

        final Properties properties = new Properties();
        try (final BufferedReader is = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(".properties"), Charset.forName("UTF-8")))) {
            properties.load(is);
            final String pv = properties.getProperty("europack.apis", "");
            final HashMap<String, String> hm = PreferencesUtil.getMap(PREFS, "ddbapikeys");
            jComboBox1.removeAllItems();
            for (String pva : pv.split("\\|")) {
                if (hm.containsKey(pva)) {
                    jComboBox1.addItem(pva);
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not load quotes from properies. {}", e.getMessage());
        }

    }

    /**
     *
     * @param settings
     * @return
     */
    @Override
    public boolean onNext(WizardSettings settings) {
        if (ddbidgetter != null) {
            settings.put(DDBIdGetter.class.getSimpleName(), ddbidgetter);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (ddbidgetter != null) {
            ddbidgetter.dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();

        jTextField1.setToolTipText("Type 'ddbtest' to get Cosmina's set of testdata...");
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });

        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Number of found DDB-Objects:");

        jLabel2.setText("0");

        jList1.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(jList1);

        jLabel3.setText("DDB API Search");

        jComboBox1.setFocusable(false);
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 352, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        doSearch();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            doSearch();
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        final DefaultListModel listModel = (DefaultListModel) jList1.getModel();
        listModel.clear();
        setNextEnabled(false);
        jLabel2.setText("0");
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void doSearch() {
        if (!jTextField1.isEnabled()) {
            return;
        }
        final String text = jTextField1.getText().trim();
        jTextField1.setText("Searching...");
        jComboBox1.setEnabled(false);
        jTextField1.setEnabled(false);
        jLabel2.setText("0");
        jButton1.setEnabled(false);
        jList1.setEnabled(false);
        setPrevEnabled(false);
        setNextEnabled(false);
        final DefaultListModel listModel = (DefaultListModel) jList1.getModel();
        listModel.clear();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ddbidgetter = new DDBIdGetter(jComboBox1.getItemAt(jComboBox1.getSelectedIndex()), text);

                    jLabel2.setText(Integer.toString(ddbidgetter.getNumberOfResults()));
                    final DefaultListModel listModel = (DefaultListModel) jList1.getModel();
                    // delete list first
                    listModel.clear();
                    for (String s : ddbidgetter.getFirstDdbIds()) {
                        listModel.addElement(s);
                    }
                    setNextEnabled(true);
                } catch (IOException | InterruptedException ex) {
                    LOG.error("{}", ex.getMessage());
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                jComboBox1.setEnabled(true);
                jTextField1.setText(text);
                jTextField1.setEnabled(true);
                jButton1.setEnabled(true);
                jList1.setEnabled(true);
                setPrevEnabled(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
