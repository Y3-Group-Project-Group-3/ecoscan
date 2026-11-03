package com.example.monitortest.ui.results.components.projection;

import java.awt.*;

import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws the horizontal emissions ruler and its scale labels.
// All coordinates and scale values are supplied by EmissionProjectionScale.
public final class EmissionScalePainter {

    // Utility class: prevents unnecessary instantiation.
    private EmissionScalePainter() {
    }

    // Draws the main horizontal emissions ruler.
    public static void paintRuler( Graphics2D g2, int startX, int endX, int rulerY) {

        // Apply the existing ruler colour and rounded stroke.
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke( 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));

        // Draw the ruler across the available horizontal space.
        g2.drawLine(startX, rulerY, endX, rulerY);
    }

    // Draws the ruler's tick marks and their emissions values.
    public static void paintTicks(
            Graphics2D g2,
            int startX,
            int endX,
            int rulerY,
            int panelWidth,
            double scaleMaximum
    ) {

        // Configure the existing tick font, colour and line thickness.
        g2.setFont(font(Font.PLAIN, 13));
        g2.setColor(new Color(100, 119, 146));
        g2.setStroke(new BasicStroke(1.5f));

        int tickCount = 5;

        // Distribute five tick marks evenly across the ruler.
        for (int i = 0; i < tickCount; i++) {

            double proportion = (double) i / (tickCount - 1);

            int tickX = startX + (int) Math.round(proportion * (endX - startX));

            double tickValue = proportion * scaleMaximum;

            // Draw a tick crossing the horizontal ruler.
            g2.drawLine(tickX, rulerY - 10, tickX, rulerY + 10);

            // Calculate the text position, keeping labels inside the panel.
            String tickText = formatScaleValue(tickValue);
            FontMetrics metrics = g2.getFontMetrics();

            int textWidth = metrics.stringWidth(tickText);
            int labelX = tickX - textWidth / 2;

            labelX = Math.max( 2, Math.min(labelX, panelWidth - textWidth - 2) );

            // Draw the corresponding emissions value above the ruler.
            g2.drawString(tickText, labelX, rulerY - 22);
        }
    }

    // Formats ruler ticks into grams, kilograms or tonnes.
    private static String formatScaleValue(double grams) {

        if (grams == 0) {
            return "0 g";
        }

        if (grams >= 1_000_000) {
            return String.format("%.1f t", grams / 1_000_000);
        }

        if (grams >= 1000) {
            return String.format("%.1f kg", grams / 1000);
        }

        if (grams >= 1) {
            return String.format("%.1f g", grams);
        }

        return String.format("%.3f g", grams);
    }
}
