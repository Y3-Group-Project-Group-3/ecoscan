package com.example.monitortest.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ScanHistory {

    // data/scan_history.csv in the project folder
    private static final Path FILE = Paths.get("data", "scan_history.csv");
    private static final String HEADER = "date,device,use,score,co2,result";

    // Reads the app's own history file
    public List<ScanRecord> getScans() {
        return readScans(FILE);
    }

    // Reads any CSV file in the same format (reused by Import)
    public List<ScanRecord> readScans(Path file) {

        List<ScanRecord> scans = new ArrayList<>();

        if (!Files.exists(file)) {
            System.out.println("No scan history file at " + file.toAbsolutePath());
            return scans;
        }

        try {
            List<String> lines = Files.readAllLines(file);

            // Skip line 0 (the header)
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");

                if (values.length == 6) {
                    scans.add(new ScanRecord(
                            values[0], values[1], values[2],
                            values[3], values[4], values[5]
                    ));
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read " + file + ": " + e.getMessage());
        }

        return scans;
    }

    // Saves to the app's own history file
    public void saveScans(List<ScanRecord> scans) {
        writeScans(scans, FILE);
    }

    // Writes scans to any CSV file (reused by Export)
    public void writeScans(List<ScanRecord> scans, Path file) {

        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (ScanRecord scan : scans) {
            lines.add(scan.toCsvLine());
        }

        try {
            Files.write(file, lines);
        } catch (IOException e) {
            System.out.println("Couldn't save " + file + ": " + e.getMessage());
        }
    }
}