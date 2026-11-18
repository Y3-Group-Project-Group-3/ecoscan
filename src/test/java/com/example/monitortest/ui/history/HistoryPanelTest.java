package com.example.monitortest.ui.history;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.components.ScanRow;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.sections.HistoryListSection;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Integration tests for the History page.
// Checks that the scan list and the action buttons work together.
class HistoryPanelTest {

    @TempDir
    Path tempDir;

    private String originalHome;

    // Point EcoScan at a temporary home folder and save one test scan.
    @BeforeEach
    void setUp() throws IOException {

        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 40.0, 15.0, 8_000_000_000L, false,
                "Test GPU", "Test", 2_000_000_000L, 10.0,
                "Test", "Test Board", "1.0",
                List.of(), List.of()
        );

        new ScanHistoryCsvRepository( tempDir.resolve("EcoScan").resolve("scans.csv") )
                .save( new SavedScan(UUID.randomUUID(), "Brandon scan", LocalDateTime.of(2026, 11, 6, 9, 0), hardware, false) );
    }

    // Restore the real home folder after each test.
    @AfterEach
    void tearDown() {
        System.setProperty("user.home", originalHome);
    }

    // HIS-IT-001
    // Verify that selecting a scan row enables the Delete Selected button.
    @Test
    @DisplayName("HIS-IT-001 - Selecting a scan row enables Delete Selected")
    void selectingRowEnablesDelete() {

        HistoryPanel panel = new HistoryPanel();

        // Nothing is selected yet, so Delete Selected starts disabled.
        assertFalse( findButton(panel, "Delete Selected").isEnabled() );

        // Click the saved scan row.
        ScanRow row = findAll(panel, ScanRow.class).get(0);
        row.dispatchEvent(new MouseEvent( row, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 5, 5, 1, false ));

        assertTrue( findButton(panel, "Delete Selected").isEnabled() );
    }

    // HIS-IT-002
    // Verify that the scan action buttons are inside the Saved Hardware Scans card.
    @Test
    @DisplayName("HIS-IT-002 - Export, Print and Delete are inside the scans card")
    void scanButtonsAreInScansCard() {

        HistoryPanel panel = new HistoryPanel();

        HistoryListSection scansCard = findAll(panel, HistoryListSection.class).get(0);

        // Each scan action should be found inside the scans card itself.
        assertNotNull( findButton(scansCard, "Export Results") );
        assertNotNull( findButton(scansCard, "Print List") );
        assertNotNull( findButton(scansCard, "Delete Selected") );
    }
}