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
package de.ddb.labs.europack.gui;

import com.github.cjwizard.StackWizardSettings;
import com.github.cjwizard.WizardContainer;
import com.github.cjwizard.WizardListener;
import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.github.cjwizard.pagetemplates.TitledPageTemplate;
import de.ddb.labs.europack.gui.helper.GrayCellRenderer;
import de.ddb.labs.europack.gui.helper.JStatusBar;
import de.ddb.labs.europack.gui.helper.IconLabel;
import de.ddb.labs.europack.source.ddbapi.CacheManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;

/**
 *
 * @author AO, Michael Büchner <m.buechner@dnb.de>
 */
public class Wizard extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 617118399912317706L;

    private JList<String> jListNavigation;
    private JScrollPane jScrollPane1;
    private final WizardFactory factory;
    private final WizardContainer wc;
    private IconLabel cicon01, cicon02, cicon03;

    /**
     * Creates new form WizardNavBar
     *
     */
    public Wizard() {
        super();
        this.factory = new WizardFactory();
        this.wc = new WizardContainer(factory, new TitledPageTemplate(), new StackWizardSettings());
        initComponents();
    }

    @Override
    public void dispose() {
        final int confirm = JOptionPane.showOptionDialog(null,
                "Are you sure to close this application?",
                "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (confirm == JOptionPane.YES_OPTION) {
            factory.dispose();
            super.dispose();
            CacheManager.getInstance().destroy();
        }
    }

    public void cancel() {
        factory.cancel();
    }

    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        jListNavigation = new JList<>();

        wc.setForgetTraversedPath(true);
        wc.addWizardListener(new WizardListener() {
            @Override
            public void onCanceled(List<WizardPage> path, WizardSettings settings) {
                cancel();
            }

            @Override
            public void onFinished(List<WizardPage> path, WizardSettings settings) {
                dispose();
            }

            @Override
            public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
                jListNavigation.setSelectedValue(newPage.getTitle(), true);
            }
        });

        jListNavigation.setModel(new AbstractListModel<String>() {
            @Override
            public int getSize() {
                return factory.getSize();
            }

            @Override
            public String getElementAt(int index) {
                return factory.getTitle(index);
            }
        });

        jListNavigation.setSelectedIndex(0);
        jListNavigation.setFixedCellHeight(25);
        jListNavigation.setEnabled(false);
        jListNavigation.setCellRenderer(new GrayCellRenderer());
        jScrollPane1.setViewportView(jListNavigation);
        jScrollPane1.setPreferredSize(new Dimension(200, 600));
        jScrollPane1.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        wc.setBorder(new MarginBorder());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // statusbar
        getContentPane().setLayout(new BorderLayout());

        final JStatusBar statusBar = new JStatusBar();
        cicon01 = new IconLabel(16);
        cicon01.setToolTipText("DDB-IDs-Getter");
        statusBar.addLeftComponent(cicon01, false);

        cicon02 = new IconLabel(16);
        cicon02.setToolTipText("EDM-Downloader");
        statusBar.addLeftComponent(cicon02, false);

        cicon03 = new IconLabel(16);
        cicon03.setToolTipText("Filter processor");
        statusBar.addLeftComponent(cicon03, false);

        getContentPane().add(statusBar, BorderLayout.SOUTH);
        getContentPane().add(wc, BorderLayout.CENTER);

        getContentPane().add(jScrollPane1, BorderLayout.WEST);
        setLayout(getContentPane().getLayout());
        pack();
    }

    /**
     * @return the cicon01
     */
    public IconLabel getCicon01() {
        return cicon01;
    }

    /**
     * @return the cicon02
     */
    public IconLabel getCicon02() {
        return cicon02;
    }

    /**
     * @return the cicon03
     */
    public IconLabel getCicon03() {
        return cicon03;
    }
}

