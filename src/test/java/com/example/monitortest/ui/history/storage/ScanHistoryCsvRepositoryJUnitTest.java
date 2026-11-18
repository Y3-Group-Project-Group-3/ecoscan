
package com.example.monitortest.ui.history.storage;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.history.model.SavedScan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScanHistoryCsvRepositoryJUnitTest {

    @TempDir
    Path tempDir;

    // UT-HIS-001 — Maximum Saved Scans Limit
    @Test
    void utHis001_retainsOnlyTenNewestScans() throws IOException {

        Path csvPath = tempDir.resolve("scans.csv");
        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        // Create sample hardware data without accessing real sensors.
        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        LocalDateTime startDate =
                LocalDateTime.of(2026, 1, 1, 12, 0);

        // Save twelve scans with increasing timestamps.
        for (int i = 0; i < 12; i++) {
            SavedScan scan = new SavedScan(
                    UUID.randomUUID(),
                    "Scan " + i,
                    startDate.plusDays(i),
                    hardware,
                    false
            );

            repository.save(scan);
        }

        List<SavedScan> savedScans = repository.loadAll();

        // Only the ten newest scans should remain.
        assertEquals(10, savedScans.size());

        // The two oldest scans must have been removed.
        assertFalse(savedScans.stream()
                .anyMatch(scan -> scan.getName().equals("Scan 0")));
        assertFalse(savedScans.stream()
                .anyMatch(scan -> scan.getName().equals("Scan 1")));

        // Verify the complete order, newest first.
        for (int i = 0; i < 10; i++) {
            assertEquals(
                    "Scan " + (11 - i),
                    savedScans.get(i).getName()
            );
        }
    }

    // UT-HIS-002 — Scan Sorting by Timestamp
    @Test
    void utHis002_sortsScansByTimestamp() throws IOException {

        Path csvPath = tempDir.resolve("sorting-test.csv");
        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        // Use test hardware data without accessing real sensors.
        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        // Insert scans in an order different from their timestamps.
        int[] days = {10, 5, 15, 1, 12};
        String[] names = {"A", "B", "C", "D", "E"};

        for (int i = 0; i < days.length; i++) {
            SavedScan scan = new SavedScan(
                    UUID.randomUUID(),
                    "Scan " + names[i],
                    LocalDateTime.of(2026, 1, days[i], 12, 0),
                    hardware,
                    false
            );

            repository.save(scan);
        }

        List<SavedScan> savedScans = repository.loadAll();

        assertEquals(5, savedScans.size());

        // Verify that scans are returned in descending timestamp order.
        assertEquals(
                List.of("Scan C", "Scan E", "Scan A", "Scan B", "Scan D"),
                savedScans.stream().map(SavedScan::getName).toList()
        );
    }

    // UT-HIS-003 — Delete Scan by UUID
    @Test
    void utHis003_deletesOnlySelectedScan() throws IOException {

        Path csvPath = tempDir.resolve("delete-test.csv");
        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        UUID firstId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        SavedScan first = new SavedScan(
                firstId,
                "Scan A",
                LocalDateTime.of(2026, 1, 1, 12, 0),
                hardware,
                false
        );

        SavedScan target = new SavedScan(
                targetId,
                "Scan B",
                LocalDateTime.of(2026, 1, 2, 12, 0),
                hardware,
                false
        );

        SavedScan third = new SavedScan(
                thirdId,
                "Scan C",
                LocalDateTime.of(2026, 1, 3, 12, 0),
                hardware,
                false
        );

        // Save three scans and delete the middle one by UUID.
        repository.save(first);
        repository.save(target);
        repository.save(third);

        repository.deleteByIds(Set.of(targetId));

        List<SavedScan> remaining = repository.loadAll();

        // Only the selected scan should be removed.
        assertEquals(2, remaining.size());

        assertFalse(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(targetId)));

        // Verify that both unrelated scans remain.
        assertTrue(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(firstId)));

        assertTrue(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(thirdId)));
    }

    // UT-HIS-004 — Delete Multiple Scans
    @Test
    void utHis004_deletesMultipleSelectedScans() throws IOException {

        Path csvPath = tempDir.resolve("delete-multiple-test.csv");
        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        UUID[] ids = new UUID[5];

        // Save five scans with unique identifiers.
        for (int i = 0; i < 5; i++) {
            ids[i] = UUID.randomUUID();

            SavedScan scan = new SavedScan(
                    ids[i],
                    "Scan " + i,
                    LocalDateTime.of(2026, 1, 1, 12, 0).plusDays(i),
                    hardware,
                    false
            );

            repository.save(scan);
        }

        // Delete two selected scans in a single operation.
        repository.deleteByIds(Set.of(ids[1], ids[3]));

        List<SavedScan> remaining = repository.loadAll();

        // Three scans should remain.
        assertEquals(3, remaining.size());

        // Confirm that both selected scans were deleted.
        assertFalse(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(ids[1])));

        assertFalse(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(ids[3])));

        // Confirm that all unselected scans remain.
        assertTrue(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(ids[0])));

        assertTrue(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(ids[2])));

        assertTrue(remaining.stream()
                .anyMatch(scan -> scan.getId().equals(ids[4])));
    }

    // UT-HIS-005 — Delete Non-existent Scan
    @Test
    void utHis005_deleteNonExistentScanPreservesHistory() throws IOException {

        Path csvPath = tempDir.resolve("delete-nonexistent-test.csv");
        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        repository.save(new SavedScan(
                firstId,
                "Scan A",
                LocalDateTime.of(2026, 1, 1, 12, 0),
                hardware,
                false
        ));

        repository.save(new SavedScan(
                secondId,
                "Scan B",
                LocalDateTime.of(2026, 1, 2, 12, 0),
                hardware,
                false
        ));

        repository.save(new SavedScan(
                thirdId,
                "Scan C",
                LocalDateTime.of(2026, 1, 3, 12, 0),
                hardware,
                false
        ));

        // Capture the original records before attempting deletion.
        List<SavedScan> before = repository.loadAll();

        UUID nonExistentId = UUID.randomUUID();

        // Ensure the UUID is not already present in the history.
        assertFalse(before.stream()
                .anyMatch(scan -> scan.getId().equals(nonExistentId)));

        // Attempt to delete a scan that does not exist.
        repository.deleteByIds(Set.of(nonExistentId));

        List<SavedScan> after = repository.loadAll();

        // The number of saved scans must remain unchanged.
        assertEquals(3, after.size());

        // Verify that all original IDs are preserved.
        assertEquals(
                before.stream().map(SavedScan::getId).toList(),
                after.stream().map(SavedScan::getId).toList()
        );

        // Verify that all original scan names are preserved.
        assertEquals(
                before.stream().map(SavedScan::getName).toList(),
                after.stream().map(SavedScan::getName).toList()
        );
    }

    // UT-HIS-006 — Missing History File
    @Test
    void utHis006_missingHistoryFileReturnsEmptyList() throws IOException {

        Path csvPath = tempDir.resolve("missing-history.csv");

        // Confirm that the test starts without an existing CSV file.
        assertFalse(Files.exists(csvPath));

        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        // Loading history from a missing file should return no scans.
        List<SavedScan> savedScans = repository.loadAll();

        assertNotNull(savedScans);
        assertTrue(savedScans.isEmpty());

        // Reading history should not create a CSV file.
        assertFalse(Files.exists(csvPath));
    }

    // UT-HIS-010 — Export Saved Scans
    @Test
    void utHis010_exportsSavedScans() throws IOException {

        Path csvPath = tempDir.resolve("export-source.csv");
        Path exportPath = tempDir.resolve("exported-scans.csv");

        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        HardwareInfo hardware = new HardwareInfo(
                "Test CPU", 45.0, 65.0, 16_000_000_000L,
                false,
                "Test GPU", "Test Vendor", 4_000_000_000L,
                75.0,
                "Test Manufacturer", "Test Board", "1.0",
                List.of(), List.of()
        );

        UUID[] ids = new UUID[3];

        // Create three saved scans with unique UUIDs.
        for (int i = 0; i < 3; i++) {
            ids[i] = UUID.randomUUID();

            SavedScan scan = new SavedScan(
                    ids[i],
                    "Export Scan " + i,
                    LocalDateTime.of(2026, 1, 1, 12, 0).plusDays(i),
                    hardware,
                    false
            );

            repository.save(scan);
        }

        // Confirm that the source history contains three scans.
        List<SavedScan> originalScans = repository.loadAll();
        assertEquals(3, originalScans.size());

        // Export history to a separate CSV file.
        repository.exportTo(exportPath);

        assertTrue(Files.exists(exportPath));
        assertTrue(Files.size(exportPath) > 0);

        // Export must contain the same CSV content as the source.
        assertEquals(
                Files.readString(csvPath),
                Files.readString(exportPath)
        );

        // Load the exported CSV using a separate repository instance.
        ScanHistoryCsvRepository exportedRepository =
                new ScanHistoryCsvRepository(exportPath);

        List<SavedScan> exportedScans = exportedRepository.loadAll();

        assertEquals(3, exportedScans.size());

        // Check that all exported records retain their UUIDs and names.
        assertEquals(
                originalScans.stream().map(SavedScan::getId).toList(),
                exportedScans.stream().map(SavedScan::getId).toList()
        );

        assertEquals(
                originalScans.stream().map(SavedScan::getName).toList(),
                exportedScans.stream().map(SavedScan::getName).toList()
        );

        // Confirm that each expected scan is present.
        for (int i = 0; i < 3; i++) {
            final UUID expectedId = ids[i];

            assertTrue(exportedScans.stream()
                    .anyMatch(scan -> scan.getId().equals(expectedId)));
        }
    }

    // UT-HIS-011 — Empty History Export
    @Test
    void utHis011_exportsEmptyHistoryWithoutFailure() throws IOException {

        Path csvPath = tempDir.resolve("empty-history.csv");
        Path exportPath = tempDir.resolve("empty-export.csv");

        ScanHistoryCsvRepository repository =
                new ScanHistoryCsvRepository(csvPath);

        // Start with no saved scans and no existing CSV file.
        assertFalse(Files.exists(csvPath));
        assertTrue(repository.loadAll().isEmpty());

        // Exporting empty history must not throw an exception.
        assertDoesNotThrow(() -> repository.exportTo(exportPath));

        // Both the source history and exported CSV should exist.
        assertTrue(Files.exists(csvPath));
        assertTrue(Files.exists(exportPath));

        // The exported file must contain no scan records.
        assertEquals(0L, Files.size(exportPath));

        // Confirm that loading the exported file returns no scans.
        ScanHistoryCsvRepository exportedRepository =
                new ScanHistoryCsvRepository(exportPath);

        assertTrue(exportedRepository.loadAll().isEmpty());
    }
}
