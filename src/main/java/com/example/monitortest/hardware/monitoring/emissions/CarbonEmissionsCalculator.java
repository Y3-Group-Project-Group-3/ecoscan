package com.example.monitortest.hardware.monitoring.emissions;

/**
 * Calculates estimated carbon emissions from electrical power consumption.
 * Uses a configurable carbon intensity expressed in grams of CO2e per kilowatt-hour of electricity consumed.
 */
public class CarbonEmissionsCalculator {

    // Illustrative fixed carbon intensity for the initial implementation.
    private static final double DEFAULT_CARBON_INTENSITY = 200.0;

    private double carbonIntensity;

    public CarbonEmissionsCalculator() {
        this(DEFAULT_CARBON_INTENSITY);
    }

    public CarbonEmissionsCalculator(double carbonIntensity) {
        setCarbonIntensity(carbonIntensity);
    }

    /**
     * Converts a power reading over a time interval into grams of CO2e.
     *
     * @param powerWatts Electrical power consumption in watts.
     * @param durationSeconds Duration of consumption in seconds.
     * @return Estimated emissions in grams of CO2e.
     */
    public double calculateEmissions(double powerWatts, double durationSeconds) {

        // Reject invalid sensor readings and time intervals.
        if (!Double.isFinite(powerWatts) || powerWatts < 0 || !Double.isFinite(durationSeconds) || durationSeconds < 0) {
            return Double.NaN;
        }

        // Convert watt-seconds into kilowatt-hours.
        double energyKWh = (powerWatts * durationSeconds) / 3_600_000.0;

        // Apply the configured carbon intensity.
        return energyKWh * carbonIntensity;
    }


    // Calculates the time required to reach an emissions milestone
    // assuming constant hardware power consumption.
    public double calculateTimeToEmissions(double powerWatts, double targetGrams) {

        // Reject unavailable or invalid power readings and milestones.
        if (!Double.isFinite(powerWatts) || powerWatts <= 0 || !Double.isFinite(targetGrams) || targetGrams < 0) {
            return Double.NaN;
        }

        // A positive milestone cannot be reached with zero carbon intensity.
        if (carbonIntensity <= 0) {
            return Double.NaN;
        }

        // Rearrange the existing emissions formula to calculate time.
        return (targetGrams * 3_600_000.0) / (powerWatts * carbonIntensity);
    }


    /**
     * Updates the carbon intensity used for future calculations.
     * This can later accept a value retrieved from an API.
     */
    public void setCarbonIntensity(double carbonIntensity) {

        if (!Double.isFinite(carbonIntensity) || carbonIntensity < 0) {
            throw new IllegalArgumentException(
                    "Carbon intensity must be a non-negative finite value."
            );
        }

        this.carbonIntensity = carbonIntensity;
    }

    public double getCarbonIntensity() {
        return carbonIntensity;
    }
}
