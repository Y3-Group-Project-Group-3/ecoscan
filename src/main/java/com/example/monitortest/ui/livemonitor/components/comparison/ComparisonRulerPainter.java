package com.example.monitortest.ui.livemonitor.components.comparison;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws the Live Monitor's emissions ruler and dynamically spaced tick labels.
// The current animated scale is supplied by EmissionComparisonPanel.
public final class ComparisonRulerPainter {

    // Utility class: no instances are required.
    private ComparisonRulerPainter() {
    }

    // Draws the horizontal emissions comparison ruler.
    public static void paintRuler( Graphics2D g2, int startX, int endX, int rulerY ) {
        // Preserve the existing ruler colour and rounded appearance.
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke( 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.drawLine(startX, rulerY, endX, rulerY);
    }

    // Draws dynamically spaced tick marks and their emissions labels.
    public static void paintTicks(
            Graphics2D g2,
            int startX,
            int endX,
            int rulerY,
            int panelWidth,
            double scaleMaximumGrams
    ) {

        // Calculate an appropriate rounded interval for the current scale.
        double tickInterval = calculateTickInterval(scaleMaximumGrams);

        g2.setStroke(new BasicStroke(1.5f));

        // Draw ticks at actual emissions values, starting from zero.
        for (int i = 0; i * tickInterval <= scaleMaximumGrams + tickInterval * 1e-9; i++) {

            double tickValue = i * tickInterval;

            // Convert the emissions value into its proportional ruler position.
            double fraction = tickValue / scaleMaximumGrams;

            int tickX = startX + (int) Math.round(fraction * (endX - startX));

            // Draw the tick mark using the existing styling.
            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(tickX, rulerY - 7, tickX, rulerY + 7);

            // Format the emissions value for display above the ruler.
            String tickLabel = formatScaleValue(tickValue);

            g2.setFont(font(Font.PLAIN, 11));
            g2.setColor(TEXT_MUTED);

            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(tickLabel);

            // Keep the tick label within the panel's horizontal boundaries.
            int labelX = Math.max( 5, Math.min( tickX - labelWidth / 2, panelWidth - labelWidth - 5 ) );

            g2.drawString(tickLabel, labelX, rulerY - 16);
        }
    }

    // Calculates a readable tick interval from the animated ruler scale.
    private static double calculateTickInterval(double scaleMaximum) {

        // Aim for approximately five intervals across the ruler.
        double roughInterval = scaleMaximum / 5.0;

        // Determine the interval's order of magnitude.
        double magnitude = Math.pow( 10, Math.floor(Math.log10(roughInterval)) );

        // Normalise the interval to a value between 1 and 10.
        double normalised = roughInterval / magnitude;

        // Round to a conventional interval: 1, 2, 2.5, 5 or 10.
        double rounded;

        if (normalised <= 1.0) {
            rounded = 1.0;
        } else if (normalised <= 2.0) {
            rounded = 2.0;
        } else if (normalised <= 2.5) {
            rounded = 2.5;
        } else if (normalised <= 5.0) {
            rounded = 5.0;
        } else {
            rounded = 10.0;
        }

        return rounded * magnitude;
    }

    // Formats emissions values without unnecessary trailing zeros.
    private static String formatScaleValue(double grams) {

        // Convert larger values to kilograms or tonnes.
        double value = grams;
        String unit = "g";

        if (grams >= 1_000_000) {
            value = grams / 1_000_000;
            unit = "t";
        } else if (grams >= 1_000) {
            value = grams / 1_000;
            unit = "kg";
        }

        // Preserve the original four-decimal-place formatting.
        String formatted = BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();

        return formatted + " " + unit;
    }
}
