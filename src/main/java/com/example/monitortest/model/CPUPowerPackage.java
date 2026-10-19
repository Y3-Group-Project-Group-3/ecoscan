package com.example.monitortest.model;
import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

public class CPUPowerPackage{

    public double thing;



    public double getCPUPackagePower() {

        Computer computer = LibreHardwareManager.createInstance(
                ComputerConfig.getInstance().setCpuEnabled(true)
        ).getComputer();

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

                boolean isPowerSensor = sensor.getSensorType()
                        .toString()
                        .equalsIgnoreCase("Power");

                boolean isPackageSensor = sensor.getName()
                        .equalsIgnoreCase("Package");

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


//    public double getCPUPackagePower() {
//        Computer computer = LibreHardwareManager.createInstance(
//                ComputerConfig.getInstance().setCpuEnabled(true)
//        ).getComputer();
//
//        System.out.println("Computer object: " + computer);
//        System.out.println("Hardware list: " + computer.getHardware());
//
//        if (computer.getHardware() != null) {
//            for (Hardware hardware : computer.getHardware()) {
//                System.out.println("Hardware: " + hardware.getName());
//                System.out.println("Type: " + hardware.getHardwareType());
//
//                for (Sensor sensor : hardware.getSensors()) {
//                    System.out.println(
//                            sensor.getName() + " | " +
//                                    sensor.getSensorType() + " | " +
//                                    sensor.getValue()
//                    );
//                }
//            }
//        }
//
//        return thing;
//    }

//    public double getCPUPackagePower() {
//        System.out.println("CPUPowerPackage: " + thing);
//
//        Computer computer = LibreHardwareManager.createInstance( ComputerConfig.getInstance().setCpuEnabled(true)).getComputer();
//        System.out.println(computer);
//
//
////        Debugging
//
//
//        for (Hardware hardware : computer.getHardware()) {
//
//            if (!hardware.getHardwareType().toString()
//                    .equalsIgnoreCase("Cpu")) {
//                continue;
//            }
//
//            System.out.println("CPU: " + hardware);
//
//            for (Sensor sensor : hardware.getSensors()) {
//
//                System.out.println(
//                        "Name: " + sensor.getName()
//                                + " | Type: " + sensor.getSensorType()
//                                + " | Value: " + sensor.getValue()
//                );
//            }
//        }


//        for (Hardware hardware : computer.getHardware()) {
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
////                          return (double) value;
//                            System.out.println(value);
//                            thing = value;
//                        }
//                    }
//                }
//            }
//    return thing;
//    }
}