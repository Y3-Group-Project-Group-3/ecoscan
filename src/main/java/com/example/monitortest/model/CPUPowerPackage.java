package com.example.monitortest.model;
import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

// Debugging
import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.powershell.PowerShellOperations;

public class CPUPowerPackage{

    public double getCPUPackagePower() {



        String operatingSystem = System.getProperty("os.name");

        System.out.println("Operating System: " + operatingSystem);
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));

        if (!operatingSystem.toLowerCase().contains("win")) {
            return Double.NaN;
        }

        try {
            System.out.println("Initialising LibreHardwareMonitor...");

            Computer computer = LibreHardwareManager.createInstance( ComputerConfig.getInstance().setCpuEnabled(true)).getComputer();

            if (computer == null) {
                System.out.println("Computer object is null.");
                return Double.NaN;
            }

            if (computer.getHardware() == null) {
                System.out.println("Hardware list is null.");
                return Double.NaN;
            }

            System.out.println(
                    "Hardware devices found: " + computer.getHardware().size()
            );



        //            Computer computer = LibreHardwareManager.createInstance( ComputerConfig.getInstance().setCpuEnabled(true) ).getComputer();

                if (computer.getHardware() == null) {
                    System.out.println("No hardware information retrieved.");
                    return Double.NaN;
                }

                for (Hardware hardware : computer.getHardware()) {

                    if (!hardware.getHardwareType().toString()
                            .equalsIgnoreCase("Cpu")) {
                        continue;
                    }

                    for (Sensor sensor : hardware.getSensors()) {

                        System.out.println("Hello??");
                        // Debugging
                        System.out.println(
                                "Sensor: " + sensor.getName()
                                        + " | Type: " + sensor.getSensorType()
                                        + " | Value: " + sensor.getValue()
                        );

                        boolean isPowerSensor = sensor.getSensorType().toString()
                                .equalsIgnoreCase("Power");

                        boolean isPackageSensor =
                                sensor.getName().equalsIgnoreCase("Package")
                                        || sensor.getName().equalsIgnoreCase("CPU Package");

                        if (isPowerSensor && isPackageSensor) {

                            double value = sensor.getValue();

                            System.out.println("CPU Package Power: " + value);

                            if (Double.isFinite(value) && value >= 0) {
                                return value;
                            }
                        }
                    }
                }

        } catch (Exception e) {
            System.err.println("LibreHardwareMonitor failed:");
            e.printStackTrace();
        }

        return Double.NaN;
    }

//        System.out.println("CPU package power sensor not found.");
//        return Double.NaN;
    }

//}
