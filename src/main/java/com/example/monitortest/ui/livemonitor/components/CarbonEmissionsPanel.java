package com.example.monitortest.ui.livemonitor.components;

import com.example.monitortest.hardware.demo.MacDemoConfig;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.common.InfoTooltip;
import com.example.monitortest.ui.common.InfoIcon;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Displays estimated carbon emissions associated with the monitored CPU and GPU power consumption.
public class CarbonEmissionsPanel extends RoundedPanel {

    private final JLabel totalEmissionsLabel = new JLabel("0.0000 g");
    private final JLabel averageEmissionsLabel = new JLabel("0.000000 g/s");

    // Describes the total emissions value shown in the panel.
    private JLabel totalEmissionsTitle;

    public CarbonEmissionsPanel() {
        super(Color.WHITE, new Color(226, 232, 240), 16);

        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Create the panel heading.
        JLabel heading = new JLabel("Estimated Carbon Emissions");
        heading.setFont(font(Font.BOLD, 16));
        heading.setForeground(TEXT_MAIN);

        // Arrange the two emissions measurements horizontally.
        JPanel measurementsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        measurementsPanel.setOpaque(false);

        measurementsPanel.add( createMeasurement("Total Emissions", totalEmissionsLabel) );
        measurementsPanel.add( createMeasurement("Average Emissions / Second", averageEmissionsLabel) );

        JLabel intensityLabel = new JLabel( "Based on an assumed carbon intensity of 200 g CO₂e/kWh" );

        intensityLabel.setFont(font(Font.PLAIN, 12));
        intensityLabel.setForeground(TEXT_MUTED);

        // Place an information icon beside the carbon intensity explanation.
        JPanel intensityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        intensityRow.setOpaque(false);

        intensityRow.add(intensityLabel);
        intensityRow.add(new InfoIcon(
                "Carbon Intensity",
                "Carbon intensity represents the estimated greenhouse gas " +
                        "emissions associated with generating one kilowatt-hour of electricity. " +
                        "EcoScan currently uses a fixed assumed value of 200 g CO₂e/kWh, " +
                        "rather than retrieving live electricity grid data."
        ));

        add(heading, BorderLayout.NORTH);
        add(measurementsPanel, BorderLayout.CENTER);
        add(intensityRow, BorderLayout.SOUTH);
    }


    // Creates a labelled measurement within the emissions panel.
    private JPanel createMeasurement(String title, JLabel valueLabel) {

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);

        // Retain the total emissions heading for demonstration mode.
        if ("Total Emissions".equals(title)) {
            totalEmissionsTitle = titleLabel;
        }

        titleLabel.setFont(font(Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_MUTED);

        valueLabel.setFont(font(Font.BOLD, 22));
        valueLabel.setForeground(TEXT_MAIN);

        // Arrange the live value and information icon horizontally.
        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        valueRow.setOpaque(false);

        valueRow.add(valueLabel);

        // Add a reusable information icon beside the measurement.
        valueRow.add(new InfoIcon(
                "What is CO₂e?",
                "CO₂e means carbon dioxide equivalent. It expresses the " +
                        "climate impact of different greenhouse gases using a common unit. " +
                        "EcoScan estimates emissions associated with CPU and GPU " +
                        "electricity consumption using an assumed carbon intensity. " +
                        "Actual emissions may differ."
        ));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueRow, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the displayed emissions measurements.
     * Must be called on Swing's Event Dispatch Thread.
     */
    public void updateEmissions(double totalGrams, double averageGramsPerSecond) {
        // Display the emissions using the CO₂e measurement unit.
        totalEmissionsLabel.setText( String.format("%.4f g CO₂e", totalGrams) );
        averageEmissionsLabel.setText( String.format("%.6f g CO₂e/s", averageGramsPerSecond) );
    }


    // Identifies the total emissions as a hypothetical demonstration value.
    // Does not alter the real Windows emissions calculation or display.
    public void showDemoMode() {

        // Explain that the displayed total represents a fixed example period.
        if (totalEmissionsTitle != null) {
            // Keep the displayed demonstration duration consistent with the calculation.
            totalEmissionsTitle.setText( String.format( "Illustrative Total (%.0f seconds)", MacDemoConfig.EMISSIONS_DEMO_DURATION_SECONDS )
            );
        }
    }

}
