
package com.example.monitortest.ui.results;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.model.SavedScan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResultsPanelTest {

    private ResultsPanel resultsPanel;

    @BeforeEach
    void setUp() throws Exception {
        // Swing components should be created on the EDT.
        SwingUtilities.invokeAndWait(() -> {
            resultsPanel = new ResultsPanel();
        });
    }

    @Test
    @DisplayName("RES-UT-001 - Initialises ResultsPanel successfully")
    void testResultsPanelInitialisesSuccessfully() {
        assertNotNull(resultsPanel);

        assertNotNull(resultsPanel.getRefreshButton());
        assertNotNull(resultsPanel.getSaveButton());
    }

    @Test
    @DisplayName("RES-UT-002 - Enables save button after updating hardware information")
    void testUpdateHardwareInfoEnablesSaveButton() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(hardwareInfo)
        );

        assertTrue(resultsPanel.getSaveButton().isEnabled());
    }

    @Test
    @DisplayName("RES-UT-003 - Updating hardware information resets the saved state")
    void testUpdatingHardwareInfoResetsSavedState() throws Exception {
        ResultsPanel panel = new ResultsPanel();

        HardwareInfo hardwareInfo = createHardwareInfo();

        panel.updateHardwareInfo(hardwareInfo);

        Field latestScanSavedField =
                ResultsPanel.class.getDeclaredField("latestScanSaved");
        latestScanSavedField.setAccessible(true);

        boolean latestScanSaved =
                latestScanSavedField.getBoolean(panel);

        assertFalse(latestScanSaved,
                "The latest scan should be marked as unsaved after updating hardware information");
    }

    @Test
    @DisplayName("RES-UT-004 - Disables save button when displaying a saved scan")
    void testDisplaySavedScanDisablesSaveButton() throws Exception {

        SavedScan savedScan = createSavedScan();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.displaySavedScan(savedScan)
        );

        assertFalse(resultsPanel.getSaveButton().isEnabled());
    }

    @Test
    @DisplayName("RES-UT-005 - Rejects null saved scans")
    void testDisplaySavedScanRejectsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resultsPanel.displaySavedScan(null)
        );
    }

    @Test
    @DisplayName("RES-UT-006 - Handles restoring when no latest scan exists")
    void testRestoreLatestScanDoesNothingWhenNoScanExists() throws Exception {

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.restoreLatestScan()
        );

        assertNotNull(resultsPanel);
    }

    @Test
    @DisplayName("RES-UT-007 - Enables save button after restoring latest scan")
    void testRestoreLatestScanEnablesSaveButton() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SavedScan savedScan = createSavedScan();

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.updateHardwareInfo(hardwareInfo);

            resultsPanel.displaySavedScan(savedScan);

            resultsPanel.restoreLatestScan();
        });

        assertTrue(resultsPanel.getSaveButton().isEnabled());
    }

    @Test
    @DisplayName("RES-UT-008 - Restores latest scan state after viewing historical scan")
    void testRestoreLatestScanRestoresSavedState() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(hardwareInfo)
        );

        // Display historical scan.
        SavedScan savedScan = createSavedScan();

        SwingUtilities.invokeAndWait(() -> {
            resultsPanel.displaySavedScan(savedScan);
            resultsPanel.restoreLatestScan();
        });

        // Latest scan should be available for saving.
        assertTrue(resultsPanel.getSaveButton().isEnabled());
    }

    @Test
    @DisplayName("RES-UT-009 - Creates scan timestamp when hardware information is updated")
    void testUpdateHardwareInfoCreatesScanTimestamp() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(hardwareInfo)
        );

        LocalDateTime scanTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        assertNotNull(scanTime);
    }

    @Test
    @DisplayName("RES-UT-010 - Preserves latest scan timestamp when displaying saved scan")
    void testDisplaySavedScanPreservesLatestScanTimestamp() throws Exception {

        HardwareInfo hardwareInfo = createHardwareInfo();

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.updateHardwareInfo(hardwareInfo)
        );

        LocalDateTime originalTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        SwingUtilities.invokeAndWait(() ->
                resultsPanel.displaySavedScan(createSavedScan())
        );

        LocalDateTime updatedTime = getPrivateField(
                "currentScanTime",
                LocalDateTime.class
        );

        assertEquals(originalTime, updatedTime);
    }

    private HardwareInfo createHardwareInfo() {
        return new HardwareInfo(
                "Intel Core i7-13700K",  // CPU name
                45.0,                    // CPU temperature
                65.0,                    // CPU power
                16L * 1024 * 1024 * 1024, // Total RAM (16 GB)
                false,                   // CPU power simulated

                "NVIDIA RTX 4060",       // GPU name
                "NVIDIA",                // GPU vendor
                8L * 1024 * 1024 * 1024, // GPU VRAM (8 GB)
                115.0,                   // GPU power

                "ASUS",                  // Motherboard manufacturer
                "ROG STRIX Z790",        // Motherboard model
                "1.0",                   // Motherboard version

                java.util.Collections.emptyList(), // Storage devices
                java.util.Collections.emptyList()  // Memory modules
        );
    }

    private SavedScan createSavedScan() {
        return new SavedScan(
                UUID.randomUUID(),
                "Test Scan",
                LocalDateTime.now().minusDays(1),
                createHardwareInfo(),
                false
        );
    }

    private <T> T getPrivateField(String fieldName, Class<T> type)
            throws Exception {

        Field field = ResultsPanel.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return type.cast(field.get(resultsPanel));
    }
}
