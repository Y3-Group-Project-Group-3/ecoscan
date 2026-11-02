package com.example.monitortest.ui.window;

import com.example.monitortest.ui.controller.ScanEventListener;
import com.example.monitortest.hardware.detection.ScanHardware;

import com.example.monitortest.ui.history.HistoryPanel;
import com.example.monitortest.ui.home.HomePanel;
import com.example.monitortest.ui.nav.NavBar;
import com.example.monitortest.ui.loading.LoadingPanel;
import com.example.monitortest.ui.results.ResultsPanel;
import com.example.monitortest.ui.livemonitor.LiveMonitorPanel;

import javax.swing.*;
import java.awt.*;

import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateWindow {

    public void createAndShowGUI() {
        JFrame frame = new JFrame("EcoScan");
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Create the two separate panels.
        HomePanel homePanel = new HomePanel();
        ResultsPanel resultsPanel = new ResultsPanel();
        LoadingPanel loadingPanel = new LoadingPanel();
        HistoryPanel historyPanel = new HistoryPanel();
        LiveMonitorPanel liveMonitorPanel = new LiveMonitorPanel();

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
        LiveMonitoringService monitoringService = new LiveMonitoringService(
                liveMonitorPanel::updatePowerReadings
        );

        // Create the scan button listener
        ScanEventListener scanEventListener = new ScanEventListener(
                frame,
                cardLayout,
                mainPanel,
                hardwareInfoService
        );

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
        // start monitoring independently of which navigation page is selected.
        monitoringService.start();
    }

}

