package com.example.monitortest.ui.livemonitor.components.comparison;

import java.awt.*;
import java.util.Locale;

import static com.example.monitortest.ui.theme.AppTheme.GREEN;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws the animated PC marker and cumulative emissions label
// on the Live Monitor's emissions comparison ruler.
public final class ComparisonPcPainter {

    // Utility class: no instances are required.
    private ComparisonPcPainter() {
    }

    // Draws the PC label, connector and monitor icon at the supplied position.
    // The parent panel remains responsible for calculating markerX.
    public static void paint( Graphics2D g2, int markerX, int rulerY, int panelWidth, double totalEmissionsGrams ) {

        // Format the measured cumulative emissions for display.
        String pcEmissionsLabel = String.format( Locale.UK, "%.4f g CO₂e", totalEmissionsGrams );

        // Set the label's font and colour.
        g2.setFont(font(Font.PLAIN, 12));
        g2.setColor(GREEN);

        // Measure the text so it can be centred above the PC icon.
        FontMetrics pcMetrics = g2.getFontMetrics();
        int pcLabelWidth = pcMetrics.stringWidth(pcEmissionsLabel);

        // Centre the label above the PC marker.
        int pcLabelX = markerX - pcLabelWidth / 2;

        // Keep the label inside the panel's horizontal boundaries.
        pcLabelX = Math.max( 5, Math.min(pcLabelX, panelWidth - pcLabelWidth - 5) );

        // Draw the cumulative emissions value above the monitor.
        int pcLabelY = rulerY - 78;
        g2.drawString(pcEmissionsLabel, pcLabelX, pcLabelY);

        // Draw a vertical connector from the PC to the ruler.
        g2.setColor(GREEN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(markerX, rulerY - 35, markerX, rulerY);

        // Draw the green monitor-shaped icon.
        int iconWidth = 30;
        int iconHeight = 22;

        g2.setColor(GREEN);
        g2.fillRoundRect( markerX - iconWidth / 2, rulerY - 65, iconWidth, iconHeight, 5, 5 );

        // Draw the white display inside the monitor.
        g2.setColor(Color.WHITE);
        g2.fillRoundRect( markerX - iconWidth / 2 + 4, rulerY - 61, iconWidth - 8, iconHeight - 8, 2, 2 );

        // Draw the monitor stand and base.
        g2.setColor(GREEN);
        g2.fillRect(markerX - 2, rulerY - 43, 4, 7);
        g2.fillRect(markerX - 9, rulerY - 36, 18, 3);
    }
}
