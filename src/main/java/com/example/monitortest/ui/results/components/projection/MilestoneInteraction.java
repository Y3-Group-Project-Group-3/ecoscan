package com.example.monitortest.ui.results.components.projection;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.results.components.projection.MilestoneLayoutCalculator.MilestoneLayout;

import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

// Identifies which milestone is being hovered over.
// This class performs hit testing without painting any graphics.
public final class MilestoneInteraction {

    // Utility class: no instances are required.
    private MilestoneInteraction() {
    }

    // Finds the milestone beneath the cursor, or returns null.
    // Prioritises the selected milestone when hit areas overlap.
    public static EmissionMilestone findHoveredMilestone(
            MouseEvent event,
            List<EmissionMilestone> milestones,
            List<MilestoneLayout> layouts,
            int selectedMilestoneIndex,
            int rulerY,
            FontMetrics metrics
    ) {

        int mouseX = event.getX();
        int mouseY = event.getY();

        // Check the selected milestone first when hit areas overlap.
        for (int pass = 0; pass < 2; pass++) {

            for (MilestoneLayout layout : layouts) {

                boolean selected = layout.index() == selectedMilestoneIndex;

                // First pass checks selected; second checks all others.
                if ((pass == 0) != selected) {
                    continue;
                }

                EmissionMilestone milestone = milestones.get(layout.index());

                // Detect hovering over the marker on the ruler.
                boolean overMarker = Math.abs(mouseX - layout.markerX()) <= 10 && Math.abs(mouseY - rulerY) <= 10;

                // Detect hovering over the milestone activity icon.
                boolean overIcon = Math.abs(mouseX - layout.iconX()) <= 16 && Math.abs(mouseY - layout.iconY()) <= 16;

                // Calculate the label's interactive bounds.
                int textWidth = metrics.stringWidth(milestone.name());

                int textX = layout.iconX() - textWidth / 2;

                Rectangle labelBounds = new Rectangle( textX - 6, layout.labelY() - metrics.getAscent() - 3, textWidth + 12, metrics.getHeight() + 6 );

                boolean overLabel = labelBounds.contains(mouseX, mouseY);

                // Return the first matching milestone.
                if (overMarker || overIcon || overLabel) {
                    return milestone;
                }
            }
        }

        // No interactive milestone was found at this position.
        return null;
    }
}
