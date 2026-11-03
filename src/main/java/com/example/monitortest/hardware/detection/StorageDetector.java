package com.example.monitortest.hardware.detection;

import com.example.monitortest.hardware.model.StorageDevice;
import oshi.hardware.HardwareAbstractionLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Retrieves physical storage device information using OSHI
 * and classifies drives using Windows storage information.
 */
public class StorageDetector {

    /**
     * Detects physical storage devices and converts them into EcoScan's StorageDevice model.
     *
     * @param hardware OSHI's hardware abstraction layer.
     * @return A list of detected physical storage devices.
     */
    public List<StorageDevice> getStorageDevices(HardwareAbstractionLayer hardware) {

        // Retrieve physical storage devices through OSHI.
        var diskStores = hardware.getDiskStores();

        // Retrieve SSD/HDD classifications from Windows.
        Map<Integer, String> storageTypes = WindowsStorageTypeDetector.getStorageTypes();

        List<StorageDevice> storageDevices = new ArrayList<>();

        // Convert each physical disk into EcoScan's model.
        for (var disk : diskStores) {

            // Default to UNKNOWN if the drive cannot be classified.
            String storageType = "UNKNOWN";
            String deviceName = disk.getName();

            // Match Windows physical drive identifiers such as \\.\PHYSICALDRIVE0.
            if (deviceName != null && deviceName.toUpperCase().matches(".*PHYSICALDRIVE\\d+$")) {

                // Extract the numeric physical drive identifier.
                String deviceIdText = deviceName.replaceAll("(?i)^.*PHYSICALDRIVE", "");
                int deviceId = Integer.parseInt(deviceIdText);

                // Look up the drive classification returned by Windows.
                storageType = storageTypes.getOrDefault( deviceId, "UNKNOWN");
            }

            // Store the detected drive and its classification.
            storageDevices.add(
                    new StorageDevice(
                            disk.getModel(),
                            disk.getSize(),
                            storageType
                    )
            );
        }

        return storageDevices;
    }
}
