package com.example.monitortest.ui.results.components.projection;

import java.awt.*;

import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws the projected PC emissions marker on the comparison ruler.
// Position calculations remain the responsibility of EmissionProjectionScale.
public final class PcEmissionPainter {

    // Utility class: no instances are required.
    private PcEmissionPainter() {
    }

    // Draws the PC marker, monitor icon and projected emissions label.
    public static void paint(
            Graphics2D g2, // Existing graphics context provided by the ruler
            int markerX, // Calculated horizontal position of the PC marker
            int rulerY, // Vertical position of the emissions ruler
            double emissionsGrams // Projected emissions to display beneath the PC label
    ) {

        String pcLabel = "Your PC";
        String emissionsText = formatEmissions(emissionsGrams);

        // Position the monitor above the emissions ruler.
        int monitorY = rulerY - 94;
        Color markerGreen = new Color(0x168653);

        // Draw the connector between the monitor and the ruler.
        g2.setColor(markerGreen);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(markerX, rulerY - 58, markerX, rulerY);

        // Draw a hollow green marker at the exact emissions position.
        int pcMarkerRadius = 7;

        g2.setColor(Color.WHITE);
        g2.fillOval( markerX - pcMarkerRadius, rulerY - pcMarkerRadius, pcMarkerRadius * 2, pcMarkerRadius * 2 );

        g2.setColor(markerGreen);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval( markerX - pcMarkerRadius, rulerY - pcMarkerRadius, pcMarkerRadius * 2, pcMarkerRadius * 2 );

        // Draw the monitor's white interior and green outline.
        int monitorWidth = 38;
        int monitorHeight = 27;
        int monitorX = markerX - monitorWidth / 2;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect( monitorX, monitorY, monitorWidth, monitorHeight, 3, 3 );

        g2.setColor(markerGreen);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect( monitorX, monitorY, monitorWidth, monitorHeight, 3, 3 );

        // Draw the monitor stand.
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine( markerX, monitorY + monitorHeight, markerX, monitorY + monitorHeight + 10 );
        g2.drawLine( markerX - 10, monitorY + monitorHeight + 10, markerX + 10, monitorY + monitorHeight + 10 );

        // Calculate the position of the PC label.
        g2.setFont(font(Font.BOLD, 13));
        FontMetrics labelMetrics = g2.getFontMetrics();

        int pcLabelX = markerX - labelMetrics.stringWidth(pcLabel) / 2;
        int pcLabelY = monitorY - 34;

        // Draw a white background behind the PC label.
        g2.setColor(Color.WHITE);
        g2.fillRoundRect( pcLabelX - 4, pcLabelY - labelMetrics.getAscent() - 2, labelMetrics.stringWidth(pcLabel) + 8, labelMetrics.getHeight() + 4, 6, 6 );

        // Draw the PC label.
        g2.setColor(markerGreen);
        g2.drawString(pcLabel, pcLabelX, pcLabelY);

        // Calculate the position of the emissions value.
        g2.setFont(font(Font.PLAIN, 14));
        FontMetrics emissionsMetrics = g2.getFontMetrics();

        int emissionsX = markerX - emissionsMetrics.stringWidth(emissionsText) / 2;
        int emissionsY = monitorY - 12;

        // Draw a white background behind the emissions value.
        g2.setColor(Color.WHITE);
        g2.fillRoundRect( emissionsX - 4, emissionsY - emissionsMetrics.getAscent() - 2, emissionsMetrics.stringWidth(emissionsText) + 8, emissionsMetrics.getHeight() + 4, 6, 6 );

        // Draw the projected emissions value.
        g2.setColor(markerGreen);
        g2.drawString(emissionsText, emissionsX, emissionsY);
    }

    // Formats emissions into grams, kilograms or tonnes.
    private static String formatEmissions(double grams) {

        if (grams >= 1_000_000) {
            return String.format("%.2f t CO₂e", grams / 1_000_000);
        }

        if (grams >= 1000) {
            return String.format("%.2f kg CO₂e", grams / 1000);
        }

        if (grams >= 1) {
            return String.format("%.2f g CO₂e", grams);
        }

        return String.format("%.4f g CO₂e", grams);
    }
}