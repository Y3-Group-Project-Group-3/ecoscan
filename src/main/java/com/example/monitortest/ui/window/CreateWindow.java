package com.example.monitortest.ui.window;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;
import com.example.monitortest.hardware.monitoring.LiveMonitorAvailability;
import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import com.example.monitortest.hardware.monitoring.MacLiveMonitorDemoProvider;
import com.example.monitortest.hardware.detection.ScanHardware;

import com.example.monitortest.services.startup.ApplicationElevationManager;
import com.example.monitortest.services.startup.ApplicationProcessManager;

import com.example.monitortest.ui.history.HistoryPanel;
import com.example.monitortest.ui.history.SessionRecorder;
import com.example.monitortest.ui.home.HomePanel;
import com.example.monitortest.ui.nav.NavBar;
import com.example.monitortest.ui.loading.LoadingPanel;
import com.example.monitortest.ui.results.ResultsPanel;
import com.example.monitortest.ui.livemonitor.LiveMonitorPanel;
import com.example.monitortest.ui.common.InfoTooltip;
import com.example.monitortest.ui.controller.ScanEventListener;

import javax.swing.*;
import java.awt.*;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateWindow {

    public void createAndShowGUI() {
        // Apply EcoScan's shared tooltip appearance.
        InfoTooltip.configureAppearance();

        JFrame frame = new JFrame("EcoScan");
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Create the two separate panels.
        HomePanel homePanel = new HomePanel();
        ResultsPanel resultsPanel = new ResultsPanel();
        LoadingPanel loadingPanel = new LoadingPanel();
        HistoryPanel historyPanel = new HistoryPanel();
        LiveMonitorPanel liveMonitorPanel = new LiveMonitorPanel();


        // Configure the administrator restart action for packaged EcoScan builds.
        liveMonitorPanel.setAdministratorRestartAction(() -> {

            ApplicationElevationManager elevationManager = new ApplicationElevationManager();
            ApplicationProcessManager processManager = new ApplicationProcessManager();

            // Only the packaged Windows executable supports this restart workflow.
            if (!processManager.isPackagedApplication()) {
                return;
            }

            // Confirm that the user wants to restart EcoScan.
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    "EcoScan needs to restart with administrator privileges "
                            + "to enable live monitoring.\n\n"
                            + "Would you like to restart now?",
                    "Restart as Administrator",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            // Request elevation without blocking Swing's Event Dispatch Thread.
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

                @Override
                protected Boolean doInBackground() {
                    return elevationManager.requestAdministratorElevation(processManager.getApplicationExecutable() );
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            // Trigger the normal window-closing sequence so monitoring shuts down.
                            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

                        } else {

                            // Keep EcoScan open when elevation is declined or fails.
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Administrator elevation was cancelled or failed.\n"
                                            + "EcoScan will continue with limited functionality.",
                                    "Administrator Access",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }

                    } catch (Exception ex) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Unable to restart EcoScan as administrator.",
                                "Restart Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };

            worker.execute();
        });


        /************** MAC OS Dummy Interface ***********************************/
        // Determine whether the current environment supports live monitoring.
        LiveMonitorAvailability.Status monitoringStatus = LiveMonitorAvailability.check();

        // Only display the unavailable message when monitoring cannot be shown.
        // macOS retains the existing Live Monitor interface for demonstration data.
        if (monitoringStatus != LiveMonitorAvailability.Status.AVAILABLE && monitoringStatus != LiveMonitorAvailability.Status.MAC_DEMO) {
            liveMonitorPanel.showUnavailableState(monitoringStatus);
        }

        // Populate the macOS Live Monitor with static demonstration readings.
        // The Windows hardware monitoring service remains unchanged.
        if (monitoringStatus == LiveMonitorAvailability.Status.MAC_DEMO) {

            // Clearly identify that this page displays simulated data.
            liveMonitorPanel.showDemoMode();

            // Read the configurable CPU and GPU power values.
            MacLiveMonitorDemoProvider demoProvider = new MacLiveMonitorDemoProvider();
            LiveMonitorSnapshot demoSnapshot = demoProvider.createSnapshot();

            // Populate the existing power cards and stop their loading indicators.
            liveMonitorPanel.updatePowerReadings(demoSnapshot);

            // Keep both demonstration graphs visible using fixed power values.
            liveMonitorPanel.showDemoGraphs(demoSnapshot);

            // Display a static emissions ruler with the PC before the first milestone.
            liveMonitorPanel.showDemoComparison();

            // Display hypothetical carbon emissions for the demo power values.
            liveMonitorPanel.showDemoEmissions(demoSnapshot);
        }



        // Register screens with the CardLayout.
        mainPanel.add(homePanel, "home");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(loadingPanel, "loading");
        mainPanel.add(liveMonitorPanel, "liveMonitor");
        mainPanel.add(historyPanel, "history");

        final NavBar[] navRef = new NavBar[1];

        // Handles navigation between EcoScan's main application pages.
        java.util.function.Consumer<String> navigate = page -> {

            // Reload saved records whenever the History Page is opened.
            if ("history".equals(page)) { historyPanel.refreshHistory(); }

            // Restore the latest scan when Results is opened through navigation.
            if ("results".equals(page)) { resultsPanel.restoreLatestScan(); }

            // Display the selected page.
            cardLayout.show(mainPanel, page);

            // Update the active sidebar navigation indicator.
            navRef[0].setActivePage(page);
        };



        NavBar navBar = new NavBar(navigate);
        navRef[0] = navBar;

        /******************** CSV ***************************/
        // Opens a saved hardware scan from the History Page.
        historyPanel.setViewScanListener(savedScan -> {

            // Display the selected historical hardware information.
            resultsPanel.displaySavedScan(savedScan);

            // Historical results also make the Results Page available.
            navBar.setResultsVisible(true);

            // Switch to the Results Page without starting a new hardware scan.
            cardLayout.show(mainPanel, "results");

            // Highlight Results in the navigation sidebar.
            navBar.setActivePage("results");
        });


        // Main window container
        JPanel rootPanel = new JPanel(new BorderLayout());

        // Sidebar stays on the left
        rootPanel.add(navBar, BorderLayout.WEST);

        // Pages switch within the centre
        rootPanel.add(mainPanel, BorderLayout.CENTER);


        // Create the hardware service.
        ScanHardware hardwareInfoService = new ScanHardware();

        // Create the live power monitoring service.
        // Forward each new power snapshot to the Live Monitor page.
        // Update both power measurements and emissions after each snapshot.
        final LiveMonitoringService[] monitoringRef = new LiveMonitoringService[1];

        // Create the real monitoring service only on supported Windows systems.
        // macOS uses static demonstration data and does not require hardware sensors.
        LiveMonitoringService monitoringService = monitoringStatus == LiveMonitorAvailability.Status.AVAILABLE ? new LiveMonitoringService(snapshot -> {
            // Update the CPU/GPU cards and graphs.
            liveMonitorPanel.updatePowerReadings(snapshot);

            // Retrieve the emissions accumulated by the monitoring service.
            CarbonEmissionsTracker tracker = monitoringRef[0].getEmissionsTracker();

            // Update the carbon emissions panel.
            liveMonitorPanel.updateCarbonEmissions( tracker.getTotalEmissionsGrams(), tracker.getAverageEmissionsPerSecond() );

        }) : null;

        monitoringRef[0] = monitoringService;

        // Provide the navigation bar so scanning can temporarily lock navigation.
        ScanEventListener scanEventListener = new ScanEventListener(
                frame,
                cardLayout,
                mainPanel,
                hardwareInfoService,
                navBar,
                resultsPanel
        );

        // Connect the Home page Scan Computer button
        homePanel.getScanButton().addActionListener(scanEventListener);

        // register the scan button listener
        resultsPanel.getRefreshButton().addActionListener(scanEventListener);

        // Navigate to the Live Monitor page from the Home page.
        homePanel.getLiveMonitorButton().addActionListener(e -> {
            navigate.accept("liveMonitor");
        });

        // Start on the home screen
        navigate.accept("home");

        // Setup the main window
        frame.setContentPane(rootPanel);
        navigate.accept("home");

        // Start recording this Live Monitor session.
        // The session is saved to History when EcoScan closes.
        SessionRecorder sessionRecorder = new SessionRecorder();

        // Prevent multiple shutdown requests from running simultaneously.
        final boolean[] shuttingDown = {false};

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                // Ignore additional close requests while shutdown is underway.
                if (shuttingDown[0]) {
                    return;
                }


                shuttingDown[0] = true;

                // Save this session's total emissions before monitoring stops.
                sessionRecorder.saveSession( monitoringService, monitoringStatus == LiveMonitorAvailability.Status.MAC_DEMO );

                // Prevent new manual hardware scans during shutdown.
                scanEventListener.beginShutdown();

                // Stop real monitoring only when the service exists.
                // macOS Demo Mode does not create a monitoring service.
                if (monitoringService != null) {
                    monitoringService.stop();
                }

                // Wait for monitoring to finish without blocking Swing.
                SwingWorker<Boolean, Void> shutdownWorker = new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground() {


                        // Wait for live monitoring only when the Windows service exists.
                        // No monitoring service means there is nothing to stop.
                        boolean monitoringStopped = true;

                        if (monitoringService != null) {
                            monitoringStopped = monitoringService.awaitTermination( 10, java.util.concurrent.TimeUnit.SECONDS );
                        }

                        // Wait for any active manual hardware scan to finish.
                        while (scanEventListener.isScanRunning()) {

                            try {
                                Thread.sleep(200);
                            }
                            catch (InterruptedException exception) {

                                // Preserve the interrupted status.
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }

                        // Do not close PowerShell if monitoring is still active.
                        if (!monitoringStopped) {
                            System.err.println( "PowerShell shutdown skipped: live monitoring is still active." );
                            return false;
                        }

                        try {
                            return true;
                        }
                        catch (Exception exception) {
                            System.err.println( "Failed to close PowerShell session: " + exception.getMessage() );
                            return false;
                        }
                    }

                    @Override
                    protected void done() {

                        try {
                            // Retrieve the result of the background shutdown process.
                            boolean shutdownSuccessful = get();

                            if (shutdownSuccessful) { System.out.println("EcoScan shutdown completed successfully."); }
                            else {
                                System.err.println( "EcoScan shutdown was incomplete. PowerShell may still be active." );
                            }
                        }
                        catch (Exception exception) {
                            System.err.println( "Error during EcoScan shutdown: " + exception.getMessage() );
                        }

                        // Close the application after the shutdown attempt.
                        frame.dispose();
                        System.exit(0);
                    }
                };

                shutdownWorker.execute();
            }
        });

        // Allow EcoScan to finish background operations before exiting.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1280, 760);
        frame.setLocationRelativeTo(null);
        // Begin collecting power readings automatically.
        frame.setVisible(true);

        // Start live sensor retrieval only when the environment is supported.
        if (monitoringStatus == LiveMonitorAvailability.Status.AVAILABLE) {
            monitoringService.start();
        }
    }

}