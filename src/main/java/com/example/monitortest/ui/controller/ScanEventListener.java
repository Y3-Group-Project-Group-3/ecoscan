package com.example.monitortest.ui.controller;

import com.example.monitortest.hardware.detection.ScanHardware;
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.HardwareInfoSection;
import com.example.monitortest.ui.nav.NavBar;
// Hook in loading page
import com.example.monitortest.ui.loading.LoadingPanel;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class ScanEventListener implements ActionListener {

    // Number of hardware components included in the scan.
    private static final int TOTAL_COMPONENTS = 4;

    // Stores a hardware component and its reported scanning status.
    private record ScanProgress(String component, String status) {
    }


    private final JFrame frame;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final ScanHardware hardwareInfoService;
    // Tracks the currently running hardware scan.
    private volatile boolean scanRunning = false;
    // Prevents new hardware scans once application shutdown begins.
    private volatile boolean shuttingDown = false;
    // Controls navigation availability while hardware scanning is active.
    private final NavBar navBar;

    /**
     * Creates the event listener responsible for coordinating hardware scans and updating the application interface.
     */
    public ScanEventListener(
            JFrame frame,
            CardLayout cardLayout,
            JPanel mainPanel,
            ScanHardware hardwareInfoService,
            NavBar navBar) {

        this.frame = frame;
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.hardwareInfoService = hardwareInfoService;
        this.navBar = navBar;
    }

    // Reports whether a hardware scan is currently active.
    public boolean isScanRunning() {
        return scanRunning;
    }

    // Prevents additional hardware scans during application shutdown.
    public void beginShutdown() {
        shuttingDown = true;
    }



    public void actionPerformed(ActionEvent event){

        // Ignore scan requests during shutdown or while another scan is running.
        if (shuttingDown || scanRunning) {
            return;
        }

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

        // Stop the scan if the loading panel cannot be found.
        if (loadingPanel == null) {
            refreshButton.setEnabled(true);
            JOptionPane.showMessageDialog(frame, "The loading screen could not be found.", "Hardware Scan Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Reset progress and component statuses for a new scan.
        loadingPanel.resetProgress();


        cardLayout.show(mainPanel, "loading");
        final LoadingPanel activeLoadingPanel = loadingPanel;
        // Track completed hardware stages for this scan.
        final Set<String> completedComponents = new HashSet<>();

        // Run the hardware scan in the background and report progress events.
        SwingWorker<HardwareInfo, ScanProgress> worker =
                new SwingWorker<>() {

                    @Override
                    protected HardwareInfo doInBackground() {
                        try {
                            // Perform the hardware scan and publish component status updates.
                            return hardwareInfoService.getHardwareInfo((component, status) -> {
                                publish(new ScanProgress(component, status));
                            });
                        }
                        finally {
                            // The background hardware operation has finished.
                            scanRunning = false;
                        }
                    }


                    @Override
                    protected void process(java.util.List<ScanProgress> chunks) {

                        // Process hardware status updates on the Swing EDT.
                        for (ScanProgress progressEvent : chunks) {

                            String component = progressEvent.component();
                            String status = progressEvent.status();

                            // Update the hardware row's detection status.
                            activeLoadingPanel.setPartStatus(component, status);

                            // Display the component currently being scanned.
                            if ("DETECTING".equals(status)) {
                                activeLoadingPanel.setCurrentPart(component);
                            }

                            // Update progress when a component finishes scanning.
                            if ("DONE".equals(status)) {

                                // Count each component only once.
                                completedComponents.add(component);

                                // Calculate the overall scan percentage.
                                int progress = Math.min( 100, completedComponents.size() * 100 / TOTAL_COMPONENTS );

                                activeLoadingPanel.setProgress(progress);
                            }
                        }
                    }


                    @Override
                    protected void done() {

                        try {
                            // Retrieve the completed hardware scan results.
                            HardwareInfo info = get();

                            // Ensure the progress display reaches 100% after a successful scan.
                            activeLoadingPanel.setProgress(100);

                            // Update the results sections with the latest hardware information.
                            HardwareInfoSection.updateHardwareInfo(info);
                            ComponentSection.updateComponentSection(info);

                            // Keep the completed loading page visible for two seconds.
                            Timer transitionTimer = new Timer(2000, e -> {

                                // Make Results available after a successful scan.
                                navBar.setResultsVisible(true);

                                // Display the completed hardware scan results.
                                cardLayout.show(mainPanel, "results");
                                navBar.setActivePage("results");

                                // Restore scanning and navigation after the loading transition.
                                refreshButton.setEnabled(true);
                                navBar.setNavigationEnabled(true);
                            });

                            // Ensure the transition happens only once.
                            transitionTimer.setRepeats(false);
                            transitionTimer.start();

                        } catch (Exception exception) {

                            // Report the scan failure without navigating to outdated results.
                            exception.printStackTrace();

                            JOptionPane.showMessageDialog(
                                    frame, "Failed to retrieve hardware information.", "Hardware Scan Error", JOptionPane.ERROR_MESSAGE
                            );

                            // On Failure
                            // Return to the Home page without displaying outdated scan results.
                            cardLayout.show(mainPanel, "home");
                            navBar.setActivePage("home");

                            // Restore scanning and navigation after a failed scan.
                            refreshButton.setEnabled(true);
                            navBar.setNavigationEnabled(true);
                        }
                    }

                };
        // Mark the scan as active and prevent navigation during scanning.
        scanRunning = true;
        navBar.setNavigationEnabled(false);

        worker.execute();
    }


}
