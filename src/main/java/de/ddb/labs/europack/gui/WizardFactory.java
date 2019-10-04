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

import com.github.cjwizard.PageFactory;
import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class WizardFactory implements PageFactory {

    private final static Logger LOG = LoggerFactory.getLogger(WizardFactory.class);

    private final static List<WizardPageEuropack> PAGES = new ArrayList<WizardPageEuropack>() {
        private static final long serialVersionUID = 6801682749191866940L;

        {
            add(new WizardPage01("Welcome!", "Welcome to Europack"));
            add(new WizardPage02("Data source", "Choose a source for all the data"));
            add(new WizardPage03("Data Filter(s)", "Choose filters"));
            add(new WizardPage04("Data sink", "Set a sink for all the data"));
            add(new WizardPage05("Processing", "Data magic in process..."));
        }
    };

    /**
     *
     * @return
     */
    public int getSize() {
        return PAGES.size();
    }

    public void dispose() {
        for (WizardPageEuropack wp : PAGES) {
            wp.dispose();
        }
    }

    public void cancel() {
        for (WizardPageEuropack wp : PAGES) {
            wp.cancel();
        }
    }

    /**
     *
     * @param page
     * @return
     */
    public String getTitle(int page) {
        if (page > -1 && page < PAGES.size()) {
            return PAGES.get(page).getTitle();
        }
        return null;
    }

    /**
     *
     * @param path
     * @param settings
     * @return
     */
    @Override
    public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
        LOG.info("creating page " + path.size());

        if (path.isEmpty()) {
            return PAGES.get(0);
        }

        final WizardPage lastPage = path.get(path.size() - 1);
        if (lastPage instanceof WizardPage05) {
            return PAGES.get(0);
        }

        for (int i = 0; i < PAGES.size(); i++) {
            if (PAGES.get(i) == lastPage) {
                LOG.info("Returning page: " + PAGES.get(i + 1));
                return PAGES.get(i + 1);
            }
        }

        throw new RuntimeException();
    }

    /**
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public boolean isTransient(List<WizardPage> arg0, WizardSettings arg1) {
        return false;
    }

}
