package com.example.monitortest.ui.common;

import com.example.monitortest.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Green rounded square with a white icon (sidebar logo and scanning page badge). */
public class GradientBadge extends JComponent {
    private final int size, iconSize, radius;
    private final String iconName;

    public GradientBadge(int size, String iconName, int iconSize, int radius) {
        this.size = size;
        this.iconName = iconName;
        this.iconSize = iconSize;
        this.radius = radius;
        setPreferredSize(new Dimension(size, size));
        setMaximumSize(getPreferredSize());
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create a graphics context with antialiased shapes and text.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);
        paintBadge(g2, size, iconName, iconSize, radius);
        g2.dispose();
    }

    static void paintBadge(Graphics2D g2, int size, String iconName, int iconSize, int radius) {
        g2.setPaint(new GradientPaint(0, 0, AppTheme.GREEN_LIGHT, size, size, AppTheme.GREEN));
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, radius * 2, radius * 2));
        int offset = (size - iconSize) / 2;
        new LineIcon(iconName, iconSize, Color.WHITE).paintIcon(null, g2, offset, offset);
    }
}