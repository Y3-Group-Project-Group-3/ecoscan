
package com.example.monitortest.model;

import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.ArrayList;
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


        // Check whether LibreHardwareMonitor successfully returned a hardware snapshot before accessing it.
        if (computer == null) {
            throw new IllegalStateException(
                    "LibreHardwareMonitor could not retrieve hardware sensor data."
            );
        }


        /*
         * Inspect motherboard sensors exposed by LibreHardwareMonitor.
         * This helps identify available temperatures, voltages,
         * fan speeds and any direct power measurements.
         */
        for (var device : computer.getHardware()) {

            System.out.println(
                    "Hardware: " + device.getName()
                            + " | Type: " + device.getHardwareType()
            );

            // Print the sensors belonging to each hardware device.
            for (var sensor : device.getSensors()) {

                System.out.println(
                        "  Sensor: " + sensor.getName()
                                + " | Type: " + sensor.getSensorType()
                                + " | Value: " + sensor.getValue()
                );
            }


            /*
             * Inspect subhardware devices, which may contain
             * motherboard monitoring chips and their sensors.
             */
            for (var subDevice : device.getSubHardware()) {

                System.out.println(
                        "  Subhardware: " + subDevice.getName()
                                + " | Type: " + subDevice.getHardwareType()
                );

                // Print sensors belonging to the subhardware device.
                for (var sensor : subDevice.getSensors()) {

                    System.out.println(
                            "    Sensor: " + sensor.getName()
                                    + " | Type: " + sensor.getSensorType()
                                    + " | Value: " + sensor.getValue()
                    );
                }
            }

        }


        // STEP 3: Create the generic sensor reader.
        HardwareSensorReader sensorReader = new HardwareSensorReader();

        // STEP 3.1:  Retrieve hardware info.

        double cpuPower = sensorReader.getSensorValue( computer, "Cpu","Power", "Package");

        // Support the alternative CPU package sensor name.
        if (Double.isNaN(cpuPower)) {
            cpuPower = sensorReader.getSensorValue( computer, "Cpu", "Power", "CPU Package");
        }

        // Retrieve CPU temperature.
        double cpuTemperature = sensorReader.getSensorValue( computer, "Cpu", "Temperature","Core (Tctl/Tdie)" );

         // Retrieve GPU identification through OSHI. Use default values if no graphics devices are detected.
        String gpuName = "Not detected";
        String gpuVendor = "Unknown";
        long gpuVram = 0L;
        double gpuPower = sensorReader.getGpuPower(computer);
        double gpuTemperature = sensorReader.getGpuTemperature(computer);


        // Temporarily verify GPU sensor readings before connecting them to the Results page.
        System.out.println("GPU Power: " + gpuPower + " W");
        System.out.println("GPU Temperature: " + gpuTemperature + " °C");


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



         // Retrieve motherboard identification using OSHI. These details are supplied by the system firmware.

        var baseboard = systemInfo.getHardware().getComputerSystem().getBaseboard();

        String motherboardManufacturer = baseboard.getManufacturer();
        String motherboardModel = baseboard.getModel();
        String motherboardVersion = baseboard.getVersion();

        // Temporary diagnostic output to verify detection.
        System.out.println("Motherboard Manufacturer: " + motherboardManufacturer);
        System.out.println("Motherboard Model: " + motherboardModel);
        System.out.println("Motherboard Version: " + motherboardVersion);


        /*
         * Retrieve information about individual physical RAM modules.
         * OSHI obtains these details from the operating system.
         */
        var memoryModules = systemInfo.getHardware().getMemory().getPhysicalMemory();
        System.out.println("RAM Modules Detected: " + memoryModules.size());

        for (var module : memoryModules) {
            // Display the module's identifying information.
            System.out.println("RAM Manufacturer: " + module.getManufacturer());
            System.out.println("RAM Type: " + module.getMemoryType());
            System.out.printf( "RAM Capacity: %.2f GB%n", module.getCapacity() / (1024.0 * 1024 * 1024) );
            System.out.printf( "RAM Clock Speed: %.0f MHz%n", module.getClockSpeed() / 1_000_000.0);
            System.out.println("------------------------");
        }


        // Create a collection to store the detected RAM modules.
        List<MemoryModule> detectedMemoryModules = new ArrayList<>();

        // Convert each OSHI memory module into our shared model.
        for (var module : memoryModules) {
            detectedMemoryModules.add(
                    new MemoryModule(
                            module.getManufacturer(),
                            module.getMemoryType(),
                            module.getCapacity(),
                            module.getClockSpeed()
                    )
            );
        }



        /*
         * Retrieve physical storage device information using OSHI.
         * This does not require LibreHardwareMonitor storage monitoring.
         */
        var diskStores = hardware.getDiskStores();

        System.out.println("Storage Devices Detected: " + diskStores.size());

        // Test Windows storage media type detection.
        var storageTypes = WindowsStorageTypeDetector.getStorageTypes();
        System.out.println("Windows Storage Types: " + storageTypes);

        for (var disk : diskStores) {

            // Retrieve identifying information for each physical drive.
            System.out.println("Storage Model: " + disk.getModel());
            // Display the physical disk identifier for future SSD/HDD classification.
            System.out.println("Storage Device Name: " + disk.getName());
            System.out.println("Storage Serial: " + disk.getSerial());

            // Convert capacity from bytes to decimal gigabytes.
            System.out.printf( "Storage Capacity: %.2f GB%n", disk.getSize() / 1_000_000_000.0);
            System.out.println("------------------------");
        }


        List<StorageDevice> storageDevices = new ArrayList<>();
        // Convert each OSHI disk into our shared StorageDevice model.
        for (var disk : diskStores) {

            // Default to UNKNOWN when Windows cannot identify the drive type.
            String storageType = "UNKNOWN";

            // OSHI returns names such as \\.\PHYSICALDRIVE0.
            String deviceName = disk.getName();

            // Extract the numeric identifier from the physical drive name.
            if (deviceName != null && deviceName.toUpperCase().matches(".*PHYSICALDRIVE\\d+$")) {

                String deviceIdText = deviceName.replaceAll("(?i)^.*PHYSICALDRIVE", "");
                int deviceId = Integer.parseInt(deviceIdText);
                // Retrieve the media type previously reported by Windows.
                storageType = storageTypes.getOrDefault(deviceId, "UNKNOWN");
            }

            // Create a storage device using the detected model, capacity and type.
            StorageDevice storageDevice = new StorageDevice( disk.getModel(), disk.getSize(), storageType );
            // Add the physical drive to the hardware information list.
            storageDevices.add(storageDevice);
            // Print the detected classification for testing.
            System.out.println("Storage Type: " + storageType);
        }






        // STEP 4: Construct the hardware information snapshot.

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
                gpuTemperature,
                motherboardManufacturer,
                motherboardModel,
                motherboardVersion,
                storageDevices,
                detectedMemoryModules
        );



    }
}
