package com.example.monitortest.hardware.monitoring;

// ensures that emissions continue accumulating even when you navigate away from Live Monitor.
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;
import com.example.monitortest.hardware.monitoring.emissions.CarbonIntensityService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;



// Continuously retrieves CPU and GPU power readings on a dedicated background thread.
public class LiveMonitoringService {

//    private final LiveHardwareReader reader;
    // Supplies power snapshots from either real hardware or a test fixture.
    private final Supplier<LiveMonitorSnapshot> snapshotSupplier;

    private final ScheduledExecutorService scheduler;

    // Runs carbon intensity API requests independently of hardware polling.
    private final ScheduledExecutorService carbonIntensityScheduler;

    // Track whether the monitoring service has encountered a failure.
    private boolean monitoringFailed = false;
    // Prevent multiple monitoring tasks from being scheduled.
    private boolean started = false;

    // Production constructor.
    // Existing application code can continue creating the service as before.
    public LiveMonitoringService(Consumer<LiveMonitorSnapshot> onSnapshot) {
        // Use the real hardware reader during normal operation.
        this(onSnapshot, new LiveHardwareReader()::readSnapshot);
    }

    // Package-private constructor used by integration tests.
    // Allows simulated snapshots without accessing physical hardware.
    public LiveMonitoringService(Consumer<LiveMonitorSnapshot> onSnapshot, Supplier<LiveMonitorSnapshot> snapshotSupplier) {

        // Store the callback and the selected snapshot source.
        this.onSnapshot = onSnapshot;
        this.snapshotSupplier = snapshotSupplier;

        // Use one background worker to avoid overlapping sensor requests.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {

            // Give the monitoring thread a recognisable name.
            Thread thread = new Thread(r, "EcoScan-LiveMonitor");

            // Allow the application to close without waiting on this thread.
            thread.setDaemon(true);

            return thread;
        });


        // Use a dedicated background thread for NESO API refreshes.
        // Daemon mode prevents this worker from keeping the JVM running.
        carbonIntensityScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "EcoScan-CarbonIntensity");
            thread.setDaemon(true);
            return thread;
        });

    }


    private final Consumer<LiveMonitorSnapshot> onSnapshot;
    // Track estimated emissions throughout the monitoring session.
    private final CarbonEmissionsTracker emissionsTracker = new CarbonEmissionsTracker(new CarbonEmissionsCalculator());

    // Manages the current NESO carbon intensity and offline fallback.
    // API refreshes will be scheduled separately from hardware monitoring.
    private final CarbonIntensityService carbonIntensityService = new CarbonIntensityService();


    // Applies the latest carbon intensity to future emissions calculations.
    // This method does not make a network request.
    private void applyCurrentCarbonIntensity() {

        // Retrieve the most recently stored intensity reading.
        CarbonIntensityService.IntensityReading reading = carbonIntensityService.getCurrentReading();

        // Update the emissions tracker with the selected intensity.
        // Previously accumulated emissions remain unchanged.
        emissionsTracker.updateCarbonIntensity(reading.gramsPerKwh());
    }



    // Starts periodic hardware power monitoring.
    public void start() {

        // Avoid scheduling duplicate monitoring tasks.
        if (started) { return; }
        started = true;


        // Retrieve carbon intensity immediately, then refresh every 30 minutes.
        // This runs on a separate worker and cannot block hardware polling.
        carbonIntensityScheduler.scheduleWithFixedDelay(() -> {

            // Retrieve and validate the latest NESO reading.
            // Failures are handled internally using the offline fallback.
            carbonIntensityService.refresh();

            // Apply the selected intensity to future emissions calculations.
            applyCurrentCarbonIntensity();


            // Retrieve the reading that was applied to the emissions tracker.
            CarbonIntensityService.IntensityReading reading = carbonIntensityService.getCurrentReading();

            // Report the intensity and its source for integration testing.
            System.out.printf(
                    "Carbon intensity updated: %.1f g/kWh (Source: %s)%n",
                    reading.gramsPerKwh(),
                    reading.source()
            );


        }, 0, 30, TimeUnit.MINUTES);


        // Run immediately, then wait two seconds after each completed reading.
        scheduler.scheduleWithFixedDelay(() -> {

            try {
                // Retrieve a snapshot from the configured source.
                LiveMonitorSnapshot snapshot = snapshotSupplier.get();

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

        // Stop scheduling new carbon intensity requests.
        // An active HTTP request is allowed to finish within its configured timeout.
        carbonIntensityScheduler.shutdown();

    }


    // Waits for both background schedulers to finish.
    // The supplied timeout is shared between the two schedulers.
    public boolean awaitTermination(long timeout, TimeUnit unit) {

        try {
            // Convert the total waiting period into nanoseconds.
            long timeoutNanos = unit.toNanos(timeout);

            // Record when waiting begins so both schedulers
            // share the same overall timeout.
            long startTime = System.nanoTime();

            // Wait for any active hardware monitoring task to finish.
            boolean hardwareStopped = scheduler.awaitTermination(timeoutNanos, TimeUnit.NANOSECONDS);

            // Calculate how much of the original timeout remains.
            long elapsedNanos = System.nanoTime() - startTime;
            long remainingNanos = Math.max(0L, timeoutNanos - elapsedNanos);

            // Wait for any active carbon intensity request to finish.
            boolean carbonStopped = carbonIntensityScheduler.awaitTermination( remainingNanos, TimeUnit.NANOSECONDS );

            // Report success only when both schedulers have terminated.
            return hardwareStopped && carbonStopped;

        } catch (InterruptedException exception) {

            // Preserve the calling thread's interrupted status.
            Thread.currentThread().interrupt();

            // Report that shutdown could not be confirmed.
            return false;
        }
    }


    //Provides access to the current monitoring session's emissions data.
    public CarbonEmissionsTracker getEmissionsTracker() {
        return emissionsTracker;
    }


    // Returns the latest carbon intensity and its source.
    // This method does not make an API request and is safe to call while the carbon intensity scheduler is running.
    public CarbonIntensityService.IntensityReading getCurrentCarbonIntensity() {

        // Retrieve the latest reading stored by the carbon intensity service.
        return carbonIntensityService.getCurrentReading();
    }

}
