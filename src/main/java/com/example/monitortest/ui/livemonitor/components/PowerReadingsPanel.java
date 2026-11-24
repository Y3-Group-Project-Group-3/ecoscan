package com.example.monitortest.ui.livemonitor.components;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.loading.components.Spinner;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.history.services.ScanPdfReport.formatPower;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Displays the CPU, GPU and combined power measurement cards.
// Owns the loading indicators and updates the displayed values.
public class PowerReadingsPanel extends JPanel {

    // Labels showing the current power measurements.
    private final JLabel cpuPowerLabel = new JLabel("Initialising...");
    private final JLabel gpuPowerLabel = new JLabel("Initialising...");
    private final JLabel totalPowerLabel = new JLabel("Initialising...");

    // Loading indicators shown until the first sensor reading.
    private final Spinner cpuSpinner = new Spinner();
    private final Spinner gpuSpinner = new Spinner();
    private final Spinner totalSpinner = new Spinner();


    // Builds the three power measurement cards.
    public PowerReadingsPanel() {
        super(new GridLayout(1, 3, 20, 0));

        // Match the original Live Monitor styling.
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        // Create the CPU, GPU and combined power measurement cards.
        add(createPowerCard("CPU Power", cpuPowerLabel, cpuSpinner));
        add(createPowerCard("GPU Power", gpuPowerLabel, gpuSpinner));
        add(createPowerCard("Combined CPU + GPU", totalPowerLabel, totalSpinner));

        // Start the loading animations until the first reading arrives.
        cpuSpinner.start();
        gpuSpinner.start();
        totalSpinner.start();
    }

    /**
     * Creates a styled card displaying a hardware power measurement.
     *
     * @param title Description of the measurement.
     * @param valueLabel Label updated with live power readings.
     * @return A rounded measurement card.
     */
    private JPanel createPowerCard(String title, JLabel valueLabel, Spinner spinner){

        // Reuse EcoScan's existing rounded panel component.
        RoundedPanel card = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16 );

        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Display the measurement name above its value.
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_MUTED);

        // Style the live measurement value.
        valueLabel.setFont(font(Font.BOLD, 22));
        valueLabel.setForeground(TEXT_MAIN);

        // Arrange the spinner immediately beside the measurement value.
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        valuePanel.setOpaque(false);

        valuePanel.add(spinner);
        valuePanel.add(valueLabel);

        // Add the measurement title and value row to the card.
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }



    // Updates all three measurement labels and stops their loading animations.
    // This method can safely be called from the background monitoring thread.
    public void updateReadings(LiveMonitorSnapshot snapshot) {

        // Ensure Swing components are only modified on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {

            // Update the CPU, GPU and combined power measurements.
            cpuPowerLabel.setText(formatPower(snapshot.cpuPower()));
            gpuPowerLabel.setText(formatPower(snapshot.gpuPower()));
            totalPowerLabel.setText(formatPower(snapshot.totalPower()));

            // Stop the initial loading animations.
            cpuSpinner.stop();
            gpuSpinner.stop();
            totalSpinner.stop();

            // Hide the spinners once measurements are available.
            cpuSpinner.setVisible(false);
            gpuSpinner.setVisible(false);
            totalSpinner.setVisible(false);
        });
    }


    // Formats a valid power measurement to one decimal place.
    // Unavailable sensor readings are displayed as "N/A".
    private String formatPower(double power) {

        // Reject NaN and infinite sensor values.
        if (!Double.isFinite(power)) {
            return "N/A";
        }

        // Display power readings in watts.
        return String.format("%.1f W", power);
    }


}
