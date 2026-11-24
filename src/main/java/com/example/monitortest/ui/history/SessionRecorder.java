package com.example.monitortest.ui.history;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import com.example.monitortest.hardware.monitoring.MacLiveMonitorDemoProvider;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;

import java.time.Duration;
import java.time.LocalDateTime;

// Records the current Live Monitor session.
// The session starts when EcoScan opens and is saved when the application closes.
public class SessionRecorder {

    // Date and time the current session started.
    private final LocalDateTime startedAt = LocalDateTime.now();

    // Saves finished sessions to the sessions CSV file.
    private final SessionHistoryCsvRepository sessionRepository = new SessionHistoryCsvRepository();


    // Saves the total emissions and length of the current session.
    // Windows uses the emissions accumulated by live monitoring.
    // macOS Demo Mode uses the demo power values over the time EcoScan was open.
    public void saveSession(LiveMonitoringService monitoringService, boolean macDemoMode) {

        // Calculate how long EcoScan has been open.
        // Saved with the session so the History Page can show the average emissions per second.
        double sessionSeconds = Duration.between( startedAt, LocalDateTime.now() ).toMillis() / 1000.0;

        double totalEmissionsGrams;

        // Stores the most recent carbon intensity used during monitoring.
        // NaN represents a session without an available recorded intensity.
        double carbonIntensityGramsPerKwh = Double.NaN;

        if (monitoringService != null) {

            // Retrieve the emissions accumulated during live monitoring.
            totalEmissionsGrams = monitoringService.getEmissionsTracker().getTotalEmissionsGrams();

            // Capture the latest carbon intensity applied to the emissions tracker.
            // This is the value used for the most recent emissions calculations.
            carbonIntensityGramsPerKwh = monitoringService.getEmissionsTracker().getCarbonIntensity();


        } else if (macDemoMode) {

            // Retrieve the demonstration CPU and GPU power values.
            LiveMonitorSnapshot demoSnapshot = new MacLiveMonitorDemoProvider().createSnapshot();

            // Reuse EcoScan's existing emissions calculation logic.
            totalEmissionsGrams = new CarbonEmissionsCalculator().calculateEmissions( demoSnapshot.totalPower(), sessionSeconds );

        } else {

            // Live monitoring was unavailable, so there is no session to save.
            return;
        }

        try {

            // Save the session with its final carbon intensity reading.
            // macOS demonstration sessions retain an unrecorded intensity.
            sessionRepository.save(
                    new SessionRecord( startedAt, totalEmissionsGrams, sessionSeconds, carbonIntensityGramsPerKwh )
            );


        } catch (Exception e) {

            // Allow EcoScan to close even if the session could not be saved.
            System.err.println("Unable to save the monitoring session: " + e.getMessage());
        }
    }



}