package com.example.monitortest.views;

import com.example.monitortest.model.ScanEventListener;
import com.example.monitortest.model.ScanHardware;

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



//package com.example.monitortest.views;

//
//import com.example.monitortest.model.CPUPowerPackage;
//import com.example.monitortest.Main;
//import com.example.monitortest.model.HardwareInfo;
//import com.example.monitortest.model.ScanHardware;
//import oshi.hardware.CentralProcessor;
//import oshi.hardware.GlobalMemory;
//import oshi.hardware.HardwareAbstractionLayer;
//import oshi.SystemInfo;
//
//import com.example.monitortest.model.HardwareInfo;
//import javax.swing.*;
//import java.awt.*;
//
//
//
//public class CreateWindow {
//
//    private JPanel createLoadingPanel() {
//
//        JPanel panel = new JPanel();
//
//        panel.setLayout(
//                new BoxLayout(panel, BoxLayout.Y_AXIS)
//        );
//
//        JLabel message = new JLabel(
//                "Retrieving hardware information..."
//        );
//
//        message.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        JProgressBar progressBar = new JProgressBar();
//
//        progressBar.setIndeterminate(true);
//
//        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        progressBar.setMaximumSize(
//                new Dimension(300, 20)
//        );
//
//        panel.add(Box.createVerticalGlue());
//        panel.add(message);
//        panel.add(Box.createVerticalStrut(15));
//        panel.add(progressBar);
//        panel.add(Box.createVerticalGlue());
//
//        return panel;
//    }
//
//    private JPanel createResultsPanel() {
//
//        JFrame frame = new JFrame("Hardware Monitor");
//        JPanel panel = new JPanel();
//
//        panel.setLayout(
//                new BoxLayout(panel, BoxLayout.Y_AXIS)
//        );
//
////        JButton refreshButton = new JButton("Refresh Hardware Info");
//        JLabel cpuLabel = new JLabel("CPU: Not retrieved yet");
//        JLabel memoryLabel = new JLabel("Memory: Not retrieved yet");
//        JLabel cpuPowerLabel = new JLabel("CPU Power: Not retrieved yet");
//
//
//
////            String cpuName = scanHardware.processor.getProcessorIdentifier().getName();
////            long totalMemory = scanHardware.memory.getTotal();
////            double totalMemoryGB = totalMemory / (1024.0 * 1024 * 1024);
////
////            cpuLabel.setText("CPU: " + cpuName);
////            cpuPowerLabel.setText("CPUPower: " + String.format( "%.1f", scanHardware.powerPackage.thing));
////            memoryLabel.setText( String.format("Total Memory: %.2f GB",totalMemoryGB));
//
//        cpuLabel.setText("CPU: Not retrieved yet");
//
//        frame.add(panel);
//        frame.setSize(600, 250);
//        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//
//        return panel;
//    }
//
//    public void createAndShowGUI() {
//        System.out.println("Creating Window...");
//
//
//        JFrame frame = new JFrame("Hardware Monitor");
//        CardLayout cardLayout = new CardLayout();
//        JPanel panel = new JPanel(cardLayout);
//
//        panel.add(createResultsPanel(), "results");
//        panel.add(createLoadingPanel(),"loading");
//
//        JButton refreshButton = new JButton("Refresh Hardware Info");
//
//        JLabel cpuLabel = new JLabel("CPU: Not retrieved yet");
//        JLabel memoryLabel = new JLabel("Memory: Not retrieved yet");
//        JLabel cpuPowerLabel = new JLabel("CPU Power: Not retrieved yet");
//
//
//        panel.setLayout(new GridLayout(4, 1, 10, 10));
//
//        panel.add(refreshButton);
//        panel.add(cpuLabel);
//        panel.add(cpuPowerLabel);
//        panel.add(memoryLabel);
//
//
//
//
//
////Scan Hardware
//        ScanHardware hardwareInfoService =
//        new ScanHardware();
//
//        refreshButton.addActionListener(event -> {
//
//            refreshButton.setEnabled(false);
//            cardLayout.show(panel, "loading");
//
//            SwingWorker<HardwareInfo, Void> worker =
//                    new SwingWorker<>() {
//
//                        @Override
//                        protected HardwareInfo doInBackground() {
//
//                            return hardwareInfoService.getHardwareInfo();
//                        }
//
//                        @Override
//                        protected void done() {
//
//                            try {
//
//                                HardwareInfo info = get();
//
//                                cpuLabel.setText(
//                                        "CPU: " + info.getCpuName()
//                                );
//
//                                memoryLabel.setText(
//                                        String.format(
//                                                "Total Memory: %.2f GB",
//                                                info.getTotalMemory()
//                                                        / (1024.0 * 1024 * 1024)
//                                        )
//                                );
//                            } catch (Exception exception) {
//
//                                exception.printStackTrace();
//
//                            }
//                            cardLayout.show(panel, "results");
//                        }
//                    };
//
//            worker.execute();
//        });
//
////        refreshButton.addActionListener(event -> {
////
////
////            refreshButton.setEnabled(false);
////
//////            Display the loading panel
////
////            ScanHardware scanHardware = new ScanHardware();
////            scanHardware.scanHardware();
////
//////            SystemInfo systemInfo = new SystemInfo();
//////            HardwareAbstractionLayer hardware = systemInfo.getHardware();
//////            CentralProcessor processor = hardware.getProcessor();
//////            GlobalMemory memory = hardware.getMemory();
////////          Power Package
//////            CPUPowerPackage powerPackage = new CPUPowerPackage();
//////            powerPackage.getCPUPackagePower();
////
////
////            String cpuName = scanHardware.processor.getProcessorIdentifier().getName();
////            long totalMemory = scanHardware.memory.getTotal();
////            double totalMemoryGB = totalMemory / (1024.0 * 1024 * 1024);
////
////            cpuLabel.setText("CPU: " + cpuName);
////            cpuPowerLabel.setText("CPUPower: " + String.format( "%.1f", scanHardware.powerPackage.thing));
////            memoryLabel.setText( String.format("Total Memory: %.2f GB",totalMemoryGB));
////        });
//
////        frame.add(panel);
////        frame.setSize(600, 600);
////        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
////        frame.setLocationRelativeTo(null);
////        frame.setVisible(true);
//    }
//}
//
