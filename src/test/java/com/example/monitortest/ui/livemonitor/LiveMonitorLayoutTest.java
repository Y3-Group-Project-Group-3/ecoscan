package com.example.monitortest.ui.livemonitor;

import com.example.monitortest.ui.livemonitor.components.PowerGraphPanel;

import com.example.monitortest.ui.loading.components.Spinner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Tests the layout of the Live Monitor graphs.
// All component creation and inspection occurs on Swing's EDT.
class LiveMonitorLayoutTest {

    // Retain the panel created by each test so its animations can be stopped.
    private LiveMonitorPanel testPanel;

    // LIV-UT-035
    // Verify that CPU and GPU graphs initially appear side by side.
    @Test
    @DisplayName("LIV-UT-035 - Displays CPU and GPU graphs side by side by default")
    void displaysGraphsSideBySideByDefault() throws Exception {

        // Run Swing component operations on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Record the panel so its animations can be cleaned up after the test.
            LiveMonitorPanel panel = new LiveMonitorPanel();
            testPanel = panel;

            // Locate both existing power graph components.
            List<PowerGraphPanel> graphs = new ArrayList<>();
            findGraphs(panel, graphs);

            // Verify that exactly two graph components exist.
            assertEquals(2, graphs.size());

            // Both graphs should belong to the same layout container.
            Container graphContainer = graphs.get(0).getParent().getParent();
            assertSame( graphContainer, graphs.get(1).getParent().getParent() );

            // Verify that the container uses GridLayout.
            assertInstanceOf( GridLayout.class, graphContainer.getLayout() );

            GridLayout layout = (GridLayout) graphContainer.getLayout();

            // One row means the graphs are arranged horizontally.
            assertEquals(1, layout.getRows());

            // Verify that the container holds two graph cards.
            assertEquals(2, graphContainer.getComponentCount());
        });
    }


    // LIV-UT-036
    // Verify that clicking the graph layout toggle switches
    // the CPU and GPU graphs from horizontal to vertical.
    @Test
    @DisplayName("LIV-UT-036 - Switches CPU and GPU graphs to the stacked layout")
    void switchesGraphsToStackedLayout() throws Exception {

        // Perform Swing operations on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Record the panel so its animations can be cleaned up after the test.
            LiveMonitorPanel panel = new LiveMonitorPanel();
            testPanel = panel;

            // Locate the graph layout toggle.
            JButton toggleButton = findGraphLayoutButton(panel);

            // Verify that the toggle exists.
            assertNotNull( toggleButton, "The graph layout toggle button was not found." );

            // Locate both graph components.
            List<PowerGraphPanel> graphs = new ArrayList<>();
            findGraphs(panel, graphs);

            // Verify that both graphs exist.
            assertEquals(2, graphs.size());

            // Retrieve the container holding both graph cards.
            Container graphContainer = graphs.get(0).getParent().getParent();

            // Verify that the initial layout is side by side.
            GridLayout initialLayout = (GridLayout) graphContainer.getLayout();

            assertEquals( 1, initialLayout.getRows(), "The graphs should initially be side by side." );

            // Simulate the user clicking the graph layout toggle.
            toggleButton.doClick();

            // Retrieve the updated graph layout.
            GridLayout updatedLayout = (GridLayout) graphContainer.getLayout();

            // Two rows indicate that the graphs are stacked vertically.
            assertEquals( 2, updatedLayout.getRows(), "The graphs did not switch to the stacked layout." );

            // Verify that the CPU and GPU graph cards remain present.
            assertEquals(2, graphContainer.getComponentCount());
        });
    }


    // LIV-UT-037
    // Verify that toggling twice restores the original horizontal layout.
    @Test
    @DisplayName("LIV-UT-037 - Restores side-by-side graph layout after switching to stacked")
    void restoresSideBySideGraphLayout() throws Exception {

        // Perform all Swing operations on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Record the panel so its animations can be cleaned up after the test.
            LiveMonitorPanel panel = new LiveMonitorPanel();
            testPanel = panel;

            // Find the initial graph layout toggle button.
            JButton toggleButton = findGraphLayoutButton(panel);
            assertNotNull( toggleButton, "The graph layout toggle button was not found." );

            // Locate the CPU and GPU graph components.
            List<PowerGraphPanel> graphs = new ArrayList<>();
            findGraphs(panel, graphs);
            assertEquals(2, graphs.size());

            // Find the common container holding the graph cards.
            Container graphContainer = graphs.get(0).getParent().getParent();

            // Verify the initial side-by-side layout.
            GridLayout initialLayout = (GridLayout) graphContainer.getLayout();
            assertEquals(1, initialLayout.getRows());

            // First click: switch to stacked layout.
            toggleButton.doClick();
            GridLayout stackedLayout = (GridLayout) graphContainer.getLayout();
            assertEquals( 2, stackedLayout.getRows(), "The first click should stack the graphs." );

            // Second click: return to side-by-side layout.
            toggleButton.doClick();
            GridLayout restoredLayout = (GridLayout) graphContainer.getLayout();

            // Verify that the original horizontal arrangement is restored.
            assertEquals( 1, restoredLayout.getRows(), "The second click should restore the side-by-side layout." );

            // Both graph cards must remain in the container.
            assertEquals( 2, graphContainer.getComponentCount() );
        });
    }


    // LIV-UT-038
    // Verify that switching graph layouts preserves the original
    // CPU and GPU graph components rather than recreating them.
    @Test
    @DisplayName("LIV-UT-038 - Preserves CPU and GPU graph components when switching layouts")
    void preservesGraphComponentsWhenSwitchingLayouts() throws Exception {

        // Execute Swing operations on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Record the panel so its animations can be cleaned up after the test.
            LiveMonitorPanel panel = new LiveMonitorPanel();
            testPanel = panel;

            // Locate the graph layout toggle button.
            JButton toggleButton = findGraphLayoutButton(panel);

            assertNotNull( toggleButton, "The graph layout toggle button was not found." );

            // Locate the original CPU and GPU graph components.
            List<PowerGraphPanel> originalGraphs = new ArrayList<>();
            findGraphs(panel, originalGraphs);
            assertEquals( 2, originalGraphs.size(), "The Live Monitor should contain two power graphs." );

            // Store references to the original graph objects.
            PowerGraphPanel originalCpuGraph = originalGraphs.get(0);
            PowerGraphPanel originalGpuGraph = originalGraphs.get(1);

            // First click: switch from side-by-side to stacked layout.
            toggleButton.doClick();

            // Locate the graph components after the layout change.
            List<PowerGraphPanel> stackedGraphs = new ArrayList<>();
            findGraphs(panel, stackedGraphs);

            // Verify that both original objects are still present.
            assertEquals(2, stackedGraphs.size());
            assertSame( originalCpuGraph, stackedGraphs.get(0), "The CPU graph was recreated when switching to stacked mode." );
            assertSame( originalGpuGraph, stackedGraphs.get(1), "The GPU graph was recreated when switching to stacked mode." );

            // Second click: restore the side-by-side layout.
            toggleButton.doClick();

            // Locate the graph components after restoring the layout.
            List<PowerGraphPanel> restoredGraphs = new ArrayList<>();
            findGraphs(panel, restoredGraphs);

            // Verify that the same original graph objects remain.
            assertEquals(2, restoredGraphs.size());
            assertSame( originalCpuGraph, restoredGraphs.get(0), "The CPU graph was recreated when restoring the layout." );
            assertSame( originalGpuGraph, restoredGraphs.get(1), "The GPU graph was recreated when restoring the layout." );
        });
    }


    // LIV-UT-039
    // Verify that the graph toggle updates its icon, tooltip and
    // accessible name to describe the available layout action.
    @Test
    @DisplayName("LIV-UT-039 - Updates graph toggle icon and accessibility information")
    void updatesGraphToggleButtonState() throws Exception {

        // Perform Swing operations on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Record the panel so its animations can be cleaned up after the test.
            LiveMonitorPanel panel = new LiveMonitorPanel();
            testPanel = panel;

            // Locate the initial graph layout toggle button.
            JButton toggleButton = findGraphLayoutButton(panel);
            assertNotNull( toggleButton, "The graph layout toggle button was not found." );

            // Store the icon representing the stacked layout.
            Icon stackedIcon = toggleButton.getIcon();

            // Verify the initial icon exists.
            assertNotNull( stackedIcon, "The initial graph layout icon should not be null." );

            // Verify the initial tooltip and accessible name.
            assertEquals( "Stack Graphs", toggleButton.getToolTipText() );
            assertEquals( "Stack Graphs", toggleButton.getAccessibleContext().getAccessibleName() );

            // Switch from side-by-side to stacked layout.
            toggleButton.doClick();

            // Retrieve the icon representing side-by-side layout.
            Icon sideBySideIcon = toggleButton.getIcon();

            // Confirm the icon changes when the layout changes.
            assertNotNull(sideBySideIcon);

            assertNotSame( stackedIcon, sideBySideIcon, "The toggle icon should change when switching layouts." );

            // Verify the updated tooltip and accessible name.
            assertEquals( "Side-by-Side Graphs", toggleButton.getToolTipText() );
            assertEquals("Side-by-Side Graphs", toggleButton.getAccessibleContext().getAccessibleName() );

            // Switch back to the original side-by-side layout.
            toggleButton.doClick();

            // Verify the original action is available again.
            assertEquals( "Stack Graphs", toggleButton.getToolTipText() );
            assertEquals( "Stack Graphs", toggleButton.getAccessibleContext().getAccessibleName() );

            // Verify the icon still exists after switching back.
            assertNotNull(toggleButton.getIcon());

            // Verify the icon changes from the stacked-state button icon.
            assertNotSame( sideBySideIcon, toggleButton.getIcon() );
        });
    }





    // Recursively locates graph components inside the Live Monitor page.
    private void findGraphs( Container container, List<PowerGraphPanel> graphs) {

        // Inspect every direct child of the container.
        for (Component component : container.getComponents()) {

            // Record any power graph components.
            if (component instanceof PowerGraphPanel graph) {
                graphs.add(graph);
            }

            // Search nested containers for additional graphs.
            if (component instanceof Container child) {
                findGraphs(child, graphs);
            }
        }
    }


    // Recursively finds the graph layout toggle button.
    // Uses the accessible name rather than relying on its visual position.
    private JButton findGraphLayoutButton(Container container) {

        // Search every component inside the current container.
        for (Component component : container.getComponents()) {

            // Check whether the component is the graph layout toggle.
            if (component instanceof JButton button) {

                // Retrieve the accessible name of the button.
                String accessibleName = button.getAccessibleContext().getAccessibleName();

                // The initial toggle action should be to stack the graphs.
                if ("Stack Graphs".equals(accessibleName)) {
                    return button;
                }
            }

            // Search nested containers if the toggle wasn't found here.
            if (component instanceof Container child) {
                JButton found = findGraphLayoutButton(child);
                if (found != null) {
                    return found;
                }
            }
        }

        // No matching graph toggle button was found.
        return null;
    }


    // Stop Live Monitor loading animations after each layout test.
    // This prevents unused Swing timers from affecting subsequent tests.
    @AfterEach
    void stopLiveMonitorAnimations() throws Exception {

        // Perform component operations on Swing's Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() -> {

            // Only clean up when the test successfully created a panel.
            if (testPanel == null) {
                return;
            }

            // Locate all loading spinners inside the test panel.
            List<Spinner> spinners = new ArrayList<>();
            findSpinners(testPanel, spinners);

            // Stop each spinner's repeating Swing timer.
            for (Spinner spinner : spinners) {
                spinner.stop();
            }

            // Release the reference to the completed test panel.
            testPanel = null;
        });
    }

    // Recursively finds all Spinner components inside a container.
    private void findSpinners( Container container, List<Spinner> spinners) {

        // Inspect every child component.
        for (Component component : container.getComponents()) {

            if (component instanceof Spinner spinner) {
                spinners.add(spinner);
            }

            // Search nested containers for additional spinners.
            if (component instanceof Container child) {
                findSpinners(child, spinners);
            }
        }
    }


}

