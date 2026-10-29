package com.example.monitortest.ui.results.breakdown;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.power.PowerEstimates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.label;

/**
 * Builds the expandable details panel for physical RAM modules.
 */
public final class MemoryDetailsPanel {

    // Utility class: prevent unnecessary instances.
    private MemoryDetailsPanel() {
    }

    /**
     * Creates a panel containing each detected RAM module,
     * its specifications and estimated power consumption.
     */
    public static JPanel createMemoryDetails(HardwareInfo info) {

        JPanel memoryDetails = new JPanel();
        memoryDetails.setLayout( new BoxLayout(memoryDetails, BoxLayout.Y_AXIS));
        memoryDetails.setBackground(new Color(0xF8FAF9));
        memoryDetails.setBorder(new EmptyBorder(8, 18, 14, 18));

        // Handle scans where no RAM modules were detected.
        if (info.getMemoryModules().isEmpty()) {
            memoryDetails.add(label( "No physical RAM modules detected", Font.PLAIN, 13, TEXT_MUTED ));
            return memoryDetails;
        }

        int moduleNumber = 1;

        // Create an individual row for each detected memory module.
        for (MemoryModule module : info.getMemoryModules()) {

            String memoryType = module.getMemoryType();

            // Use the shared power estimation method.
            double estimatedPower = PowerEstimates.estimateMemoryModule(module);

            String powerText = Double.isFinite(estimatedPower) ? String.format("Estimated: %.1f W", estimatedPower) : "Estimate unavailable";

            // Display the capacity, memory type and reported clock speed.
            String moduleText = String.format(
                    "Module %d · %.2f GB %s · %.0f MHz",
                    moduleNumber,
                    module.getCapacityGB(),
                    memoryType == null ? "Unknown" : memoryType,
                    module.getClockSpeedMHz()
            );

            JPanel moduleRow = new JPanel(new BorderLayout(12, 0));
            moduleRow.setOpaque(false);
            moduleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            moduleRow.setBorder(new EmptyBorder(8, 0, 8, 0));

            moduleRow.add( label(moduleText, Font.PLAIN, 13, TEXT_MAIN), BorderLayout.CENTER);
            moduleRow.add( label(powerText, Font.BOLD, 13, TEXT_MAIN), BorderLayout.EAST );

            memoryDetails.add(moduleRow);
            moduleNumber++;
        }

        return memoryDetails;
    }
}
