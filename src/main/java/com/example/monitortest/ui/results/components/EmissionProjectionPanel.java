package com.example.monitortest.ui.results.components;

import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.model.HardwareInfo;

import com.example.monitortest.ui.livemonitor.components.EmissionMilestone;
import com.example.monitortest.ui.livemonitor.components.EmissionMilestones;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.common.InfoIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static com.example.monitortest.ui.theme.AppTheme.BORDER_COLOR;

// Displays projected emissions milestones using a fixed hardware scan power reading.
public class EmissionProjectionPanel extends RoundedPanel {

    private final List<EmissionMilestone> milestones = EmissionMilestones.getMilestones();

    private final JLabel milestoneLabel;
    private final JLabel emissionsLabel;

    // Explains the projected emissions and selected milestone comparison.
    private final JLabel projectionExplanation;
    private final JLabel milestoneExplanation;

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
    private String timescale = "Hour";

    // Displays the scan-specific emissions comparison ruler.
    private final EmissionProjectionScale projectionScale = new EmissionProjectionScale();

    public EmissionProjectionPanel() {
        super(Color.WHITE, BORDER_COLOR, 16);

        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.CARD_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Explain how the scanned power relates to projected emissions.
        JLabel explanationTitle = new JLabel("UNDERSTANDING YOUR PROJECTED IMPACT");
        explanationTitle.setForeground(AppTheme.TEXT_MUTED);

        projectionExplanation = new JLabel();
        projectionExplanation.setForeground(AppTheme.TEXT_MAIN);

        milestoneExplanation = new JLabel();
        milestoneExplanation.setForeground(AppTheme.TEXT_MUTED);

        // Keep the comparison explanation and CO₂e information icon together.
        JPanel milestoneExplanationRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 6, 0)
        );
        milestoneExplanationRow.setOpaque(false);
        milestoneExplanationRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        InfoIcon co2InfoIcon = new InfoIcon(
                "What is CO₂e?",
                "Carbon dioxide equivalent (CO₂e) expresses the climate impact "
                        + "of different greenhouse gases as an equivalent amount "
                        + "of carbon dioxide."
        );

        milestoneExplanationRow.add(milestoneExplanation);
        milestoneExplanationRow.add(co2InfoIcon);

        // Stack the explanation above the existing milestone information.
        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new BoxLayout(explanationPanel, BoxLayout.Y_AXIS));
        explanationPanel.setOpaque(false);

        explanationTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectionExplanation.setAlignmentX(Component.LEFT_ALIGNMENT);
        milestoneExplanation.setAlignmentX(Component.LEFT_ALIGNMENT);

        explanationPanel.add(explanationTitle);
        explanationPanel.add(Box.createVerticalStrut(8));

        explanationPanel.add(projectionExplanation);
        explanationPanel.add(Box.createVerticalStrut(6));

        // Display the CO₂e tooltip beside the comparison explanation.
        explanationPanel.add(milestoneExplanationRow);

        // Display the selected milestone and its emissions value.
        // Create the milestone information on the left side of the header.
        JPanel milestoneInfo = new JPanel();
        milestoneInfo.setLayout(new BoxLayout(milestoneInfo, BoxLayout.Y_AXIS));
        milestoneInfo.setOpaque(false);

        JLabel milestoneTitle = new JLabel("SELECTED MILESTONE");
        milestoneTitle.setForeground(AppTheme.TEXT_MUTED);

        milestoneLabel = new JLabel();
        milestoneLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        emissionsLabel = new JLabel();
        emissionsLabel.setForeground(AppTheme.TEXT_MUTED);

        // Keep the labels aligned to the left.
        milestoneTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        milestoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emissionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        milestoneInfo.add(milestoneTitle);
        milestoneInfo.add(Box.createVerticalStrut(8));
        milestoneInfo.add(milestoneLabel);
        milestoneInfo.add(Box.createVerticalStrut(6));
        milestoneInfo.add(emissionsLabel);

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

        // Display the existing comparison ruler without the manual selection slider.
        scaleContainer.add(projectionScale);

        add(scaleContainer, BorderLayout.CENTER);

        // Display the selected milestone beneath the explanation.
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        heading.add(milestoneInfo, BorderLayout.WEST);

        // Place the explanation above the milestone and time information.
        JPanel upperContent = new JPanel();
        upperContent.setLayout(new BoxLayout(upperContent, BoxLayout.Y_AXIS));
        upperContent.setOpaque(false);

        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        explanationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Display the explanation directly above the comparison ruler.
        upperContent.add(explanationPanel);

        add(upperContent, BorderLayout.NORTH);

        updateProjection();
    }

    // Updates the fixed power reading used for milestone projections.
    public void setPowerWatts(double powerWatts) {
        this.powerWatts = powerWatts;
        updateProjection();
    }

    // Updates the selected milestone, explanation and comparison ruler.
    private void updateProjection() {


        // Update the explanation using the selected milestone and scan power.
        updateExplanation();

        // Refresh the comparison ruler and highlighted milestone.
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

    // Update both the ruler and explanation when the projection period changes.
    public void setTimescale(String timescale) {
        this.timescale = timescale;
        updateProjectionScale();
        updateExplanation();
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

        // Retrieve the duration represented by the selected timescale.
        double durationSeconds = getProjectionDurationSeconds();

        // Use the same calculator as the Environmental Impact section.
        double projectedGrams = calculator.calculateEmissions( powerWatts, durationSeconds );
        projectionScale.setProjectedEmissions(projectedGrams);
    }

    // Updates the explanation using the current scan power, period and milestone.
    private void updateExplanation() {

        EmissionMilestone milestone = milestones.get(milestoneSlider.getValue());

        // Reject unavailable or invalid power readings.
        // Zero watts is valid and produces zero projected emissions.
        if (!Double.isFinite(powerWatts) || powerWatts < 0) {
            projectionExplanation.setText( "A valid scanned power reading is required." );
            milestoneExplanation.setText("");
            return;
        }

        // Use the same projection duration as the comparison ruler.
        double durationSeconds = getProjectionDurationSeconds();

        // Calculate projected emissions using the shared calculator.
        double projectedGrams = calculator.calculateEmissions(powerWatts, durationSeconds);

        String period = switch (timescale) {
            case "Second" -> "one second";
            case "Hour" -> "one hour";
            case "Month" -> "30 days";
            case "Year" -> "365 days";
            default -> "one day";
        };

        // Display projected emissions without rounding small values to zero.
        projectionExplanation.setText(String.format(
                "At a scanned component power of %.1f W, your PC is projected to generate %s CO₂e over %s, assuming constant power consumption.",
                powerWatts,
                formatEmissions(projectedGrams),
                period
        ));

        // Explain the purpose of the comparison ruler.
        milestoneExplanation.setText( "The scale below compares your PC's projected CO₂e emissions with everyday activities and products."
        );
    }


    // Converts the selected projection period into seconds.
    // Month and Year use the existing fixed-duration assumptions.
    private double getProjectionDurationSeconds() {
        return switch (timescale) {
            case "Second" -> 1.0;
            case "Hour" -> 3600.0;
            case "Month" -> 30 * 86400.0;
            case "Year" -> 365 * 86400.0;
            default -> 86400.0;
        };
    }


    // Formats projected emissions with precision appropriate to their size.
    private String formatEmissions(double grams) {

        if (!Double.isFinite(grams) || grams < 0) {
            return "N/A";
        }

        if (grams >= 1000) {
            return String.format("%,.2f kg", grams / 1000.0);
        }

        if (grams >= 1) {
            return String.format("%,.2f g", grams);
        }

        if (grams >= 0.01) {
            return String.format("%.3f g", grams);
        }

        return String.format("%.4f g", grams);
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
