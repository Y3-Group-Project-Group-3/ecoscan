package com.example.monitortest.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// Retrieves physical storage media types reported by Windows.
public class WindowsStorageTypeDetector {

    // Returns a map of Windows disk IDs to their reported media types.
    public static Map<Integer, String> getStorageTypes() {
        Map<Integer, String> storageTypes = new HashMap<>();

        try {
            // Request disk IDs and media types in a simple CSV format.
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    "Get-PhysicalDisk | Select-Object DeviceId, MediaType | ConvertTo-Csv -NoTypeInformation"
            );

            Process process = builder.start();

            // Read the CSV output returned by PowerShell.
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                // Skip the CSV header.
                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    // Remove quotation marks from the two CSV fields.
                    String[] values = line.replace("\"", "").split(",");

                    if (values.length == 2) {
                        int deviceId = Integer.parseInt(values[0].trim());
                        String mediaType = values[1].trim().toUpperCase();

                        // Only accept recognised storage classifications.
                        if (mediaType.equals("SSD") || mediaType.equals("HDD")) {
                            storageTypes.put(deviceId, mediaType);
                        }
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            // Missing classifications will remain UNKNOWN.
            System.err.println("Storage type detection failed: " + e.getMessage());
        }

        return storageTypes;
    }
}
