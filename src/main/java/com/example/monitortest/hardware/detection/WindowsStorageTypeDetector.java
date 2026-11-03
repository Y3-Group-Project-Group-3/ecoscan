package com.example.monitortest.hardware.detection;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Retrieves physical storage media types reported by Windows.
public class WindowsStorageTypeDetector {

    // Returns a map of Windows disk IDs to their reported media types.
    public static Map<Integer, String> getStorageTypes() {
        Map<Integer, String> storageTypes = new HashMap<>();

        // Track the temporary file so it can be deleted after processing.
        java.nio.file.Path outputFile = null;

        try {
            // Request disk IDs and media types in a simple CSV format.
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    "Get-PhysicalDisk | Select-Object DeviceId, MediaType | ConvertTo-Csv -NoTypeInformation"
            );


            // Create a temporary file for PowerShell output.
            outputFile = java.nio.file.Files.createTempFile("ecoscan-storage-", ".csv");
            builder.redirectOutput(outputFile.toFile());
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = builder.start();



// Wait up to 10 seconds for PowerShell to finish.
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);

            if (!completed) {
                // Stop the process if storage detection takes too long.
                process.destroyForcibly();
                process.waitFor();

                System.err.println("Storage detection timed out.");

            } else if (process.exitValue() != 0) {
                // Avoid parsing incomplete output after a command failure.
                System.err.println("PowerShell storage detection failed.");

            } else {
                // Read the output only after PowerShell has finished.
                try (BufferedReader reader =
                             java.nio.file.Files.newBufferedReader(outputFile)) {

                    String line;

                    // Skip the CSV header.
                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] values = line.replace("\"", "").split(",");

                        if (values.length == 2) {
                            try {
                                int deviceId = Integer.parseInt(values[0].trim());
                                String mediaType = values[1].trim().toUpperCase();

                                // Only accept recognised storage classifications.
                                if (mediaType.equals("SSD") || mediaType.equals("HDD")) {
                                    storageTypes.put(deviceId, mediaType);
                                }

                            } catch (NumberFormatException e) {
                                // Continue processing other drives.
                                System.err.println(
                                        "Skipping storage entry with invalid device ID: "
                                                + values[0]
                                );
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            // Missing classifications will remain UNKNOWN.
            System.err.println("Storage type detection failed: " + e.getMessage());

        } finally {
            // Always attempt to delete the temporary PowerShell output file.
            if (outputFile != null) {
                try {
                    java.nio.file.Files.deleteIfExists(outputFile);

                } catch (java.io.IOException e) {
                    // Report cleanup failures without interrupting the scan.
                    System.err.println( "Failed to delete temporary storage file: " + e.getMessage());
                }
            }
        }


        return storageTypes;
    }
}
