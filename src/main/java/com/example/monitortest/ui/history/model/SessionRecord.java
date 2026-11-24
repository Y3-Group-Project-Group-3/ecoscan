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

    // Most recent electricity carbon intensity when the session ended.
    // NaN indicates that the value was not recorded in an older session.
    private final double carbonIntensityGramsPerKwh;


    // Creates a session record without a recorded length.
    public SessionRecord( LocalDateTime startedAt, double totalEmissionsGrams) {
        this(startedAt, totalEmissionsGrams, 0);
    }


    // Creates a session without a recorded carbon intensity.
    // Preserves compatibility with existing session records.
    public SessionRecord( LocalDateTime startedAt, double totalEmissionsGrams, double durationSeconds ) {
        this(startedAt, totalEmissionsGrams, durationSeconds, Double.NaN);
    }


    // Creates a session containing its latest recorded carbon intensity.
    public SessionRecord( LocalDateTime startedAt, double totalEmissionsGrams, double durationSeconds, double carbonIntensityGramsPerKwh) {

        // Require a valid session start time.
        this.startedAt = Objects.requireNonNull(startedAt);

        // Store the recorded emissions and duration.
        this.totalEmissionsGrams = totalEmissionsGrams;
        this.durationSeconds = durationSeconds;

        // Store the final carbon intensity reading, if available.
        // NaN is reserved for sessions without a recorded value.
        if (Double.isNaN(carbonIntensityGramsPerKwh)) {
            this.carbonIntensityGramsPerKwh = Double.NaN;
        } else if (!Double.isFinite(carbonIntensityGramsPerKwh) || carbonIntensityGramsPerKwh < 0) {
            throw new IllegalArgumentException( "Carbon intensity must be non-negative and finite." );
        } else {
            this.carbonIntensityGramsPerKwh = carbonIntensityGramsPerKwh;
        }
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

    // Returns the latest carbon intensity recorded for the session.
    public double getCarbonIntensityGramsPerKwh() { return carbonIntensityGramsPerKwh; }

    // Indicates whether this session contains a recorded intensity.
    public boolean hasCarbonIntensity() { return Double.isFinite(carbonIntensityGramsPerKwh); }

}