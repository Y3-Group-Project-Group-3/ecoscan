package com.example.monitortest.ui.livemonitor;

import com.example.monitortest.hardware.demo.MacDemoConfig;
import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.ui.livemonitor.components.PowerReadingsPanel;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.livemonitor.components.CarbonEmissionsPanel;
import com.example.monitortest.ui.livemonitor.components.comparison.EmissionComparisonPanel;
import com.example.monitortest.ui.livemonitor.components.LiveMonitorUnavailablePanel;
import com.example.monitortest.ui.livemonitor.components.PowerGraphsContainer;
import com.example.monitortest.hardware.monitoring.LiveMonitorAvailability;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;


/**
 * Coordinates the EcoScan Live Monitor interface.
 *
 * Delegates power measurement cards, graph presentation and
 * unavailable-state messages to dedicated UI components.
 *
 * Displays live carbon emissions and intensity information,
 * and supports static macOS demonstration mode.
 */

public class LiveMonitorPanel extends JPanel {

    // Owns both power graphs, their layout toggle and update behaviour.
    private final PowerGraphsContainer powerGraphsContainer = new PowerGraphsContainer();

    // Displays the estimated emissions accumulated during monitoring.
    private final CarbonEmissionsPanel carbonEmissionsPanel = new CarbonEmissionsPanel();
    // Displays the PC's emissions against household appliance milestones.
    private final EmissionComparisonPanel emissionComparisonPanel = new EmissionComparisonPanel();

    // Owns the CPU, GPU and combined power measurement cards.
    private final PowerReadingsPanel powerReadingsPanel = new PowerReadingsPanel();

    // Displays the page title and identifies macOS demonstration mode.
    private final JLabel heading = new JLabel("Live Monitor", SwingConstants.CENTER);

    // Displays the appropriate message when hardware monitoring is unavailable.
    // Also manages administrator restart controls.
    private final LiveMonitorUnavailablePanel unavailablePanel = new LiveMonitorUnavailablePanel();

    public LiveMonitorPanel() {
        super(new BorderLayout());

        /********************* PANEL INITIALISATION *********************/
        // Match the background used by the other EcoScan pages.
        setBackground(BG_COLOR);

        heading.setFont(font(Font.BOLD, 26));
        heading.setForeground(TEXT_MAIN);

        /********************* POWER & EMISSIONS SUMMARY *********************/
        // Stack the power cards and carbon emissions panel above the graphs.
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        // Display the extracted power measurement component.
        summaryPanel.add(powerReadingsPanel);

        // Match the horizontal spacing of the existing power cards.
        JPanel emissionsContainer = new JPanel(new BorderLayout());
        emissionsContainer.setOpaque(false);
        emissionsContainer.setBorder( BorderFactory.createEmptyBorder(0, 40, 20, 40) );
        emissionsContainer.add(carbonEmissionsPanel, BorderLayout.CENTER);

        summaryPanel.add(emissionsContainer);

        // Give the comparison bar the same horizontal margins as the other panels.
        JPanel comparisonContainer = new JPanel(new BorderLayout());
        comparisonContainer.setOpaque(false);
        comparisonContainer.setBorder( BorderFactory.createEmptyBorder(0, 40, 20, 40) );


        // Give the emissions comparison ruler the same rounded card styling.
        RoundedPanel comparisonCard = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16);
        comparisonCard.setLayout(new BorderLayout());
        comparisonCard.setBorder( BorderFactory.createEmptyBorder(15, 20, 15, 20) );

        // Keep the custom-drawn ruler inside the styled card.
        comparisonCard.add(emissionComparisonPanel, BorderLayout.CENTER);
        comparisonContainer.add(comparisonCard, BorderLayout.CENTER);

        // Place the comparison bar below the carbon emissions summary.
        summaryPanel.add(comparisonContainer);

        /********************* CONTENT LAYOUT *********************/
        // Create a vertically stacked container for the page content.
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Keep the summary and graph components aligned across the page.
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        powerGraphsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Keep the summary section at its preferred height.
        summaryPanel.setMaximumSize( new Dimension( Integer.MAX_VALUE, summaryPanel.getPreferredSize().height ) );

        // Allow the graph component to expand horizontally.
        powerGraphsContainer.setMaximumSize( new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE) );

        // Add the summary section above the graphs.
        contentPanel.add(summaryPanel);

        // Display the extracted graph component beneath the summary.
        contentPanel.add(powerGraphsContainer);

        /********************* SCROLL PANE CONFIGURATION *********************/
        // Allow vertical scrolling when the content exceeds the window height.
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Improve scrolling speed.
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Prevent horizontal scrolling.
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        /********************* FINAL PANEL ASSEMBLY *********************/
        // Keep the page heading visible while the content scrolls.
        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

    }

    /********************* LIVE MONITOR UPDATES *********************/
    // Forwards live hardware measurements to the extracted components.
    // Each component handles its own Swing thread safety.
    public void updatePowerReadings(LiveMonitorSnapshot snapshot) {

        // Update the CPU, GPU and combined power measurement cards.
        powerReadingsPanel.updateReadings(snapshot);

        // Update both power graphs using the same snapshot.
        powerGraphsContainer.updateGraphs(snapshot);
    }


    public void updateCarbonEmissions(double totalGrams, double averageGramsPerSecond) {

        // Update both emissions components on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            carbonEmissionsPanel.updateEmissions( totalGrams, averageGramsPerSecond );
            // Pass both the cumulative emissions and the emissions rate.
            emissionComparisonPanel.updateEmissions( totalGrams, averageGramsPerSecond );
        });
    }

    // Updates the displayed carbon intensity and its source.
    // Ensures updates from background threads are handled on Swing's EDT.
    public void updateCarbonIntensity(double intensity, String source) {

        // Schedule the label update on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            // Forward the new intensity and source to the emissions panel.
            carbonEmissionsPanel.updateCarbonIntensity( intensity, source );
        });
    }


    /********************* ADMINISTRATOR & AVAILABILITY *********************/
    // Passes the administrator restart action to the unavailable-state panel.
    // Preserves the existing public interface used by CreateWindow.
    public void setAdministratorRestartAction(Runnable action) {
        // Let the dedicated component manage the restart callback.
        unavailablePanel.setAdministratorRestartAction(action);
    }

    // Replaces the Live Monitor content with an explanation when hardware monitoring is unavailable in the current environment.
    public void showUnavailableState(LiveMonitorAvailability.Status status) {

        // Keep the existing monitoring UI when the environment is supported.
        if (status == LiveMonitorAvailability.Status.AVAILABLE) {
            return;
        }

        // Ignore statuses that do not have an unavailable-state message.
        if (status != LiveMonitorAvailability.Status.WINDOWS_ONLY
                && status != LiveMonitorAvailability.Status.ADMIN_REQUIRED) {
            return;
        }

        // Ask the dedicated component to build the appropriate message.
        unavailablePanel.showStatus(status);

        // Replace the existing Live Monitor content.
        removeAll();
        add(unavailablePanel, BorderLayout.CENTER);

        // Recalculate and redraw the updated interface.
        revalidate();
        repaint();
    }


    // Identifies that the Live Monitor is showing simulated macOS data.
    // The existing measurement cards and graphs remain visible.
    public void showDemoMode() {

        // Use the shared macOS demonstration heading.
        heading.setText(MacDemoConfig.LIVE_MONITOR_HEADING);

        // Refresh the heading appearance.
        heading.revalidate();
        heading.repaint();
    }

    /************************************* MAC ********************************/
    // Displays static CPU and GPU graphs for macOS demonstration mode.
    public void showDemoGraphs(LiveMonitorSnapshot snapshot) {
        // Delegate the demonstration readings to the graph component.
        powerGraphsContainer.showDemoGraphs(snapshot);
    }

    // Enables the static emissions comparison ruler for macOS.
    // Reuses the existing comparison panel and its painters.
    public void showDemoComparison() {
        // Display the comparison without continuous emissions updates.
        emissionComparisonPanel.showDemoMode();
    }

    // Displays static, hypothetical carbon emissions for macOS Demo Mode.
    // Uses simulated power values without accumulating emissions over time.
    public void showDemoEmissions(LiveMonitorSnapshot snapshot) {

        // Clearly label the hypothetical emissions total.
        carbonEmissionsPanel.showDemoMode();

        // Reuse EcoScan's existing emissions calculation logic.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Retrieve the combined simulated CPU and GPU power.
        double totalPower = snapshot.totalPower();

        // Calculate the hypothetical emissions produced in one second.
        double emissionsPerSecond = calculator.calculateEmissions(totalPower, 1.0);

        // Calculate hypothetical emissions over the configured demo period.
        double totalEmissions = calculator.calculateEmissions( totalPower, MacDemoConfig.EMISSIONS_DEMO_DURATION_SECONDS );

        // Display the static values using the existing emissions component.
        carbonEmissionsPanel.updateEmissions( totalEmissions, emissionsPerSecond );
    }


}