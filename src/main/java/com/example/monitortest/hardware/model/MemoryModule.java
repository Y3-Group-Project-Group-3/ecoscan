package com.example.monitortest.hardware.model;

// Represents an individual physical RAM module detected by OSHI.
public class MemoryModule {

    private final String manufacturer;
    private final String memoryType;
    private final long capacityBytes;
    private final long clockSpeedHz;

    // Store the specifications of an individual memory module.
    public MemoryModule(String manufacturer, String memoryType, long capacityBytes, long clockSpeedHz) {
        this.manufacturer = manufacturer;
        this.memoryType = memoryType;
        this.capacityBytes = capacityBytes;
        this.clockSpeedHz = clockSpeedHz;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

    public long getClockSpeedHz() {
        return clockSpeedHz;
    }

    // Convert the module capacity from bytes to binary gigabytes.
    public double getCapacityGB() {
        return capacityBytes / (1024.0 * 1024 * 1024);
    }

    // Convert the reported clock frequency from Hz to MHz.
    public double getClockSpeedMHz() {
        return clockSpeedHz / 1_000_000.0;
    }
}
