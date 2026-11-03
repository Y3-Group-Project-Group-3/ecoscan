package com.example.monitortest.ui.livemonitor.components.comparison;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

// Detects mouse interactions with rendered milestone graphics.
// Hit areas are supplied by the painter, not recalculated here.
public final class ComparisonMilestoneInteraction {

    // Utility class: no instances are required.
    private ComparisonMilestoneInteraction() {
    }

    // Describes the interactive regions of a rendered milestone.
    // A null labelBounds means its label was not displayed.
    public record MilestoneHitArea(
            EmissionMilestone milestone,
            Rectangle dotBounds,
            Rectangle iconBounds,
            Rectangle labelBounds
    ) {
    }

    // Returns the milestone beneath the mouse, or null if none matches.
    public static EmissionMilestone findHoveredMilestone( MouseEvent event, List<MilestoneHitArea> hitAreas ) {

        int mouseX = event.getX();
        int mouseY = event.getY();

        // Check the rendered milestones in their original drawing order.
        for (MilestoneHitArea area : hitAreas) {

            // Detect the circular marker or associated activity icon.
            boolean overDot = area.dotBounds().contains(mouseX, mouseY);
            boolean overIcon = area.iconBounds().contains(mouseX, mouseY);

            // Only check the label when the painter actually displayed it.
            boolean overLabel = area.labelBounds() != null && area.labelBounds().contains(mouseX, mouseY);

            if (overDot || overIcon || overLabel) {
                return area.milestone();
            }
        }

        // The cursor is not currently over a rendered milestone.
        return null;
    }
}
