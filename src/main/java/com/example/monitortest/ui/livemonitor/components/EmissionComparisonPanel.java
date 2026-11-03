package com.example.monitortest.ui.livemonitor.components;

import com.example.monitortest.ui.livemonitor.components.icons.EmissionIconRenderer;
import com.example.monitortest.ui.loading.components.Spinner;



import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;

import static com.example.monitortest.ui.theme.UIUtility.font;
import static com.example.monitortest.ui.theme.AppTheme.*;

public class EmissionComparisonPanel extends JPanel {

    // Track whether the panel has received its first valid emissions reading.
    private boolean isLoading = true;
    // Controls the ruler's opacity during the fade-in animation.
    private float rulerOpacity = 0.0f;

    // Reuse the application's existing animated loading spinner.
    private final Spinner loadingSpinner = new Spinner();



    // Stores the total emissions accumulated during the session.
    private double totalEmissionsGrams = 0.0;

    // The emissions value currently represented by the animated PC marker.
    private double displayedEmissionsGrams = 0.0;

    // Start with a smaller scale so the first milestone begins off-screen.
    private double scaleMaximumGrams = 0.02;

    // Keep the first milestone's target at 90% of the ruler.
    private double targetScaleMaximumGrams = 0.2 / NEXT_MILESTONE_POSITION;

    // Animates the ruler's opacity from transparent to fully visible.
    private final Timer fadeTimer;

    // Controls the animation of the emissions scale.
    private final Timer zoomTimer;

    // Animates the PC marker towards the latest emissions reading.
    private final Timer pcAnimationTimer;

    // Most recently measured emissions rate, expressed in grams per second.
    private double emissionsRateGramsPerSecond = 0.0;

    // The next emissions milestone that the PC is approaching.
    private double nextMilestoneGrams = 0.2;
    private static final double NEXT_MILESTONE_POSITION = 0.90;

    // Records when the previous animation frame was processed.
    private long lastAnimationTime = System.nanoTime();
    private double emissionsCorrectionGrams = 0.0;

    // Retrieve the shared emissions milestones.
    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

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

        // Update the scale animation approximately 60 times per second.
        zoomTimer = new Timer(16, e -> {

            // Gradually move the displayed scale towards its target.
            scaleMaximumGrams += (targetScaleMaximumGrams - scaleMaximumGrams) * 0.08;

            // Stop animating once the displayed scale is sufficiently close.
            if (Math.abs(targetScaleMaximumGrams - scaleMaximumGrams) < targetScaleMaximumGrams * 0.001) {

                scaleMaximumGrams = targetScaleMaximumGrams;
                // Stop the timer that triggered this animation event.
                ((Timer) e.getSource()).stop();
            }

            // Redraw the ruler and PC marker at their updated positions.
            repaint();
        });


        // Gradually increase the ruler's opacity after loading completes.
        fadeTimer = new Timer(16, e -> {

            // Increase opacity over approximately 500 milliseconds.
            rulerOpacity = Math.min(1.0f, rulerOpacity + 0.032f);

            // Stop the animation once the ruler is fully visible.
            if (rulerOpacity >= 1.0f) {
                rulerOpacity = 1.0f;
                ((Timer) e.getSource()).stop();
            }

            repaint();
        });




        // Continuously update the PC marker approximately 60 times per second.
        pcAnimationTimer = new Timer(16, e -> {

            long now = System.nanoTime();

            // Calculate how much time has passed since the previous frame.
            double elapsedSeconds = (now - lastAnimationTime) / 1_000_000_000.0;

            lastAnimationTime = now;

            // Limit large time gaps, such as when the application is suspended.
            elapsedSeconds = Math.min(elapsedSeconds, 0.1);

            // Estimate the emissions accumulated since the previous frame.
            displayedEmissionsGrams += emissionsRateGramsPerSecond * elapsedSeconds;

            // Redraw the PC marker at its updated position.
            repaint();
        });



        // Change the cursor when hovering over a milestone marker.
        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // Use the same hover detection as the tooltip.
                boolean hoveringMilestone = findHoveredMilestone(e) != null;
                setCursor(Cursor.getPredefinedCursor( hoveringMilestone ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Restore the normal cursor when leaving the panel.
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
        // Store the latest emissions rate for continuous PC animation.
        emissionsRateGramsPerSecond = averageGramsPerSecond;

        // Calculate the difference between the latest measurement and the PC marker's estimated cumulative emissions.
        emissionsCorrectionGrams = totalEmissionsGrams - displayedEmissionsGrams;


        // Start continuous animation when the first reading arrives.
        if (!pcAnimationTimer.isRunning()) {

            // Initialise the marker at the latest measured emissions.
            displayedEmissionsGrams = totalEmissionsGrams;


            // Gradually reconcile the estimated emissions with the latest reading.
            // Avoid snapping the marker directly to the measured total.
            double correction = emissionsCorrectionGrams * 0.08;

            displayedEmissionsGrams += correction;
            emissionsCorrectionGrams -= correction;


            // Initialise the timestamp to avoid a large first-frame jump.
            lastAnimationTime = System.nanoTime();

            pcAnimationTimer.start();
        }




        // The first valid reading marks the end of the loading state.
        // A reading of 0.0 is valid and should also dismiss loading.
        if (isLoading) {

            // The first valid reading ends the loading state.
            isLoading = false;

            // Stop the spinner and remove it from view.
            loadingSpinner.stop();
            loadingSpinner.setVisible(false);

            // Begin fading in the emissions ruler.
            rulerOpacity = 0.0f;
            fadeTimer.start();

            // Start the first milestone animation.
            if (!zoomTimer.isRunning()) {
                zoomTimer.start();
            }
        }



        // Find the next milestone that the PC has not yet reached.
        double nextMilestone = Double.NaN;


        // Find the first milestone that the PC has not yet nearly reached.
        for (EmissionMilestone milestone : milestones) {

            // Once the PC reaches 95% of a milestone, select the following one.
            if (totalEmissionsGrams < milestone.emissionsGrams() * 0.95) {
                nextMilestone = milestone.emissionsGrams();
                break;
            }
        }


        if (!Double.isNaN(nextMilestone)) {
            // Track the milestone that should remain visible.
            nextMilestoneGrams = nextMilestone;

            // Position the next milestone at 90% of the ruler's width.
            targetScaleMaximumGrams = nextMilestone / NEXT_MILESTONE_POSITION;

        } else {
            // Beyond the final milestone, continue expanding the range.
            while (totalEmissionsGrams >= targetScaleMaximumGrams) {
                targetScaleMaximumGrams *= 2.0;
            }
        }


        // Begin animating if the target scale has changed.
        if (scaleMaximumGrams < targetScaleMaximumGrams && !zoomTimer.isRunning()) {
            zoomTimer.start();
        }

        // Redraw the ruler and PC marker using the updated scale.
        repaint();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create a separate graphics context for custom drawing.
        Graphics2D g2 = (Graphics2D) g.create();


        // Display the loading state instead of the ruler until data arrives.
        if (isLoading) {

            // Use the same font and muted text colour as the rest of the panel.
            g2.setFont(font(Font.PLAIN, 13));
            g2.setColor(TEXT_MUTED);

            String message = "Waiting for hardware readings...";

            // Centre the message within the comparison panel.
            FontMetrics metrics = g2.getFontMetrics();


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

        // Apply the fade-in opacity to everything drawn on the ruler.
        g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, rulerOpacity ) );


        // Draw the comparison panel's title.
        g2.setFont(font(Font.BOLD, 15));
        g2.setColor(TEXT_MUTED);
        g2.drawString("Emissions Comparison", 30, 28);

        // Draw a short description beneath the title.
        g2.setFont(font(Font.PLAIN, 12));
        g2.drawString( "Your PC's carbon footprint compared with everyday activities", 30, 46 );



        // Enable anti-aliasing for smoother lines and shapes.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        // Calculate the ruler's dimensions using the panel's actual width.
        int horizontalPadding = 30;
        int startX = horizontalPadding;
        int endX = getWidth() - horizontalPadding;
        // Position the ruler slightly below centre to make room for the title.
        int rulerY = getHeight() / 2 + 25;

        // Draw the horizontal emissions ruler.
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.drawLine(startX, rulerY, endX, rulerY);




// Calculate an appropriate rounded interval for the animated scale.
        double tickInterval = calculateTickInterval(scaleMaximumGrams);

        g2.setStroke(new BasicStroke(1.5f));

        // Draw ticks at actual emissions values, starting from zero.
        for (int i = 0; i * tickInterval <= scaleMaximumGrams + tickInterval * 1e-9; i++) {

            double tickValue = i * tickInterval;

            // Convert the emissions value into its proportional ruler position.
            double fraction = tickValue / scaleMaximumGrams;

            int tickX = startX + (int) Math.round(fraction * (endX - startX));

            // Draw the tick mark.
            g2.setColor(new Color(148, 163, 184));
            g2.drawLine( tickX, rulerY - 7, tickX, rulerY + 7 );

            // Display the rounded emissions value above the tick.
            String tickLabel = formatScaleValue(tickValue);

            g2.setFont(font(Font.PLAIN, 11));
            g2.setColor(TEXT_MUTED);

            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(tickLabel);

            // Keep labels within the panel's boundaries.
            int labelX = Math.max( 5, Math.min(tickX - labelWidth / 2, getWidth() - labelWidth - 5) );
            g2.drawString(tickLabel, labelX, rulerY - 16);
        }





        // Track the end of the last label to prevent overlapping text.
        int lastLabelEndX = Integer.MIN_VALUE;

        // Use the same animated scale for milestones and the PC.
        // This keeps all positions mathematically proportional.
        double milestoneDisplayScale = scaleMaximumGrams;


        // Draw milestones that are visible or are the upcoming comparison.
        for (EmissionMilestone milestone : milestones) {

            boolean isUpcoming = milestone.emissionsGrams() == nextMilestoneGrams;
            boolean isOnScale = milestone.emissionsGrams() <= scaleMaximumGrams;

            // Skip milestones that are neither visible nor upcoming.
            if (!isOnScale && !isUpcoming) {
                continue;
            }

            // Calculate the milestone's true proportional position.
            double position = milestone.emissionsGrams() / scaleMaximumGrams;


            // Position every milestone using the animated ruler scale. Upcoming milestones will enter from the right as the scale expands.
            double displayPosition = position;
            int milestoneX = startX + (int) Math.round(displayPosition * (endX - startX));


            // Only draw milestone dots once they enter the visible ruler.
            if (isOnScale) {
                int dotDiameter = 10;
                g2.setColor(new Color(100, 116, 139));
                // Use the same position as the milestone icon and label.
                g2.fillOval( milestoneX - dotDiameter / 2, rulerY - dotDiameter / 2, dotDiameter, dotDiameter );
            }


            // Draw the library icon beneath its milestone dot.
            EmissionIconRenderer.drawIcon( g2, milestone.name(), milestoneX, rulerY + 25 );

            // Set the appearance of the milestone labels.
            g2.setFont(font(Font.PLAIN, 12));
            g2.setColor(TEXT_MUTED);

            // Measure the label so we can centre it beneath its milestone.
            FontMetrics metrics = g2.getFontMetrics();
            String label = milestone.name();

            int labelWidth = metrics.stringWidth(label);
            int labelX = milestoneX - labelWidth / 2;
            int labelY = rulerY + 55;

            // Only display labels that fit inside the panel and don't overlap.
            if (labelX >= 0 && labelX + labelWidth <= getWidth() && labelX >= lastLabelEndX + 12) {
                g2.drawString(label, labelX, labelY);
                // Remember where this label ends.
                lastLabelEndX = labelX + labelWidth;
            }

        }


        // Calculate the available width of the emissions ruler.
        int rulerWidth = endX - startX;

        // Position the PC using its smoothly animated emissions value.
        double progress = displayedEmissionsGrams / scaleMaximumGrams;

        // Keep the PC marker within the visible ruler for now.
        // Automatic zooming will replace this temporary restriction.
        progress = Math.max(0.0, Math.min(1.0, progress));

        int pcX = startX + (int) Math.round(progress * rulerWidth);


        // Format the PC's cumulative emissions to match the summary panel.
        String pcEmissionsLabel = String.format( Locale.UK, "%.4f g CO₂e", totalEmissionsGrams );

        // Set the label's font and colour.
        g2.setFont(font(Font.PLAIN, 12));
        g2.setColor(GREEN);

        // Measure the text so it can be centred above the PC icon.
        FontMetrics pcMetrics = g2.getFontMetrics();

        int pcLabelWidth = pcMetrics.stringWidth(pcEmissionsLabel);

        // Centre the label above the PC marker.
        int pcLabelX = pcX - pcLabelWidth / 2;

        // Keep the label within the panel's horizontal boundaries.
        pcLabelX = Math.max( 5, Math.min(pcLabelX, getWidth() - pcLabelWidth - 5) );

        // Position the label above the monitor icon.
        int pcLabelY = rulerY - 78;

        // Draw the emissions label.
        g2.drawString(pcEmissionsLabel, pcLabelX, pcLabelY);


        // Draw a vertical marker connecting the PC to the ruler.
        g2.setColor(GREEN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(pcX, rulerY - 35, pcX, rulerY);

        // Draw a temporary PC icon using a monitor-shaped rectangle.
        int iconWidth = 30;
        int iconHeight = 22;

        g2.setColor(GREEN);
        g2.fillRoundRect( pcX - iconWidth / 2, rulerY - 65, iconWidth, iconHeight, 5, 5 );

        // Draw the screen inside the monitor.
        g2.setColor(Color.WHITE);
        g2.fillRoundRect( pcX - iconWidth / 2 + 4, rulerY - 61, iconWidth - 8, iconHeight - 8, 2, 2);

        // Draw the monitor stand.
        g2.setColor(GREEN);
        g2.fillRect(pcX - 2, rulerY - 43, 4, 7);
        g2.fillRect(pcX - 9, rulerY - 36, 18, 3);


        // Release the temporary graphics context.
        g2.dispose();
    }


    // Draws a temporary preview of the next milestone while it is outside the currently displayed emissions range.
    private void drawNextMilestonePreview( Graphics2D g2, int endX, int rulerY ) {
        // Never preview a milestone that can already be drawn normally.
        if (nextMilestoneGrams <= scaleMaximumGrams + 0.000001) {
            return;
        }

        // Find the milestone associated with the current target.
        EmissionMilestone upcomingMilestone = null;

        for (EmissionMilestone milestone : milestones) {
            if (milestone.emissionsGrams() == nextMilestoneGrams) {
                upcomingMilestone = milestone;
                break;
            }
        }

        if (upcomingMilestone == null) {
            return;
        }

        // Reserve space near the right end of the ruler.
        int previewX = endX - 45;

        // Draw the icon below the ruler, matching the normal milestones.
        EmissionIconRenderer.drawIcon( g2, upcomingMilestone.name(), previewX, rulerY + 25 );

        // Draw the milestone name below its icon.
        g2.setFont(font(Font.PLAIN, 12));
        g2.setColor(TEXT_MUTED);

        FontMetrics metrics = g2.getFontMetrics();
        String label = upcomingMilestone.name();

        // Keep the preview label inside the panel.
        int labelX = Math.max( 0, Math.min( getWidth() - metrics.stringWidth(label), previewX - metrics.stringWidth(label) / 2 ) );

        g2.drawString(label, labelX, rulerY + 55);
    }


    // Format emissions values without unnecessary trailing zeros.
    private String formatScaleValue(double grams) {

        // Convert the value to an appropriate measurement unit.
        double value = grams;
        String unit = "g";

        if (grams >= 1_000_000) {
            value = grams / 1_000_000;
            unit = "t";
        } else if (grams >= 1_000) {
            value = grams / 1_000;
            unit = "kg";
        }

        // Retain up to four decimal places, removing trailing zeros.
        String formatted = BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();

        return formatted + " " + unit;
    }






    @Override
    public String getToolTipText(MouseEvent event) {

        // Identify which milestone the mouse is hovering over.
        EmissionMilestone milestone = findHoveredMilestone(event);

        if (milestone == null) {
            return null;
        }

        // Display the existing milestone information.
        return "<html><b>" + milestone.name() + "</b>"
                + "<br>Usage: " + milestone.usageDescription()
                + "<br>Estimated emissions: "
                + milestone.emissionsGrams() + " g CO₂e</html>";
    }




    // Calculate a readable tick interval for the current ruler scale.
    private double calculateTickInterval(double scaleMaximum) {

        // Aim for approximately five intervals across the ruler.
        double roughInterval = scaleMaximum / 5.0;

        // Find the power of ten closest to the interval's magnitude.
        double magnitude = Math.pow( 10, Math.floor(Math.log10(roughInterval)) );

        // Normalise the interval to a value between 1 and 10.
        double normalised = roughInterval / magnitude;

        // Round to a conventional interval: 1, 2, 2.5, 5 or 10.
        double rounded;

        if (normalised <= 1.0) {
            rounded = 1.0;
        } else if (normalised <= 2.0) {
            rounded = 2.0;
        } else if (normalised <= 2.5) {
            rounded = 2.5;
        } else if (normalised <= 5.0) {
            rounded = 5.0;
        } else {
            rounded = 10.0;
        }

        return rounded * magnitude;
    }


    // Find the milestone being hovered over, including its dot, icon and label.
    private EmissionMilestone findHoveredMilestone(MouseEvent event) {

        int startX = 30;
        int endX = getWidth() - 30;
        int rulerY = getHeight() / 2 + 25;

        for (EmissionMilestone milestone : milestones) {

            // Ignore milestones that are not yet visible on the ruler.
            if (milestone.emissionsGrams() > scaleMaximumGrams) {
                continue;
            }

            // Calculate the milestone's proportional position.
            double position = milestone.emissionsGrams() / scaleMaximumGrams;

            int milestoneX = startX + (int) Math.round(position * (endX - startX));

            // Match the font used when drawing milestone labels.
            FontMetrics metrics = getFontMetrics(font(Font.PLAIN, 12));

            int labelWidth = metrics.stringWidth(milestone.name());

            // Define the horizontal bounds of the milestone label.
            int labelLeft = milestoneX - labelWidth / 2;
            int labelRight = milestoneX + labelWidth / 2;

            int mouseX = event.getX();
            int mouseY = event.getY();

            // Detect hovering around the milestone dot.
            boolean overDot = Math.abs(mouseX - milestoneX) <= 12 && Math.abs(mouseY - rulerY) <= 12;

            // Detect hovering over the milestone icon.
            boolean overIcon = Math.abs(mouseX - milestoneX) <= 14 && mouseY >= rulerY + 10 && mouseY <= rulerY + 40;

            // Detect hovering over the milestone label.
            boolean overLabel = mouseX >= labelLeft && mouseX <= labelRight && mouseY >= rulerY + 42 && mouseY <= rulerY + 60;
            if (overDot || overIcon || overLabel) {
                return milestone;
            }
        }

        return null;
    }



}