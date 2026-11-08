
package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SavedScan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Temporarily verifies that only ten hardware scans are retained.
// Uses a separate CSV file to protect real scan history.
public class ScanHistoryRepositoryTest {

    public static void main(String[] args) throws IOException {
        loadPersistenceTest();
    }


    // Creates a persistent test record that can be loaded in a later execution.
    private static void savePersistenceTest() throws IOException {

        // Use the normal EcoScan CSV to obtain a real hardware snapshot.
        ScanHistoryCsvRepository mainRepository =
                new ScanHistoryCsvRepository();

        List<SavedScan> existingScans = mainRepository.loadAll();

        if (existingScans.isEmpty()) {
            System.out.println("No hardware scan available for testing.");
            return;
        }

        // Save a test record in a separate, persistent CSV file.
        Path testPath = mainRepository.getCsvPath()
                .resolveSibling("persistence-test.csv");

        ScanHistoryCsvRepository testRepository =
                new ScanHistoryCsvRepository(testPath);

        SavedScan sample = existingScans.get(0);

        SavedScan testScan = new SavedScan(
                UUID.randomUUID(),
                "Persistence Test Scan",
                LocalDateTime.now(),
                sample.getHardwareInfo(),
                sample.isIncludeEstimates()
        );

        testRepository.save(testScan);

        System.out.println("Test scan saved.");
        System.out.println("Test CSV: " + testPath);
    }

    // Loads the saved test record without creating another scan.
    private static void loadPersistenceTest() throws IOException {

        ScanHistoryCsvRepository mainRepository =
                new ScanHistoryCsvRepository();

        Path testPath = mainRepository.getCsvPath()
                .resolveSibling("persistence-test.csv");

        ScanHistoryCsvRepository testRepository =
                new ScanHistoryCsvRepository(testPath);

        // Read the file created during the previous execution.
        List<SavedScan> scans = testRepository.loadAll();

        System.out.println("Saved scans found: " + scans.size());

        // Display the restored scan information.
        for (SavedScan scan : scans) {
            System.out.println("Name: " + scan.getName());
            System.out.println("Timestamp: " + scan.getScannedAt());
            System.out.println("CPU: " + scan.getHardwareInfo().getCpuName());
        }
    }

}
