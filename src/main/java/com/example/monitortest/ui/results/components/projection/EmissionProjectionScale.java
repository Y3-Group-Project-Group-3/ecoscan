package com.example.monitortest.ui.results.components.projection;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.EmissionMilestones;
import com.example.monitortest.ui.common.GraphicsUtils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import static com.example.monitortest.ui.theme.UIUtility.font;

// Displays a scan-specific emissions ruler without changing the Live Monitor.
public class EmissionProjectionScale extends JPanel {

    // Both monitoring pages use the same reference milestones.
    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

    // Emissions projected for the selected period.
    private double projectedEmissionsGrams = Double.NaN;

    // Milestone selected by the user.
    private int selectedMilestoneIndex = 0;

    // Horizontal inset used to position the emissions ruler.
    private static final int RULER_HORIZONTAL_PADDING = 30;

    public EmissionProjectionScale() {
        setOpaque(false);
        // Allow enough space for staggered milestone labels.
        setPreferredSize(new Dimension(0, 360));

        // Register the panel for milestone tooltips.
        setToolTipText("");

        // Update the cursor when the mouse moves over a milestone.
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

                // Determine whether the cursor is over an interactive milestone.
                boolean hoveringMilestone = findHoveredMilestone(e) != null;

                // Show a hand cursor for milestones and the default cursor elsewhere.
                setCursor(Cursor.getPredefinedCursor( hoveringMilestone ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR ));
            }
        });

        // Restore the normal cursor when leaving the ruler.
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

        // Create an independent graphics context with smooth shape rendering.
        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        int startX = RULER_HORIZONTAL_PADDING;
        int endX = getWidth() - RULER_HORIZONTAL_PADDING;
        // Position the ruler slightly higher to balance the milestone lanes.
        int rulerY = getHeight() / 2;

        // Draw the base ruler before adding milestone graphics.
        EmissionScalePainter.paintRuler(g2, startX, endX, rulerY);

        // Measure milestone labels using the same font as the painter.
        FontMetrics milestoneMetrics = g2.getFontMetrics(font(Font.PLAIN, 12));

        // Calculate the staggered positions for all visible milestones.
        List<MilestoneLayoutCalculator.MilestoneLayout> layouts = calculateMilestoneLayouts( milestoneMetrics, startX, endX, rulerY );

        // Draw milestone connectors, icons and labels at those positions.
        MilestonePainter.paint( g2, milestones, layouts, startX, endX, rulerY );

        // Draw the ruler's tick marks and emissions labels.
        EmissionScalePainter.paintTicks( g2, startX, endX, rulerY, getWidth(), getScaleMaximum() );

        // Draw the PC marker only when a valid emissions projection exists.
        if (Double.isFinite(projectedEmissionsGrams) && projectedEmissionsGrams >= 0) {

            // Calculate the PC marker's position on the emissions ruler.
            int markerX = getPosition(projectedEmissionsGrams, startX, endX);

            // Delegate the marker's appearance to the dedicated painter.
            PcEmissionPainter.paint( g2, markerX, rulerY, projectedEmissionsGrams );
        }

        g2.dispose();
    }

    // Identifies the milestone beneath the mouse using the same layout rules as the painted comparison ruler.
    private EmissionMilestone findHoveredMilestone(MouseEvent event) {

        // Ignore mouse interactions before the ruler has valid dimensions.
        if (getWidth() <= 60 || getHeight() <= 0) {
            return null;
        }

        // Match the coordinates used by paintComponent().
        int startX = RULER_HORIZONTAL_PADDING;
        int endX = getWidth() - RULER_HORIZONTAL_PADDING;
        int rulerY = getHeight() / 2;

        // Obtain font measurements without requesting a graphics context.
        FontMetrics metrics = getFontMetrics(font(Font.PLAIN, 12));

        // Calculate the same milestone positions used during painting.
        List<MilestoneLayoutCalculator.MilestoneLayout> layouts = calculateMilestoneLayouts( metrics, startX, endX, rulerY );

        // Delegate mouse hit testing to the interaction helper.
        return MilestoneInteraction.findHoveredMilestone(
                event,
                milestones,
                layouts,
                selectedMilestoneIndex,
                rulerY,
                metrics
        );
    }


    // Calculates milestone positions using the dedicated layout calculator.
    // This wrapper preserves the interface used by drawing and hover detection.
    private List<MilestoneLayoutCalculator.MilestoneLayout> calculateMilestoneLayouts( FontMetrics metrics, int startX, int endX, int rulerY) {
        return MilestoneLayoutCalculator.calculateLayouts(
                metrics,
                milestones,
                selectedMilestoneIndex,
                startX,
                endX,
                rulerY,
                getWidth(),
                getScaleMaximum()
        );
    }

}
