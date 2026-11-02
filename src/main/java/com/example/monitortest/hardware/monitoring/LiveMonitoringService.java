package com.example.monitortest.hardware.monitoring;

// ensures that emissions continue accumulating even when you navigate away from Live Monitor.
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;



// Continuously retrieves CPU and GPU power readings on a dedicated background thread.
public class LiveMonitoringService {

    private final LiveHardwareReader reader;
    private final ScheduledExecutorService scheduler;

    public LiveMonitoringService(Consumer<LiveMonitorSnapshot> onSnapshot) {

        reader = new LiveHardwareReader();

        // Use a single background thread to avoid overlapping sensor requests.
        // allow us to retrieve hardware information continuously without freezing the application's GUI.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "EcoScan-LiveMonitor");
            thread.setDaemon(true);
            return thread;
        });

        this.onSnapshot = onSnapshot;
    }

    private final Consumer<LiveMonitorSnapshot> onSnapshot;
    // Track estimated emissions throughout the monitoring session.
    private final CarbonEmissionsTracker emissionsTracker = new CarbonEmissionsTracker(new CarbonEmissionsCalculator());

    // Starts periodic hardware power monitoring.
    public void start() {

        // Run immediately, then wait two seconds after each completed reading.
        scheduler.scheduleWithFixedDelay(() -> {

            try {
                // Retrieve the latest CPU and GPU power readings.
                LiveMonitorSnapshot snapshot = reader.readSnapshot();

                // Record when the retrieval completed.
                long timestamp = System.currentTimeMillis();

                // Accumulate estimated emissions using the latest snapshot.
                emissionsTracker.addSnapshot(snapshot, timestamp);

                // Forward the snapshot to the existing Live Monitor interface.
                onSnapshot.accept(snapshot);

            } catch (Exception e) {
                System.err.println( "Live monitoring failed: " + e.getMessage() );
            }

            // set the polling delay
        }, 0, 2, TimeUnit.SECONDS);
    }


    // Stops monitoring and interrupts any active collection task.
    public void stop() {
        scheduler.shutdownNow();
    }

    //Provides access to the current monitoring session's emissions data.
    public CarbonEmissionsTracker getEmissionsTracker() {
        return emissionsTracker;
    }
}
