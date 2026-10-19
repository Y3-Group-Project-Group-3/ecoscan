package com.example.monitortest.model;

public class HardwareInfo {

    private final String cpuName;
    private final double cpuTemperature;
    private final double cpuPower;
    private final long totalMemory;

    public HardwareInfo(
            String cpuName,
            double cpuTemperature,
            double cpuPower,
            long totalMemory
    ) {
        this.cpuName = cpuName;
        this.cpuTemperature = cpuTemperature;
        this.cpuPower = cpuPower;
        this.totalMemory = totalMemory;
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
}