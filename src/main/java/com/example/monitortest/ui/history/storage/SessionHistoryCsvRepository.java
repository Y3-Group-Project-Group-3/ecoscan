package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SessionRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Manages Live Monitor session records stored in a single CSV file.
// Each row stores the session start time and its total emissions.
public class SessionHistoryCsvRepository {

    // Header written at the top of the sessions CSV file.
    private static final String HEADER = "startedAt,totalEmissionsGrams";

    // Location of EcoScan's saved session history.
    private final Path csvPath;


    // Creates the repository using EcoScan's default data directory.
    public SessionHistoryCsvRepository() {

        // Place saved sessions alongside the saved hardware scans.
        Path dataDirectory = Path.of(
                System.getProperty("user.home"),
                "EcoScan"
        );

        this.csvPath = dataDirectory.resolve("sessions.csv");
    }

    // Creates a repository using a specified CSV file.
    // Allows testing without modifying the user's real session history.
    public SessionHistoryCsvRepository(Path csvPath) {
        this.csvPath = java.util.Objects.requireNonNull(csvPath);
    }


    // Loads every saved session from the CSV file.
    // Returns an empty list when no sessions have been saved yet.
    public List<SessionRecord> loadAll() throws IOException {
        return readSessions(csvPath);
    }


    // Adds a finished session to the end of the CSV file.
    public void save(SessionRecord session) throws IOException {

        // Ensure the EcoScan data directory exists.
        Files.createDirectories(csvPath.getParent());

        // Write the header the first time a session is saved.
        if (Files.notExists(csvPath)) {
            Files.writeString( csvPath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8 );
        }

        // Append the session as a new CSV row.
        Files.writeString( csvPath, toCsvRow(session) + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.APPEND );
    }


    // Imports every session from another sessions CSV file.
    // Returns the number of sessions that were imported.
    public int importFrom(Path importPath) throws IOException {

        // Read the sessions from the selected file.
        List<SessionRecord> importedSessions = readSessions(importPath);

        // Add each imported session to EcoScan's session history.
        for (SessionRecord session : importedSessions) {
            save(session);
        }

        return importedSessions.size();
    }


    // Deletes every saved session by removing the sessions CSV file.
    // A new file is created the next time a session is saved.
    public void deleteAll() throws IOException {
        Files.deleteIfExists(csvPath);
    }


    // Reads every valid session from the given CSV file.
    // Rows that cannot be read are skipped.
    private List<SessionRecord> readSessions(Path path) throws IOException {

        List<SessionRecord> sessions = new ArrayList<>();

        // A fresh installation has no saved sessions.
        if (Files.notExists(path)) {
            return sessions;
        }

        // Read the file one line at a time.
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {

            // Skip blank lines and the header row.
            if (line.isBlank() || line.startsWith("startedAt")) {
                continue;
            }

            String[] values = line.split(",");

            // Each row must contain a start time and a total.
            if (values.length != 2) {
                continue;
            }

            try {

                // Convert the CSV values back into a session record.
                LocalDateTime startedAt = LocalDateTime.parse( values[0].trim() );
                double totalEmissionsGrams = Double.parseDouble( values[1].trim() );

                sessions.add( new SessionRecord(startedAt, totalEmissionsGrams) );

            } catch (RuntimeException e) {

                // Skip rows that are not in the expected format.
                System.err.println("Skipping invalid session row: " + line);
            }
        }

        return sessions;
    }


    // Converts a session into a single CSV row.
    private String toCsvRow(SessionRecord session) {
        return session.getStartedAt() + "," + String.format( Locale.UK, "%.4f", session.getTotalEmissionsGrams() );
    }



}