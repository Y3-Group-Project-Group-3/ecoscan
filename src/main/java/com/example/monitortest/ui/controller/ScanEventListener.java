package com.example.monitortest.ui.controller;

import com.example.monitortest.hardware.detection.ScanHardware;
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.HardwareInfoSection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScanEventListener implements ActionListener {

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final ScanHardware hardwareInfoService;


    /**
     * Creates the event listener responsible for coordinating
     * hardware scans and updating the application interface.
     */
    public ScanEventListener(
            JFrame frame,
            CardLayout cardLayout,
            JPanel mainPanel,
            ScanHardware hardwareInfoService) {

        this.frame = frame;
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.hardwareInfoService = hardwareInfoService;
    }



    public void actionPerformed(ActionEvent event){
        // Configure the button
        JButton refreshButton = (JButton) event.getSource();

        refreshButton.setEnabled(false);
        cardLayout.show(mainPanel, "loading");

        SwingWorker<HardwareInfo, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected HardwareInfo doInBackground() {
                        return hardwareInfoService.getHardwareInfo();
                    }

                    @Override
                    protected void done() {
                        try {
                            HardwareInfo info = get();

                            // Update both sections using the same completed scan.
                            HardwareInfoSection.updateHardwareInfo(info);
                            ComponentSection.updateComponentSection(info);

                        } catch (Exception exception) {
                            exception.printStackTrace();
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Failed to retrieve hardware information.",
                                    "Hardware Scan Error",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            refreshButton.setEnabled(true);
                            cardLayout.show(mainPanel, "results");
                        }
                    }
                };
        worker.execute();
    }


}
