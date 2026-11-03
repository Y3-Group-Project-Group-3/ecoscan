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



    /**
     * Will update the existing rows using the latest scan results.
     * The implementation will be moved here in the next step.
     */
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

        // Build the RAM breakdown using its dedicated UI class.
        JPanel memoryDetailsPanel = MemoryDetailsPanel.createMemoryDetails(info);

        // Calculate the total estimated power of all supported RAM modules.
        double estimatedRamPower = info.getEstimatedRamPower();

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
                false,
                memoryDetailsPanel
        );

        //Updates placeholders when scan is complete
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
                false,
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


    // Replace an existing component row while preserving its position.
    private void replaceComponentRow(String componentName, JPanel newRow) {

        // Retrieve the existing row from the component map.
        JPanel oldRow = componentRows.get(componentName);

        if (oldRow == null || componentList == null) {
            return;
        }

        // Find the existing row's position in the list.
        int index = componentList.getComponentZOrder(oldRow);

        if (index < 0) {
            return;
        }

        // Replace the old row with the updated component.
        componentList.remove(oldRow);
        componentList.add(newRow, index);

        // Update the reference so future scans replace the correct row.
        componentRows.put(componentName, newRow);
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
