package com.example.monitortest.model;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class ScanHardware {

    public HardwareInfo getHardwareInfo() {

        SystemInfo systemInfo = new SystemInfo();
        CPUPowerPackage powerPackage = new CPUPowerPackage();

        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();
        String cpuName = processor.getProcessorIdentifier().getName();

        long totalMemory = memory.getTotal();
        //placeholder
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