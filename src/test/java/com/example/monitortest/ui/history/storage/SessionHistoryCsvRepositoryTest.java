package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SessionRecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Verifies that Live Monitor sessions can be loaded from
// both historical and current CSV file formats.
class SessionHistoryCsvRepositoryTest {

    // JUnit creates a temporary directory for each test.
    // This prevents tests from modifying EcoScan's real session history.
    @TempDir
    Path tempDir;

    // HIS-UT-035
    // Verify that original two-column sessions remain readable.
    @Test
    @DisplayName("HIS-UT-035 - Loads legacy sessions without duration or intensity")
    void loadsLegacyTwoColumnSession() throws IOException {

        // Create a CSV using the original session format.
        Path csvPath = tempDir.resolve("sessions.csv");

        Files.writeString(csvPath, "startedAt,totalEmissionsGrams\n" + "2026-11-24T12:00,0.25\n" );

        // Load the historical session.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository(csvPath);

        List<SessionRecord> sessions = repository.loadAll();

        // Verify that the original data is retained.
        assertEquals(1, sessions.size());
        assertEquals(0.25, sessions.get(0).getTotalEmissionsGrams(), 0.001);

        // Neither duration nor carbon intensity was recorded.
        assertFalse(sessions.get(0).hasDuration());
        assertFalse(sessions.get(0).hasCarbonIntensity());
    }

    // HIS-UT-036
    // Verify that three-column sessions retain their recorded duration.
    @Test
    @DisplayName("HIS-UT-036 - Loads legacy sessions with duration but no intensity")
    void loadsLegacyThreeColumnSession() throws IOException {

        // Create a CSV containing the previous session format.
        Path csvPath = tempDir.resolve("sessions.csv");

        Files.writeString(csvPath, "startedAt,totalEmissionsGrams,durationSeconds\n" + "2026-11-24T12:00,0.25,60.0\n" );

        // Load the historical session.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository(csvPath);

        List<SessionRecord> sessions = repository.loadAll();

        // Verify that the duration is preserved.
        assertEquals(1, sessions.size());
        assertEquals(60.0, sessions.get(0).getDurationSeconds(), 0.001);

        // Carbon intensity was not available in this CSV format.
        assertFalse(sessions.get(0).hasCarbonIntensity());
    }

    // HIS-UT-037
    // Verify that the new four-column CSV format restores carbon intensity.
    @Test
    @DisplayName("HIS-UT-037 - Loads sessions with recorded carbon intensity")
    void loadsFourColumnSession() throws IOException {

        // Create a CSV containing a recorded intensity value.
        Path csvPath = tempDir.resolve("sessions.csv");

        Files.writeString(csvPath,
                "startedAt,totalEmissionsGrams,durationSeconds,carbonIntensityGramsPerKwh\n"
                        + "2026-11-24T12:00,0.25,60.0,151.0\n"
        );

        // Load the session using the updated repository.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository(csvPath);

        List<SessionRecord> sessions = repository.loadAll();

        // Verify that the new field is available and correct.
        assertEquals(1, sessions.size());
        assertTrue(sessions.get(0).hasCarbonIntensity());
        assertEquals( 151.0, sessions.get(0).getCarbonIntensityGramsPerKwh(), 0.001 );
    }


    // HIS-UT-038
    // Verify that a new session retains its carbon intensity
    // after being saved to CSV and loaded again.
    @Test
    @DisplayName("HIS-UT-038 - Preserves carbon intensity during CSV round-trip")
    void preservesCarbonIntensityDuringCsvRoundTrip() throws IOException {

        // Create a temporary sessions CSV repository.
        Path csvPath = tempDir.resolve("sessions.csv");
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository(csvPath);

        // Create a session containing a valid carbon intensity reading.
        SessionRecord original = new SessionRecord(
                LocalDateTime.of(2026, 11, 24, 12, 0),
                0.25,
                60.0,
                151.0
        );

        // Save the session using the updated CSV format.
        repository.save(original);

        // Reload the saved records from disk.
        List<SessionRecord> sessions = repository.loadAll();

        // Verify that exactly one session was restored.
        assertEquals(1, sessions.size());

        SessionRecord restored = sessions.get(0);

        // Verify the recorded session information was preserved.
        assertEquals(original.getStartedAt(), restored.getStartedAt());
        assertEquals(0.25, restored.getTotalEmissionsGrams(), 0.0001);
        assertEquals(60.0, restored.getDurationSeconds(), 0.001);

        // Verify carbon intensity survived the CSV round-trip.
        assertTrue(restored.hasCarbonIntensity());
        assertEquals( 151.0, restored.getCarbonIntensityGramsPerKwh(), 0.001 );
    }


    // HIS-UT-039
    // Verify that older sessions remain readable after the
    // repository rewrites history using the new CSV format.
    @Test
    @DisplayName("HIS-UT-039 - Preserves legacy sessions when rewriting CSV")
    void preservesLegacySessionsWhenRewritingCsv() throws IOException {

        // Create a temporary CSV containing a legacy three-column session.
        Path csvPath = tempDir.resolve("sessions.csv");

        Files.writeString( csvPath, "startedAt,totalEmissionsGrams,durationSeconds\n" + "2026-11-23T12:00,0.50,120.0\n" );

        // Load the existing history and append a new session.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository(csvPath);

        repository.save(new SessionRecord(
                LocalDateTime.of(2026, 11, 24, 12, 0),
                0.25,
                60.0,
                151.0
        ));

        // Reload both sessions after the file has been rewritten.
        List<SessionRecord> sessions = repository.loadAll();

        // Verify that neither session was lost.
        assertEquals(2, sessions.size());

        // The first session should retain its original duration and emissions.
        SessionRecord legacy = sessions.get(0);

        assertEquals(0.50, legacy.getTotalEmissionsGrams(), 0.001);
        assertEquals(120.0, legacy.getDurationSeconds(), 0.001);

        // The legacy session must not be assigned an invented intensity.
        assertFalse(legacy.hasCarbonIntensity());

        // The new session should retain its recorded intensity.
        SessionRecord recent = sessions.get(1);

        assertTrue(recent.hasCarbonIntensity());
        assertEquals( 151.0, recent.getCarbonIntensityGramsPerKwh(), 0.001 );
    }


}
