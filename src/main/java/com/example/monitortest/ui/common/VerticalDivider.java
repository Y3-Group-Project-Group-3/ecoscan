package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable vertical divider for separating groups of UI controls.
public class VerticalDivider extends JComponent {

    private final float heightRatio;

    // Creates a divider with a configurable height ratio.
    public VerticalDivider(float heightRatio) {
        this.heightRatio = Math.max(0f, Math.min(1f, heightRatio));

        // Reserve horizontal space without forcing the parent's height.
        setPreferredSize(new Dimension(12, 42));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Enable smooth line rendering.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(1.5f));

        // Draw the line centred vertically.
        int lineHeight = Math.round(getHeight() * heightRatio);
        int startY = (getHeight() - lineHeight) / 2;
        int centreX = getWidth() / 2;

        g2.drawLine(centreX, startY, centreX, startY + lineHeight);

        g2.dispose();
    }
}