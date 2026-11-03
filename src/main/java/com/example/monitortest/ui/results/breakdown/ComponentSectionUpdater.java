package com.example.monitortest.ui.results.breakdown;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;

import javax.swing.*;
import java.util.Map;

import static com.example.monitortest.ui.results.components.ComponentRow.componentRow;

/**
 * Updates the Component Breakdown using completed hardware scan results.
 *
 * Keeps hardware-specific display updates separate from the initial
 * construction of the Component Breakdown interface.
 */
public class ComponentSectionUpdater {

    private final JPanel componentList;
    private final Map<String, JPanel> componentRows;
    private final PowerSummaryPanel powerSummaryPanel;

    /**
     * Receives references to the existing UI components.
     * No additional hardware scan is performed.
     */
    public ComponentSectionUpdater(
            JPanel componentList,
            Map<String, JPanel> componentRows,
            PowerSummaryPanel powerSummaryPanel
    ) {
        this.componentList = componentList;
        this.componentRows = componentRows;
        this.powerSummaryPanel = powerSummaryPanel;
    }



    // Updates the hardware breakdown and power summary using completed scan data.
    public void updateComponentSection(HardwareInfo info) {


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
                componentList, "CPU", info.getCpuName(), "MEASURED", info.getCpuName(), cpuPowerText, "—", cpuShareText, isRowExpanded("CPU")
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
                componentList, "GPU", info.getGpuName(), "MEASURED", info.getGpuName(), gpuPowerText, "—", gpuShareText, isRowExpanded("GPU")
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

        // Build the RAM breakdown using its dedicated UI class.
        JPanel memoryDetailsPanel = MemoryDetailsPanel.createMemoryDetails(info);


        // Display whether the RAM estimate covers every detected module.
        String ramPowerContribution = formatPowerEstimate( info.getEstimatedRamPower(), info.hasCompleteRamPowerEstimate() );

        // Display RAM specifications without inventing a power measurement.
        JPanel ramRow = componentRow(
                componentList,
                "RAM",
                ramSummary,
                "",
                ramSpecifications,
                "Not available",
                ramAdditionalInfo,
                ramPowerContribution,
                // Preserve the RAM expansion state when rebuilding its details.
                isRowExpanded("RAM"),
                memoryDetailsPanel
        );

        // Insert or refresh the RAM row using completed scan results.
        replaceComponentRow("RAM", ramRow);


        /**************************** Storage **************/
        // Retrieve the physical storage devices detected during the scan.
        var storageDevices = info.getStorageDevices();
        // Display the actual number of detected drives.
        String storageSummary = storageDevices.size() + " detected";
        // Build a summary of the detected storage types.
        long ssdCount = storageDevices.stream().filter(device -> "SSD".equalsIgnoreCase(device.getType())).count();
        long hddCount = storageDevices.stream().filter(device -> "HDD".equalsIgnoreCase(device.getType())).count();

        String storageSpecifications = String.format( "%d SSD · %d HDD", ssdCount, hddCount);

        // Display whether the storage estimate covers every detected drive.
        String storagePowerContribution = formatPowerEstimate(
                info.getEstimatedStoragePower(),
                info.hasCompleteStoragePowerEstimate()
        );

        // Display the number of physical drives in the additional information column.
        String storageAdditionalInfo = storageDevices.size() + " physical drives";

        // Create the storage breakdown using its dedicated UI class.
        JPanel storageDetailsPanel = StorageDetailsPanel.createStorageDetails(info);

        // Include the storage details inside the expandable component row.
        JPanel storageRow = componentRow(
                componentList,
                "Storage",
                storageSummary,
                "ESTIMATED",
                storageSpecifications,
                "Not measured",
                storageAdditionalInfo,
                storagePowerContribution,
                // Preserve the Storage expansion state when rebuilding its details.
                isRowExpanded("Storage"),
                storageDetailsPanel
        );

        // Replace the existing Storage row without relying on its index.
        replaceComponentRow("Storage", storageRow);


        /**************** Power / Estimates Power **************************/
        // Update the power summary using the latest hardware scan.
        if (powerSummaryPanel != null) {
            powerSummaryPanel.updateHardwareInfo(info);
        }

        // Refresh the layout to display the updated row.
        componentList.revalidate();
        componentList.repaint();
    }



    // Inserts a component row on the first scan or replaces it on refresh.
    private void replaceComponentRow(String componentName, JPanel newRow) {

        // Retrieve the existing component row, if one is displayed.
        JPanel oldRow = componentRows.get(componentName);

        if (oldRow == null) {
            // First scan: add the component to the empty list.
            componentList.add(newRow);
        } else {
            // Subsequent scans: locate the component's existing position.
            int index = componentList.getComponentZOrder(oldRow);
            if (index >= 0) {
                // Replace the previous row while preserving its position.
                componentList.remove(oldRow);
                componentList.add(newRow, index);
            } else {
                // Recover gracefully if the previous row is no longer attached.
                componentList.add(newRow);
            }
        }

        // Store the latest row reference for future refresh operations.
        componentRows.put(componentName, newRow);
    }


    // Determines whether an existing component row is currently expanded.
    private boolean isRowExpanded(String componentName) {

        // Retrieve the component row from the latest scan.
        JPanel row = componentRows.get(componentName);

        // New rows are collapsed by default.
        if (row == null) {
            return false;
        }

        // Retrieve the expandable content stored by ComponentRow.
        Object content = row.getClientProperty("expandableContent");

        // Preserve the existing visibility when a row is replaced.
        if (content instanceof JPanel expandableContent) {
            return expandableContent.isVisible();
        }

        return false;
    }




    // Format a power estimate based on detected hardware coverage.
    private static String formatPowerEstimate( double estimatedPower, boolean complete ) {

        // No supported devices have an available estimate.
        if (!Double.isFinite(estimatedPower)) {
            return "Estimate unavailable";
        }

        // Some detected devices could not be estimated.
        if (!complete) {
            return String.format( "Partial estimate: %.1f W", estimatedPower
            );
        }

        // Every detected device has a supported estimate.
        return String.format( "Estimated: %.1f W", estimatedPower
        );
    }


}
