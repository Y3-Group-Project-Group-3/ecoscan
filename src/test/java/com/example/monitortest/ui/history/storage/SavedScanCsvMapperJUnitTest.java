
package com.example.monitortest.ui.history.storage;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
import com.example.monitortest.ui.history.model.SavedScan;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SavedScanCsvMapperJUnitTest {

    // UT-HIS-009 — Saved Scan Data Mapping
    @Test
    void utHis009_preservesSavedScanData() {

        UUID scanId = UUID.randomUUID();
        LocalDateTime scanDate =
                LocalDateTime.of(2026, 11, 18, 14, 30, 15);

        // Include multiple RAM modules and storage devices
        // to verify the mapper's nested Base64 encoding.
        List<MemoryModule> memoryModules = List.of(
                new MemoryModule(
                        "Kingston", "DDR4",
                        8_000_000_000L, 3_200_000_000L
                ),
                new MemoryModule(
                        "Corsair, Ltd", "DDR5",
                        16_000_000_000L, 4_800_000_000L
                )
        );

        List<StorageDevice> storageDevices = List.of(
                new StorageDevice(
                        "Samsung 990 EVO, 2TB",
                        2_000_000_000_000L,
                        "SSD"
                ),
                new StorageDevice(
                        "WD \"Blue\"",
                        1_000_000_000_000L,
                        "HDD"
                )
        );

        HardwareInfo hardware = new HardwareInfo(
                "Intel Core i7, 12th Gen",
                48.5,
                65.75,
                24_000_000_000L,
                true,

                "NVIDIA RTX 4060",
                "NVIDIA",
                8_000_000_000L,
                115.25,

                "ASUS",
                "PRIME B760",
                "Rev 1.0",

                storageDevices,
                memoryModules
        );

        SavedScan original = new SavedScan(
                scanId,
                "Office PC, Test \"A\"",
                scanDate,
                hardware,
                true
        );

        // Serialize the complete saved scan and restore it.
        String csvRow = SavedScanCsvMapper.toCsvRow(original);
        SavedScan restored = SavedScanCsvMapper.fromCsvRow(csvRow);

        assertNotNull(csvRow);
        assertNotNull(restored);

        // Verify scan metadata.
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getScannedAt(), restored.getScannedAt());
        assertEquals(
                original.isIncludeEstimates(),
                restored.isIncludeEstimates()
        );

        HardwareInfo actual = restored.getHardwareInfo();

        // Verify CPU and system memory fields.
        assertEquals(hardware.getCpuName(), actual.getCpuName());
        assertEquals(
                hardware.getCpuTemperature(),
                actual.getCpuTemperature()
        );
        assertEquals(hardware.getCpuPower(), actual.getCpuPower());
        assertEquals(hardware.getTotalMemory(), actual.getTotalMemory());
        assertEquals(
                hardware.isCpuPowerSimulated(),
                actual.isCpuPowerSimulated()
        );

        // Verify GPU fields.
        assertEquals(hardware.getGpuName(), actual.getGpuName());
        assertEquals(hardware.getGpuVendor(), actual.getGpuVendor());
        assertEquals(hardware.getGpuVram(), actual.getGpuVram());
        assertEquals(hardware.getGpuPower(), actual.getGpuPower());

        // Verify motherboard fields.
        assertEquals(
                hardware.getMotherboardManufacturer(),
                actual.getMotherboardManufacturer()
        );
        assertEquals(
                hardware.getMotherboardModel(),
                actual.getMotherboardModel()
        );
        assertEquals(
                hardware.getMotherboardVersion(),
                actual.getMotherboardVersion()
        );

        // Verify all RAM module properties.
        List<MemoryModule> restoredMemory = actual.getMemoryModules();

        assertEquals(memoryModules.size(), restoredMemory.size());

        for (int i = 0; i < memoryModules.size(); i++) {
            MemoryModule expected = memoryModules.get(i);
            MemoryModule result = restoredMemory.get(i);

            assertEquals(expected.getManufacturer(), result.getManufacturer());
            assertEquals(expected.getMemoryType(), result.getMemoryType());
            assertEquals(expected.getCapacityBytes(), result.getCapacityBytes());
            assertEquals(expected.getClockSpeedHz(), result.getClockSpeedHz());
        }

        // Verify all storage device properties.
        List<StorageDevice> restoredStorage = actual.getStorageDevices();

        assertEquals(storageDevices.size(), restoredStorage.size());

        for (int i = 0; i < storageDevices.size(); i++) {
            StorageDevice expected = storageDevices.get(i);
            StorageDevice result = restoredStorage.get(i);

            assertEquals(expected.getModel(), result.getModel());
            assertEquals(expected.getCapacityBytes(), result.getCapacityBytes());
            assertEquals(expected.getType(), result.getType());
        }

        // Verify that the restored scan produces identical CSV.
        assertEquals(
                csvRow,
                SavedScanCsvMapper.toCsvRow(restored)
        );
    }
}
