package com.example.monitortest.hardware.monitoring.emissions;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;

/**
 * Tracks estimated carbon emissions across live power measurements.
 * Uses the average power between consecutive readings to estimate electricity consumption over each measured time interval.
 */
public class CarbonEmissionsTracker {

    private final CarbonEmissionsCalculator calculator;

    private double totalEmissionsGrams = 0.0;
    private double monitoredSeconds = 0.0;

    private double previousPower = Double.NaN;
    private long previousTimestamp = -1;

    public CarbonEmissionsTracker(CarbonEmissionsCalculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Processes a new power snapshot and accumulates estimated emissions.
     *
     * @param snapshot Latest CPU and GPU power measurements.
     * @param timestamp Timestamp in milliseconds.
     */
    public void addSnapshot(LiveMonitorSnapshot snapshot, long timestamp) {

        double currentPower = snapshot.totalPower();

        // Missing readings cannot be used for emissions calculations.
        if (!Double.isFinite(currentPower) || currentPower < 0) {
            previousPower = Double.NaN;
            previousTimestamp = -1;
            return;
        }

        // The first valid snapshot establishes the starting point.
        if (previousTimestamp < 0) {
            previousPower = currentPower;
            previousTimestamp = timestamp;
            return;
        }

        // Calculate the actual time between consecutive measurements.
        double durationSeconds = (timestamp - previousTimestamp) / 1000.0;

        if (durationSeconds <= 0) {
            return;
        }

        // Estimate average power during this interval.
        double averagePower = (previousPower + currentPower) / 2.0;

        // Convert average power and elapsed time into estimated emissions.
        double emissions = calculator.calculateEmissions( averagePower, durationSeconds );

        totalEmissionsGrams += emissions;
        monitoredSeconds += durationSeconds;

        // Retain this reading for the next interval.
        previousPower = currentPower;
        previousTimestamp = timestamp;
    }


    // Returns the accumulated estimated emissions in grams of CO2e.
    public double getTotalEmissionsGrams() {
        return totalEmissionsGrams;
    }


    // Returns the average emissions per monitored second.
    public double getAverageEmissionsPerSecond() {

        if (monitoredSeconds == 0) {
            return 0.0;
        }

        return totalEmissionsGrams / monitoredSeconds;
    }
}
