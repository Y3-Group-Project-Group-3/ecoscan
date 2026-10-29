package com.example.monitortest.model;

import oshi.SystemInfo;
import com.google.gson.JsonObject;

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



        return new HardwareInfo(
                cpuName,
                cpuTemperature,
                cpuPower,
                totalMemory,
                true // CPU power is simulated on macOS.
        );

    }
}
