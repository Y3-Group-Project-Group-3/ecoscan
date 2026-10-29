package com.example.monitortest.hardware.power;

import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;

/**
 * Provides approximate power consumption estimates for RAM modules and physical storage devices.
 *
 * These values are fixed assumptions based on hardware categories, not live measurements of the installed components.
 *
 * Actual consumption varies by device, capacity and workload.
 * Estimates are intended for EcoScan's component-level power
 * breakdown and should not be treated as precise measurements.
 */
public final class PowerEstimates {

    // Utility class: prevent unnecessary instances.
    private PowerEstimates() {
    }

    /**
     * Estimates the power consumption of one physical RAM module
     * using its DDR generation.
     *
     * @param module Physical RAM module to estimate.
     * @return Estimated watts, or NaN for unsupported memory types.
     */
    public static double estimateMemoryModule(MemoryModule module) {

        if (module == null || module.getMemoryType() == null) {
            return Double.NaN;
        }

        return switch (module.getMemoryType().trim().toUpperCase()) {
            case "DDR2" -> 4.0;
            case "DDR3" -> 3.0;
            case "DDR4" -> 2.5;
            case "DDR5" -> 3.5;
            default -> Double.NaN;
        };
    }


    /**
     * Estimates the power consumption of a physical storage device
     * using its SSD or HDD classification.
     *
     * @param device Physical storage device to estimate.
     * @return Estimated watts, or NaN for unsupported device types.
     */

    public static double estimateStorageDevice(StorageDevice device) {

        if (device == null || device.getType() == null) {
            return Double.NaN;
        }

        return switch (device.getType().trim().toUpperCase()) {
            case "SSD" -> 2.5;
            case "HDD" -> 5.0;
            default -> Double.NaN;
        };
    }
}
