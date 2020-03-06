/*
 * Source: https://stackoverflow.com/questions/13366780/how-to-add-real-time-date-and-time-into-a-jframe-component-e-g-status-bar
 */
package de.ddb.labs.europack.gui.helper;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 *
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




