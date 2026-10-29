
package com.example.monitortest.model;

import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * Coordinates hardware information collection using OSHI and LibreHardwareMonitor.
 */
public class ScanHardware {

    /**
     * Retrieves hardware specifications and sensor readings.
     * @return Hardware information for the current scan.
     */
    public HardwareInfo getHardwareInfo() {


        // Use simulated hardware information when running on macOS.
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("mac")) {
            DummyHardwareProvider dummyProvider = new DummyHardwareProvider();
            return dummyProvider.getHardwareInfo();
        }


        // STEP 1: Retrieve hardware specifications using OSHI.
        SystemInfo systemInfo = new SystemInfo();

        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();

        String cpuName = processor.getProcessorIdentifier().getName();
        long totalMemory = memory.getTotal();

        // STEP 2: Retrieve a single LibreHardwareMonitor snapshot.
        HardwareSnapshotProvider snapshotProvider = new HardwareSnapshotProvider();
        Computer computer = snapshotProvider.getSnapshot();

        // STEP 3: Create the generic sensor reader.
        HardwareSensorReader sensorReader = new HardwareSensorReader();

        // Retrieve CPU package power.
        double cpuPower = sensorReader.getSensorValue( computer, "Cpu","Power", "Package");

        // Support the alternative CPU package sensor name.
        if (Double.isNaN(cpuPower)) {
            cpuPower = sensorReader.getSensorValue( computer, "Cpu", "Power", "CPU Package");
        }

        // Retrieve CPU temperature.
        double cpuTemperature = sensorReader.getSensorValue( computer, "Cpu", "Temperature","Core (Tctl/Tdie)" );

        // STEP 4: Construct the hardware information snapshot.
        return new HardwareInfo(
                cpuName,
                cpuTemperature,
                cpuPower,
                totalMemory,
                false // CPU power comes from a real sensor, not dummy data.
        );
    }
}
