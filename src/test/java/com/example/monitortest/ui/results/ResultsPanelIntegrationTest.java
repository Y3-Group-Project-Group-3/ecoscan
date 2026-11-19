package com.example.monitortest.ui.results;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.model.SavedScan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResultsPanelIntegrationTest {

    private ResultsPanel resultsPanel;

    @BeforeEach
    void setUp() throws Exception {
        // Create Swing components on the Event Dispatch Thread.
        SwingUtilities.invokeAndWait(() ->
                resultsPanel = new ResultsPanel()
        );
    }

    @Test
    @DisplayName("RES-IT-001 - Displays newly detected hardware information")
    void displaysNewlyDetectedHardwareInformation() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(hardwareInfo)
        );

        assertNotNull(resultsPanel);
        assertTrue(resultsPanel.getSaveButton().isEnabled());

        assertNotNull(getPrivateField(
                "currentHardwareInfo",
                HardwareInfo.class
        ));
    }

    @Test
    @DisplayName("RES-IT-002 - Updates results when new hardware information arrives")
    void updatesResultsWhenNewHardwareInformationArrives() throws Exception {

        HardwareInfo firstScan = createHardwareInfo();
        HardwareInfo secondScan = createDifferentHardwareInfo();

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.updateHardwareInfo(firstScan);
            resultsPanel.updateHardwareInfo(secondScan);
        });

        HardwareInfo displayedInfo = getPrivateField(
                "currentHardwareInfo",
                HardwareInfo.class
        );

        assertSame(secondScan, displayedInfo);
    }

    @Test
    @DisplayName("RES-IT-003 - Records timestamp when new hardware information arrives")
    void recordsTimestampWhenNewHardwareInformationArrives() throws Exception {

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(createHardwareInfo())
        );

        LocalDateTime scanTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        assertNotNull(scanTime);
        assertFalse(scanTime.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("RES-IT-004 - Displays hardware information from a saved scan")
    void displaysHardwareInformationFromSavedScan() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();
        SavedScan savedScan = createSavedScan(hardwareInfo);

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.displaySavedScan(savedScan)
        );

        assertFalse(resultsPanel.getSaveButton().isEnabled());

        assertTrue(getPrivateField(
                "viewingSavedScan",
                Boolean.class
        ));
    }

    @Test
    @DisplayName("RES-IT-005 - Preserves latest scan while displaying historical scan")
    void preservesLatestScanWhileDisplayingHistoricalScan() throws Exception {

        HardwareInfo latestHardware = createHardwareInfo();
        HardwareInfo historicalHardware = createDifferentHardwareInfo();

        SavedScan savedScan = createSavedScan(historicalHardware);

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.updateHardwareInfo(latestHardware);
            resultsPanel.displaySavedScan(savedScan);
        });

        HardwareInfo storedLatestHardware = getPrivateField(
                "currentHardwareInfo",
                HardwareInfo.class
        );

        assertSame(latestHardware, storedLatestHardware);
    }

    @Test
    @DisplayName("RES-IT-006 - Restores latest hardware information after viewing history")
    void restoresLatestHardwareInformationAfterViewingHistory() throws Exception {

        HardwareInfo latestHardware = createHardwareInfo();
        HardwareInfo historicalHardware = createDifferentHardwareInfo();

        SavedScan savedScan = createSavedScan(historicalHardware);

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.updateHardwareInfo(latestHardware);
            resultsPanel.displaySavedScan(savedScan);
            resultsPanel.restoreLatestScan();
        });

        HardwareInfo restoredHardware = getPrivateField(
                "currentHardwareInfo",
                HardwareInfo.class
        );

        assertSame(latestHardware, restoredHardware);
        assertTrue(resultsPanel.getSaveButton().isEnabled());
    }

    @Test
    @DisplayName("RES-IT-007 - Exits historical viewing mode after restoring latest scan")
    void exitsHistoricalViewingModeAfterRestoringLatestScan() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.updateHardwareInfo(createHardwareInfo());
            resultsPanel.displaySavedScan(
                    createSavedScan(createDifferentHardwareInfo())
            );
            resultsPanel.restoreLatestScan();
        });

        assertFalse(getPrivateField(
                "viewingSavedScan",
                Boolean.class
        ));
    }

    @Test
    @DisplayName("RES-IT-008 - Preserves latest scan timestamp after restoration")
    void preservesLatestScanTimestampAfterRestoration() throws Exception {

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(createHardwareInfo())
        );

        LocalDateTime originalTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.displaySavedScan(
                    createSavedScan(createDifferentHardwareInfo())
            );
            resultsPanel.restoreLatestScan();
        });

        LocalDateTime restoredTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        assertEquals(originalTime, restoredTime);
    }

    @Test
    @DisplayName("RES-IT-009 - Contains results sections and scan controls")
    void containsResultsSectionsAndScanControls() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(resultsPanel.getRefreshButton());
            assertNotNull(resultsPanel.getSaveButton());

            assertTrue(resultsPanel.getComponentCount() > 0);

            assertTrue(containsComponent(
                    resultsPanel,
                    JScrollPane.class
            ));
        });
    }

    @Test
    @DisplayName("RES-IT-010 - Retains main UI components after hardware update")
    void retainsMainUIComponentsAfterHardwareUpdate() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            int originalComponentCount = resultsPanel.getComponentCount();

            resultsPanel.updateHardwareInfo(createHardwareInfo());

            assertEquals(
                    originalComponentCount,
                    resultsPanel.getComponentCount()
            );

            assertNotNull(resultsPanel.getRefreshButton());
            assertNotNull(resultsPanel.getSaveButton());
        });
    }

    // Creates simulated hardware information for the first scan.
    private HardwareInfo createHardwareInfo() {

        return new HardwareInfo(
                "TEST CPU - Intel i7",
                52.5,
                87.3,
                32L * 1024 * 1024 * 1024,
                false,

                "TEST GPU - NVIDIA RTX",
                "NVIDIA",
                12L * 1024 * 1024 * 1024,
                143.7,

                "TEST MOTHERBOARD",
                "TEST MODEL",
                "2.1",

                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    // Creates different simulated hardware information for comparison.
    private HardwareInfo createDifferentHardwareInfo() {

        return new HardwareInfo(
                "TEST CPU - AMD Ryzen",
                60.0,
                95.0,
                16L * 1024 * 1024 * 1024,
                false,

                "TEST GPU - AMD Radeon",
                "AMD",
                8L * 1024 * 1024 * 1024,
                120.0,

                "TEST BOARD",
                "TEST MODEL 2",
                "3.0",

                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    // Creates a historical scan using the supplied hardware information.
    private SavedScan createSavedScan(HardwareInfo hardwareInfo) {

        return new SavedScan(
                UUID.randomUUID(),
                "Integration Test Scan",
                LocalDateTime.now().minusDays(1),
                hardwareInfo,
                false
        );
    }

    // Retrieves a private field for integration-state verification.
    private <T> T getPrivateField(String fieldName, Class<T> type)
            throws Exception {

        Field field = ResultsPanel.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return type.cast(field.get(resultsPanel));
    }

    // Searches the component hierarchy for a component of the requested type.
    private boolean containsComponent(
            Container container,
            Class<? extends Component> componentType) {

        for (Component component : container.getComponents()) {

            if (componentType.isInstance(component)) {
                return true;
            }

            if (component instanceof Container) {
                if (containsComponent(
                        (Container) component,
                        componentType
                )) {
                    return true;
                }
            }
        }

        return false;
    }
}