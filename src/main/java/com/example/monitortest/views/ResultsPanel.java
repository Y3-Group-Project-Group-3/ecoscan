package com.example.monitortest.views;

import com.example.monitortest.model.HardwareInfo;

import javax.swing.*;
import java.awt.*;

public class ResultsPanel extends JPanel {

    private final JLabel cpuLabel;
    private final JLabel memoryLabel;
    private final JLabel cpuPowerLabel;
    private final JButton refreshButton;

    public ResultsPanel() {
        setLayout(new GridLayout(4, 1, 10, 10));

        refreshButton = new JButton("Refresh Hardware Info");

        cpuLabel = new JLabel("CPU: Not retrieved yet");
        memoryLabel = new JLabel("Memory: Not retrieved yet");
        cpuPowerLabel = new JLabel("CPU Power: Not retrieved yet");

        add(refreshButton);
        add(cpuLabel);
        add(cpuPowerLabel);
        add(memoryLabel);
    }



    public JButton getRefreshButton() {
        return refreshButton;
    }

    public void updateHardwareInfo(HardwareInfo info) {

        cpuLabel.setText("CPU: " + info.getCpuName());


//        cpuPowerLabel.setText(String.format("CPU Power: %.2f w", info.getCpuPower()));
        if (Double.isNaN(info.getCpuPower())) {
            cpuPowerLabel.setText("CPU Power: Not supported");
        } else {
            cpuPowerLabel.setText(
                    String.format("CPU Power: %.1f W", info.getCpuPower())
            );
        }
        memoryLabel.setText( String.format( "Total Memory: %.2f GB", info.getTotalMemory() / (1024.0 * 1024 * 1024) ) );


    }
}
