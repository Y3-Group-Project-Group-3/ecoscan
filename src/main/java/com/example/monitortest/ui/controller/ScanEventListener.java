package com.example.monitortest.ui.controller;

import com.example.monitortest.hardware.detection.ScanHardware;
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.HardwareInfoSection;
// Hook in loading page
import com.example.monitortest.ui.loading.LoadingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class ScanEventListener implements ActionListener {

    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final ScanHardware hardwareInfoService;


    /**
     * Creates the event listener responsible for coordinating hardware scans and updating the application interface.
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


        /************************ Loading Page **********************/
        // Find the existing loading panel managed by CardLayout.
        LoadingPanel loadingPanel = null;

        for (Component component : mainPanel.getComponents()) {
            if (component instanceof LoadingPanel panel) {
                loadingPanel = panel;
                break;
            }
        }

        // Reset progress and component statuses for a new scan.
        loadingPanel.resetProgress();

        cardLayout.show(mainPanel, "loading");
        final LoadingPanel activeLoadingPanel = loadingPanel;
        // Track completed hardware stages for this scan.
        final Set<String> completedComponents = new HashSet<>();

        SwingWorker<HardwareInfo, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected HardwareInfo doInBackground() {

                        // Perform the hardware scan and receive progress updates.
                        return hardwareInfoService.getHardwareInfo((component, status) -> {

                            // Forward each update to the Swing Event Dispatch Thread.
                            SwingUtilities.invokeLater(() -> {

                                if (activeLoadingPanel != null) {

                                    // Update the component's detection status.
                                    activeLoadingPanel.setPartStatus(component, status);

                                    if ("DETECTING".equals(status)) {
                                        activeLoadingPanel.setCurrentPart(component);
                                    }

                                    // Count each component only once when detection finishes.
                                    if ("DONE".equals(status)) {
                                        completedComponents.add(component);

                                        // Four components, each representing 25% of the scan.
                                        int progress = completedComponents.size() * 25;

                                        activeLoadingPanel.setProgress(progress);
                                    }
                                }
                            });
                        });
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
                            // Stop any remaining loading animation.
                            if (activeLoadingPanel != null) {
                                activeLoadingPanel.stopLoadingAnimation();
                            }

                            // Keep the completed loading page visible for 2 seconds.
                            Timer transitionTimer = new Timer(2000, e -> {
                                // Re-enable scanning and display the results.
                                refreshButton.setEnabled(true);
                                cardLayout.show(mainPanel, "results");
                            });

                            // Run the transition only once.
                            transitionTimer.setRepeats(false);
                            transitionTimer.start();
                        }
                    }
                };
        worker.execute();
    }


}
