package com.example.monitortest.ui.window;

import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;
import com.example.monitortest.hardware.monitoring.LiveMonitorAvailability;
import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import com.example.monitortest.hardware.detection.ScanHardware;

import com.example.monitortest.services.startup.ApplicationElevationManager;
import com.example.monitortest.services.startup.ApplicationProcessManager;

import com.example.monitortest.ui.history.HistoryPanel;
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

                            // An elevated instance has been launched successfully.
                            // Close the current instance.
                            frame.dispose();

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


        // Determine whether the current environment supports live monitoring.
        LiveMonitorAvailability.Status monitoringStatus = LiveMonitorAvailability.check();
        // Display an explanation when live monitoring is unavailable.
        if (monitoringStatus != LiveMonitorAvailability.Status.AVAILABLE) {
            liveMonitorPanel.showUnavailableState(monitoringStatus);
        }

        // Register screens with the CardLayout.
        mainPanel.add(homePanel, "home");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(loadingPanel, "loading");
        mainPanel.add(liveMonitorPanel, "liveMonitor");
        mainPanel.add(historyPanel, "history");

        final NavBar[] navRef = new NavBar[1];

        java.util.function.Consumer<String> navigate = page -> {
            cardLayout.show(mainPanel, page);
            navRef[0].setActivePage(page);
        };

        NavBar navBar = new NavBar(navigate);
        navRef[0] = navBar;
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

        LiveMonitoringService monitoringService = new LiveMonitoringService(snapshot -> {
            // Update the CPU/GPU cards and graphs.
            liveMonitorPanel.updatePowerReadings(snapshot);

            // Retrieve the emissions accumulated by the monitoring service.
            CarbonEmissionsTracker tracker = monitoringRef[0].getEmissionsTracker();

            // Update the carbon emissions panel.
            liveMonitorPanel.updateCarbonEmissions( tracker.getTotalEmissionsGrams(), tracker.getAverageEmissionsPerSecond() );

        });

        monitoringRef[0] = monitoringService;

        // Create the scan button listener
        ScanEventListener scanEventListener = new ScanEventListener( frame, cardLayout, mainPanel, hardwareInfoService );

        // Connect the Home page Scan Computer button
        homePanel.getScanButton().addActionListener(scanEventListener);

        // register the scan button listener
        resultsPanel.getRefreshButton().addActionListener(scanEventListener);

        homePanel.getHistoryButton().addActionListener(e -> navigate.accept("history"));

        // Start on the home screen
        navigate.accept("home");

        // Setup the main window
        frame.setContentPane(rootPanel);
        navigate.accept("home");

        // Stop background monitoring when the application closes.
        // ensures the scheduler receives a shutdown request when ecoscan closes
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                monitoringService.stop();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 760);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // Begin collecting power readings automatically.
        frame.setVisible(true);

        // Start live sensor retrieval only when the environment is supported.
        if (monitoringStatus == LiveMonitorAvailability.Status.AVAILABLE) {
            monitoringService.start();
        }
    }

}

