package com.example.monitortest.ui.history.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Tests carbon intensity storage and backwards compatibility for saved Live Monitor session records.
class SessionRecordTest {

    // HIS-UT-032
    // Verify that new sessions retain their recorded carbon intensity.
    @Test
    @DisplayName("HIS-UT-032 - Stores carbon intensity for a new session")
    void storesCarbonIntensity() {

        // Create a session with a recorded intensity of 151 g/kWh.
        SessionRecord session = new SessionRecord(
                LocalDateTime.of(2026, 11, 24, 12, 0),
                0.25,
                60.0,
                151.0
        );

        // Verify that the value is available and stored correctly.
        assertTrue(session.hasCarbonIntensity());
        assertEquals(151.0, session.getCarbonIntensityGramsPerKwh(), 0.001);
    }

    // HIS-UT-033
    // Verify that older session constructors produce an unrecorded intensity.
    @Test
    @DisplayName("HIS-UT-033 - Supports sessions without recorded carbon intensity")
    void supportsLegacySessions() {

        // Create a session using the original three-argument constructor.
        SessionRecord session = new SessionRecord(
                LocalDateTime.of(2026, 11, 24, 12, 0),
                0.25,
                60.0
        );

        // An older session should not claim to have an intensity reading.
        assertFalse(session.hasCarbonIntensity());
        assertTrue(Double.isNaN(session.getCarbonIntensityGramsPerKwh()));
    }

    // HIS-UT-034
    // Reject impossible negative carbon intensity readings.
    @Test
    @DisplayName("HIS-UT-034 - Rejects invalid negative carbon intensity")
    void rejectsNegativeIntensity() {

        // Negative carbon intensity cannot represent a valid reading.
        assertThrows(
                IllegalArgumentException.class,
                () -> new SessionRecord(
                        LocalDateTime.of(2026, 11, 24, 12, 0),
                        0.25,
                        60.0,
                        -10.0
                )
        );
    }
}
