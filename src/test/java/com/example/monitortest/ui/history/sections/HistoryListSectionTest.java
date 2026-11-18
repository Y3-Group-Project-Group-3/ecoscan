package com.example.monitortest.ui.history.sections;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.components.ScanRow;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the Saved Hardware Scans list on the History page.
// Uses a temporary folder so the real saved scans are never changed.
class HistoryListSectionTest {

    @TempDir
    Path tempDir;

    private String originalHome;

    private ScanHistoryCsvRepository repository;

    // Point EcoScan at a temporary home folder and save three test scans.
    @BeforeEach
    void setUp() throws IOException {

        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        repository = new ScanHistoryCsvRepository( tempDir.resolve("EcoScan").resolve("scans.csv") );

        repository.save( createScan("Office PC", LocalDateTime.of(2026, 11, 3, 9, 0)) );
        repository.save( createScan("Brandon scan", LocalDateTime.of(2026, 11, 6, 9, 0)) );
        repository.save( createScan("Home PC", LocalDateTime.of(2026, 11, 5, 9, 0)) );
    }

    // Restore the real home folder after each test.
    @AfterEach
    void tearDown() {
        System.setProperty("user.home", originalHome);
    }

    // Creates a saved scan with simple test hardware.
    private SavedScan createScan(String name, LocalDateTime scannedAt) {

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 40.0, 15.0, 8_000_000_000L, false,
                "Test GPU", "Test", 2_000_000_000L, 10.0,
                "Test", "Test Board", "1.0",
                List.of(), List.of()
        );

        return new SavedScan(UUID.randomUUID(), name, scannedAt, hardware, false);
    }

    // Returns the scan names in the order they are displayed.
    private List<String> rowNames(HistoryListSection section) {
        return findAll(section, ScanRow.class).stream()
                .map(row -> findAll(row, JLabel.class).get(1).getText())
                .toList();
    }

    // Presses the mouse on a row, holding Ctrl when requested.
    private void pressRow(ScanRow row, boolean ctrlDown) {
        row.dispatchEvent(new MouseEvent( row, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                ctrlDown ? InputEvent.CTRL_DOWN_MASK : 0, 5, 5, 1, false ));
    }

    // HIS-UT-020
    // Verify that the sort dropdown lists the newest scans first by default.
    @Test
    @DisplayName("HIS-UT-020 - Sort dropdown defaults to newest first")
    void listsNewestFirst() {

        HistoryListSection section = new HistoryListSection();

        assertEquals( List.of("Brandon scan", "Home PC", "Office PC"), rowNames(section) );
    }

    // HIS-UT-021
    // Verify that setting the sort dropdown to oldest first reverses the order.
    @Test
    @DisplayName("HIS-UT-021 - Sort dropdown set to oldest first reverses the order")
    void listsOldestFirst() {

        HistoryListSection section = new HistoryListSection();

        // Same as choosing "Oldest first" in the sort dropdown.
        section.setNewestFirst(false);

        assertEquals( List.of("Office PC", "Home PC", "Brandon scan"), rowNames(section) );
    }

    // HIS-UT-022
    // Verify that searching by name only shows matching scans.
    @Test
    @DisplayName("HIS-UT-022 - Search only shows scans matching the name")
    void searchesByName() {

        HistoryListSection section = new HistoryListSection();

        // Capital letters should not affect the search.
        section.setSearchText("OFFICE");

        assertEquals( List.of("Office PC"), rowNames(section) );
    }

    // HIS-UT-023
    // Verify that a search with no results shows a message.
    @Test
    @DisplayName("HIS-UT-023 - Shows a message when no scans match the search")
    void showsNoMatchMessage() {

        HistoryListSection section = new HistoryListSection();

        section.setSearchText("laptop");

        // No rows should be shown, only the no-match message.
        assertTrue( rowNames(section).isEmpty() );
        assertNotNull( findLabel(section, "No saved scans match your search.") );
    }

    // HIS-UT-024
    // Verify that Ctrl/Cmd-click selects more than one row.
    @Test
    @DisplayName("HIS-UT-024 - Ctrl/Cmd-click selects more than one row")
    void selectsMultipleRows() {

        HistoryListSection section = new HistoryListSection();
        List<ScanRow> rows = findAll(section, ScanRow.class);

        // Click the first row, then Ctrl-click the third row.
        pressRow(rows.get(0), false);
        pressRow(rows.get(2), true);

        assertTrue( rows.get(0).isSelected() );
        assertFalse( rows.get(1).isSelected() );
        assertTrue( rows.get(2).isSelected() );
    }

    // HIS-UT-025
    // Verify that Clear Scans also deletes scans hidden by the search.
    @Test
    @DisplayName("HIS-UT-025 - Clear Scans deletes all scans, including ones hidden by the search")
    void clearScansDeletesHiddenScans() throws IOException {

        HistoryListSection section = new HistoryListSection();

        // Only Office PC is visible, the other two are hidden by the search.
        section.setSearchText("office");

        section.deleteAll();

        // Every saved scan should be removed from the CSV.
        assertTrue( repository.loadAll().isEmpty() );
        assertFalse( section.hasStoredScans() );
    }
}