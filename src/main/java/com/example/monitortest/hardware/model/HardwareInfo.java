package com.example.monitortest.hardware.model;

import com.example.monitortest.hardware.power.PowerEstimates;

import java.util.List;

/**
 * Stores hardware specifications and sensor measurements,
 * including whether CPU power is simulated.
 */
public class HardwareInfo {

    private final String cpuName;
    private final double cpuTemperature;
    private final double cpuPower;
    private final long totalMemory;

    private final String gpuName;
    private final String gpuVendor;
    private final long gpuVram;
    private final double gpuPower;

    private final String motherboardManufacturer;
    private final String motherboardModel;
    private final String motherboardVersion;

    // physical storage devices detected during the hardware scan.
    private final List<StorageDevice> storageDevices;

    // Stores the individual physical RAM modules detected during a scan.
    private final List<MemoryModule> memoryModules;

    // Identifies whether CPU power is simulated.
    private final boolean cpuPowerSimulated;

    public HardwareInfo(
            String cpuName,
            double cpuTemperature,
            double cpuPower,
            long totalMemory,
            boolean cpuPowerSimulated,
            String gpuName,
            String gpuVendor,
            long gpuVram,

            double gpuPower,

            String motherboardManufacturer,
            String motherboardModel,
            String motherboardVersion,

            List<StorageDevice> storageDevices,

            List<MemoryModule> memoryModules

    ) {
        this.cpuName = cpuName;
        this.cpuTemperature = cpuTemperature;
        this.cpuPower = cpuPower;
        this.totalMemory = totalMemory;
        this.cpuPowerSimulated = cpuPowerSimulated;

        // Store GPU identification information.
        this.gpuName = gpuName;
        this.gpuVendor = gpuVendor;
        this.gpuVram = gpuVram;
        this.gpuPower = gpuPower;

        // Store motherboard identification information.
        this.motherboardManufacturer = motherboardManufacturer;
        this.motherboardModel = motherboardModel;
        this.motherboardVersion = motherboardVersion;

        // Store an independent, unmodifiable copy of the detected drives.
        this.storageDevices = List.copyOf(storageDevices);

        // Store an independent, unmodifiable copy of the detected RAM modules.
        this.memoryModules = List.copyOf(memoryModules);


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


    // @return detected GPU model name
    public String getGpuName() {
        return gpuName;
    }

    // @return detected GPU manufacturer
    public String getGpuVendor() {
        return gpuVendor;
    }

    // @return detected GPU video memory in bytes
    public long getGpuVram() {
        return gpuVram;
    }


    // @return GPU package power in watts, or NaN if unavailable
    public double getGpuPower() {
        return gpuPower;
    }

    // @return motherboard manufacturer
    public String getMotherboardManufacturer() {
        return motherboardManufacturer;
    }

    // @return motherboard model
    public String getMotherboardModel() {
        return motherboardModel;
    }

    // @return motherboard version
    public String getMotherboardVersion() {
        return motherboardVersion;
    }


    /**
     * Returns the physical storage devices detected during the scan.
     * @return an unmodifiable list of storage devices
     */
    public List<StorageDevice> getStorageDevices() {
        return storageDevices;
    }


    /**
     * Returns the physical RAM modules detected during the scan.
     * @return an unmodifiable list of memory modules
     */
    public List<MemoryModule> getMemoryModules() {
        return memoryModules;
    }


    /**
     * Indicates whether the CPU power value is simulated.
     * @return true for simulated readings, false otherwise.
     */
    public boolean isCpuPowerSimulated() {
        return cpuPowerSimulated;
    }


    /**
     * Calculates the total measured power consumption of supported hardware components at scan time.
     * @return total measured power in watts, or NaN if no valid power measurements are available
     */
    public double getTotalMeasuredPower() {

        double totalPower = 0.0;
        boolean hasMeasurements = false;

        // Include CPU package power when a valid reading exists.
        if (isValidMeasuredPower(cpuPower)) {
            totalPower += cpuPower;
            hasMeasurements = true;
        }

        // Include GPU package power when a valid reading exists.
        if (isValidMeasuredPower(cpuPower)) {
            totalPower += gpuPower;
            hasMeasurements = true;
        }

        // Distinguish missing measurements from actual zero power.
        return hasMeasurements ? totalPower : Double.NaN;
    }


    /**
     * Calculates the combined power of available hardware components.
     *
     * Includes measured CPU/GPU power and estimated RAM/Storage power.
     * Returns NaN when no valid power information is available.
     *
     * This represents a component-level subtotal, not total system power
     * measured at the wall socket.
     */
    public double getCombinedComponentPower() {

        double totalPower = 0.0;
        boolean hasPowerData = false;

        // Include available measured CPU and GPU power.
        double measuredPower = getTotalMeasuredPower();

        if (Double.isFinite(measuredPower)) {
            totalPower += measuredPower;
            hasPowerData = true;
        }

        // Include estimated RAM power.
        double ramPower = getEstimatedRamPower();

        if (Double.isFinite(ramPower)) {
            totalPower += ramPower;
            hasPowerData = true;
        }

        // Include estimated storage power.
        double storagePower = getEstimatedStoragePower();

        if (Double.isFinite(storagePower)) {
            totalPower += storagePower;
            hasPowerData = true;
        }

        // Return unavailable rather than zero when no power data exists.
        return hasPowerData ? totalPower : Double.NaN;
    }


    /**
     * Checks whether both CPU and GPU measured power readings are available.
     * @return true if both measurements are valid.
     */
    public boolean hasCompleteMeasuredPower() {
        // Both CPU and GPU measurements must be available.
        return isValidMeasuredPower(cpuPower) && isValidMeasuredPower(gpuPower);
    }


    // Calculate the total estimated power consumption of detected RAM modules.
    public double getEstimatedRamPower() {

        double totalPower = 0.0;
        boolean hasEstimate = false;

        // Calculate the estimate for each physical memory module.
        for (MemoryModule module : memoryModules) {

            double estimatedPower = PowerEstimates.estimateMemoryModule(module);

            // Only include modules with a supported power estimate.
            if (isValidEstimatedPower(estimatedPower)) {
                totalPower += estimatedPower;
                hasEstimate = true;
            }
        }

        // Return unavailable if no modules could be estimated.
        return hasEstimate ? totalPower : Double.NaN;
    }


    // Check whether every detected RAM module has a supported power estimate.
    public boolean hasCompleteRamPowerEstimate() {

        // No detected RAM modules means complete coverage cannot be confirmed.
        if (memoryModules.isEmpty()) {
            return false;
        }

        // Check that every detected module has a valid estimate.
        for (MemoryModule module : memoryModules) {

            double estimatedPower = PowerEstimates.estimateMemoryModule(module);

            if (!isValidEstimatedPower(estimatedPower)) {
                return false;
            }
        }

        return true;
    }




    // Calculate the total estimated power consumption of detected storage devices.
    public double getEstimatedStoragePower() {

        double totalPower = 0.0;
        boolean hasEstimate = false;

        // Calculate the estimate for each physical storage device.
        for (StorageDevice device : storageDevices) {

            double estimatedPower = PowerEstimates.estimateStorageDevice(device);

            // Only include devices with a supported power estimate.
            if (isValidEstimatedPower(estimatedPower)) {
                totalPower += estimatedPower;
                hasEstimate = true;
            }
        }

        // Return unavailable if no storage devices could be estimated.
        return hasEstimate ? totalPower : Double.NaN;
    }


    // Check whether every detected storage device has a supported power estimate.
    public boolean hasCompleteStoragePowerEstimate() {

        // No detected storage devices means complete coverage cannot be confirmed.
        if (storageDevices.isEmpty()) {
            return false;
        }

        // Check that every detected drive has a valid estimate.
        for (StorageDevice device : storageDevices) {

            double estimatedPower = PowerEstimates.estimateStorageDevice(device);

            if (!Double.isFinite(estimatedPower)) {
                return false;
            }
        }

        return true;
    }




    /**
     * Checks whether a measured power value is valid.
     * A valid measurement must be finite and non-negative.
     *
     * @param power Power measurement in watts.
     * @return true if the measurement is valid.
     */
    private static boolean isValidMeasuredPower(double power) {
        return Double.isFinite(power) && power >= 0.0;
    }


    /**
     * Checks whether an estimated power value is valid
     * An estimate must be finite and non-negative.
     *
     * @param power Estimated power consumption in watts.
     * @return true if the estimate is valid.
     */
    private static boolean isValidEstimatedPower(double power) {
        return Double.isFinite(power) && power >= 0.0;
    }



}
