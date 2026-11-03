package com.example.monitortest.ui.livemonitor.components.comparison;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.icons.EmissionIconRenderer;
import com.example.monitortest.ui.livemonitor.components.comparison.ComparisonMilestoneInteraction.MilestoneHitArea;



import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Draws milestone markers, icons and labels on the Live Monitor ruler.
// Positions depend on the current animated scale supplied by the parent.
public final class ComparisonMilestonePainter {

    // Utility class: no instances are required.
    private ComparisonMilestonePainter() {
    }

    // Draws visible milestones and the next upcoming milestone.
    // The current scale determines when milestone graphics enter the ruler.
    public static List<MilestoneHitArea> paint(
            Graphics2D g2,
            List<EmissionMilestone> milestones,
            double scaleMaximumGrams,
            double nextMilestoneGrams,
            int startX,
            int endX,
            int rulerY,
            int panelWidth
    ) {

        // Collect interactive regions for milestones drawn on the ruler.
        List<MilestoneHitArea> hitAreas = new ArrayList<>();

        // Track the end of the previous label to prevent overlapping text.
        int lastLabelEndX = Integer.MIN_VALUE;

        // Examine milestones in their original order.
        for (EmissionMilestone milestone : milestones) {

            boolean isUpcoming = milestone.emissionsGrams() == nextMilestoneGrams;

            boolean isOnScale = milestone.emissionsGrams() <= scaleMaximumGrams;

            // Ignore milestones that are neither visible nor upcoming.
            if (!isOnScale && !isUpcoming) {
                continue;
            }

            // Calculate the milestone's position on the animated ruler.
            double position = milestone.emissionsGrams() / scaleMaximumGrams;

            // Upcoming milestones may initially lie beyond the right edge.
            // Preserve this behaviour so they enter as the scale expands.
            double displayPosition = position;

            int milestoneX = startX + (int) Math.round( displayPosition * (endX - startX) );

            // Draw the milestone dot only when it is within the scale.
            if (isOnScale) {
                int dotDiameter = 10;
                g2.setColor(new Color(100, 116, 139));
                g2.fillOval( milestoneX - dotDiameter / 2, rulerY - dotDiameter / 2, dotDiameter, dotDiameter );
            }

            // Draw the existing activity icon beneath the milestone.
            EmissionIconRenderer.drawIcon( g2, milestone.name(), milestoneX, rulerY + 25);

            // Use the original font and colour for milestone labels.
            g2.setFont(font(Font.PLAIN, 12));
            g2.setColor(TEXT_MUTED);

            FontMetrics metrics = g2.getFontMetrics();

            String label = milestone.name();
            int labelWidth = metrics.stringWidth(label);
            // Centre the label beneath its milestone icon.
            int labelX = milestoneX - labelWidth / 2;
            int labelY = rulerY + 55;
            // Display labels only when they fit and do not overlap.
            boolean fitsPanel = labelX >= 0 && labelX + labelWidth <= panelWidth;
            boolean hasSpace = labelX >= lastLabelEndX + 12;

            // Only displayed labels should have an interactive label region.
            Rectangle labelBounds = null;

            if (fitsPanel && hasSpace) {

                // Draw the milestone name without changing its appearance.
                g2.drawString(label, labelX, labelY);

                // Reserve this horizontal space for the displayed label.
                lastLabelEndX = labelX + labelWidth;

                // Match the existing label hover region.
                labelBounds = new Rectangle( labelX, rulerY + 42, labelWidth + 1, 19 );
            }

            // Preserve the original behaviour: only milestones already on
            // the displayed scale participate in mouse interaction.
            if (isOnScale) {

                // Match the original hover region around the milestone dot.
                Rectangle dotBounds = new Rectangle( milestoneX - 12, rulerY - 12, 25, 25 );

                // Match the original hover region around the activity icon.
                Rectangle iconBounds = new Rectangle( milestoneX - 14, rulerY + 10, 29, 31 );

                // Store the drawn milestone and its available interactive areas.
                hitAreas.add(new MilestoneHitArea( milestone, dotBounds, iconBounds, labelBounds));
            }


        }
        // Return the interactive areas created during this drawing pass.
        return List.copyOf(hitAreas);
    }
}
