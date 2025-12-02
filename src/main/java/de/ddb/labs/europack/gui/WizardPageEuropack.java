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

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public abstract class WizardPageEuropack extends WizardPage {

    private static final long serialVersionUID = 5181179310845917964L;

    public WizardPageEuropack(String title, String description) {
        super(title, description);

    }

    public void dispose() {
    }

    public void cancel() {
    }
}




