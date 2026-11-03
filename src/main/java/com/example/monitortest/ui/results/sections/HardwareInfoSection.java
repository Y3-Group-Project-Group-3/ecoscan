package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.GREEN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;

public class HardwareInfoSection {
    // Power Info Labels
    public static JLabel cpuLabel;
    public static JLabel memoryLabel;
    public static JLabel cpuPowerLabel;
    private static JLabel cpuTemperatureLabel;

    // Labels for displaying GPU identification and sensor readings.
    private static JLabel gpuNameLabel;
    private static JLabel gpuPowerLabel;
    private static JLabel gpuTemperatureLabel;

    // Labels for displaying motherboard identification.
    private static JLabel motherboardManufacturerLabel;
    private static JLabel motherboardModelLabel;
    private static JLabel motherboardVersionLabel;

    // Labels for storage devices
    private static JLabel storageLabel;

    // Labels for RAM
    private static JLabel memoryModulesLabel;


    // Total Measured power at time of scan
    private static JLabel totalMeasuredPowerLabel;

    public static JPanel createHardwareInfoSection() {

        // Identify simulated CPU power readings on macOS.

        JPanel section = column();

        section.add(label( "DETECTED HARDWARE", Font.BOLD, AppTheme.FONT_LABEL_SIZE, GREEN ));

        section.add(Box.createVerticalStrut(14));

        JPanel hardwareCard = card();
        hardwareCard.setLayout(new BoxLayout(hardwareCard, BoxLayout.Y_AXIS));

        cpuLabel = label("CPU: Not retrieved yet", Font.PLAIN, AppTheme.FONT_BODY_SIZE, TEXT_MAIN);
        memoryLabel = label("Memory: Not retrieved yet", Font.PLAIN, AppTheme.FONT_BODY_SIZE, TEXT_MAIN);
        cpuPowerLabel = label("CPU Power: Not retrieved yet", Font.PLAIN, AppTheme.FONT_BODY_SIZE, TEXT_MAIN);
        cpuTemperatureLabel = new JLabel("CPU Temperature: Not retrieved yet");

        // Initialise GPU labels before a hardware scan.
        gpuNameLabel = new JLabel("GPU: Not retrieved yet");
        gpuPowerLabel = new JLabel("GPU Power: Not retrieved yet");
        gpuTemperatureLabel = new JLabel("GPU Temperature: Not retrieved yet");

        // Initialise motherboard information before a scan.
        motherboardManufacturerLabel = new JLabel("Motherboard Manufacturer: Not retrieved yet");
        motherboardModelLabel = new JLabel("Motherboard Model: Not retrieved yet");
        motherboardVersionLabel = new JLabel("Motherboard Version: Not retrieved yet");

        // Initialise the storage label before a scan.
        storageLabel = new JLabel("Storage: Not retrieved yet");

        // Initialise the RAM module information before a scan.
        memoryModulesLabel = new JLabel("RAM Modules: Not retrieved yet");

        // Display the combined power consumption of measured components.
        totalMeasuredPowerLabel = new JLabel( "Total Measured Power: Not retrieved yet" );



        // The actual elements on the page
        hardwareCard.add(cpuLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(memoryLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(cpuPowerLabel);

        hardwareCard.add(gpuNameLabel);
        hardwareCard.add(gpuPowerLabel);
        hardwareCard.add(gpuTemperatureLabel);

        hardwareCard.add(motherboardManufacturerLabel);
        hardwareCard.add(motherboardModelLabel);
        hardwareCard.add(motherboardVersionLabel);

        hardwareCard.add(storageLabel);

        hardwareCard.add(memoryModulesLabel);

        hardwareCard.add(totalMeasuredPowerLabel);

        section.add(hardwareCard);

        return section;
    }


    public static void updateHardwareInfo(HardwareInfo info) {

        cpuLabel.setText("CPU: " + info.getCpuName());

        // Display CPU power and identify simulated measurements.
        if (Double.isNaN(info.getCpuPower())) {
            cpuPowerLabel.setText("CPU Power: Not supported");
        } else {
            cpuPowerLabel.setText(
                    String.format( "CPU Power: %.1f W%s", info.getCpuPower(), info.isCpuPowerSimulated() ? " (Simulated)" : "")
            );
        }

        // Display the CPU temperature retrieved during the scan. Show an unavailable message if no valid sensor reading exists.
        if (!Double.isFinite(info.getCpuTemperature())) {
            cpuTemperatureLabel.setText("CPU Temperature: Not available");
        } else {
            cpuTemperatureLabel.setText(String.format( "CPU Temperature: %.1f °C", info.getCpuTemperature() ));
        }


        // Convert total memory from bytes to gigabytes.
        memoryLabel.setText( String.format( "Total Memory: %.2f GB", info.getTotalMemory() / (1024.0 * 1024 * 1024))
        );


        // Update GPU information using the measurements stored in the shared HardwareInfo model.
        gpuNameLabel.setText("GPU: " + info.getGpuName());

        // Display power only when the sensor provides a valid reading.
        if (Double.isNaN(info.getGpuPower())) {
            gpuPowerLabel.setText("GPU Power: Not available");
        } else {
            gpuPowerLabel.setText(String.format( "GPU Power: %.1f W", info.getGpuPower()));
        }

        // Display motherboard information retrieved through OSHI.
        motherboardManufacturerLabel.setText( "Motherboard Manufacturer: " + info.getMotherboardManufacturer() );
        motherboardModelLabel.setText( "Motherboard Model: " + info.getMotherboardModel() );
        motherboardVersionLabel.setText( "Motherboard Version: " + info.getMotherboardVersion());



        // Retrieve the physical storage devices from the scan result.
        var storageDevices = info.getStorageDevices();

        if (storageDevices.isEmpty()) {
            storageLabel.setText("Storage: No devices detected");
        } else {

            // Build an HTML string to display multiple drives on separate lines.
            StringBuilder storageText = new StringBuilder("<html>Storage:");

            for (StorageDevice device : storageDevices) {
                storageText.append(String.format( "<br>%s - %.2f GB", device.getModel(), device.getCapacityGB()));
            }

            storageText.append("</html>");
            storageLabel.setText(storageText.toString());
        }



        var memoryModules = info.getMemoryModules();

        if (memoryModules.isEmpty()) {
            memoryModulesLabel.setText("RAM Modules: No modules detected");
        } else {

            // Build a separate display line for each detected RAM module.
            StringBuilder memoryText = new StringBuilder( "<html>RAM Modules: " + memoryModules.size() );

            int moduleNumber = 1;

            for (MemoryModule module : memoryModules) {

                memoryText.append(String.format(
                        "<br>Module %d: %.2f GB %s - %.0f MHz (%s)",
                        moduleNumber,
                        module.getCapacityGB(),
                        module.getMemoryType(),
                        module.getClockSpeedMHz(),
                        module.getManufacturer()
                ));

                moduleNumber++;
            }

            memoryText.append("</html>");
            memoryModulesLabel.setText(memoryText.toString());
        }


        /*
         * Display the combined power consumption of all
         * components with valid power measurements.
         */
        double totalPower = info.getTotalMeasuredPower();

        if (!Double.isFinite(totalPower)) {
            totalMeasuredPowerLabel.setText( "Total Measured Power: Not available" );
        } else {
            totalMeasuredPowerLabel.setText(String.format( "Total Measured Power: %.1f W", totalPower ));
        }


    }

}
