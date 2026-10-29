package com.example.monitortest.model;

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
    private final double gpuTemperature;

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
            String gpuName, String gpuVendor, long gpuVram,
            double gpuPower, double gpuTemperature,

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
        this.gpuTemperature = gpuTemperature;


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

    // @return GPU core temperature in Celsius, or NaN if unavailable
    public double getGpuTemperature() {
        return gpuTemperature;
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
        if (Double.isFinite(cpuPower) && cpuPower >= 0) {
            totalPower += cpuPower;
            hasMeasurements = true;
        }

        // Include GPU package power when a valid reading exists.
        if (Double.isFinite(gpuPower) && gpuPower >= 0) {
            totalPower += gpuPower;
            hasMeasurements = true;
        }

        // Distinguish missing measurements from actual zero power.
        return hasMeasurements ? totalPower : Double.NaN;
    }


    // Estimate RAM power using the detected type of each memory module.
    // The baselines are approximate assumptions, not sensor measurements.
    public double getEstimatedRamPower() {
        double estimatedPower = 0.0;

        // Select an estimated wattage based on each module's RAM generation.
        for (MemoryModule module : memoryModules) {
            String memoryType = module.getMemoryType();

            if (memoryType == null) {
                continue;
            }

            switch (memoryType.toUpperCase()) {
                case "DDR2":
                    estimatedPower += 4.0;
                    break;

                case "DDR3":
                    estimatedPower += 3.0;
                    break;

                case "DDR4":
                    estimatedPower += 2.5;
                    break;

                case "DDR5":
                    estimatedPower += 3.5;
                    break;

                default:
                    // Unknown memory types are excluded rather than assigned an unsupported estimate.
                    break;
            }
        }

        return estimatedPower;
    }


    // Estimate storage power based on each physical drive's detected type.
    // These values are approximate baselines, not sensor measurements.
    public double getEstimatedStoragePower() {
        double estimatedPower = 0.0;
        boolean hasEstimates = false;

        // Calculate an estimate for each recognised physical storage device.
        for (StorageDevice device : storageDevices) {
            String storageType = device.getType();

            if (storageType == null) {
                continue;
            }

            switch (storageType.trim().toUpperCase()) {
                case "SSD":
                    estimatedPower += 2.5;
                    hasEstimates = true;
                    break;

                case "HDD":
                    estimatedPower += 5.0;
                    hasEstimates = true;
                    break;

                default:
                    // Unknown drives are excluded from the estimate.
                    break;
            }
        }

        // Distinguish unavailable estimates from an actual zero value.
        return hasEstimates ? estimatedPower : Double.NaN;
    }



}
