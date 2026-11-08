package com.example.monitortest.ui.results.components.projection;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.example.monitortest.ui.theme.UIUtility.font;

// Calculates milestone positions on the emissions comparison ruler.
// This class performs layout calculations without drawing UI components.
public final class MilestoneLayoutCalculator {

    // Utility class: no instances are required.
    private MilestoneLayoutCalculator() {
    }

    // Stores the calculated coordinates for one milestone.
    public record MilestoneLayout(
            int index,
            int markerX,
            int iconX,
            int iconY,
            int labelY
    ) {}

    // Calculates staggered four-lane positions for the supplied milestones.
    // The selected milestone is always placed before ordinary milestones.
    public static List<MilestoneLayout> calculateLayouts(
            FontMetrics metrics,
            List<EmissionMilestone> milestones,
            int selectedMilestoneIndex,
            int startX,
            int endX,
            int rulerY,
            int panelWidth,
            double scaleMaximum
    ) {

        List<MilestoneLayout> layouts = new ArrayList<>();

        // Track occupied horizontal intervals separately for each lane.
        List<List<int[]>> occupied = new ArrayList<>();

        for (int lane = 0; lane < 4; lane++) {
            occupied.add(new ArrayList<>());
        }

        // Two lanes above and two below the ruler.
        int[] iconOffsets = {-100, 48, -155, 103};
        int[] labelOffsets = {-65, 80, -120, 135};

        int minimumGap = 16;
        int horizontalPadding = 12;

        // Reserve space for the selected milestone before placing others.
        EmissionMilestone selected = milestones.get(selectedMilestoneIndex);

        int selectedMarkerX = getPosition( selected.emissionsGrams(), startX, endX, scaleMaximum );
        int selectedWidth = Math.max( metrics.stringWidth(selected.name()) + 12, 32 );

        // Centre the selected milestone as closely as possible to its marker.
        int selectedIconX = Math.max( horizontalPadding + selectedWidth / 2, Math.min( selectedMarkerX, panelWidth - horizontalPadding - selectedWidth / 2 ) );

        // Reserve lane 0 for the selected milestone.
        int selectedStart = selectedIconX - selectedWidth / 2;
        int selectedEnd = selectedIconX + selectedWidth / 2;

        occupied.get(0).add(new int[]{selectedStart, selectedEnd});

        layouts.add(new MilestoneLayout( selectedMilestoneIndex, selectedMarkerX, selectedIconX, rulerY + iconOffsets[0], rulerY + labelOffsets[0] ));

        // Process remaining milestones in their original emissions order.
        for (int i = 0; i < milestones.size(); i++) {

            if (i == selectedMilestoneIndex) {
                continue;
            }

            EmissionMilestone milestone = milestones.get(i);

            // Skip milestones outside the visible emissions scale.
            if (milestone.emissionsGrams() > scaleMaximum) {
                continue;
            }

            int markerX = getPosition(
                    milestone.emissionsGrams(), startX, endX, scaleMaximum
            );

            int itemWidth = Math.max(
                    metrics.stringWidth(milestone.name()) + 12, 32
            );

            int bestLane = -1;
            int bestIconX = markerX;
            int smallestDisplacement = Integer.MAX_VALUE;

            // Find the lane requiring the smallest horizontal adjustment.
            for (int lane = 0; lane < 4; lane++) {

                int iconX = Math.max(
                        markerX, horizontalPadding + itemWidth / 2
                );

                // Move right while occupied intervals overlap this item.
                boolean adjusted;

                do {
                    adjusted = false;

                    int itemStart = iconX - itemWidth / 2;
                    int itemEnd = iconX + itemWidth / 2;

                    for (int[] interval : occupied.get(lane)) {
                        boolean overlaps = itemStart < interval[1] + minimumGap && itemEnd > interval[0] - minimumGap;
                        if (overlaps) {
                            iconX = interval[1] + minimumGap + itemWidth / 2;
                            adjusted = true;
                            break;
                        }
                    }

                } while (adjusted && iconX + itemWidth / 2 <= panelWidth - horizontalPadding);

                // Skip lanes where the item would exceed the panel boundary.
                if (iconX + itemWidth / 2
                        > panelWidth - horizontalPadding) {
                    continue;
                }

                int displacement = Math.abs(iconX - markerX);
                if (displacement < smallestDisplacement) {
                    smallestDisplacement = displacement;
                    bestLane = lane;
                    bestIconX = iconX;
                }
            }

            // Ordinary milestones may be hidden when all lanes are full.
            if (bestLane == -1) {
                continue;
            }

            // Reserve the selected interval within the chosen lane.
            occupied.get(bestLane).add(new int[]{
                    bestIconX - itemWidth / 2,
                    bestIconX + itemWidth / 2
            });

            layouts.add(new MilestoneLayout( i, markerX, bestIconX, rulerY + iconOffsets[bestLane], rulerY + labelOffsets[bestLane] ));
        }

        return layouts;
    }

    // Converts an emissions value to its position along the ruler.
    private static int getPosition( double emissionsGrams, int startX, int endX, double scaleMaximum ) {
        // Constrain the emissions proportion to the ruler's visible range.
        double proportion = Math.max( 0.0, Math.min(1.0, emissionsGrams / scaleMaximum));
        return startX + (int) Math.round(proportion * (endX - startX));
    }
}