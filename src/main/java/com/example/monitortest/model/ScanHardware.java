package com.example.monitortest.model;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class ScanHardware {

    public HardwareInfo getHardwareInfo() {

        SystemInfo systemInfo = new SystemInfo();

        HardwareAbstractionLayer hardware = systemInfo.getHardware();

        CentralProcessor processor = hardware.getProcessor();

        GlobalMemory memory = hardware.getMemory();

        CPUPowerPackage powerPackage = new CPUPowerPackage();

        String cpuName = processor.getProcessorIdentifier().getName();

        long totalMemory = memory.getTotal();

        // Placeholder values until you integrate
        // actual temperature and power readings.
        double cpuTemperature = Double.NaN;
        double cpuPower = powerPackage.getCPUPackagePower();

        return new HardwareInfo(
                cpuName,
                cpuTemperature,
                cpuPower,
                totalMemory
        );
    }
}

//import com.example.monitortest.model.CPUPowerPackage;
//import oshi.SystemInfo;
//import oshi.hardware.CentralProcessor;
//import oshi.hardware.GlobalMemory;
//import oshi.hardware.HardwareAbstractionLayer;
//
//public class ScanHardware {
//
//
//    public CentralProcessor processor;
//    public GlobalMemory memory;
//    public CPUPowerPackage powerPackage;
//
//    public void scanHardware() {
//
//        System.out.println("Scanning hardware...");
//        SystemInfo systemInfo = new SystemInfo();
//
//        HardwareAbstractionLayer hardware = systemInfo.getHardware();
//        processor = hardware.getProcessor();
//        memory = hardware.getMemory();
////          Power Package
//        powerPackage = new CPUPowerPackage();
//        powerPackage.getCPUPackagePower();
//    }
//
//}
