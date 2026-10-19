package com.example.monitortest;
//package com.example.monitortest;
//import java.util.List;
//
//import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
//import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;
//import oshi.SystemInfo;
//import oshi.hardware.*;
//import oshi.software.os.OperatingSystem;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//public class Main extends Application {
//
//    @Override
//    public void start(Stage stage) {
//
//        // Collect hardware information using OSHI.
//        SystemInfo systemInfo = new SystemInfo();
//
//        HardwareAbstractionLayer hardware =
//                systemInfo.getHardware();
//
//        OperatingSystem operatingSystem =
//                systemInfo.getOperatingSystem();
//
//        CentralProcessor processor =
//                hardware.getProcessor();
//
//        GlobalMemory memory =
//                hardware.getMemory();
//
//        ComputerSystem computer =
//                hardware.getComputerSystem();
//
//        // Create the window title.
//        Label title = new Label("PC Hardware Monitor");
//        title.setStyle(
//                "-fx-font-size: 24px; -fx-font-weight: bold;"
//        );
//
//        // Create labels displaying the hardware details.
//        Label manufacturer = new Label(
//                "Manufacturer: " + computer.getManufacturer()
//        );
//
//        Label model = new Label(
//                "Model: " + computer.getModel()
//        );
//
//        Label cpu = new Label(
//                "Processor: "
//                        + processor.getProcessorIdentifier().getName()
//        );
//        Label cores = new Label(
//                "Physical cores: "
//                        + processor.getPhysicalProcessorCount()
//                        + " | Logical processors: "
//                        + processor.getLogicalProcessorCount()
//        );
//
//        // Get CPU Power
//        Sensors sensors =
//                systemInfo.getHardware().getSensors();
//
//        double cpuVoltage = sensors.getCpuVoltage();
//
//        if(Double.isFinite(cpuVoltage) && cpuVoltage > 0) {
//            System.out.printf(
//                    "CPU Voltage: %.3f V%n",
//                    cpuVoltage
//            );
//        }else{
//            System.out.println("CPU Volate Not Available :(");
//        }
//
//        Label voltateLabel = new Label("CPU Voltage: " + cpuVoltage);
//
//        double totalRamGB =
//                memory.getTotal() / (1024.0 * 1024 * 1024);
//
//        double availableRamGB =
//                memory.getAvailable() / (1024.0 * 1024 * 1024);
//
//
//        Label ram = new Label(
//                String.format(
//                        "RAM: %.2f GB total, %.2f GB available",
//                        totalRamGB,
//                        availableRamGB
//                )
//        );
//
//        Label os = new Label(
//                "Operating system: " + operatingSystem
//        );
//
//        Label cpuPowerLabel = new Label("CPU Package Power: Unavailable");
//
//        CpuPowerService powerService = new CpuPowerService();
//
//        Double power = powerService.getCpuPackagePower();
//
//        if (power != null) {
//            cpuPowerLabel.setText(
//                    String.format("CPU Package Power: %.2f W", power)
//            );
//        }
//
//        // Arrange the controls vertically.
//        VBox layout = new VBox(
//                12,
//                title,
//                manufacturer,
//                model,
//                cpu,
//                cores,
//                ram,
//                os,
//                voltateLabel,
//                cpuPowerLabel
//        );
//
//        layout.setPadding(new Insets(24));
//
//        // Create the scene and display the window.
//        Scene scene = new Scene(layout, 650, 350);
//
//        stage.setTitle("Hardware Monitor");
//        stage.setScene(scene);
//        stage.setMinWidth(500);
//        stage.setMinHeight(300);
//        stage.show();
//    }
//
//
//
//
//
//// Use IntelliJ's auto-import feature for these library classes:
//// LibreHardwareManager, ComputerConfig, Computer, Hardware, Sensor
//    public class CpuPowerService {
//
//        public Double getCpuPackagePower() {
//
//            Computer computer =
//                    LibreHardwareManager.createInstance(
//                            ComputerConfig.getInstance()
//                                    .setCpuEnabled(true)
//                    ).getComputer();
//
//            for (Hardware hardware : computer.getHardware()) {
//
//                // Only inspect CPU hardware.
//                if (!hardware.getHardwareType().toString()
//                        .equalsIgnoreCase("Cpu")) {
//                    continue;
//                }
//
//                // Search the CPU's sensors for package power.
//                for (Sensor sensor : hardware.getSensors()) {
//
//                    boolean isPowerSensor =
//                            sensor.getSensorType().toString()
//                                    .equalsIgnoreCase("Power");
//
//                    boolean isPackageSensor =
//                            sensor.getName().toLowerCase()
//                                    .contains("package");
//
//                    if (isPowerSensor && isPackageSensor) {
//
//                        float value = (float) sensor.getValue();
//
//                        if (Float.isFinite(value) && value >= 0) {
//                            return (double) value;
//                        }
//                    }
//                }
//            }
//
//            // No suitable sensor was found.
//            return null;
//        }
//    }
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
//

// Java Swing
import com.example.monitortest.views.CreateWindow;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            CreateWindow window = new CreateWindow();
            window.createAndShowGUI();



//            JFrame frame = new JFrame("Hardware Monitor");
//
//            JButton refreshButton =
//                    new JButton("Refresh Hardware Info");
//
//            JLabel cpuLabel =
//                    new JLabel("CPU: Not retrieved yet");
//
//            JLabel memoryLabel =
//                    new JLabel("Memory: Not retrieved yet");
//
//            JLabel cpuPowerLabel = new JLabel("CPU Power: Not retrieved yet");
//
//            JPanel panel = new JPanel();
//
//            panel.setLayout(new GridLayout(4, 1, 10, 10));
//
//            panel.add(refreshButton);
//            panel.add(cpuLabel);
//            panel.add(cpuPowerLabel);
//            panel.add(memoryLabel);
//
//
//            // Create the loading screen.
//            JPanel loadingPanel = new JPanel();
//            loadingPanel.setLayout(new GridBagLayout());
//
//            JPanel loadingContent = new JPanel();
//            loadingContent.setLayout(
//                    new BoxLayout(loadingContent, BoxLayout.Y_AXIS)
//            );
//
//            JLabel loadingLabel = new JLabel(
//                    "Retrieving hardware information..."
//            );
//
//            loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            JProgressBar progressBar = new JProgressBar();
//            progressBar.setIndeterminate(true);
//
//            progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
//            progressBar.setMaximumSize(new Dimension(300, 20));
//
//            loadingContent.add(loadingLabel);
//            loadingContent.add(Box.createVerticalStrut(15));
//            loadingContent.add(progressBar);
//
//            loadingPanel.add(loadingContent);
//
////Scan Hardware
//            refreshButton.addActionListener(event -> {
//
//                SystemInfo systemInfo = new SystemInfo();
//
//                HardwareAbstractionLayer hardware =
//                        systemInfo.getHardware();
//
//                CentralProcessor processor =
//                        hardware.getProcessor();
//
//                GlobalMemory memory =
//                        hardware.getMemory();
//
////                Power Package
//                CPUPowerPackage powerPackage = new CPUPowerPackage();
//                powerPackage.getCPUPackagePower();
//
//                String cpuName =
//                        processor.getProcessorIdentifier().getName();
//
//                long totalMemory = memory.getTotal();
//
//                double totalMemoryGB =
//                        totalMemory / (1024.0 * 1024 * 1024);
//
//                cpuLabel.setText("CPU: " + cpuName);
//                cpuPowerLabel.setText("CPUPower: " +
//                        String.format(
//                                "%.1f",
//                                powerPackage.thing
//                        )
//                );
//
//                memoryLabel.setText(
//                        String.format(
//                                "Total Memory: %.2f GB",
//                                totalMemoryGB
//                        )
//                );
//            });
//
//            frame.add(panel);
//
//            frame.setSize(600, 250);
//
//            frame.setDefaultCloseOperation(
//                    JFrame.EXIT_ON_CLOSE
//            );
//
//            frame.setLocationRelativeTo(null);
//
//            frame.setVisible(true);
        });
    }
}