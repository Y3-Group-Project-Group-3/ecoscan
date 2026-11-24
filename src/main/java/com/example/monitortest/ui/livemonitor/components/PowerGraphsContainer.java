package com.example.monitortest.ui.livemonitor.components;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.ui.common.OutlinedActionButton;
import com.example.monitortest.ui.loading.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.example.monitortest.ui.theme.AppTheme.FONT_FAMILY;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.UIUtility.font;

// Displays the CPU and GPU power graphs.
// Manages graph layout changes without recreating either graph.
public class PowerGraphsContainer extends JPanel {

    // Graphs displaying CPU and GPU power consumption.
    private final PowerGraphPanel cpuGraph = new PowerGraphPanel(com.example.monitortest.ui.theme.AppTheme.GREEN);

    private final PowerGraphPanel gpuGraph = new PowerGraphPanel(com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED);

    // Holds both graphs and allows switching between layouts.
    private JPanel graphsPanel;

    // Tracks whether the graphs are displayed side by side.
    private boolean sideBySideView = true;

    // Switches between horizontal and vertically stacked graph layouts.
    private final OutlinedActionButton graphLayoutButton = new OutlinedActionButton("");

    // Builds the container for both CPU and GPU power graphs.
    public PowerGraphsContainer() {
        super();

        // Stack the graph toolbar and graph cards vertically.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Create the graph panel using the existing side-by-side layout.
        graphsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        graphsPanel.setOpaque(false);

        // Preserve the existing graph height and margins.
        graphsPanel.setPreferredSize(new Dimension(0, 300));
        graphsPanel.setMinimumSize(new Dimension(0, 300));
        graphsPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, 300) );
        graphsPanel.setBorder( BorderFactory.createEmptyBorder(10, 40, 30, 40) );

        // Create rounded cards for both graphs.
        RoundedPanel cpuSection = createGraphCard( "CPU Power Consumption", cpuGraph );
        RoundedPanel gpuSection = createGraphCard( "GPU Power Consumption", gpuGraph );

        // Add the graph cards without recreating their graph components.
        graphsPanel.add(cpuSection);
        graphsPanel.add(gpuSection);

        // Place the layout toggle above the graph cards.
        add(createGraphToolbar());

        // Display the CPU and GPU graphs beneath the toolbar.
        add(graphsPanel);
    }

    // Creates a styled card containing a heading and power graph.
    // Reuses the same card styling as the original LiveMonitorPanel.
    private RoundedPanel createGraphCard( String title, PowerGraphPanel graph ) {

        // Create the rounded white card with a subtle border.
        RoundedPanel card = new RoundedPanel( Color.WHITE, new Color(226, 232, 240), 16 );

        // Arrange the graph heading above the graph itself.
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder( BorderFactory.createEmptyBorder(20, 20, 15, 20) );

        // Create and style the heading.
        JLabel graphTitle = new JLabel(title);
        graphTitle.setFont(font(Font.BOLD, 15));
        graphTitle.setForeground(TEXT_MAIN);

        // Add the heading and existing graph component.
        card.add(graphTitle, BorderLayout.NORTH);
        card.add(graph, BorderLayout.CENTER);

        return card;
    }


    // Configures the graph layout button and its toolbar.
    private JPanel createGraphToolbar() {

        // Initially, the graphs are side by side.
        // The button therefore offers the option to stack them.
        graphLayoutButton.setIcon(createGraphLayoutIcon(false));
        graphLayoutButton.setToolTipText("Stack Graphs");

        // Give the icon-only button an accessible name.
        graphLayoutButton.getAccessibleContext().setAccessibleName("Stack Graphs");

        // Use consistent dimensions and interaction styling.
        graphLayoutButton.setPreferredSize(new Dimension(42, 36));
        graphLayoutButton.setHorizontalAlignment(SwingConstants.CENTER);
        graphLayoutButton.setFocusPainted(false);
        graphLayoutButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );

        // Switch layouts when the user clicks the button.
        graphLayoutButton.addActionListener(event -> toggleGraphLayout());

        // Align the button with the right-hand graph margin.
        JPanel toolbar = new JPanel( new FlowLayout(FlowLayout.RIGHT, 40, 0) );

        toolbar.setOpaque(false);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize( new Dimension(Integer.MAX_VALUE, 45) );

        toolbar.add(graphLayoutButton);

        return toolbar;
    }


    // Switches the existing graphs between horizontal and vertical layouts.
    // The graph instances are retained, preserving their recorded readings.
    private void toggleGraphLayout() {

        // Switch to the opposite layout.
        sideBySideView = !sideBySideView;

        if (sideBySideView) {

            // Arrange graphs in one row with two columns.
            graphsPanel.setLayout(new GridLayout(1, 2, 20, 0));
            updateGraphHeight(300);

            // Offer the option to stack graphs.
            graphLayoutButton.setIcon(createGraphLayoutIcon(false));
            graphLayoutButton.setToolTipText("Stack Graphs");
            graphLayoutButton.getAccessibleContext().setAccessibleName("Stack Graphs");

        } else {

            // Arrange graphs vertically.
            graphsPanel.setLayout(new GridLayout(2, 1, 0, 20));
            updateGraphHeight(520);

            // Offer the option to return to side-by-side graphs.
            graphLayoutButton.setIcon(createGraphLayoutIcon(true));
            graphLayoutButton.setToolTipText("Side-by-Side Graphs");
            graphLayoutButton.getAccessibleContext().setAccessibleName("Side-by-Side Graphs");
        }

        // Refresh the graph container after changing its layout.
        graphsPanel.revalidate();
        graphsPanel.repaint();
    }


    // Updates the graph container height for the selected layout.
    private void updateGraphHeight(int height) {

        // Keep the graph region at a consistent height under BoxLayout.
        graphsPanel.setPreferredSize(new Dimension(0, height));
        graphsPanel.setMinimumSize(new Dimension(0, height));
        graphsPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, height) );

        // Recalculate the enclosing layout when graph height changes.
        revalidate();
        repaint();
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


    // Adds new CPU and GPU power measurements to their graphs.
    // Ensures graph updates occur on Swing's Event Dispatch Thread.
    public void updateGraphs(LiveMonitorSnapshot snapshot) {
        // Queue the updates on the Swing UI thread.
        SwingUtilities.invokeLater(() -> {
            // Append each reading to its corresponding graph.
            cpuGraph.addReading(snapshot.cpuPower());
            gpuGraph.addReading(snapshot.gpuPower());
        });
    }



    // Displays static CPU and GPU graph readings for macOS demo mode.
    // These values remain fixed rather than accumulating live samples.
    public void showDemoGraphs(LiveMonitorSnapshot snapshot) {

        // Ensure graph changes are performed on Swing's UI thread.
        SwingUtilities.invokeLater(() -> {

            // Set each graph to its demonstration power reading.
            cpuGraph.setDemoPower(snapshot.cpuPower());
            gpuGraph.setDemoPower(snapshot.gpuPower());
        });
    }

}
