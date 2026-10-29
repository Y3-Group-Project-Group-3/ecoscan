
package com.example.monitortest.model;

/**
 * Stores hardware specifications and sensor measurements,
 * including whether CPU power is simulated.
 */
public class HardwareInfo {

    private final String cpuName;
    private final double cpuTemperature;
    private final double cpuPower;
    private final long totalMemory;

    // Identifies whether CPU power is simulated.
    private final boolean cpuPowerSimulated;

    public HardwareInfo(
            String cpuName,
            double cpuTemperature,
            double cpuPower,
            long totalMemory,
            boolean cpuPowerSimulated
    ) {
        this.cpuName = cpuName;
        this.cpuTemperature = cpuTemperature;
        this.cpuPower = cpuPower;
        this.totalMemory = totalMemory;
        this.cpuPowerSimulated = cpuPowerSimulated;
    }

    public String getCpuName() {
        return cpuName;
    }

    public double getCpuTemperature() {
        return cpuTemperature;
    }

    public double getCpuPower() {
        return cpuPower;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * Indicates whether the CPU power value is simulated.
     * @return true for simulated readings, false otherwise.
     */
    public boolean isCpuPowerSimulated() {
        return cpuPowerSimulated;
    }
}
