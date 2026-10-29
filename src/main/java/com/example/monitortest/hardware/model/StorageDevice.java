package com.example.monitortest.hardware.model;
// Represents a physical storage device detected by OSHI.
public class StorageDevice {

    private final String model;
    private final long capacityBytes;

    // Identifies the physical drive type for power estimation.
    private final String type;

    /**
     * Creates a storage device with its model and capacity.
     *
     * @param model storage device model
     * @param capacityBytes total capacity in bytes
     */

    // Creates a physical storage device with its model, capacity and type.
    public StorageDevice(String model, long capacityBytes, String type) {
        this.model = model;
        this.capacityBytes = capacityBytes;
        this.type = type;
    }


    // Supports existing code that does not yet provide a drive type.
    public StorageDevice(String model, long capacityBytes) {
        this(model, capacityBytes, "UNKNOWN");
    }


    public String getModel() {
        return model;
    }

    // Returns SSD, HDD or UNKNOWN.
    public String getType() {
        return type;
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

     // Converts storage capacity from bytes to decimal GB.
    public double getCapacityGB() {
        return capacityBytes / 1_000_000_000.0;
    }
}
