package com.example.monitortest.views;

import com.example.monitortest.model.ScanEventListener;
import com.example.monitortest.model.ScanHardware;

import javax.swing.*;
import java.awt.*;

public class CreateWindow {

   // card borders

    public void createAndShowGUI() {
        JFrame frame = new JFrame("EcoScan");

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Create the two separate panels.
//        HomePanel homePanel = new HomePanel();
        HomePanelNew homePanel = new HomePanelNew();
        ResultsPanelNew resultsPanel = new ResultsPanelNew();
        LoadingPanel loadingPanel = new LoadingPanel();
        HistoryPanel historyPanel = new HistoryPanel();

        // Register screens with the CardLayout.
        mainPanel.add(homePanel, "home");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(loadingPanel, "loading");
        mainPanel.add(historyPanel, "history");

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

        // Connect the Home Page to History Page button
        homePanel.getHistoryButton().addActionListener(e -> {
            cardLayout.show(mainPanel, "history");
        });

        // Temp Back button
        historyPanel.getBackButton().addActionListener(e -> {
            cardLayout.show(mainPanel, "home");
        });
        // Temp Back button
        resultsPanel.getBackButton().addActionListener(e -> {
            cardLayout.show(mainPanel, "home");
        });

        // Start on the home screen
        cardLayout.show(mainPanel, "home");

        // Setup the main window
        // TODO:: Can be in its own class
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}