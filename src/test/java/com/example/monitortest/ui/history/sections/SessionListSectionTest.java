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
    // Verify that the average emissions per session is calculated and shown.
    @Test
    @DisplayName("HIS-UT-026 - Shows the average emissions per session")
    void showsAverageEmissions() throws IOException {

        // Save two sessions of 0.12 g and 0.02 g.
        SessionHistoryCsvRepository repository = new SessionHistoryCsvRepository( tempDir.resolve("EcoScan").resolve("sessions.csv") );

        repository.save( new SessionRecord(LocalDateTime.of(2026, 11, 18, 16, 48), 0.12) );
        repository.save( new SessionRecord(LocalDateTime.of(2026, 11, 18, 17, 22), 0.02) );

        SessionListSection section = new SessionListSection();

        // Expected: (0.12 + 0.02) / 2 = 0.07 g.
        assertNotNull( findLabel(section, "Average per session: 0.07 g CO₂e") );
    }

    // HIS-UT-027
    // Verify that no average is shown when there are no saved sessions.
    @Test
    @DisplayName("HIS-UT-027 - Shows no average when there are no saved sessions")
    void showsNoAverageWithoutSessions() {

        SessionListSection section = new SessionListSection();

        // There is nothing to average, so the label should be empty.
        assertNull( findLabelContaining(section, "Average per session") );
    }
}