package com.example.monitortest.hardware.monitoring;

/**
 * Stores a single set of live CPU and GPU power measurements.
 * Power values are expressed in watts.
 * Double.NaN represents an unavailable sensor reading.
 */
public record LiveMonitorSnapshot(
        double cpuPower,
        double gpuPower
) {

    // Returns the sum of available, valid power readings.
    // Missing sensors do not prevent emissions estimation.
    public double totalPower() {

        // Treat negative or non-finite readings as unavailable.
        boolean cpuAvailable = Double.isFinite(cpuPower) && cpuPower >= 0;
        boolean gpuAvailable = Double.isFinite(gpuPower) && gpuPower >= 0;

        // No valid sensors means power consumption cannot be estimated.
        if (!cpuAvailable && !gpuAvailable) {
            return Double.NaN;
        }

        // Begin with zero and add only available measurements.
        double total = 0.0;

        if (cpuAvailable) {
            total += cpuPower;
        }

        if (gpuAvailable) {
            total += gpuPower;
        }

        return total;
    }

}
