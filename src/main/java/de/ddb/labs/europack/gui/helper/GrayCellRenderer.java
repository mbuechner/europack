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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class GrayCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 5354965030958309811L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (!isSelected && index % 2 != 0) {
            label.setBackground(UIManager.getColor("Menu.background"));
        }

        label.setEnabled(true);
        // setHorizontalAlignment(SwingConstants.CENTER);
        final Border border = new MatteBorder(0, 0, 1, 0, UIManager.getColor("windowBorder"));
        final Border margin = new EmptyBorder(0, 5, 0, 0);
        label.setBorder(new CompoundBorder(border, margin));
        return label;
    }
}


