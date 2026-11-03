package com.example.monitortest.ui.livemonitor.components.comparison;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;

import java.util.List;

// Manages the target range of the Live Monitor emissions ruler.
// The panel's animation timer remains responsible for moving the displayed ruler scale towards this target.
public final class EmissionScaleController {

    // Keep the upcoming milestone near 90% of the ruler.
    private static final double NEXT_MILESTONE_POSITION = 0.90;

    // Retain the original starting milestone and target range.
    private double nextMilestoneGrams = 0.2;
    private double targetScaleMaximumGrams = 0.2 / NEXT_MILESTONE_POSITION;

    // Updates the next milestone and target scale using accumulated emissions.
    public void updateTarget( double totalEmissionsGrams, List<EmissionMilestone> milestones ) {

        // Find the first milestone the PC has not nearly reached.
        double nextMilestone = Double.NaN;

        for (EmissionMilestone milestone : milestones) {

            // Advance once emissions reach 95% of a milestone.
            if (totalEmissionsGrams < milestone.emissionsGrams() * 0.95) {
                nextMilestone = milestone.emissionsGrams();
                break;
            }
        }

        if (!Double.isNaN(nextMilestone)) {

            // Track the next milestone that should remain visible.
            nextMilestoneGrams = nextMilestone;

            // Position that milestone at 90% of the target ruler range.
            targetScaleMaximumGrams = nextMilestone / NEXT_MILESTONE_POSITION;

        } else {

            // After the final milestone, expand the scale by doubling.
            // Retain the previous upcoming milestone value, as before.
            while (totalEmissionsGrams >= targetScaleMaximumGrams) {
                targetScaleMaximumGrams *= 2.0;
            }
        }
    }

    // Returns the scale maximum the zoom animation should approach.
    public double getTargetScaleMaximumGrams() {
        return targetScaleMaximumGrams;
    }

    // Returns the milestone currently used for the upcoming comparison.
    public double getNextMilestoneGrams() {
        return nextMilestoneGrams;
    }
}
