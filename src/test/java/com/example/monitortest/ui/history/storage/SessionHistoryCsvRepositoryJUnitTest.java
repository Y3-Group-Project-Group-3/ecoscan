
package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SessionRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionHistoryCsvRepositoryJUnitTest {

    @TempDir
    Path tempDir;

    // UT-HIS-012 — Session History: Missing File
    @Test
    void utHis012_missingSessionHistoryFileReturnsEmptyList()
            throws IOException {

        Path csvPath = tempDir.resolve("sessions.csv");

        // Confirm that the session history file does not exist.
        assertFalse(Files.exists(csvPath));

        SessionHistoryCsvRepository repository =
                new SessionHistoryCsvRepository(csvPath);

        // Loading missing session history should return an empty list.
        List<SessionRecord> sessions = repository.loadAll();

        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());

        // Reading missing history must not create a CSV file.
        assertFalse(Files.exists(csvPath));
    }

    // UT-HIS-013 — Session History: Invalid Record
    @Test
    void utHis013_skipsInvalidSessionRecords() throws IOException {

        Path csvPath = tempDir.resolve("invalid-sessions.csv");

        // Prepare a CSV containing valid and invalid session records.
        String csvContent = String.join("\n",
                "startedAt,totalEmissionsGrams",
                "2026-11-18T10:00:00,12.5000",
                "invalid-date,15.0000",
                "2026-11-18T11:00:00,invalid-number",
                "2026-11-18T12:00:00",
                "2026-11-18T13:00:00,25.7500"
        ) + "\n";

        Files.writeString(
                csvPath,
                csvContent,
                StandardCharsets.UTF_8
        );

        SessionHistoryCsvRepository repository =
                new SessionHistoryCsvRepository(csvPath);

        // Invalid rows should be skipped without stopping the load.
        List<SessionRecord> sessions =
                assertDoesNotThrow(repository::loadAll);

        // Only the two valid session records should be returned.
        assertNotNull(sessions);
        assertEquals(2, sessions.size());

        // Verify the first valid session.
        assertEquals(
                LocalDateTime.of(2026, 11, 18, 10, 0),
                sessions.get(0).getStartedAt()
        );

        assertEquals(
                12.5000,
                sessions.get(0).getTotalEmissionsGrams(),
                0.0001
        );

        // Verify the second valid session.
        assertEquals(
                LocalDateTime.of(2026, 11, 18, 13, 0),
                sessions.get(1).getStartedAt()
        );

        assertEquals(
                25.7500,
                sessions.get(1).getTotalEmissionsGrams(),
                0.0001
        );

        // Reading must not modify the original CSV file.
        assertEquals(
                csvContent,
                Files.readString(csvPath, StandardCharsets.UTF_8)
        );
    }

    // UT-HIS-014 — Import Valid Sessions
    @Test
    void utHis014_importsValidSessions() throws IOException {

        Path csvPath = tempDir.resolve("local-sessions.csv");
        Path importPath = tempDir.resolve("import-sessions.csv");

        SessionHistoryCsvRepository repository =
                new SessionHistoryCsvRepository(csvPath);

        // Save an existing session before importing.
        repository.save(new SessionRecord(
                LocalDateTime.of(2026, 11, 17, 9, 0),
                5.2500
        ));

        // Prepare an external CSV file with two valid sessions.
        String importContent = String.join("\n",
                "startedAt,totalEmissionsGrams",
                "2026-11-18T10:00:00,12.5000",
                "2026-11-18T13:00:00,25.7500"
        ) + "\n";

        Files.writeString(
                importPath,
                importContent,
                StandardCharsets.UTF_8
        );

        // Import the external sessions into existing history.
        int importedCount = repository.importFrom(importPath);

        assertEquals(2, importedCount);

        // The existing session and both imported sessions must remain.
        List<SessionRecord> sessions = repository.loadAll();

        assertEquals(3, sessions.size());

        // Verify the original session.
        assertEquals(
                LocalDateTime.of(2026, 11, 17, 9, 0),
                sessions.get(0).getStartedAt()
        );
        assertEquals(
                5.2500,
                sessions.get(0).getTotalEmissionsGrams(),
                0.0001
        );

        // Verify the first imported session.
        assertEquals(
                LocalDateTime.of(2026, 11, 18, 10, 0),
                sessions.get(1).getStartedAt()
        );
        assertEquals(
                12.5000,
                sessions.get(1).getTotalEmissionsGrams(),
                0.0001
        );

        // Verify the second imported session.
        assertEquals(
                LocalDateTime.of(2026, 11, 18, 13, 0),
                sessions.get(2).getStartedAt()
        );
        assertEquals(
                25.7500,
                sessions.get(2).getTotalEmissionsGrams(),
                0.0001
        );

        // Importing must not modify the external source CSV.
        assertEquals(
                importContent,
                Files.readString(importPath, StandardCharsets.UTF_8)
        );

        // Confirm that the three records persist on disk.
        SessionHistoryCsvRepository reloadedRepository =
                new SessionHistoryCsvRepository(csvPath);

        assertEquals(3, reloadedRepository.loadAll().size());
    }

    // UT-HIS-015 — Import Invalid Sessions
    @Test
    void utHis015_invalidImportPreservesExistingHistory()
            throws IOException {

        Path csvPath = tempDir.resolve("existing-sessions.csv");
        Path importPath = tempDir.resolve("invalid-import.csv");

        SessionHistoryCsvRepository repository =
                new SessionHistoryCsvRepository(csvPath);

        LocalDateTime originalDate =
                LocalDateTime.of(2026, 11, 17, 9, 0);

        double originalEmissions = 5.2500;

        // Save a valid session before attempting the invalid import.
        repository.save(new SessionRecord(
                originalDate,
                originalEmissions
        ));

        List<SessionRecord> before = repository.loadAll();

        assertEquals(1, before.size());

        // Prepare an import file containing invalid dates and numbers.
        String invalidContent = String.join("\n",
                "startedAt,totalEmissionsGrams",
                "invalid-date,12.5000",
                "2026-11-18T11:00:00,invalid-number"
        ) + "\n";

        Files.writeString(
                importPath,
                invalidContent,
                StandardCharsets.UTF_8
        );

        // Invalid records must not be imported.
        int importedCount =
                assertDoesNotThrow(() -> repository.importFrom(importPath));

        assertEquals(0, importedCount);

        // The existing history must remain unchanged.
        List<SessionRecord> after = repository.loadAll();

        assertEquals(1, after.size());

        assertEquals(
                originalDate,
                after.get(0).getStartedAt()
        );

        assertEquals(
                originalEmissions,
                after.get(0).getTotalEmissionsGrams(),
                0.0001
        );

        // Confirm that the original session remains after reloading.
        SessionHistoryCsvRepository reloadedRepository =
                new SessionHistoryCsvRepository(csvPath);

        List<SessionRecord> reloaded =
                reloadedRepository.loadAll();

        assertEquals(1, reloaded.size());

        assertEquals(
                originalDate,
                reloaded.get(0).getStartedAt()
        );

        assertEquals(
                originalEmissions,
                reloaded.get(0).getTotalEmissionsGrams(),
                0.0001
        );

        // Importing must not modify the invalid source file.
        assertEquals(
                invalidContent,
                Files.readString(importPath, StandardCharsets.UTF_8)
        );
    }

    // UT-HIS-016 — Clear Session History
    @Test
    void utHis016_clearsSessionHistory() throws IOException {

        Path csvPath = tempDir.resolve("clear-sessions.csv");

        SessionHistoryCsvRepository repository =
                new SessionHistoryCsvRepository(csvPath);

        // Save three sessions before clearing the history.
        repository.save(new SessionRecord(
                LocalDateTime.of(2026, 11, 18, 9, 0),
                5.2500
        ));

        repository.save(new SessionRecord(
                LocalDateTime.of(2026, 11, 18, 10, 0),
                12.5000
        ));

        repository.save(new SessionRecord(
                LocalDateTime.of(2026, 11, 18, 11, 0),
                25.7500
        ));

        // Confirm that all three sessions were saved.
        assertTrue(Files.exists(csvPath));
        assertEquals(3, repository.loadAll().size());

        // Clear the session history.
        repository.deleteAll();

        // Clearing history should remove the CSV file.
        assertFalse(Files.exists(csvPath));

        // Loading history after deletion should return no sessions.
        List<SessionRecord> remaining = repository.loadAll();

        assertNotNull(remaining);
        assertTrue(remaining.isEmpty());

        // Verify that the cleared state persists in a new repository.
        SessionHistoryCsvRepository reloadedRepository =
                new SessionHistoryCsvRepository(csvPath);

        assertTrue(reloadedRepository.loadAll().isEmpty());

        // Reading the cleared history must not recreate the CSV file.
        assertFalse(Files.exists(csvPath));
    }
}
