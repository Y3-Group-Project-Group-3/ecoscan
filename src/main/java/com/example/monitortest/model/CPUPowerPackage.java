//package com.example.monitortest.model;
//import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
//import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
//import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;
//
//public class CPUPowerPackage{
//
//    public double getCPUPackagePower() {
//
//        String operatingSystem = System.getProperty("os.name");
//
//        if (!operatingSystem.toLowerCase().contains("win")) {
//            return Double.NaN;
//        }
//
//
//        Computer computer = LibreHardwareManager.createInstance( ComputerConfig.getInstance().setCpuEnabled(true) ).getComputer();
//
//        if (computer.getHardware() == null) {
//            System.out.println("No hardware information retrieved.");
//            return Double.NaN;
//        }
//
//        for (Hardware hardware : computer.getHardware()) {
//
//            if (!hardware.getHardwareType().toString()
//                    .equalsIgnoreCase("Cpu")) {
//                continue;
//            }
//
//            for (Sensor sensor : hardware.getSensors()) {
//
//                boolean isPowerSensor = sensor.getSensorType().toString().equalsIgnoreCase("Power");
//                boolean isPackageSensor = sensor.getName().equalsIgnoreCase("Package");
//                if (isPowerSensor && isPackageSensor) {
//
//                    double value = sensor.getValue();
//
//                    System.out.println("CPU Package Power: " + value);
//
//                    if (Double.isFinite(value) && value >= 0) {
//                        return value;
//                    }
//                }
//            }
//        }
//
//        System.out.println("CPU package power sensor not found.");
//        return Double.NaN;
//    }
//}

package com.example.monitortest.model;
import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.powershell.PowerShellOperations;

public class CPUPowerPackage{

    public double getCPUPackagePower() {


        // Windows Debugging Test
//        ComputerConfig config =
//                ComputerConfig.getInstance().setCpuEnabled(true);

//        String rawJson =
//                PowerShellOperations.GET.getComputerJsonData(config);
//
//        System.out.println("=== RAW POWERSHELL OUTPUT ===");
//        System.out.println(rawJson == null
//                ? "NULL"
//                : rawJson.isBlank()
//                ? "EMPTY"
//                : rawJson);
//        System.out.println("=============================");

        String operatingSystem = System.getProperty("os.name");

        if (!operatingSystem.toLowerCase().contains("win")) {
            return Double.NaN;
        }


        Computer computer = LibreHardwareManager.createInstance( ComputerConfig.getInstance().setCpuEnabled(true) ).getComputer();

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

        System.out.println("CPU package power sensor not found.");
        return Double.NaN;
    }
}