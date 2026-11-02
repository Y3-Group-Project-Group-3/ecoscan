package com.example.monitortest.ui.livemonitor;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.ui.livemonitor.components.PowerGraphPanel;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.loading.components.Spinner;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;

/**
 * Displays live hardware monitoring information.
 * Sensor readings will be added once the monitoring service is implemented.
 */
public class LiveMonitorPanel extends JPanel {

    // Labels displaying the latest power measurements.
    // Show the initial sensor retrieval state until the first snapshot arrives.
    private final JLabel cpuPowerLabel = new JLabel("Initialising...");
    private final JLabel gpuPowerLabel = new JLabel("Initialising...");
    private final JLabel totalPowerLabel = new JLabel("Initialising...");

    // Graphs displaying recent CPU and GPU power consumption.
    private final PowerGraphPanel cpuGraph = new PowerGraphPanel(GREEN);
    private final PowerGraphPanel gpuGraph = new PowerGraphPanel(TEXT_MUTED);

    // Spinners displayed while the initial sensor retrieval is running.
    private final Spinner cpuSpinner = new Spinner();
    private final Spinner gpuSpinner = new Spinner();
    private final Spinner totalSpinner = new Spinner();

    public LiveMonitorPanel() {
        super(new BorderLayout());

        // Match the background used by the other EcoScan pages.
        setBackground(BG_COLOR);

        // Display a temporary heading for the new page.
        JLabel heading = new JLabel("Live Monitor", SwingConstants.CENTER);
        heading.setFont(font(Font.BOLD, 26));
        heading.setForeground(TEXT_MAIN);

        // Create a horizontal container for the three power cards.
        JPanel readingsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        readingsPanel.setOpaque(false);
        readingsPanel.setBorder( BorderFactory.createEmptyBorder(30, 40, 20, 40) );

        // Create the three measurement cards with their initialisation spinners.
        readingsPanel.add(createPowerCard("CPU Power", cpuPowerLabel, cpuSpinner));
        readingsPanel.add(createPowerCard("GPU Power", gpuPowerLabel, gpuSpinner));
        readingsPanel.add(createPowerCard("Combined CPU + GPU", totalPowerLabel, totalSpinner));

        // Start the animations while waiting for the first snapshot.
        cpuSpinner.start();
        gpuSpinner.start();
        totalSpinner.start();

        /********************* GRAPHS ********************/
        // Create a container for both power graphs.
        JPanel graphsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        graphsPanel.setOpaque(false);
        graphsPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        // Create rounded containers for the CPU and GPU graphs.
        RoundedPanel cpuSection = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16 );
        RoundedPanel gpuSection = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16 );

        // Arrange each graph beneath its heading.
        cpuSection.setLayout(new BorderLayout(0, 10));
        gpuSection.setLayout(new BorderLayout(0, 10));

        // Add internal spacing so graph elements do not touch the card borders.
        cpuSection.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        gpuSection.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        // Create headings for the graph cards.
        JLabel cpuGraphTitle = new JLabel("CPU Power Consumption");
        JLabel gpuGraphTitle = new JLabel("GPU Power Consumption");

        cpuGraphTitle.setFont(font(Font.BOLD, 15));
        gpuGraphTitle.setFont(font(Font.BOLD, 15));

        cpuGraphTitle.setForeground(TEXT_MAIN);
        gpuGraphTitle.setForeground(TEXT_MAIN);

        // Add the existing graphs without recreating them.
        cpuSection.add(cpuGraphTitle, BorderLayout.NORTH);
        cpuSection.add(cpuGraph, BorderLayout.CENTER);

        gpuSection.add(gpuGraphTitle, BorderLayout.NORTH);
        gpuSection.add(gpuGraph, BorderLayout.CENTER);

        graphsPanel.add(cpuSection);
        graphsPanel.add(gpuSection);

        // Arrange the readings above the graphs.
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(readingsPanel, BorderLayout.NORTH);
        contentPanel.add(graphsPanel, BorderLayout.CENTER);

        add(heading, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

    }

    /**
     * Updates the displayed CPU and GPU power measurements.
     * Ensures Swing components are updated on the Event Dispatch Thread.
     */
    public void updatePowerReadings(LiveMonitorSnapshot snapshot) {

        // Schedule GUI updates on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            cpuPowerLabel.setText(formatPower(snapshot.cpuPower()));
            gpuPowerLabel.setText(formatPower(snapshot.gpuPower()));
            totalPowerLabel.setText(formatPower(snapshot.totalPower()));
            // Add the latest measurements to the scrolling graphs.
            cpuGraph.addReading(snapshot.cpuPower());
            gpuGraph.addReading(snapshot.gpuPower());
        });

        // The first retrieval has completed, so stop the loading animations.
        cpuSpinner.stop();
        gpuSpinner.stop();
        totalSpinner.stop();

        // Remove the spinners from view once readings become available.
        cpuSpinner.setVisible(false);
        gpuSpinner.setVisible(false);
        totalSpinner.setVisible(false);
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


    // Formats a power measurement to one decimal place.
    private String formatPower(double power) {
        return Double.isFinite(power) ? String.format("%.1f W", power) : "N/A";
    }
}
