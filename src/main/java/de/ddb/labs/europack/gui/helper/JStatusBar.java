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
package de.ddb.labs.europack.gui.helper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author See
 * https://stackoverflow.com/questions/13366780/how-to-add-real-time-date-and-time-into-a-jframe-component-e-g-status-bar
 */
public final class JStatusBar extends JPanel {

    private final static long serialVersionUID = 1L;

    private final JPanel leftPanel;
    private final JPanel rightPanel;

    public JStatusBar() {
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 3));
        createPartControl();
    }

    protected void createPartControl() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), 23));
        
        leftPanel.setOpaque(false);
        add(leftPanel, BorderLayout.WEST);
      
        rightPanel.setOpaque(false);
        add(rightPanel, BorderLayout.EAST);
    }

    public void addLeftComponent(JComponent component, boolean rightSeparator) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        component.setBorder(new EmptyBorder(0, 0, 0, 10));
        panel.add(component);
        if (rightSeparator) {
            final SeparatorPanel sp = new SeparatorPanel(Color.GRAY, Color.WHITE);
            sp.setBorder(new EmptyBorder(0, 0, 0, 0));
            panel.add(sp);
        }
        leftPanel.add(panel);
    }

    public void addRightComponent(JComponent component, boolean leftSeparator) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        component.setBorder(new EmptyBorder(0, 10, 0, 0));
        if (leftSeparator) {
            final SeparatorPanel sp = new SeparatorPanel(Color.GRAY, Color.WHITE);
            sp.setBorder(new EmptyBorder(0, 0, 0, 0));
            panel.add(sp);
        }
        panel.add(component);
        rightPanel.add(panel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;

        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
        y++;

        g.setColor(new Color(218, 215, 201));
        g.drawLine(0, y, getWidth(), y);
        y++;

        g.setColor(new Color(233, 231, 217));
        g.drawLine(0, y, getWidth(), y);

        y = getHeight() - 3;

        g.setColor(new Color(233, 232, 218));
        g.drawLine(0, y, getWidth(), y);
        y++;

        g.setColor(new Color(233, 231, 216));
        g.drawLine(0, y, getWidth(), y);
        y++;

        g.setColor(new Color(221, 221, 220));
        g.drawLine(0, y, getWidth(), y);
    }

}


