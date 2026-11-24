package com.example.monitortest.ui.history.storage;

import com.example.monitortest.ui.history.model.SessionRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Manages Live Monitor session records stored in a single CSV file.
// Each row stores the session start time, its total emissions and how long it lasted.
public class SessionHistoryCsvRepository {


    // Session CSV columns, including the most recent carbon intensity.
    private static final String HEADER = "startedAt,totalEmissionsGrams,durationSeconds,carbonIntensityGramsPerKwh";


    // Only the most recent sessions are kept, matching the saved scans limit.
    public static final int MAX_SAVED_SESSIONS = 10;

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


    // Adds a finished session to the CSV file.
    // Keeps only the most recent sessions once the limit is reached.
    public void save(SessionRecord session) throws IOException {

        // Load the existing sessions and add the new one.
        List<SessionRecord> sessions = loadAll();
        sessions.add(session);

        // Rewrite the file, keeping only the newest sessions.
        writeSessions(sessions);
    }


    // Imports sessions from another sessions CSV file.
    // Sessions that are already saved are skipped.
    // Returns the number of new sessions that were imported.
    public int importFrom(Path importPath) throws IOException {

        // Read the sessions from the selected file.
        List<SessionRecord> importedSessions = readSessions(importPath);

        // Load the sessions that are already saved.
        List<SessionRecord> sessions = loadAll();

        int added = 0;

        for (SessionRecord session : importedSessions) {

            // Skip a session when one with the same start time and emissions is already saved.
            boolean alreadySaved = sessions.stream().anyMatch( saved ->
                    saved.getStartedAt().equals(session.getStartedAt())
                            && saved.getTotalEmissionsGrams() == session.getTotalEmissionsGrams() );

            if (!alreadySaved) {
                sessions.add(session);
                added++;
            }
        }

        // Only rewrite the file when there are new sessions to add.
        if (added > 0) {
            writeSessions(sessions);
        }

        return added;
    }


    // Copies the saved session history to a CSV file chosen by the user.
    // The exported file uses the same format, so it can be imported again later.
    public void exportTo(Path exportPath) throws IOException {

        // Create a history file with just the header if no sessions have been saved yet.
        if (Files.notExists(csvPath)) {
            writeSessions(new ArrayList<>());
        }

        // Copy the history file, replacing any existing file at the chosen location.
        Files.copy( csvPath, exportPath, StandardCopyOption.REPLACE_EXISTING );
    }


    // Deletes the given sessions and keeps every other session.
    // Sessions are matched by their start time and total emissions.
    public void deleteSessions(List<SessionRecord> sessionsToDelete) throws IOException {

        // Load the full session history.
        List<SessionRecord> remaining = loadAll();

        // Remove every session that matches one of the selected sessions.
        remaining.removeIf( session -> sessionsToDelete.stream().anyMatch( selected ->
                selected.getStartedAt().equals(session.getStartedAt())
                        && selected.getTotalEmissionsGrams() == session.getTotalEmissionsGrams() ) );

        // Remove the file when no sessions are left.
        if (remaining.isEmpty()) {
            deleteAll();
            return;
        }

        // Rewrite the file with the sessions that are left.
        writeSessions(remaining);
    }


    // Deletes every saved session by removing the sessions CSV file.
    // A new file is created the next time a session is saved.
    public void deleteAll() throws IOException {
        Files.deleteIfExists(csvPath);
    }


    // Writes the given sessions to the CSV file, oldest first.
    // Only the newest sessions are kept once the limit is reached.
    private void writeSessions(List<SessionRecord> sessions) throws IOException {

        // Ensure the EcoScan data directory exists.
        Files.createDirectories(csvPath.getParent());

        // Keep the file in date order, oldest first, as sessions are added.
        sessions.sort( Comparator.comparing(SessionRecord::getStartedAt) );

        // Skip the oldest sessions when there are more than the limit.
        int start = Math.max(0, sessions.size() - MAX_SAVED_SESSIONS);

        StringBuilder csv = new StringBuilder(HEADER).append(System.lineSeparator());

        for (int i = start; i < sessions.size(); i++) {
            csv.append(toCsvRow(sessions.get(i))).append(System.lineSeparator());
        }

        Files.writeString( csvPath, csv.toString(), StandardCharsets.UTF_8 );
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

            // Preserve empty trailing values when reading CSV rows.
            // This allows a missing carbon intensity to be recognised.
            String[] values = line.split(",", -1);

            // Support all three session formats:
            // 2 columns: original sessions without duration.
            // 3 columns: sessions with duration but no intensity.
            // 4 columns: sessions containing carbon intensity.
            if (values.length < 2 || values.length > 4) {
                continue;
            }


            try {

                // Convert the CSV values back into a session record.
                LocalDateTime startedAt = LocalDateTime.parse( values[0].trim() );
                double totalEmissionsGrams = Double.parseDouble( values[1].trim() );

                // Older sessions without a recorded duration use zero.
                double durationSeconds = values.length >= 3
                        ? Double.parseDouble(values[2].trim())
                        : 0.0;

                // Older sessions without carbon intensity use NaN.
                // Empty fourth-column values also represent missing readings.
                double carbonIntensityGramsPerKwh = Double.NaN;

                if (values.length == 4 && !values[3].trim().isEmpty()) {
                    carbonIntensityGramsPerKwh = Double.parseDouble(values[3].trim());
                }

                // Construct a session with its available historical data.
                sessions.add(new SessionRecord( startedAt, totalEmissionsGrams, durationSeconds, carbonIntensityGramsPerKwh ));


            } catch (RuntimeException e) {

                // Skip rows that are not in the expected format.
                System.err.println("Skipping invalid session row: " + line);
            }
        }

        return sessions;
    }



    // Converts a saved Live Monitor session into a CSV row.
    // Missing carbon intensity readings are stored as an empty field.
    private String toCsvRow(SessionRecord session) {

        // Format the carbon intensity only when it was recorded.
        // Older sessions retain an empty fourth column.
        String intensity = session.hasCarbonIntensity()
                ? String.format( Locale.UK, "%.1f", session.getCarbonIntensityGramsPerKwh() )
                : "";

        // Preserve the existing date, emissions and duration fields.
        // Append the carbon intensity as the fourth CSV column.
        return session.getStartedAt() + ","
                + String.format( Locale.UK, "%.4f", session.getTotalEmissionsGrams() ) + ","
                + String.format( Locale.UK, "%.1f", session.getDurationSeconds() ) + ","
                + intensity;
    }




}