package com.example.monitortest.ui.results.components;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.EmissionMilestones;
import com.example.monitortest.ui.livemonitor.components.icons.EmissionIconRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.ArrayList;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Displays a scan-specific emissions ruler without changing the Live Monitor.
public class EmissionProjectionScale extends JPanel {

    // Both monitoring pages use the same reference milestones.
    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

    // Emissions projected for the selected period.
    private double projectedEmissionsGrams = Double.NaN;

    // Milestone selected by the user.
    private int selectedMilestoneIndex = 0;

    // Stores the calculated position of a milestone's icon and label.
    private record MilestoneLayout(
            int index, // Identifies the milestone
            int markerX, // Its exact emissions position on the ruler
            int iconX, // Horizontal position of the icon, adjusted to avoid overlap
            int iconY, // Vertical position in one of the four lanes
            int labelY // Vertical position of the milestone name
    ) {}



    public EmissionProjectionScale() {
        setOpaque(false);
        // Allow enough space for staggered milestone labels.
        setPreferredSize(new Dimension(0, 360));

        // Register the panel for milestone tooltips.
        setToolTipText("");

        // Display a hand cursor when hovering over an interactive milestone.
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

                // Identify the milestone currently beneath the cursor.
                EmissionMilestone milestone = findHoveredMilestone(e);

                if (milestone != null) {
                    // Show the hand cursor over milestone markers, icons and labels.
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    // Preserve the existing milestone tooltip.
                    setToolTipText(milestone.usageDescription());
                } else {
                    // Restore the normal cursor outside milestone hit areas.
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
            }
        });

        // Restore the normal cursor when the mouse leaves the comparison ruler.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // Restore the default cursor when leaving the comparison scale.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });


    }

    // Displays the same milestone information as the Live Monitor.
    @Override
    public String getToolTipText(MouseEvent event) {

        EmissionMilestone milestone = findHoveredMilestone(event);

        if (milestone == null) {
            return null;
        }

        return "<html><b>" + milestone.name() + "</b>"
                + "<br>Usage: " + milestone.usageDescription()
                + "<br>Estimated emissions: "
                + milestone.emissionsGrams() + " g CO₂e</html>";
    }


    // Updates the green PC marker using the selected period's emissions.
    public void setProjectedEmissions(double grams) {
        projectedEmissionsGrams = grams;
        repaint();
    }

    // Updates the milestone highlighted on the ruler.
    public void setSelectedMilestoneIndex(int index) {
        if (index < 0 || index >= milestones.size()) {
            return;
        }

        selectedMilestoneIndex = index;
        repaint();
    }

    // Determines the maximum emissions value displayed by the ruler.
    private double getScaleMaximum() {

        double milestoneGrams = milestones.get(selectedMilestoneIndex).emissionsGrams();

        double largestValue = milestoneGrams;

        if (Double.isFinite(projectedEmissionsGrams) && projectedEmissionsGrams >= 0) {
            largestValue = Math.max(largestValue, projectedEmissionsGrams);
        }

        // Leave some space beyond the largest marker.
        return Math.max(0.25, largestValue * 1.25);
    }

    // Converts an emissions value into a horizontal ruler position.
    private int getPosition(double emissionsGrams, int startX, int endX) {
        double maximum = getScaleMaximum();
        double proportion = Math.max( 0.0, Math.min(1.0, emissionsGrams / maximum) );
        return startX + (int) Math.round( proportion * (endX - startX) );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Smooth the ruler's lines and shapes.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );


        int startX = 30;
        int endX = getWidth() - 30;
        // Position the ruler slightly higher to balance the milestone lanes.
        int rulerY = getHeight() / 2;

        // Draw the horizontal emissions ruler first.
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke( 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.drawLine(startX, rulerY, endX, rulerY);

        // Draw milestone markers above the ruler before drawing the PC marker.
        drawMilestones(g2, startX, endX, rulerY);

        // Draw the ruler's tick marks and emissions labels.
        double scaleMaximum = getScaleMaximum();

        g2.setFont(font(Font.PLAIN, 13));
        g2.setColor(new Color(100, 119, 146));
        g2.setStroke(new BasicStroke(1.5f));

        int tickCount = 5;

        for (int i = 0; i < tickCount; i++) {

            double proportion = (double) i / (tickCount - 1);
            int tickX = startX + (int) Math.round( proportion * (endX - startX));
            double tickValue = proportion * scaleMaximum;

            // Draw a tick crossing the horizontal ruler.
            g2.drawLine(tickX, rulerY - 10, tickX, rulerY + 10);

            String tickText = formatScaleValue(tickValue);
            FontMetrics metrics = g2.getFontMetrics();

            int labelX = tickX - metrics.stringWidth(tickText) / 2;
            labelX = Math.max(2, Math.min( labelX, getWidth() - metrics.stringWidth(tickText) - 2 ));

            g2.drawString(tickText, labelX, rulerY - 22);
        }


        // Draw the projected PC emissions marker when a valid reading exists.

        if (Double.isFinite(projectedEmissionsGrams) && projectedEmissionsGrams >= 0) {

            int markerX = getPosition( projectedEmissionsGrams, startX, endX );
            // Draw a white background behind the PC icon and text.
            // This prevents milestone graphics from showing through the PC marker.
            String pcLabel = "Your PC";
            String emissionsText = formatEmissions(projectedEmissionsGrams);

            // Match the existing positions of the PC icon and its labels.
            int monitorY = rulerY - 94;

            // Cover milestone icons, labels and connector lines behind the PC graphic.
            g2.setColor(Color.WHITE);
            Color markerGreen = new Color(0x168653);
            g2.setColor(markerGreen);
            g2.setStroke(new BasicStroke(2.5f));
            // Connect the computer marker to the emissions ruler.
            g2.drawLine(markerX, rulerY - 58, markerX, rulerY);

            // Draw a hollow green circle at the PC's exact emissions position.
            // The white centre distinguishes it from the filled milestone dots.
            int pcMarkerRadius = 7;

            g2.setColor(Color.WHITE);
            g2.fillOval( markerX - pcMarkerRadius, rulerY - pcMarkerRadius, pcMarkerRadius * 2, pcMarkerRadius * 2 );
            g2.setColor(markerGreen);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval( markerX - pcMarkerRadius, rulerY - pcMarkerRadius, pcMarkerRadius * 2, pcMarkerRadius * 2 );


            // Draw a monitor outline above the ruler.
            int monitorWidth = 38;
            int monitorHeight = 27;
            int monitorX = markerX - monitorWidth / 2;
            // Fill the monitor interior so milestone graphics cannot show through it.
            g2.setColor(Color.WHITE);
            g2.fillRoundRect( monitorX, monitorY, monitorWidth, monitorHeight, 3, 3 );

            // Draw the green monitor outline over the white interior.
            g2.setColor(markerGreen);
            g2.setStroke(new BasicStroke(4f));
            g2.drawRoundRect( monitorX, monitorY, monitorWidth, monitorHeight, 3, 3 );

            // Draw the monitor stand.
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(markerX, monitorY + monitorHeight, markerX, monitorY + monitorHeight + 10);

            g2.drawLine(markerX - 10, monitorY + monitorHeight + 10, markerX + 10, monitorY + monitorHeight + 10);

            g2.setFont(font(Font.BOLD, 13));
            FontMetrics labelMetrics = g2.getFontMetrics();

            // Draw a small white background behind the PC label.
            int pcLabelX = markerX - labelMetrics.stringWidth(pcLabel) / 2;
            int pcLabelY = monitorY - 34;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect( pcLabelX - 4, pcLabelY - labelMetrics.getAscent() - 2, labelMetrics.stringWidth(pcLabel) + 8, labelMetrics.getHeight() + 4, 6, 6 );

            // Draw the green PC label over its background.
            g2.setColor(markerGreen);
            g2.drawString(pcLabel, pcLabelX, pcLabelY);

            // Display the projected emissions directly below the PC label.
            g2.setFont(font(Font.PLAIN, 14));
            FontMetrics emissionsMetrics = g2.getFontMetrics();

            // Draw a separate white background behind the emissions value.
            int emissionsX = markerX - emissionsMetrics.stringWidth(emissionsText) / 2;
            int emissionsY = monitorY - 12;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    emissionsX - 4,
                    emissionsY - emissionsMetrics.getAscent() - 2,
                    emissionsMetrics.stringWidth(emissionsText) + 8,
                    emissionsMetrics.getHeight() + 4,
                    6,
                    6
            );

            // Draw the emissions value above its background.
            g2.setColor(markerGreen);
            g2.drawString(emissionsText, emissionsX, emissionsY);

        }


        g2.dispose();
    }

    // Formats emissions values for the ruler's tick labels.
    private String formatScaleValue(double grams) {

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



    // Draws visible milestone markers, icons and labels on the scan ruler.
    /*
        For each milestone, the method:
        1 - Calculates its exact position on the emissions ruler.
        2 - Measures how much horizontal space its name requires.
        3 - Checks the four available lanes
        4 - Chooses the lane that requires the smallest horizontal adjustment.
        5 - Stores the resulting position in a MilestoneLayout record.
     */

    // Draws milestones using the staggered four-lane layout.
    private void drawMilestones( Graphics2D g2, int startX, int endX, int rulerY) {

        List<MilestoneLayout> layouts = calculateMilestoneLayouts(g2, startX, endX, rulerY);

        Color milestoneColor = new Color(100, 119, 146);
        Color connectorColor = new Color(190, 200, 212);

        // Draw connectors first so the ruler can appear above them.
        for (MilestoneLayout layout : layouts) {

            // All milestones use the same connector colour and thickness.
            g2.setColor(connectorColor);
            g2.setStroke(new BasicStroke(1.2f));

            int iconX = layout.iconX();
            int iconY = layout.iconY();

            // Connect the actual emissions position to the staggered icon.
            // Leave a gap so connector lines do not enter milestone icons.
            int iconGap = 18;
            int markerX = layout.markerX();

            // Determine whether the milestone icon is above or below the ruler.
            boolean aboveRuler = iconY < rulerY;

            // Stop the vertical connector before reaching the icon.
            int connectorEndY = aboveRuler ? iconY + iconGap : iconY - iconGap;

            if (iconX == markerX) {
                // Straight connector when the icon is aligned with its milestone.
                g2.drawLine( markerX, rulerY, markerX, connectorEndY );

            } else {

                // Offset connector: vertical from ruler, then horizontal,
                // then a short vertical section stopping before the icon.
                int bendY = aboveRuler ? iconY + iconGap + 12 : iconY - iconGap - 12;
                g2.drawLine(markerX, rulerY, markerX, bendY);
                g2.drawLine(markerX, bendY, iconX, bendY);
                g2.drawLine(iconX, bendY, iconX, connectorEndY);
            }

        }

        // Redraw the ruler over the connector lines.
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke( 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.drawLine(startX, rulerY, endX, rulerY);

        // Draw the milestone dots, icons and labels.
        for (MilestoneLayout layout : layouts) {

            EmissionMilestone milestone = milestones.get(layout.index());
            // All milestones use the same grey marker.
            g2.setColor(milestoneColor);
            int radius = 4;

            g2.fillOval( layout.markerX() - radius, rulerY - radius, radius * 2, radius * 2 );

            // Reuse the Live Monitor's milestone icon renderer.
            EmissionIconRenderer.drawIcon( g2, milestone.name(), layout.iconX(), layout.iconY() );

            // Display the milestone name with a background to hide connector lines.
            g2.setFont(font(Font.PLAIN, 12));

            FontMetrics metrics = g2.getFontMetrics();

            String label = milestone.name();

            int textWidth = metrics.stringWidth(label);
            int textX = layout.iconX() - textWidth / 2;
            int textY = layout.labelY();

            // Add a little spacing around the text.
            int paddingX = 6;
            int paddingY = 3;

            int backgroundX = textX - paddingX;
            int backgroundY = textY - metrics.getAscent() - paddingY;
            int backgroundWidth = textWidth + paddingX * 2;
            int backgroundHeight = metrics.getHeight() + paddingY * 2;

            // Cover connector lines behind the label.
            g2.setColor(Color.WHITE);
            g2.fillRoundRect( backgroundX, backgroundY, backgroundWidth, backgroundHeight, 6, 6 );

            // Draw all milestone names in the same neutral colour.
            g2.setColor(milestoneColor);
            g2.drawString(label, textX, textY);

        }
    }




    // Finds the milestone beneath the mouse using its staggered layout.
    private EmissionMilestone findHoveredMilestone(MouseEvent event) {

        int startX = 30;
        int endX = getWidth() - 30;
        // Position the ruler slightly higher to balance the milestone lanes.
        int rulerY = getHeight() / 2;

        // Calculate the same positions used when drawing the milestones.
        Graphics2D g2 = (Graphics2D) getGraphics();

        if (g2 == null) {
            return null;
        }

        try {
            List<MilestoneLayout> layouts = calculateMilestoneLayouts(g2, startX, endX, rulerY);

            int mouseX = event.getX();
            int mouseY = event.getY();

            // Check the selected milestone first when hit areas overlap.
            for (int pass = 0; pass < 2; pass++) {

                for (MilestoneLayout layout : layouts) {

                    boolean selected = layout.index() == selectedMilestoneIndex;

                    if ((pass == 0) != selected) {
                        continue;
                    }

                    EmissionMilestone milestone = milestones.get(layout.index());

                    // Detect hovering over the dot on the ruler.
                    boolean overMarker =  Math.abs(mouseX - layout.markerX()) <= 10 && Math.abs(mouseY - rulerY) <= 10;

                    // Detect hovering over the staggered activity icon.
                    boolean overIcon = Math.abs(mouseX - layout.iconX()) <= 16 && Math.abs(mouseY - layout.iconY()) <= 16;

                    // Detect hovering over the milestone name.
                    g2.setFont(font(Font.PLAIN, 12));
                    FontMetrics metrics = g2.getFontMetrics();

                    int textWidth = metrics.stringWidth(milestone.name());
                    int textX = layout.iconX() - textWidth / 2;

                    Rectangle labelBounds = new Rectangle( textX - 6, layout.labelY() - metrics.getAscent() - 3, textWidth + 12, metrics.getHeight() + 6 );
                    boolean overLabel = labelBounds.contains(mouseX, mouseY);

                    if (overMarker || overIcon || overLabel) {
                        return milestone;
                    }
                }
            }

        } finally {
            g2.dispose();
        }

        return null;
    }




    // Calculates four-lane positions while guaranteeing the selected milestone is visible.
    private List<MilestoneLayout> calculateMilestoneLayouts( Graphics2D g2, int startX, int endX, int rulerY) {

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

        g2.setFont(font(Font.PLAIN, 12));
        FontMetrics metrics = g2.getFontMetrics();

        double scaleMaximum = getScaleMaximum();

        // Reserve space for the selected milestone before placing others.
        EmissionMilestone selected = milestones.get(selectedMilestoneIndex);

        int selectedMarkerX = getPosition( selected.emissionsGrams(), startX, endX );
        int selectedWidth = Math.max( metrics.stringWidth(selected.name()) + 12, 32 );

        // Centre the selected milestone as closely as possible to its marker.
        int selectedIconX = Math.max( horizontalPadding + selectedWidth / 2, Math.min( selectedMarkerX, getWidth() - horizontalPadding - selectedWidth / 2 ) );

        // Reserve lane 0 for the selected milestone's position.
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

            if (milestone.emissionsGrams() > scaleMaximum) {
                continue;
            }
            int markerX = getPosition( milestone.emissionsGrams(), startX, endX );
            int itemWidth = Math.max( metrics.stringWidth(milestone.name()) + 12, 32 );
            int bestLane = -1;
            int bestIconX = markerX;
            int smallestDisplacement = Integer.MAX_VALUE;

            // Find a position that does not overlap an existing label.
            for (int lane = 0; lane < 4; lane++) {

                int iconX = Math.max( markerX, horizontalPadding + itemWidth / 2 );
                // Move right only when an occupied interval overlaps.
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

                } while (adjusted && iconX + itemWidth / 2 <= getWidth() - horizontalPadding);

                // Skip this lane if the item would extend beyond the panel.
                if (iconX + itemWidth / 2
                        > getWidth() - horizontalPadding) {
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

            occupied.get(bestLane).add(new int[]{
                    bestIconX - itemWidth / 2,
                    bestIconX + itemWidth / 2
            });

            layouts.add(new MilestoneLayout( i, markerX, bestIconX, rulerY + iconOffsets[bestLane], rulerY + labelOffsets[bestLane] ));
        }

        return layouts;
    }






    // Formats the projected emissions using readable units.
    private String formatEmissions(double grams) {

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
