package com.example.monitortest.ui.livemonitor.components.comparison;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.EmissionMilestones;
import com.example.monitortest.ui.livemonitor.components.comparison.ComparisonMilestoneInteraction.MilestoneHitArea;
import com.example.monitortest.ui.common.GraphicsUtils;

import com.example.monitortest.ui.loading.components.Spinner;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.List;
import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.UIUtility.font;
import static com.example.monitortest.ui.theme.AppTheme.*;

public class EmissionComparisonPanel extends JPanel {

    // Track whether the panel has received its first valid emissions reading.
    private boolean isLoading = true;

    // Reuse the application's existing animated loading spinner.
    private final Spinner loadingSpinner = new Spinner();

    // Stores the total emissions accumulated during the session.
    private double totalEmissionsGrams = 0.0;

    // Manages the upcoming milestone and the ruler's target scale.
    // The displayed scale remains animated by this panel's zoom timer.
    private final EmissionScaleController scaleController = new EmissionScaleController();

    // Coordinates the three Live Monitor animations.
    // The controller requests repaints without owning the Swing panel.
    private final EmissionAnimationController animationController = new EmissionAnimationController( scaleController, this::repaint );

    // Retrieve the shared emissions milestones.
    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

    // Stores milestone interaction regions from the latest drawing pass.
    // An empty list means no milestone is currently interactive.
    private List<MilestoneHitArea> milestoneHitAreas = List.of();

    public EmissionComparisonPanel() {

        // Allow the parent layout to control the panel's width.
        setPreferredSize(new Dimension(0, 240));

        // Match the background of EcoScan's existing panels.
        setBackground(Color.WHITE);

        // Use absolute positioning for the loading spinner.
        // The ruler itself continues to be drawn using paintComponent().
        setLayout(null);

        // Add the spinner as a child component.
        add(loadingSpinner);

        // Begin animating while waiting for the first reading.
        loadingSpinner.start();

        // Register this panel with Swing's tooltip manager.
        setToolTipText("");

        // Display the hand cursor when hovering over an interactive milestone.
        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

                // Use the same hit testing as the existing tooltip.
                boolean hoveringMilestone = findHoveredMilestone(e) != null;

                setCursor(Cursor.getPredefinedCursor( hoveringMilestone ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR ));
            }
        });

        // Restore the normal cursor whenever the mouse leaves the panel.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });



    }


    public void updateEmissions(double totalGrams, double averageGramsPerSecond) {

        // Ignore invalid emissions measurements.
        if (!Double.isFinite(totalGrams) || totalGrams < 0) {
            return;
        }
        totalEmissionsGrams = totalGrams;

        // Send the latest measured total and emissions rate to the
        // controller responsible for the animated PC marker.
        animationController.updateEmissions( totalEmissionsGrams, averageGramsPerSecond );


        // The first valid emissions reading ends the loading state.
        if (isLoading) {

            isLoading = false;

            // Stop and hide the existing loading spinner.
            loadingSpinner.stop();
            loadingSpinner.setVisible(false);

            // Start the original fade and initial zoom animations.
            animationController.startFade();
            animationController.startInitialZoom();
        }

        // Update the next milestone and target emissions range.
        scaleController.updateTarget( totalEmissionsGrams, milestones );

        // Ask the animation controller to zoom if expansion is needed.
        animationController.startZoomIfNeeded();

        // Refresh the ruler using the latest emissions and scale state.
        repaint();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create an independent graphics context with smooth shape rendering.
        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        // Display the loading state instead of the ruler until data arrives.
        if (isLoading) {

            // Use the same font and muted text colour as the rest of the panel.
            g2.setFont(font(Font.PLAIN, 13));
            g2.setColor(TEXT_MUTED);

            String message = "Waiting for hardware readings...";

            // Centre the message within the comparison panel.
            FontMetrics metrics = g2.getFontMetrics();

            milestoneHitAreas = List.of();

            // Calculate the combined width of the spinner, gap and message.
            int spinnerSize = 12;
            int gap = 10;
            int textWidth = metrics.stringWidth(message);
            int combinedWidth = spinnerSize + gap + textWidth;

            // Centre the spinner and message together.
            int groupX = (getWidth() - combinedWidth) / 2;
            int centreY = getHeight() / 2;

            // Position the spinner vertically in line with the message.
            loadingSpinner.setBounds( groupX, centreY - spinnerSize / 2, spinnerSize, spinnerSize );

            // Position the message immediately to the right of the spinner.
            int textX = groupX + spinnerSize + gap;
            int textY = centreY + (metrics.getAscent() - metrics.getDescent()) / 2;


            g2.drawString(message, textX, textY);

            // Release the graphics context before returning.
            g2.dispose();
            return;
        }

        // Apply the current fade-in opacity from the animation controller.
        g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, animationController.getRulerOpacity() ) );

        // Draw the comparison panel's title.
        g2.setFont(font(Font.BOLD, 15));
        g2.setColor(TEXT_MUTED);
        g2.drawString("Emissions Comparison", 30, 28);

        // Draw a short description beneath the title.
        g2.setFont(font(Font.PLAIN, 12));
        g2.drawString( "Your PC's carbon footprint compared with everyday activities", 30, 46 );

        // Calculate the ruler's dimensions using the panel's actual width.
        int horizontalPadding = 30;
        int startX = horizontalPadding;
        int endX = getWidth() - horizontalPadding;
        // Position the ruler slightly below centre to make room for the title.
        int rulerY = getHeight() / 2 + 25;

        // Draw the horizontal emissions ruler using the dedicated painter.
        ComparisonRulerPainter.paintRuler(g2, startX, endX, rulerY);

        // Use one consistent animated scale for this painting operation.
        double scaleMaximumGrams = animationController.getScaleMaximumGrams();

        // Draw dynamically spaced tick marks for the current animated scale.
        ComparisonRulerPainter.paintTicks( g2, startX, endX, rulerY, getWidth(), scaleMaximumGrams );


        // Draw milestones and store the hit areas for mouse interaction.
        milestoneHitAreas = ComparisonMilestonePainter.paint(
                g2, milestones, scaleMaximumGrams, scaleController.getNextMilestoneGrams(), startX, endX, rulerY, getWidth()
        );

        // Calculate the available width of the emissions ruler.
        int rulerWidth = endX - startX;

        // Position the PC using the controller's animated emissions total.
        double progress = animationController.getDisplayedEmissionsGrams() / scaleMaximumGrams;

        // Keep the PC marker within the visible ruler for now.
        // Automatic zooming will replace this temporary restriction.
        progress = Math.max(0.0, Math.min(1.0, progress));

        int pcX = startX + (int) Math.round(progress * rulerWidth);

        // Draw the PC marker using its animated position and measured emissions.
        ComparisonPcPainter.paint( g2, pcX, rulerY, getWidth(), totalEmissionsGrams );

        // Release the temporary graphics context.
        g2.dispose();
    }

    @Override
    public String getToolTipText(MouseEvent event) {

        // Identify which milestone the mouse is hovering over.
        EmissionMilestone milestone = findHoveredMilestone(event);

        if (milestone == null) { return null; }

        // Display the existing milestone information.
        return "<html><b>" + milestone.name() + "</b>"
                + "<br>Usage: " + milestone.usageDescription()
                + "<br>Estimated emissions: "
                + milestone.emissionsGrams() + " g CO₂e</html>";
    }

    // Identifies the milestone beneath the cursor using its rendered bounds.
    private EmissionMilestone findHoveredMilestone(MouseEvent event) {

        // Do not provide milestone tooltips during the loading state.
        if (isLoading) { return null; }

        // Use the interaction regions produced by the most recent paint.
        return ComparisonMilestoneInteraction.findHoveredMilestone( event, milestoneHitAreas );
    }
}