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

    // How long the session lasted in seconds.
    // Zero when the length was not recorded, such as sessions saved before this was added.
    private final double durationSeconds;

    // Creates a session record without a recorded length.
    public SessionRecord(
            LocalDateTime startedAt,
            double totalEmissionsGrams
    ) {
        this(startedAt, totalEmissionsGrams, 0);
    }

    // Creates a session record from its start time, total emissions and length.
    public SessionRecord(
            LocalDateTime startedAt,
            double totalEmissionsGrams,
            double durationSeconds
    ) {

        // Check that a start time has been provided.
        this.startedAt = Objects.requireNonNull(startedAt);

        this.totalEmissionsGrams = totalEmissionsGrams;
        this.durationSeconds = durationSeconds;
    }

    // Returns the date and time the session started.
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    // Returns the total emissions for the session.
    public double getTotalEmissionsGrams() {
        return totalEmissionsGrams;
    }

    // Returns how long the session lasted in seconds.
    public double getDurationSeconds() {
        return durationSeconds;
    }

    // Returns true when the session's length was recorded.
    public boolean hasDuration() {
        return durationSeconds > 0;
    }
}