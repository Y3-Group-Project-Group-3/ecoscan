package com.example.monitortest.ui.loading.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static com.example.monitortest.ui.theme.UIUtility.smooth;


// Custom JPanel that draws a rounded background with a configurable border colour and corner radius.
public class RoundedPanel extends JPanel {

    private final Color fill;
    private final Color border;
    private final int arc;

    public RoundedPanel(Color fill, Color border, int arc) {
        this.fill = fill;
        this.border = border;
        this.arc = arc;

        // Allow the custom rounded background to be visible.
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = smooth(g);

        // Create a rounded rectangle within the panel bounds.
        RoundRectangle2D shape = new RoundRectangle2D.Float( 0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc );

        // Draw the background.
        g2.setColor(fill);
        g2.fill(shape);

        // Draw the outline.
        g2.setColor(border);
        g2.draw(shape);

        g2.dispose();
    }
}
