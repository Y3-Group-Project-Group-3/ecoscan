package com.example.monitortest.ui.common;

import java.awt.*;
import java.util.Map;

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


    // Creates a graphics context with smooth shapes, strokes and text.
    // The caller must dispose of the returned Graphics2D context.
    public static Graphics2D createSmoothTextGraphics(Graphics g) {

        // Start with the existing antialiased graphics configuration.
        Graphics2D g2 = createSmoothGraphics(g);

        // Configure precise stroke rendering.
        g2.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );

        // Enable smooth text rendering.
        g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

        // Apply operating-system text rendering preferences when available.
        Object desktopHints = Toolkit.getDefaultToolkit() .getDesktopProperty("awt.font.desktophints");

        if (desktopHints instanceof Map<?, ?> hints) {
            g2.addRenderingHints(hints);
        }

        return g2;
    }

}