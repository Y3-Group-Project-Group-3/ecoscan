
package com.example.monitortest.ui.history.storage;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
import com.example.monitortest.ui.history.model.SavedScan;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;

// Converts saved hardware scans into CSV-compatible records.
// Keeps the conversion logic separate from file reading and writing.
public final class SavedScanCsvMapper {

    // Prevent instances of this utility class.
    private SavedScanCsvMapper() {
    }

    // Converts a complete saved scan into one CSV record.
    public static String toCsvRow(SavedScan scan) {

        HardwareInfo info = scan.getHardwareInfo();

        // Extract scan metadata and all fixed hardware fields.
        // RAM and storage collections are encoded into single fields.
        return CsvUtility.createRow(
                scan.getId().toString(),
                scan.getName(),
                scan.getScannedAt().toString(),
                Boolean.toString(scan.isIncludeEstimates()),

                info.getCpuName(),
                Double.toString(info.getCpuTemperature()),
                Double.toString(info.getCpuPower()),
                Long.toString(info.getTotalMemory()),
                Boolean.toString(info.isCpuPowerSimulated()),

                info.getGpuName(),
                info.getGpuVendor(),
                Long.toString(info.getGpuVram()),
                Double.toString(info.getGpuPower()),

                info.getMotherboardManufacturer(),
                info.getMotherboardModel(),
                info.getMotherboardVersion(),

                encodeMemoryModules(info.getMemoryModules()),
                encodeStorageDevices(info.getStorageDevices())
        );
    }


    // Converts all RAM modules into one CSV-safe field.
    private static String encodeMemoryModules(List<MemoryModule> modules) {

        // Encode each module independently before combining them.
        return modules.stream()
                .map(module -> {
                    // Preserve every field required to reconstruct the module.
                    String moduleData = CsvUtility.createRow( module.getManufacturer(), module.getMemoryType(), Long.toString(module.getCapacityBytes()), Long.toString(module.getClockSpeedHz()) );
                    return encodeText(moduleData);
                })
                .collect(Collectors.joining(";"));
    }


    // Converts all storage devices into one CSV-safe field.
    private static String encodeStorageDevices(List<StorageDevice> devices) {

        // Encode each detected physical drive independently.
        return devices.stream()
                .map(device -> {
                    // Preserve the model, capacity and physical drive type.
                    String deviceData = CsvUtility.createRow( device.getModel(), Long.toString(device.getCapacityBytes()), device.getType() );
                    return encodeText(deviceData);
                })
                .collect(Collectors.joining(";"));
    }

    // Encodes text using URL-safe Base64.
    // This keeps nested hardware records on one physical CSV line.
    private static String encodeText(String text) {
        // Convert the text to UTF-8 bytes before encoding.
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        // URL-safe Base64 avoids our semicolon delimiter.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    // Reconstructs a saved hardware scan from one complete CSV record.
    public static SavedScan fromCsvRow(String csvRow) {

        // Separate the CSV record into its original fields.
        List<String> fields = CsvUtility.parseRow(csvRow);

        // Ensure the record matches our expected CSV structure.
        if (fields.size() != 18) {
            throw new IllegalArgumentException(
                    "Expected 18 CSV fields, found " + fields.size()
            );
        }

        // Restore the original scan metadata.
        UUID id = UUID.fromString(fields.get(0));
        String name = fields.get(1);
        LocalDateTime scannedAt = LocalDateTime.parse(fields.get(2));
        boolean includeEstimates = parseBoolean(fields.get(3));

        // Reconstruct the hardware information using its existing constructor.
        HardwareInfo hardwareInfo = new HardwareInfo(
                fields.get(4),                  // CPU name
                Double.parseDouble(fields.get(5)),
                Double.parseDouble(fields.get(6)),
                Long.parseLong(fields.get(7)),
                parseBoolean(fields.get(8)),

                fields.get(9),                  // GPU name
                fields.get(10),                 // GPU vendor
                Long.parseLong(fields.get(11)),
                Double.parseDouble(fields.get(12)),

                fields.get(13),                 // Motherboard manufacturer
                fields.get(14),                 // Motherboard model
                fields.get(15),                 // Motherboard version

                decodeStorageDevices(fields.get(17)),
                decodeMemoryModules(fields.get(16))
        );

        // Return the complete saved scan.
        return new SavedScan(
                id,
                name,
                scannedAt,
                hardwareInfo,
                includeEstimates
        );
    }


    // Reconstructs individual RAM modules from one encoded CSV field.
    private static List<MemoryModule> decodeMemoryModules(String encoded) {

        List<MemoryModule> modules = new ArrayList<>();

        // An empty field represents a scan with no detected RAM modules.
        if (encoded.isEmpty()) {
            return modules;
        }

        // Decode each module and reconstruct its original properties.
        for (String entry : encoded.split(";", -1)) {

            List<String> values = CsvUtility.parseRow(decodeText(entry));

            if (values.size() != 4) {
                throw new IllegalArgumentException("Invalid memory module data.");
            }

            modules.add(new MemoryModule(
                    values.get(0),
                    values.get(1),
                    Long.parseLong(values.get(2)),
                    Long.parseLong(values.get(3))
            ));
        }

        return modules;
    }

    // Reconstructs physical storage devices from one encoded CSV field.
    private static List<StorageDevice> decodeStorageDevices(String encoded) {

        List<StorageDevice> devices = new ArrayList<>();

        // An empty field represents a scan with no detected drives.
        if (encoded.isEmpty()) {
            return devices;
        }

        // Decode each drive and restore its model, capacity and type.
        for (String entry : encoded.split(";", -1)) {

            List<String> values = CsvUtility.parseRow(decodeText(entry));

            if (values.size() != 3) {
                throw new IllegalArgumentException("Invalid storage device data.");
            }

            devices.add(new StorageDevice(
                    values.get(0),
                    Long.parseLong(values.get(1)),
                    values.get(2)
            ));
        }

        return devices;
    }


    // Decodes text previously encoded using URL-safe Base64.
    private static String decodeText(String encoded) {

        // Convert the stored Base64 value back into its original UTF-8 text.
        byte[] bytes = Base64.getUrlDecoder().decode(encoded);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Parses a boolean strictly so invalid CSV values are not silently accepted.
    private static boolean parseBoolean(String value) {

        if ("true".equals(value)) {
            return true;
        }

        if ("false".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Invalid boolean value in saved scan: " + value
        );
    }






}
