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
    // Track whether the monitoring service has encountered a failure.
    private boolean monitoringFailed = false;

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

                // Check whether either power sensor returned a valid reading.
                boolean cpuAvailable = Double.isFinite(snapshot.cpuPower());
                boolean gpuAvailable = Double.isFinite(snapshot.gpuPower());

                // Treat monitoring as unavailable only when both readings are missing.
                if (!cpuAvailable && !gpuAvailable) {

                    // Report the first failure without repeatedly logging it.
                    if (!monitoringFailed) {
                        System.err.println("Live monitoring: CPU and GPU power readings unavailable.");
                        monitoringFailed = true;
                    }

                } else if (monitoringFailed) {

                    // At least one sensor has recovered.
                    System.out.println("Live monitoring recovered successfully.");
                    monitoringFailed = false;
                }

                // Record when the retrieval completed.
                long timestamp = System.currentTimeMillis();

                // Accumulate estimated emissions using the latest snapshot.
                emissionsTracker.addSnapshot(snapshot, timestamp);

                // Forward the snapshot to the existing Live Monitor interface.
                onSnapshot.accept(snapshot);

            } catch (Exception e) {

                // Report the first failure without repeating the same error every poll.
                if (!monitoringFailed) {
                    System.err.println("Live monitoring failed: " + e.getMessage());
                    monitoringFailed = true;
                }
            }

            // set the polling delay
        }, 0, 2, TimeUnit.SECONDS);
    }


    // Stops live monitoring without interrupting an active hardware sensor retrieval.
    public void stop() {

        // Stop accepting new monitoring tasks.
        // Allow any currently running retrieval to finish naturally.
        scheduler.shutdown();
    }

    //Provides access to the current monitoring session's emissions data.
    public CarbonEmissionsTracker getEmissionsTracker() {
        return emissionsTracker;
    }
}
