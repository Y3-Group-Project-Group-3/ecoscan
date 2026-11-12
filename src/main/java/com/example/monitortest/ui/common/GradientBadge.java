package com.example.monitortest.ui.common;

import com.example.monitortest.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Renders the sidebar's EcoScan leaf logo as window icons (title bar and taskbar).
     * Several sizes are supplied so Windows can pick a sharp one at any display scaling.
     * The proportions match the sidebar logo (36 px badge, 19 px leaf, 10 px corner radius)
     * and the corners stay transparent.
     */
    public static List<Image> createAppIconImages() {
        List<Image> images = new ArrayList<>();

        for (int size : new int[] {16, 20, 24, 32, 40, 48, 64, 128, 256}) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int iconSize = Math.round(size * 19f / 36f);
            int radius = Math.round(size * 10f / 36f);

            g2.setPaint(new GradientPaint(0, 0, AppTheme.GREEN_LIGHT, size, size, AppTheme.GREEN));
            g2.fill(new RoundRectangle2D.Float(0, 0, size, size, radius * 2, radius * 2));

            // Thicker leaf outline at small sizes, so it stays visible in the 16 px title bar.
            float stroke = size <= 24 ? 2.8f : size <= 40 ? 2.4f : 2f;
            int offset = (size - iconSize) / 2;
            new LineIcon("LEAF", iconSize, Color.WHITE).withStroke(stroke).paintIcon(null, g2, offset, offset);

            g2.dispose();
            images.add(image);
        }

        return images;
    }

    static void paintBadge(Graphics2D g2, int size, String iconName, int iconSize, int radius) {
        g2.setPaint(new GradientPaint(0, 0, AppTheme.GREEN_LIGHT, size, size, AppTheme.GREEN));
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, radius * 2, radius * 2));
        int offset = (size - iconSize) / 2;
        new LineIcon(iconName, iconSize, Color.WHITE).paintIcon(null, g2, offset, offset);
    }
}