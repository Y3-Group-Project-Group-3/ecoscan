package com.example.monitortest.ui.history.model;

import com.example.monitortest.hardware.model.HardwareInfo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

// Represents a single hardware scan saved by the user.
// Stores the scan information independently of the Results Page.
public class SavedScan {

    // Unique ID used to identify the scan in CSV storage.
    private final UUID id;

    // Name chosen by the user when saving the scan.
    private final String name;

    // Original date and time of the hardware scan.
    private final LocalDateTime scannedAt;

    // Hardware information captured during the scan.
    private final HardwareInfo hardwareInfo;

    // Power calculation mode selected when the scan was saved.
    private final boolean includeEstimates;

    // Creates a saved scan from its original hardware information.
    public SavedScan(
            UUID id,
            String name,
            LocalDateTime scannedAt,
            HardwareInfo hardwareInfo,
            boolean includeEstimates
    ) {

        // Check that required information has been provided.
        this.id = Objects.requireNonNull(id);
        this.scannedAt = Objects.requireNonNull(scannedAt);
        this.hardwareInfo = Objects.requireNonNull(hardwareInfo);

        // Prevent scans from being saved without a valid name.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Scan name cannot be empty."
            );
        }

        this.name = name.trim();
        this.includeEstimates = includeEstimates;
    }

    // Returns the unique identifier of the saved scan.
    public UUID getId() {
        return id;
    }

    // Returns the name chosen by the user.
    public String getName() {
        return name;
    }

    // Returns the original scan timestamp.
    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    // Returns the saved hardware information.
    public HardwareInfo getHardwareInfo() {
        return hardwareInfo;
    }

    // Indicates whether estimated power was included.
    public boolean isIncludeEstimates() {
        return includeEstimates;
    }
}
