package com.example.monitortest.hardware.monitoring;

import com.example.monitortest.hardware.detection.MacHardwareProfileLoader;
import com.example.monitortest.hardware.demo.MacDemoConfig;
import com.google.gson.JsonObject;
import oshi.SystemInfo;

// Provides static demonstration power values for the macOS Live Monitor.
// Reuses the existing Mac hardware profiles rather than real power sensors.
public class MacLiveMonitorDemoProvider {

    // Creates a single simulated CPU/GPU power snapshot.
    // Values are sourced from mac-hardware-profiles.json.
    public LiveMonitorSnapshot createSnapshot() {

        // Identify the computer's processor using OSHI.
        // This reads hardware identification, not live power sensors.
        SystemInfo systemInfo = new SystemInfo();

        String cpuName = systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName();

        // Retrieve the matching profile or the default JSON profile.
        MacHardwareProfileLoader profileLoader = new MacHardwareProfileLoader();
        JsonObject profile = profileLoader.findProfile(cpuName);

        // Use shared fallback values when a hardware profile cannot be loaded.
        double cpuPower = MacDemoConfig.DEFAULT_CPU_POWER_WATTS;
        double gpuPower = MacDemoConfig.DEFAULT_GPU_POWER_WATTS;

        // Read the configurable demonstration values.
        if (profile != null) {

            if (profile.has("cpuPower")) {
                cpuPower = profile.get("cpuPower").getAsDouble();
            }

            if (profile.has("gpuPower")) {
                gpuPower = profile.get("gpuPower").getAsDouble();
            }
        }

        // Return one static snapshot for the existing Live Monitor.
        return new LiveMonitorSnapshot(cpuPower, gpuPower);
    }
}
