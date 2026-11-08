package com.example.monitortest.hardware.detection;

import com.example.monitortest.hardware.demo.MacDemoConfig;
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
import oshi.SystemInfo;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Provides simulated hardware information for development and testing on platforms without supported sensors.
 * All values returned by this class are dummy data.
 */
public class DummyHardwareProvider {

    /**
     * Creates a hardware snapshot using real system specifications
     * and simulated sensor readings.
     */
    public HardwareInfo getHardwareInfo() {

        // Retrieve actual hardware specifications using OSHI.
        SystemInfo systemInfo = new SystemInfo();

        String cpuName = systemInfo.getHardware()
                .getProcessor()
                .getProcessorIdentifier()
                .getName();

        long totalMemory = systemInfo.getHardware()
                .getMemory()
                .getTotal();

        // Retrieve the real CPU temperature using OSHI.
        double cpuTemperature = systemInfo.getHardware()
                .getSensors()
                .getCpuTemperature();


        /*
         * Retrieve simulated CPU power from the JSON hardware profiles.
         * OSHI supplies the real CPU name used to find a matching profile.
         */
        MacHardwareProfileLoader profileLoader = new MacHardwareProfileLoader();
        JsonObject profile = profileLoader.findProfile(cpuName);

        // Use the matched profile's CPU power.
        // If the JSON file cannot be loaded, use a generic fallback.
        double cpuPower = 65.0;

        if (profile != null && profile.has("cpuPower")) {
            cpuPower = profile.get("cpuPower").getAsDouble();
        }

        // Simulate a memory module using the Mac's actual total memory capacity.
        // Apple Silicon uses unified memory, represented here as a single module
        // so that the memory breakdown interface can be tested.
        List<MemoryModule> memoryModules = List.of(
                new MemoryModule(
                        "Apple",
                        "LPDDR5",
                        totalMemory,
                        3_200_000_000L // Simulated clock frequency: 3200 MHz.
                )
        );

        // Simulate a physical SSD for macOS front-end development.
        // This allows storage information and power estimates to use
        // the same data flow as detected Windows storage devices.
        List<StorageDevice> storageDevices = List.of(
                new StorageDevice(
                        MacDemoConfig.STORAGE_NAME,
                        MacDemoConfig.STORAGE_CAPACITY_BYTES,
                        MacDemoConfig.STORAGE_TYPE
                )
        );


        return new HardwareInfo(
                cpuName,
                cpuTemperature,
                cpuPower,
                totalMemory,
                true,
                // Simulated GPU specifications and power for macOS UI testing.
                MacDemoConfig.GPU_NAME,
                MacDemoConfig.GPU_VENDOR,
                totalMemory, // Apple Silicon uses unified memory.
                MacDemoConfig.DEFAULT_GPU_POWER_WATTS, // ,         // Fabricated GPU power in watts.
                MacDemoConfig.GPU_VENDOR,
                "Apple Motherboard",
                "1.0",
                storageDevices, // Simulated SSD.
                memoryModules   // Simulated RAM modules.
        );



    }
}
