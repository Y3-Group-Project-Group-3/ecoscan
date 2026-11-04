package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SavedScan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

// Manages persistent hardware scan records stored in a single CSV file.
// File handling is kept separate from CSV serialization and the History UI.
public class ScanHistoryCsvRepository {

    // Maximum number of hardware scans EcoScan can retain.
    private static final int MAX_SAVED_SCANS = 10;

    // Location of EcoScan's saved hardware scan history.
    private final Path csvPath;


    // Creates the repository using EcoScan's default data directory.
    public ScanHistoryCsvRepository() {

        // Place saved data under the current user's home directory.
        Path dataDirectory = Path.of(
                System.getProperty("user.home"),
                "EcoScan"
        );

        this.csvPath = dataDirectory.resolve("scans.csv");
    }

    // Creates a repository using a specified CSV file.
    // Allows testing without modifying the user's real scan history.
    public ScanHistoryCsvRepository(Path csvPath) {
        this.csvPath = java.util.Objects.requireNonNull(csvPath);
    }

    // Returns the location of the CSV file.
    // Useful when verifying where EcoScan has stored its data.
    public Path getCsvPath() {
        return csvPath;
    }


    // Loads every saved hardware scan from the CSV file.
// Returns an empty list when no history file exists yet.
    public List<SavedScan> loadAll() throws IOException {

        List<SavedScan> scans = new ArrayList<>();

        // A fresh installation has no saved scans.
        if (Files.notExists(csvPath)) {
            return scans;
        }

        // Read the complete file using UTF-8 encoding.
        String content = Files.readString(
                csvPath,
                StandardCharsets.UTF_8
        );

        // Separate the file into complete CSV records.
        List<String> records = readRecords(content);

        // Convert each record back into a SavedScan object.
        for (String record : records) {

            scans.add(SavedScanCsvMapper.fromCsvRow(record));
        }

        return scans;
    }


    // Separates CSV file content into complete records.
    // Newlines inside quoted fields are preserved.
    private List<String> readRecords(String content) {

        List<String> records = new ArrayList<>();
        StringBuilder currentRecord = new StringBuilder();

        boolean insideQuotes = false;

        // Read the CSV content one character at a time.
        for (int i = 0; i < content.length(); i++) {

            char character = content.charAt(i);

            if (character == '"') {

                // Quotation marks control whether a newline ends a record.
                insideQuotes = !insideQuotes;
                currentRecord.append(character);

            } else if ((character == '\n' || character == '\r')
                    && !insideQuotes) {

                // Preserve complete records, excluding blank lines.
                if (!currentRecord.isEmpty()) {
                    records.add(currentRecord.toString());
                    currentRecord.setLength(0);
                }

                // Treat Windows CRLF as one record separator.
                if (character == '\r'
                        && i + 1 < content.length()
                        && content.charAt(i + 1) == '\n') {
                    i++;
                }

            } else {
                currentRecord.append(character);
            }
        }

        // Reject an incomplete quoted CSV record.
        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "CSV file contains an unclosed quotation mark."
            );
        }

        // Include the final record even without a trailing newline.
        if (!currentRecord.isEmpty()) {
            records.add(currentRecord.toString());
        }

        return records;
    }



    // Saves a hardware scan while retaining only the ten newest records.
// Uses a temporary file to protect existing history during writing.
    public void save(SavedScan newScan) throws IOException {

        // Load the existing records and add the new scan.
        List<SavedScan> scans = loadAll();
        scans.add(newScan);

        // Sort scans by their original scan timestamp, newest first.
        scans.sort( Comparator.comparing(SavedScan::getScannedAt).reversed() );

        // Remove the oldest records when the maximum is exceeded.
        if (scans.size() > MAX_SAVED_SCANS) {
            scans = new ArrayList<>( scans.subList(0, MAX_SAVED_SCANS) );
        }

        // Convert the retained scans into CSV records.
        List<String> csvRows = new ArrayList<>();

        for (SavedScan scan : scans) {
            csvRows.add(SavedScanCsvMapper.toCsvRow(scan));
        }

        // Ensure the EcoScan data directory exists.
        Files.createDirectories(csvPath.getParent());

        // Create a temporary file in the same directory as scans.csv.
        Path temporaryFile = Files.createTempFile( csvPath.getParent(), "scans-", ".tmp" );

        try {

            // Write the updated records without changing the existing CSV.
            Files.write( temporaryFile, csvRows, StandardCharsets.UTF_8 );

            // Atomically replace the existing file when supported.
            Files.move( temporaryFile, csvPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING );

        } finally {

            // Remove the temporary file if writing or replacement fails.
            Files.deleteIfExists(temporaryFile);
        }
    }



}
