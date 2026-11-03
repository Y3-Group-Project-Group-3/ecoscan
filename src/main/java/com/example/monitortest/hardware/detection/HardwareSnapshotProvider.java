package com.example.monitortest.hardware.detection;


import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;

import java.util.Locale;

/**
 * Retrieves a hardware snapshot using LibreHardwareMonitor.
 * LibreHardwareMonitor's PowerShell integration is Windows-only.
 */
public class HardwareSnapshotProvider {

    /**
     * Retrieves CPU and GPU hardware information.
     *
     * @return A hardware snapshot, or null if unavailable.
     */
    // Prevent concurrent LibreHardwareMonitor requests from different threads.
    public static synchronized Computer getSnapshot() {

        // Avoid calling Windows-only functionality on macOS or Linux.
        String operatingSystem = System.getProperty("os.name", "");

        // LibreHardwareMonitor sensor retrieval is supported on Windows only.
        if (!operatingSystem.toLowerCase(Locale.ROOT).startsWith("windows")) {
            return null;
        }


        // Enable only the hardware sensors currently required by EcoScan.
        // RAM, storage and motherboard specifications are retrieved through OSHI.
        ComputerConfig config = ComputerConfig.getInstance()
                .setCpuEnabled(true)
                .setGpuEnabled(true)
                .setMotherboardEnabled(false)
                .setPsuEnabled(false)
                .setMemoryEnabled(false)
                .setStorageEnabled(false);

        // Retrieve the hardware snapshot.
        // The caller can reuse this snapshot for multiple sensor lookups.
        try {


            Computer computer = LibreHardwareManager.createInstance(config).getComputer();
            if (computer == null || computer.getHardware() == null || computer.getHardware().isEmpty()) {
                System.err.println("LibreHardwareMonitor returned no hardware information.");
                return null;
            }

//            // Check whether LibreHardwareMonitor returned a computer object.
//            if (computer == null) {
//                System.err.println("LHM DEBUG: Computer object is null.");
//                return null;
//            }
//
//            // Check whether the computer contains a hardware collection.
//            if (computer.getHardware() == null) {
//                System.err.println("LHM DEBUG: Hardware collection is null.");
//                return null;
//            }
//
//            // Check whether any hardware components were detected.
//            if (computer.getHardware().isEmpty()) {
//                System.err.println("LHM DEBUG: Hardware collection is empty.");
//                return null;
//            }
//
//            // Report successful sensor discovery for comparison.
//            System.out.println("LHM DEBUG: Detected " + computer.getHardware().size() + " hardware components.");


            return computer;

        } catch (RuntimeException exception) {
            // Report retrieval failures without terminating the application.
            System.err.println( "Hardware snapshot retrieval failed: " + exception.getMessage());
            return null;
        }
    }
}
