package com.example.monitortest.views.sections;

import com.example.monitortest.model.HardwareInfo;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.GREEN;
import static com.example.monitortest.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.theme.UIUtility.*;
import static com.example.monitortest.theme.UIUtility.label;

public class HardwareInfoSection {
    // Power Info Labels
    public static JLabel cpuLabel;
    public static JLabel memoryLabel;
    public static JLabel cpuPowerLabel;

    public static JPanel createHardwareInfoSection() {

        // Identify simulated CPU power readings on macOS.

        JPanel section = column();

        section.add(label(
                "DETECTED HARDWARE",
                Font.BOLD,
                12,
                GREEN
        ));

        section.add(Box.createVerticalStrut(14));

        JPanel hardwareCard = card();
        hardwareCard.setLayout(new BoxLayout(hardwareCard, BoxLayout.Y_AXIS));

        cpuLabel = label("CPU: Not retrieved yet", Font.PLAIN, 13, TEXT_MAIN);
        memoryLabel = label("Memory: Not retrieved yet", Font.PLAIN, 13, TEXT_MAIN);
        cpuPowerLabel = label("CPU Power: Not retrieved yet", Font.PLAIN, 13, TEXT_MAIN);



        hardwareCard.add(cpuLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(memoryLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(cpuPowerLabel);

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

        // Convert total memory from bytes to gigabytes.
        memoryLabel.setText( String.format( "Total Memory: %.2f GB", info.getTotalMemory() / (1024.0 * 1024 * 1024))
        );
    }

}
