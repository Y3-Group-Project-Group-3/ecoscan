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
    // Return combined power only when both readings are available.
    public double totalPower() {
        if (!Double.isFinite(cpuPower) || !Double.isFinite(gpuPower)) {
            return Double.NaN;
        }

        return cpuPower + gpuPower;
    }
}
