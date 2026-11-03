package com.example.monitortest.ui.common;

import java.awt.*;

// Shared helper methods for custom Swing painting.
public final class GraphicsUtils {

    // Prevent instantiation of this utility class.
    private GraphicsUtils() {
    }

    // Creates an independent Graphics2D context with antialiasing enabled.
    public static Graphics2D createSmoothGraphics(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        return g2;
    }
}