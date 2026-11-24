package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the Live Monitor Sessions list on the History page.
// Uses a temporary folder so the real saved sessions are never changed.
class SessionListSectionTest {

    @TempDir
    Path tempDir;

    private String originalHome;

    // Point EcoScan at an empty temporary home folder.
    @BeforeEach
    void setUp() {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
    }

    // Restore the real home folder after each test.
    @AfterEach
    void tearDown() {
        System.setProperty("user.home", originalHome);
    }

    // HIS-UT-026
    // Verify that each session shows its own average emissions per second.
    @Test
    @DisplayName("HIS-UT-026 - Shows the average emissions per second for a session")
    void showsAverageEmissions() throws IOException {

        // Save a session of 0.12 g over 60 seconds.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("EcoScan").resolve("sessions.csv") );

        repository.save( new SessionRecord(LocalDateTime.of(2026, 11, 18, 16, 48), 0.12, 60) );

        SessionListSection section = new SessionListSection();

        // Expected: 0.12 / 60 = 0.002 g per second.
        assertNotNull( findLabel(section, "0.002000 g CO₂e/s") );
    }

    // HIS-UT-027
    // Verify that older sessions without a recorded length show "Not recorded".
    @Test
    @DisplayName("HIS-UT-027 - Shows Not recorded for sessions without a length")
    void showsNotRecordedForOlderSessions() throws IOException {

        // Save a session without a length, like those saved before the length was recorded.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("EcoScan").resolve("sessions.csv") );

        repository.save( new SessionRecord(LocalDateTime.of(2026, 11, 18, 16, 48), 0.12) );

        SessionListSection section = new SessionListSection();

        // There is no length to divide by, so no average can be shown.
        assertNotNull( findLabel(section, "Not recorded") );
    }
}