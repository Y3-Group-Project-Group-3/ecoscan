package com.example.monitortest.views;

import com.example.monitortest.model.ScanEventListener;
import com.example.monitortest.model.ScanHardware;

import com.example.monitortest.views.home.components.NavBar;

import javax.swing.*;
import java.awt.*;

public class CreateWindow {

    public void createAndShowGUI() {
        JFrame frame = new JFrame("EcoScan");
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Create the two separate panels.
        HomePanelNew homePanel = new HomePanelNew();
        ResultsPanelNew resultsPanel = new ResultsPanelNew();
        LoadingPanel loadingPanel = new LoadingPanel();
        HistoryPanel historyPanel = new HistoryPanel();

        // Register screens with the CardLayout.
        mainPanel.add(homePanel, "home");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(loadingPanel, "loading");
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

        // Create the scan button listener
        ScanEventListener scanEventListener = new ScanEventListener(
                frame,
                cardLayout,
                mainPanel,
                resultsPanel,
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
        // TODO:: Can be in its own class
        frame.setContentPane(rootPanel);
        navigate.accept("home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 760);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}

