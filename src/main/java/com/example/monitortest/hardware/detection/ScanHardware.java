
package com.example.monitortest.hardware.detection;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Comparator;
import java.util.List;

/**
 * Coordinates hardware information collection using OSHI and LibreHardwareMonitor.
 */
public class ScanHardware {

    /**
     * Retrieves hardware specifications and sensor readings.
     * @return Hardware information for the current scan.
     */
    public HardwareInfo getHardwareInfo() {

        /***************************** OS Detection ************************/
        // Use simulated hardware information when running on macOS.
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        /***************************** If mac, use dummy data  ************************/
        if (operatingSystem.contains("mac")) {
            DummyHardwareProvider dummyProvider = new DummyHardwareProvider();
            return dummyProvider.getHardwareInfo();
        }

        /***************************** Accessing OSHI ************************/
        // Retrieve hardware specifications using OSHI.
        SystemInfo systemInfo = new SystemInfo();

        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();

        String cpuName = processor.getProcessorIdentifier().getName();
        long totalMemory = memory.getTotal();


        /***************************** Managing sensor failures ************************/
        /*
         * Attempt to retrieve sensor information from LibreHardwareMonitor.
         * If retrieval fails, continue using OSHI hardware specifications.
         */
        HardwareSnapshotProvider snapshotProvider = new HardwareSnapshotProvider();

        Computer computer = null;

        try {
            // Retrieve a snapshot containing the available hardware sensors.
            computer = snapshotProvider.getSnapshot();

        } catch (Exception e) {
            // Report the failure without stopping the entire hardware scan.
            System.err.println( "Warning: LibreHardwareMonitor snapshot retrieval failed: " + e.getMessage()
            );
        }


        // Warn if LibreHardwareMonitor did not return sensor data.
        // OSHI hardware specifications can still be retrieved.
        if (computer == null) {
            System.err.println( "Warning: LibreHardwareMonitor sensor data is unavailable.");
        }



        /***************************** Individual Sensor Retrieval ************************/
        // CPU and GPU are together because they're the only actual sensors with power info.
        // Default sensor readings to unavailable.
        double cpuPower = Double.NaN;
        double cpuTemperature = Double.NaN;
        double gpuPower = Double.NaN;

        if (computer != null) {

            HardwareSensorReader sensorReader = new HardwareSensorReader();

            // Attempt to retrieve CPU sensor readings.
            try {
                cpuPower = sensorReader.getSensorValue( computer, "Cpu", "Power", "Package" );

                // Support the alternative CPU package sensor name.
                if (Double.isNaN(cpuPower)) {
                    cpuPower = sensorReader.getSensorValue(computer, "Cpu", "Power", "CPU Package");
                }

                cpuTemperature = sensorReader.getSensorValue(computer, "Cpu", "Temperature", "Core (Tctl/Tdie)");

            } catch (Exception e) {
                System.err.println( "Warning: CPU sensor retrieval failed: " + e.getMessage());
            }

            // Attempt to retrieve GPU sensor readings independently.
            try {
                gpuPower = sensorReader.getGpuPower(computer);
            } catch (Exception e) {
                System.err.println( "Warning: GPU sensor retrieval failed: " + e.getMessage()
                );
            }
        }


        String gpuName = "Not detected";
        String gpuVendor = "Unknown";
        long gpuVram = 0L;

        var graphicsCards = systemInfo.getHardware().getGraphicsCards();
        if (!graphicsCards.isEmpty()) {

            // Select the GPU with the largest reported VRAM.
            // This is a temporary approach until we support multiple GPUs.
            var gpu = graphicsCards.stream()
                    .max(Comparator.comparingLong(GraphicsCard::getVRam))
                    .orElseThrow();

            gpuName = gpu.getName();
            gpuVendor = gpu.getVendor();
            gpuVram = gpu.getVRam();
        }


        /***************************** MOTHERBOARD ************************/
         // Retrieve motherboard identification using OSHI. These details are supplied by the system firmware.
        var baseboard = systemInfo.getHardware().getComputerSystem().getBaseboard();
        String motherboardManufacturer = baseboard.getManufacturer();
        String motherboardModel = baseboard.getModel();
        String motherboardVersion = baseboard.getVersion();

        /***************************** RAM ************************/
        // Retrieve the installed RAM modules using the dedicated detector.
        MemoryDetector memoryDetector = new MemoryDetector();
        List<MemoryModule> detectedMemoryModules = memoryDetector.getMemoryModules(memory);

        /***************************** STORAGE ************************/
        // Retrieve physical drives and their SSD/HDD classifications using the dedicated storage detector.
        StorageDetector storageDetector = new StorageDetector();
        List<StorageDevice> storageDevices = storageDetector.getStorageDevices(hardware);


        /***************************** THE SCAN INFO ITSELF ************************/
        // Construct the hardware information snapshot.
        return new HardwareInfo(
                cpuName,
                cpuTemperature,
                cpuPower,
                totalMemory,
                false,
                gpuName,
                gpuVendor,
                gpuVram,
                gpuPower,
                motherboardManufacturer,
                motherboardModel,
                motherboardVersion,
                storageDevices,
                detectedMemoryModules
        );



    }
}
