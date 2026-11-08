package com.example.monitortest.ui.results.components.projection;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.icons.EmissionIconRenderer;
import com.example.monitortest.ui.results.components.projection.MilestoneLayoutCalculator.MilestoneLayout;

import java.awt.*;
import java.util.List;

import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws milestone connectors, markers, icons and labels on the emissions ruler.
// Positions are supplied by MilestoneLayoutCalculator.
public final class MilestonePainter {

    // Utility class: no instances are required.
    private MilestonePainter() {
    }

    // Draws the supplied milestone layouts without calculating new positions.
    public static void paint( Graphics2D g2, List<EmissionMilestone> milestones, List<MilestoneLayout> layouts, int startX, int endX, int rulerY) {

        Color milestoneColor = new Color(100, 119, 146);
        Color connectorColor = new Color(190, 200, 212);

        // Draw connector lines before the ruler and milestone graphics.
        for (MilestoneLayout layout : layouts) {

            g2.setColor(connectorColor);
            g2.setStroke(new BasicStroke(1.2f));

            int iconX = layout.iconX();
            int iconY = layout.iconY();
            int markerX = layout.markerX();

            // Leave space between the connector and the activity icon.
            int iconGap = 18;

            boolean aboveRuler = iconY < rulerY;
            int connectorEndY = aboveRuler ? iconY + iconGap : iconY - iconGap;
            if (iconX == markerX) {
                // Draw a straight connector when no horizontal offset is needed.
                g2.drawLine( markerX, rulerY, markerX, connectorEndY );
            } else {

                // Route the connector around staggered milestone positions.
                int bendY = aboveRuler ? iconY + iconGap + 12 : iconY - iconGap - 12;

                g2.drawLine(markerX, rulerY, markerX, bendY);
                g2.drawLine(markerX, bendY, iconX, bendY);
                g2.drawLine(iconX, bendY, iconX, connectorEndY);
            }
        }

        // Draw the ruler over the connectors to preserve the original layering.
        EmissionScalePainter.paintRuler(g2, startX, endX, rulerY);

        // Draw each milestone using the positions supplied by the calculator.
        for (MilestoneLayout layout : layouts) {

            EmissionMilestone milestone = milestones.get(layout.index());

            // Draw the circular marker at the exact emissions position.
            g2.setColor(milestoneColor);

            int radius = 4;

            g2.fillOval( layout.markerX() - radius, rulerY - radius, radius * 2, radius * 2 );

            // Reuse the icon renderer shared with the Live Monitor.
            EmissionIconRenderer.drawIcon( g2, milestone.name(), layout.iconX(), layout.iconY() );

            // Measure the milestone name using the original font.
            g2.setFont(font(Font.PLAIN, 12));

            FontMetrics metrics = g2.getFontMetrics();
            String label = milestone.name();

            int textWidth = metrics.stringWidth(label);
            int textX = layout.iconX() - textWidth / 2;
            int textY = layout.labelY();

            // Provide spacing around the milestone name.
            int paddingX = 6;
            int paddingY = 3;

            int backgroundX = textX - paddingX;
            int backgroundY = textY - metrics.getAscent() - paddingY;

            int backgroundWidth = textWidth + paddingX * 2;
            int backgroundHeight = metrics.getHeight() + paddingY * 2;

            // Hide connector lines behind the milestone label.
            g2.setColor(Color.WHITE);
            g2.fillRoundRect( backgroundX, backgroundY, backgroundWidth, backgroundHeight, 6, 6 );

            // Draw the milestone name over its white background.
            g2.setColor(milestoneColor);
            g2.drawString(label, textX, textY);
        }
    }
}