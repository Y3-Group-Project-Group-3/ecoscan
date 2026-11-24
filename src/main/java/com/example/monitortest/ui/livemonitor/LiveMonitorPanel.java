package com.example.monitortest.ui.livemonitor;

import com.example.monitortest.hardware.demo.MacDemoConfig;
import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.ui.livemonitor.components.PowerGraphPanel;
import com.example.monitortest.ui.livemonitor.components.PowerReadingsPanel;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.loading.components.Spinner;
import com.example.monitortest.ui.livemonitor.components.CarbonEmissionsPanel;
import com.example.monitortest.ui.livemonitor.components.comparison.EmissionComparisonPanel;
import com.example.monitortest.ui.common.OutlinedActionButton;
import com.example.monitortest.hardware.monitoring.LiveMonitorAvailability;
import com.example.monitortest.services.startup.ApplicationProcessManager;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;

/**
 * Displays live hardware monitoring information.
 * Sensor readings will be added once the monitoring service is implemented.
 */
public class LiveMonitorPanel extends JPanel {

    // Graphs displaying recent CPU and GPU power consumption.
    private final PowerGraphPanel cpuGraph = new PowerGraphPanel(GREEN);
    private final PowerGraphPanel gpuGraph = new PowerGraphPanel(TEXT_MUTED);

    // Displays the estimated emissions accumulated during monitoring.
    private final CarbonEmissionsPanel carbonEmissionsPanel = new CarbonEmissionsPanel();
    // Displays the PC's emissions against household appliance milestones.
    private final EmissionComparisonPanel emissionComparisonPanel = new EmissionComparisonPanel();

    // Container holding both CPU and GPU graphs.
    private JPanel graphsPanel;
    // Tracks whether the graphs are currently displayed side by side.
    private boolean sideBySideView = true;

    // Owns the CPU, GPU and combined power measurement cards.
    private final PowerReadingsPanel powerReadingsPanel = new PowerReadingsPanel();

    // Displays the page title and identifies macOS demonstration mode.
    private final JLabel heading = new JLabel("Live Monitor", SwingConstants.CENTER);

    // check if app requires restart
    private Runnable administratorRestartAction;

    public LiveMonitorPanel() {
        super(new BorderLayout());

        // Match the background used by the other EcoScan pages.
        setBackground(BG_COLOR);

        heading.setFont(font(Font.BOLD, 26));
        heading.setForeground(TEXT_MAIN);

        /********************* GRAPHS ********************/
        // Create a container for both power graphs.
//        JPanel graphsPanel = new JPanel(new GridLayout(2, 1, 0, 20));

        // Display CPU and GPU graphs side by side.
        // One row, two columns, with a 20-pixel horizontal gap.
        // Initialise the shared graph container in side-by-side mode.
        graphsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        graphsPanel.setOpaque(false);

//        // Reserve enough vertical space for both graphs.
//        graphsPanel.setPreferredSize(new Dimension(0, 520));
//        graphsPanel.setMinimumSize(new Dimension(0, 520));

        // Reserve enough height for the side-by-side graphs.
        graphsPanel.setPreferredSize(new Dimension(0, 300));
        graphsPanel.setMinimumSize(new Dimension(0, 300));

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

        /********************* Emissions Panel *****************************/
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
        RoundedPanel comparisonCard = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16
        );

        comparisonCard.setLayout(new BorderLayout());
        comparisonCard.setBorder( BorderFactory.createEmptyBorder(15, 20, 15, 20) );

        // Keep the custom-drawn ruler inside the styled card.
        comparisonCard.add(emissionComparisonPanel, BorderLayout.CENTER);

        comparisonContainer.add(comparisonCard, BorderLayout.CENTER);


        // Place the comparison bar below the carbon emissions summary.
        summaryPanel.add(comparisonContainer);

        // Create a vertically stacked container for the page content.
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Ensure the summary and graphs fill the available width.
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        graphsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Keep the graphs at their preferred height.
        summaryPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, summaryPanel.getPreferredSize().height) );

        // Allow the graph container to fill the available width.
        graphsPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, 300) );

        // Add the summary section above the graphs.
        contentPanel.add(summaryPanel);


        // Use EcoScan's existing secondary button styling.
        OutlinedActionButton graphLayoutButton = new OutlinedActionButton("");

        // Display the icon for stacking the graphs vertically.
        graphLayoutButton.setIcon(createGraphLayoutIcon(false));

        // Explain the action when the user hovers over the button.
        graphLayoutButton.setToolTipText("Stack Graphs");

        // Provide an accessible description for the icon-only button.
        graphLayoutButton.getAccessibleContext().setAccessibleName("Stack Graphs");

        // Give the compact toggle a consistent size.
        graphLayoutButton.setPreferredSize(new Dimension(46, 38));

        // Keep the icon centred inside the button.
        graphLayoutButton.setHorizontalAlignment(SwingConstants.CENTER);


        // Explain the button's action on hover.
        graphLayoutButton.setToolTipText("Stack Graphs");

        // Provide an accessible name for the icon-only control.
        graphLayoutButton.getAccessibleContext().setAccessibleName("Stack Graphs");

        // Give the button consistent dimensions.
        graphLayoutButton.setPreferredSize(new Dimension(42, 36));

        // Keep the visual appearance compact.
        graphLayoutButton.setFocusPainted(false);

        // Display a hand cursor when hovering.
        graphLayoutButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );


        // Switch layouts whenever the button is clicked.
        graphLayoutButton.addActionListener( event -> toggleGraphLayout(graphLayoutButton) );

        // Position the toggle above the graphs, aligned to the right.
        JPanel graphToolbar = new JPanel( new FlowLayout(FlowLayout.RIGHT, 40, 0) );

        // Match the background of the Live Monitor page.
        graphToolbar.setOpaque(false);

        // Allow the toolbar to fill the content width without excessive height.
        graphToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        graphToolbar.setMaximumSize( new Dimension(Integer.MAX_VALUE, 45) );

        // Add the toggle button to the toolbar.
        graphToolbar.add(graphLayoutButton);

        // Place the graph layout toggle immediately above the graphs.
        contentPanel.add(graphToolbar);
        contentPanel.add(graphsPanel);

        // Allow vertical scrolling when the content exceeds the window height.
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Improve scrolling speed.
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Prevent horizontal scrolling.
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        // Keep the page heading visible while the content scrolls.
        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

    }

    // Updates the measurement cards and power graphs.
    // The child component handles its own Swing thread safety.
    public void updatePowerReadings(LiveMonitorSnapshot snapshot) {

        // Update the extracted CPU, GPU and combined power cards.
        powerReadingsPanel.updateReadings(snapshot);

        // Keep graph updates on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            // Add the latest readings to the existing graphs.
            cpuGraph.addReading(snapshot.cpuPower());
            gpuGraph.addReading(snapshot.gpuPower());
        });
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


    /**
     * Configures the action used to restart EcoScan with administrator privileges.
     * @param action the administrator restart operation
     */
    public void setAdministratorRestartAction(Runnable action) {
        this.administratorRestartAction = action;
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



    // Replaces the live monitoring interface with an explanatory message when the current environment is unsupported.
    public void showUnavailableState(LiveMonitorAvailability.Status status) {

        // The existing monitoring interface should remain unchanged
        // when the environment meets the requirements.
        if (status == LiveMonitorAvailability.Status.AVAILABLE) {
            return;
        }

        String title;
        String description;

        // Select the appropriate message for the current environment.
        switch (status) {

            case WINDOWS_ONLY -> {
                title = "Windows Only";
                description = "Live hardware monitoring is currently supported on Windows only.";
            }

            case ADMIN_REQUIRED -> {
                title = "Administrator Privileges Required";
                description = "Live hardware monitoring requires administrator privileges to access hardware sensors.";
            }

            default -> {
                return;
            }
        }

        // Create a centred message panel.
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(BG_COLOR);

        JPanel messageContent = new JPanel();
        messageContent.setLayout( new BoxLayout(messageContent, BoxLayout.Y_AXIS) );
        messageContent.setOpaque(false);

        // Display the availability message heading.
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 22));
        titleLabel.setForeground(TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Display the explanation beneath the heading.
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(font(Font.PLAIN, 14));
        descriptionLabel.setForeground(TEXT_MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        messageContent.add(titleLabel);
        messageContent.add(Box.createVerticalStrut(12));
        messageContent.add(descriptionLabel);

        // Offer administrator elevation only when it is required.
        if (status == LiveMonitorAvailability.Status.ADMIN_REQUIRED) {

            ApplicationProcessManager processManager = new ApplicationProcessManager();

            messageContent.add(Box.createVerticalStrut(20));

            if (processManager.isPackagedApplication()) {

                // The packaged EcoScan executable supports Windows UAC elevation.
                JButton restartButton = new JButton("Restart as Administrator");

                restartButton.setFont(font(Font.BOLD, 13));
                restartButton.setBackground(new Color(22, 137, 76));
                restartButton.setForeground(Color.WHITE);
                restartButton.setFocusPainted(false);
                restartButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
                restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Delegate the restart operation to CreateWindow.
                restartButton.addActionListener(e -> {
                    if (administratorRestartAction != null) {
                        administratorRestartAction.run();
                    }
                });

                messageContent.add(restartButton);

            } else {

                // IntelliJ launches cannot safely restart using java.exe alone.
                JLabel instructionLabel = new JLabel( "Restart IDE as administrator to enable live monitoring." );

                instructionLabel.setFont(font(Font.PLAIN, 13));
                instructionLabel.setForeground(TEXT_MUTED);
                instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                messageContent.add(instructionLabel);
            }
        }

        messagePanel.add(messageContent);

        // Replace the page contents with the unavailable message.
        removeAll();
        add(messagePanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }


    // Switches between side-by-side and vertically stacked graph layouts.
    // Keeps the existing graph components and their recorded measurements.
    private void toggleGraphLayout(JButton toggleButton) {

        // Switch to the opposite layout.
        sideBySideView = !sideBySideView;

        if (sideBySideView) {

            // Display both graphs horizontally.
            graphsPanel.setLayout(new GridLayout(1, 2, 20, 0));

            // Use a shorter container for the horizontal layout.
            updateGraphHeight(300);

            // Display the option to stack the graphs vertically.
            toggleButton.setIcon(createGraphLayoutIcon(false));
            toggleButton.setToolTipText("Stack Graphs");
            toggleButton.getAccessibleContext().setAccessibleName("Stack Graphs");

        } else {

            // Display the graphs vertically.
            graphsPanel.setLayout(new GridLayout(2, 1, 0, 20));

            // Restore the original height for the stacked layout.
            updateGraphHeight(520);

            // Display the option to arrange graphs horizontally.
            toggleButton.setIcon(createGraphLayoutIcon(true));
            toggleButton.setToolTipText("Side-by-Side Graphs");
            toggleButton.getAccessibleContext().setAccessibleName("Side-by-Side Graphs");
        }

        // Refresh the container without recreating its child components.
        graphsPanel.revalidate();
        graphsPanel.repaint();
    }


    // Creates an icon representing one of the two graph layouts.
    // true = side-by-side layout; false = stacked layout.
    private Icon createGraphLayoutIcon(boolean sideBySide) {

        // Create a transparent 24x24 image.
        BufferedImage image = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_ARGB );

        Graphics2D graphics = image.createGraphics();

        try {
            // Smooth the icon's edges.
            graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

            // Match EcoScan's existing text colour.
            graphics.setColor(TEXT_MAIN);

            if (sideBySide) {
                // Draw two vertical rectangles.
                graphics.fillRoundRect(2, 4, 9, 16, 3, 3);
                graphics.fillRoundRect(13, 4, 9, 16, 3, 3);

            } else {
                // Draw two horizontal rectangles.
                graphics.fillRoundRect(3, 3, 18, 8, 3, 3);
                graphics.fillRoundRect(3, 13, 18, 8, 3, 3);
            }

        } finally {
            // Release the graphics resources.
            graphics.dispose();
        }

        // Return the image as a Swing-compatible icon.
        return new ImageIcon(image);
    }



    // Updates the graph container's dimensions for the selected layout.
    private void updateGraphHeight(int height) {
        // Ensure BoxLayout allocates the required vertical space.
        graphsPanel.setPreferredSize(new Dimension(0, height));
        graphsPanel.setMinimumSize(new Dimension(0, height));
        graphsPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, height) );
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
    // Displays permanent demonstration graph lines for a simulated snapshot.
    // Both graphs use the same static CPU/GPU values as the measurement cards.
    public void showDemoGraphs(LiveMonitorSnapshot snapshot) {

        // Configure the CPU graph with simulated power.
        cpuGraph.setDemoPower(snapshot.cpuPower());

        // Configure the GPU graph with simulated power.
        gpuGraph.setDemoPower(snapshot.gpuPower());
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







    // Formats a power measurement to one decimal place.
    private String formatPower(double power) {
        return Double.isFinite(power) ? String.format("%.1f W", power) : "N/A";
    }
}
