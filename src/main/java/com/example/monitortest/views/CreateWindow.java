package com.example.monitortest.views;

import com.example.monitortest.model.ScanEventListener;
import com.example.monitortest.model.ScanHardware;
import com.example.monitortest.model.services.PawnIOService;

import javax.swing.*;
import java.awt.*;

public class CreateWindow {

    public void createAndShowGUI() {
        JFrame frame = new JFrame("EcoScan");

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Create the two separate panels.
        HomePanel homePanel = new HomePanel();
        ResultsPanel resultsPanel = new ResultsPanel();
        LoadingPanel loadingPanel = new LoadingPanel();

        // Register screens with the CardLayout.
        mainPanel.add(homePanel, "home");
        mainPanel.add(resultsPanel, "results");
        mainPanel.add(loadingPanel, "loading");

        homePanel.getScanButton().addActionListener(e ->{
            cardLayout.show(mainPanel, "results");
        });

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

        // register the scan button listener
        resultsPanel.getRefreshButton().addActionListener(scanEventListener);


//        Testing the backend - Old
//        PawnIOManager pawnIOManager = new PawnIOManager();
//        System.out.println(
//                "PawnIO Status: " + pawnIOManager.checkPawnIOStatus()
//        );


//        Testing the backend - New
        PawnIOService pawnIOService = new PawnIOService();
        System.out.println( "PawnIO Status: " + pawnIOService.checkPawnIOStatus());

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