package com.example.monitortest.views.sections;

import com.example.monitortest.model.HardwareInfo;
import com.example.monitortest.model.MemoryModule;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.*;
import static com.example.monitortest.theme.AppTheme.BORDER_COLOR;
import static com.example.monitortest.theme.UIUtility.column;
import static com.example.monitortest.theme.UIUtility.label;
import static com.example.monitortest.views.results.components.ComponentRow.componentRow;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.util.HashMap;
import java.util.Map;

public class ComponentSection {

    // Reference to the existing Component Breakdown list.
    private static JPanel componentList;
    // Track component rows by name instead of their position.
    private static final Map<String, JPanel> componentRows = new HashMap<>();

    // Displays the combined power of components with available measurements.
    private static JLabel totalPowerLabel;

    // Create the Component Breakdown before scan results are available.
    public static JPanel createComponentSection() {
        return createComponentSection(null);
    }


    // list of components, click a row to open details
    // Build the component breakdown using the supplied scan results.
    public static JPanel createComponentSection(HardwareInfo info) {
        JPanel section = column();
        section.add(label("COMPONENT BREAKDOWN", Font.BOLD, 12, GREEN));
        section.add(Box.createVerticalStrut(4));
        section.add(label("Hardware assessment", Font.BOLD, 20, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        JPanel list = new JPanel();
        // Keep a reference so the rows can be refreshed after scanning.
        componentList = list;
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD_BG);
        list.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        list.setAlignmentX(LEFT_ALIGNMENT);

        // data: components from the scanner CSV + their assessment


        // Use placeholders until a hardware scan provides real values.
        String cpuName = info == null ? "Not retrieved yet" : info.getCpuName();
        String cpuPowerText = "Not available";
        String cpuShareText = "Not available";

        if (info != null) {
            // Display the measured CPU power when the sensor is available.
            if (!Double.isNaN(info.getCpuPower())) { cpuPowerText = String.format("%.1f W", info.getCpuPower()); }

            // Calculate the CPU's share of available measured component power.
            double totalPower = info.getTotalMeasuredPower();
            if (!Double.isNaN(info.getCpuPower()) && Double.isFinite(totalPower) && totalPower > 0) {
                cpuShareText = String.format( "%.1f%%", info.getCpuPower() / totalPower * 100 );
            }
        }

        JPanel cpuRow = componentRow( list, "CPU", cpuName, "MEASURED", cpuName, cpuPowerText, "—", cpuShareText, false );
        list.add(cpuRow);
        componentRows.put("CPU", cpuRow);



    // Display GPU information if available, otherwise use placeholders.
        String gpuName = info == null ? "Not retrieved yet" : info.getGpuName();

        String gpuPowerText = "Not available";
        String gpuShareText = "Not available";

        if (info != null) {
            double gpuPower = info.getGpuPower();
            double totalPower = info.getTotalMeasuredPower();

            // Format the measured GPU power.
            if (Double.isFinite(gpuPower)) {
                gpuPowerText = String.format("%.1f W", gpuPower);
            }

            // Calculate the GPU's share of measured component power.
            if (Double.isFinite(gpuPower)  && Double.isFinite(totalPower) && totalPower > 0) {
                gpuShareText = String.format( "%.1f%%", gpuPower / totalPower * 100 );
            }
        }
        JPanel gpuRow = componentRow(
                list, "GPU", gpuName, "MEASURED", gpuName, gpuPowerText, "—", gpuShareText, false
        );
        list.add(gpuRow);
        componentRows.put("GPU", gpuRow);


        // Create the RAM row with placeholders until a scan completes.
        JPanel ramRow = componentRow(
                list, "RAM", "Not retrieved yet", "EFFICIENT", "Not retrieved yet", "Not available", "—", "Not available", false
        );
        list.add(ramRow);
        componentRows.put("RAM", ramRow);


        list.add(componentRow(list, "Storage", "2 detected", "AGEING",
                "2 drives detected", "Low impact", "~6 years", "Older drive may need replacing", false));
        list.add(componentRow(list, "Operating System", "", "SUPPORTED",
                "Windows", "–", "–", "Supported", false));

        // Add the expandable component rows.
        section.add(list);

        // Add spacing before the total power summary.
        section.add(Box.createVerticalStrut(12));

        // Display a placeholder until the first scan completes.
        totalPowerLabel = label( "Total Measured Power: Not retrieved yet", Font.BOLD, 16, TEXT_MAIN );

        totalPowerLabel.setAlignmentX(LEFT_ALIGNMENT);
        section.add(totalPowerLabel);

        return section;

    }



    // Refresh the Component Breakdown using the completed hardware scan.
    public static void updateComponentSection(HardwareInfo info) {

        // Ensure the component list and scan results are available.
        if (componentList == null || info == null) {
            return;
        }

        /********** CPU ***********/
        // Format the CPU power reading.
        double cpuPower = info.getCpuPower();
        String cpuPowerText = Double.isFinite(cpuPower) ? String.format("%.1f W", cpuPower) : "Not available";
        // Calculate the CPU's share of available measured power.
        double totalPower = info.getTotalMeasuredPower();
        String cpuShareText = "Not available";
        if (Double.isFinite(cpuPower) && Double.isFinite(totalPower) && totalPower > 0) {
            cpuShareText = String.format( "%.1f%%", cpuPower / totalPower * 100);
        }
        // Replace the existing CPU row with the latest scan results.
        JPanel cpuRow = componentRow(
                componentList, "CPU", info.getCpuName(), "MEASURED", info.getCpuName(), cpuPowerText, "—", cpuShareText, false
        );

        replaceComponentRow("CPU", cpuRow);

        /********** GPU ***********/
        // Retrieve and format the GPU power reading.
        double gpuPower = info.getGpuPower();
        String gpuPowerText = Double.isFinite(gpuPower) ? String.format("%.1f W", gpuPower) : "Not available";
        // Calculate the GPU's share of available measured component power.
        String gpuShareText = "Not available";

        if (Double.isFinite(gpuPower) && Double.isFinite(totalPower) && totalPower > 0) {
            gpuShareText = String.format( "%.1f%%", gpuPower / totalPower * 100);
        }

        // Replace the hardcoded GPU row with the latest scan results.
        JPanel gpuRow = componentRow(
                componentList, "GPU", info.getGpuName(), "MEASURED", info.getGpuName(), gpuPowerText, "—", gpuShareText, false
        );

        replaceComponentRow("GPU", gpuRow);

        /********** RAM ***********/
        // Retrieve the physical RAM modules detected by OSHI.
        var memoryModules = info.getMemoryModules();

        String ramSummary = String.format( "%.2f GB · %d modules", info.getTotalMemory() / (1024.0 * 1024 * 1024), memoryModules.size());
        String ramSpecifications = "Not available";
        String ramAdditionalInfo = "—";

        if (!memoryModules.isEmpty()) {
            // Use the first module to describe the memory type and reported speed.
            MemoryModule firstModule = memoryModules.get(0);

            ramSpecifications = String.format( "%d × %.2f GB %s", memoryModules.size(), firstModule.getCapacityGB(), firstModule.getMemoryType() );
            ramAdditionalInfo = String.format( "%.0f MHz (reported)", firstModule.getClockSpeedMHz() );
        }

        // Display RAM specifications without inventing a power measurement.
        JPanel ramRow = componentRow(
                componentList, "RAM", ramSummary, "EFFICIENT", ramSpecifications, "Not available", ramAdditionalInfo, "—", false
        );

        //Updates placeholders when scan is complete
        replaceComponentRow("RAM", ramRow);




        // Display the sum of available component power measurements.
        double measuredPower = info.getTotalMeasuredPower();
        if (Double.isFinite(measuredPower)) {
            totalPowerLabel.setText(String.format( "Total Measured Power: %.1f W", measuredPower ));
        } else {
            totalPowerLabel.setText("Total Measured Power: Not available");
        }


        // Refresh the layout to display the updated row.
        componentList.revalidate();
        componentList.repaint();
    }


    // Replace an existing component row while preserving its position.
    private static void replaceComponentRow(String componentName, JPanel newRow) {

        JPanel oldRow = componentRows.get(componentName);
        if (oldRow == null || componentList == null) { return; }
        // Find the existing row's position in the list.
        int index = componentList.getComponentZOrder(oldRow);
        if (index < 0) { return; }

        // Replace the row and update its reference.
        componentList.remove(oldRow);
        componentList.add(newRow, index);
        componentRows.put(componentName, newRow);
    }



}
