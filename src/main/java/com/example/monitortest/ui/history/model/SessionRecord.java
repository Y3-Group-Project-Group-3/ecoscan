package com.example.monitortest.ui.history.model;

import java.time.LocalDateTime;
import java.util.Objects;

// Represents a single Live Monitor session.
// A session starts when EcoScan opens and ends when it is closed.
public class SessionRecord {

    // Date and time the session started.
    private final LocalDateTime startedAt;

    // Total estimated emissions for the session in grams of CO2e.
    private final double totalEmissionsGrams;

    // Creates a session record from its start time and total emissions.
    public SessionRecord(
            LocalDateTime startedAt,
            double totalEmissionsGrams
    ) {

        // Check that a start time has been provided.
        this.startedAt = Objects.requireNonNull(startedAt);

        this.totalEmissionsGrams = totalEmissionsGrams;
    }

    // Returns the date and time the session started.
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    // Returns the total emissions for the session.
    public double getTotalEmissionsGrams() {
        return totalEmissionsGrams;
    }
}