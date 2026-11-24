package com.example.monitortest.ui.history.storage;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.model.SessionRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the History changes made from the Finalising card.
// Covers the session limit, duplicate imports, importing scans and deleting selected sessions.
// Uses a temporary folder so the real saved scans and sessions are never changed.
class HistoryNewFeaturesTest {

    @TempDir
    Path tempDir;

    // HIS-UT-028
    // Verify that only the 10 most recent sessions are kept.
    @Test
    @DisplayName("HIS-UT-028 - Only the 10 most recent sessions are kept")
    void keepsOnlyTenMostRecentSessions() throws IOException {

        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("sessions.csv") );

        // Save 12 sessions, one minute apart.
        for (int i = 0; i < 12; i++) {
            repository.save( new SessionRecord(LocalDateTime.of(2026, 11, 22, 10, i), 0.01, 60) );
        }

        List<SessionRecord> sessions = repository.loadAll();

        // Only 10 should remain, and the 2 oldest should be removed.
        assertEquals(10, sessions.size());
        assertTrue( sessions.stream().noneMatch(s -> s.getStartedAt().equals(LocalDateTime.of(2026, 11, 22, 10, 0))) );
        assertTrue( sessions.stream().noneMatch(s -> s.getStartedAt().equals(LocalDateTime.of(2026, 11, 22, 10, 1))) );
    }

    // HIS-UT-029
    // Verify that importing the same sessions file twice does not add duplicates.
    @Test
    @DisplayName("HIS-UT-029 - Importing the same sessions file twice adds nothing new")
    void skipsDuplicateSessionImports() throws IOException {

        // Create a sessions file containing 2 sessions.
        SessionHistoryCsvRepository source = new SessionHistoryCsvRepository( tempDir.resolve("export.csv") );
        source.save( new SessionRecord(LocalDateTime.of(2026, 11, 22, 10, 0), 0.04, 50) );
        source.save( new SessionRecord(LocalDateTime.of(2026, 11, 22, 11, 0), 0.09, 120) );

        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("sessions.csv") );

        // The first import adds both sessions, the second finds nothing new.
        assertEquals(2, repository.importFrom( tempDir.resolve("export.csv") ));
        assertEquals(0, repository.importFrom( tempDir.resolve("export.csv") ));

        assertEquals(2, repository.loadAll().size());
    }

    // HIS-UT-030
    // Verify that Import Scans adds the scans from an exported scans file.
    @Test
    @DisplayName("HIS-UT-030 - Import Scans adds scans from an exported CSV")
    void importsScansFromExportedFile() throws IOException {

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 10.0, 8_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                6.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        // Save 2 scans and export them.
        ScanHistoryCsvRepository original = new ScanHistoryCsvRepository( tempDir.resolve("scans.csv") );
        original.save( new SavedScan(UUID.randomUUID(), "Office PC", LocalDateTime.of(2026, 11, 5, 10, 0), hardware, false) );
        original.save( new SavedScan(UUID.randomUUID(), "Home PC", LocalDateTime.of(2026, 11, 6, 10, 0), hardware, false) );
        original.exportTo( tempDir.resolve("export.csv") );

        // Import the exported file into an empty history.
        ScanHistoryCsvRepository repository = new ScanHistoryCsvRepository( tempDir.resolve("imported.csv") );
        int imported = repository.importFrom( tempDir.resolve("export.csv") );

        // Both scans should be imported with their names.
        assertEquals(2, imported);
        List<SavedScan> scans = repository.loadAll();
        assertTrue( scans.stream().anyMatch(s -> s.getName().equals("Office PC")) );
        assertTrue( scans.stream().anyMatch(s -> s.getName().equals("Home PC")) );
    }

    // HIS-UT-031
    // Verify that deleting a selected session only removes that session.
    @Test
    @DisplayName("HIS-UT-031 - Deleting one selected session leaves the others")
    void deletesOnlySelectedSession() throws IOException {

        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("sessions.csv") );

        SessionRecord first = new SessionRecord(LocalDateTime.of(2026, 11, 22, 10, 0), 0.01, 60);
        SessionRecord second = new SessionRecord(LocalDateTime.of(2026, 11, 22, 11, 0), 0.04, 60);
        SessionRecord third = new SessionRecord(LocalDateTime.of(2026, 11, 22, 12, 0), 0.09, 60);

        repository.save(first);
        repository.save(second);
        repository.save(third);

        // Delete only the middle session.
        repository.deleteSessions( List.of(second) );

        List<SessionRecord> sessions = repository.loadAll();

        // The other 2 sessions should still be there.
        assertEquals(2, sessions.size());
        assertTrue( sessions.stream().noneMatch(s -> s.getStartedAt().equals(second.getStartedAt())) );
    }
}