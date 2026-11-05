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


    // Saves the total emissions for the current session.
    // Windows uses the emissions accumulated by live monitoring.
    // macOS Demo Mode uses the demo power values over the time EcoScan was open.
    public void saveSession(LiveMonitoringService monitoringService, boolean macDemoMode) {

        double totalEmissionsGrams;

        if (monitoringService != null) {

            // Retrieve the emissions accumulated during live monitoring.
            totalEmissionsGrams = monitoringService.getEmissionsTracker().getTotalEmissionsGrams();

        } else if (macDemoMode) {

            // Retrieve the demonstration CPU and GPU power values.
            LiveMonitorSnapshot demoSnapshot = new MacLiveMonitorDemoProvider().createSnapshot();

            // Calculate how long EcoScan has been open.
            double sessionSeconds = Duration.between( startedAt, LocalDateTime.now() ).toMillis() / 1000.0;

            // Reuse EcoScan's existing emissions calculation logic.
            totalEmissionsGrams = new CarbonEmissionsCalculator().calculateEmissions( demoSnapshot.totalPower(), sessionSeconds );

        } else {

            // Live monitoring was unavailable, so there is no session to save.
            return;
        }

        try {

            // Save the finished session to the sessions CSV file.
            sessionRepository.save( new SessionRecord(startedAt, totalEmissionsGrams) );

        } catch (Exception e) {

            // Allow EcoScan to close even if the session could not be saved.
            System.err.println("Unable to save the monitoring session: " + e.getMessage());
        }
    }



}