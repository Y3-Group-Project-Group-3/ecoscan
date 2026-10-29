package com.example.monitortest.hardware.detection;

import com.example.monitortest.hardware.model.MemoryModule;
import oshi.hardware.GlobalMemory;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves physical RAM module information using OSHI
 * and converts it into EcoScan's MemoryModule model.
 */
public class MemoryDetector {

    /**
     * Detects the installed physical memory modules.
     *
     * @param memory OSHI's system memory information.
     * @return A list of detected memory modules.
     */
    public List<MemoryModule> getMemoryModules(GlobalMemory memory) {

        // Retrieve the installed physical RAM modules.
        var physicalModules = memory.getPhysicalMemory();

        // Store the converted modules.
        List<MemoryModule> detectedModules = new ArrayList<>();

        // Convert OSHI modules into EcoScan's shared model.
        for (var module : physicalModules) {
            detectedModules.add(
                    new MemoryModule(
                            module.getManufacturer(),
                            module.getMemoryType(),
                            module.getCapacity(),
                            module.getClockSpeed()
                    )
            );
        }

        return detectedModules;
    }
}
