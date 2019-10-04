/*
 * Copyright 2019 Deutsche Digitale Bibliothek.
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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 *
 * @author See https://stackoverflow.com/questions/13366780/how-to-add-real-time-date-and-time-into-a-jframe-component-e-g-status-bar
 */
public final class SeparatorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color leftColor;
    private final Color rightColor;

    public SeparatorPanel(Color leftColor, Color rightColor) {
        this.leftColor = leftColor;
        this.rightColor = rightColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(leftColor);
        g.drawLine(0, 0, 0, getHeight());
        g.setColor(rightColor);
        g.drawLine(1, 0, 1, getHeight());
    }

}



