package com.example.monitortest.ui.results.breakdown;


import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.power.PowerEstimates;
import com.example.monitortest.hardware.model.StorageDevice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.label;


// Creates the detailed breakdown of detected physical storage devices.
public class StorageDetailsPanel {

    // Prevent instances because this class only creates UI components.
    private StorageDetailsPanel() {
    }

    /**
     * Creates a panel displaying each storage device and its estimated power.
     */
    public static JPanel createStorageDetails(HardwareInfo info) {

        JPanel storageDetails = new JPanel();
        storageDetails.setLayout(new BoxLayout(storageDetails, BoxLayout.Y_AXIS));
        storageDetails.setBackground(new Color(0xF8FAF9));
        storageDetails.setBorder(new EmptyBorder(8, 18, 14, 18));

        // Handle scans where no physical storage devices were detected.
        if (info.getStorageDevices().isEmpty()) {
            storageDetails.add(label( "No physical storage devices detected", Font.PLAIN, 13, TEXT_MUTED ));
            return storageDetails;
        }

        // Create a separate row for each detected physical drive.
        for (StorageDevice device : info.getStorageDevices()) {

            String type = device.getType();

            // Retrieve the estimate from the shared calculation utility.
            double estimatedPower = PowerEstimates.estimateStorageDevice(device);

            String powerText = Double.isFinite(estimatedPower) ? String.format("Estimated: %.1f W", estimatedPower) : "Estimate unavailable";
            String driveText = String.format( "%s · %.2f GB · %s", device.getModel(), device.getCapacityGB(), type == null ? "UNKNOWN" : type);

            JPanel driveRow = new JPanel(new BorderLayout(12, 0));
            driveRow.setOpaque(false);
            driveRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            driveRow.setBorder(new EmptyBorder(8, 0, 8, 0));
            driveRow.add( label(driveText, Font.PLAIN, 13, TEXT_MAIN), BorderLayout.CENTER);
            driveRow.add( label(powerText, Font.BOLD, 13, TEXT_MAIN), BorderLayout.EAST );

            storageDetails.add(driveRow);
        }

        return storageDetails;
    }
}
