package com.example.monitortest.ui.results.components;

import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.model.HardwareInfo;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.EmissionMilestones;
import com.example.monitortest.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// Displays projected emissions milestones using a fixed hardware scan power reading.
public class EmissionProjectionPanel extends JPanel {

    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

    private final JLabel milestoneLabel;
    private final JLabel emissionsLabel;
    private final JLabel timeLabel;

    private final JSlider milestoneSlider;

    // The power reading supplied by the latest hardware scan.
    private double powerWatts = Double.NaN;

    // Reuse the emissions calculation logic shared with the Live Monitor.
    private final CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

    // Stores the hardware information from the latest completed scan.
    private HardwareInfo hardwareInfo;

    // Determines whether RAM and storage estimates are included.
    private boolean includeEstimates = false;

    // Projection period selected in the Environmental Impact section.
    private String timescale = "Day";

    // Displays the scan-specific emissions comparison ruler.
    private final EmissionProjectionScale projectionScale = new EmissionProjectionScale();

    public EmissionProjectionPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.CARD_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Display the selected milestone and its emissions value.
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.setOpaque(false);

        milestoneLabel = new JLabel();
        milestoneLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        emissionsLabel = new JLabel();
        emissionsLabel.setForeground(AppTheme.TEXT_MUTED);

        heading.add(milestoneLabel);
        heading.add(emissionsLabel);

        add(heading, BorderLayout.NORTH);

        // Each slider position represents one milestone, not a fixed number of grams.
        milestoneSlider = new JSlider(0, milestones.size() - 1, 0);
        milestoneSlider.setOpaque(false);
        milestoneSlider.setPaintTicks(true);
        milestoneSlider.setMajorTickSpacing(1);
        milestoneSlider.setSnapToTicks(true);

        milestoneSlider.addChangeListener(e -> updateProjection());

        // Arrange the comparison ruler above the milestone selection slider.
        JPanel scaleContainer = new JPanel();
        scaleContainer.setLayout(new BoxLayout(scaleContainer, BoxLayout.Y_AXIS));
        scaleContainer.setOpaque(false);

        projectionScale.setAlignmentX(Component.LEFT_ALIGNMENT);
        milestoneSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Give the ruler sufficient space for its graphics.
        projectionScale.setPreferredSize(new Dimension(0, 360));
        projectionScale.setMaximumSize( new Dimension(Integer.MAX_VALUE, 360));

        scaleContainer.add(projectionScale);
        scaleContainer.add(Box.createVerticalStrut(12));
        scaleContainer.add(milestoneSlider);

        add(scaleContainer, BorderLayout.CENTER);

        // Display the estimated time required to reach the selected milestone.
        JPanel result = new JPanel(new GridLayout(2, 1, 0, 6));
        result.setOpaque(false);

        JLabel resultTitle = new JLabel("ESTIMATED TIME TO MILESTONE");
        resultTitle.setForeground(AppTheme.TEXT_MUTED);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));

        result.add(resultTitle);
        result.add(timeLabel);

        add(result, BorderLayout.SOUTH);

        updateProjection();
    }

    // Updates the fixed power reading used for milestone projections.
    public void setPowerWatts(double powerWatts) {
        this.powerWatts = powerWatts;
        updateProjection();
    }

    // Recalculates the projected time whenever the slider or power reading changes.
    private void updateProjection() {
        EmissionMilestone milestone = milestones.get(milestoneSlider.getValue());

        milestoneLabel.setText(milestone.name());
        emissionsLabel.setText( String.format("%.1f g CO₂e", milestone.emissionsGrams()) );

        if (!Double.isFinite(powerWatts) || powerWatts <= 0) {
            timeLabel.setText("Power reading unavailable");
            // Keep the ruler updated even when power data is unavailable.
            updateProjectionScale();
            return;
        }

        // Calculate the projected time using the shared emissions calculator.
        double seconds = calculator.calculateTimeToEmissions( powerWatts, milestone.emissionsGrams() );

        timeLabel.setText(formatDuration(seconds));

        // Refresh the ruler whenever the selected milestone or power changes.
        updateProjectionScale();
    }

    // Stores the latest hardware scan and refreshes the projection.
    public void updateHardwareInfo(HardwareInfo info) {
        this.hardwareInfo = info;
        updateSelectedPower();
    }

    // Changes the power calculation mode without requiring another scan.
    public void setIncludeEstimates(boolean includeEstimates) {
        this.includeEstimates = includeEstimates;
        updateSelectedPower();
    }

    // Selects the appropriate power subtotal for the current mode.
    private void updateSelectedPower() {

        if (hardwareInfo == null) {
            setPowerWatts(Double.NaN);
            return;
        }
        double selectedPower = includeEstimates ? hardwareInfo.getCombinedComponentPower() : hardwareInfo.getTotalMeasuredPower();
        setPowerWatts(selectedPower);
    }

    // Receives the selected projection period from Environmental Impact.
    public void setTimescale(String timescale) {
        this.timescale = timescale;
        updateProjectionScale();
    }


    // Updates the comparison ruler using the selected timescale and scan power.
    private void updateProjectionScale() {

        // Keep the highlighted milestone synchronised with the slider.
        projectionScale.setSelectedMilestoneIndex(
                milestoneSlider.getValue()
        );

        // Do not display a projected emissions marker without valid power data.
        if (!Double.isFinite(powerWatts) || powerWatts < 0) {
            projectionScale.setProjectedEmissions(Double.NaN);
            return;
        }

        // Determine the duration represented by the Environmental Impact selection.
        double durationSeconds = switch (timescale) {
            case "Second" -> 1.0;
            case "Hour" -> 3600.0;
            case "Month" -> 30 * 86400.0;
            case "Year" -> 365 * 86400.0;
            default -> 86400.0;
        };

        // Use the same calculator as the Environmental Impact section.
        double projectedGrams = calculator.calculateEmissions( powerWatts, durationSeconds );
        projectionScale.setProjectedEmissions(projectedGrams);
    }


    // Formats projected seconds into a readable duration.
    private String formatDuration(double seconds) {
        if (seconds >= 365 * 24 * 3600) {
            return String.format("%.1f years", seconds / (365 * 24 * 3600));
        }

        if (seconds >= 24 * 3600) {
            return String.format("%.1f days", seconds / (24 * 3600));
        }

        if (seconds >= 3600) {
            return String.format("%.1f hours", seconds / 3600);
        }

        if (seconds >= 60) {
            return String.format("%.1f minutes", seconds / 60);
        }

        return String.format("%.1f seconds", seconds);
    }
}
