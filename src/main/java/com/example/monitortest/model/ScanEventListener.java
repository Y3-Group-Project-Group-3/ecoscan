package com.example.monitortest.model;

import com.example.monitortest.views.ResultsPanel;
import com.example.monitortest.views.ResultsPanelNew;
import com.example.monitortest.views.sections.HardwareInfoSection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScanEventListener implements ActionListener {

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final ResultsPanelNew resultsPanel;
    private final ScanHardware hardwareInfoService;

    public ScanEventListener(
            JFrame frame,
            CardLayout cardLayout,
            JPanel mainPanel,
            ResultsPanelNew resultsPanel,
            ScanHardware hardwareInfoService) {

        this.frame = frame;
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.resultsPanel = resultsPanel;
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
                            // TODO:: Link this to the Refresh hardware button on the new results page
                            HardwareInfoSection.updateHardwareInfo(info);

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
