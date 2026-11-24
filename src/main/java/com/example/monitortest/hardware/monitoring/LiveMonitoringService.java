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


    // Refresh the NESO carbon intensity every 30 minutes
    // when the previous API request was successful.
    private static final long CARBON_REFRESH_MINUTES = 30;

    // Retry after one minute when the API is unavailable.
    // This allows EcoScan to recover when connectivity returns.
    private static final long CARBON_RETRY_MINUTES = 1;


    // Time between successful API refreshes, measured in milliseconds.
    private final long carbonRefreshDelayMillis;

    // Time before retrying a failed API request, measured in milliseconds.
    private final long carbonRetryDelayMillis;


    // Supplies power snapshots from either real hardware or a test fixture.
    private final Supplier<LiveMonitorSnapshot> snapshotSupplier;
    private final ScheduledExecutorService scheduler;

    // Runs carbon intensity API requests independently of hardware polling.
    private final ScheduledExecutorService carbonIntensityScheduler;

    // Track whether the monitoring service has encountered a failure.
    private boolean monitoringFailed = false;
    // Prevent multiple monitoring tasks from being scheduled.
    private boolean started = false;


    // Production constructor: reads real hardware measurements and retrieves current electricity carbon intensity from NESO.
    public LiveMonitoringService( Consumer<LiveMonitorSnapshot> onSnapshot ) {
        // Use the real hardware reader and real NESO API service.
        this( onSnapshot, new LiveHardwareReader()::readSnapshot, new CarbonIntensityService() );
    }


    // Package-private constructor used by integration tests.
    // Allows simulated snapshots without accessing physical hardware.
    public LiveMonitoringService( Consumer<LiveMonitorSnapshot> onSnapshot, Supplier<LiveMonitorSnapshot> snapshotSupplier) {
        // Delegate to the configurable constructor using a simulated
        // NESO reading of 151 g/kWh.
        this( onSnapshot, snapshotSupplier, new CarbonIntensityService(() -> 151.0) );

    }


    // Configurable constructor used by automated integration tests.
// Allows both hardware snapshots and carbon intensity responses
// to be simulated independently.
    public LiveMonitoringService(
            Consumer<LiveMonitorSnapshot> onSnapshot,
            Supplier<LiveMonitorSnapshot> snapshotSupplier,
            CarbonIntensityService carbonIntensityService
    ) {

        // Delegate to the configurable constructor.
        this(
                onSnapshot,
                snapshotSupplier,
                carbonIntensityService,
                TimeUnit.MINUTES.toMillis(CARBON_REFRESH_MINUTES),
                TimeUnit.MINUTES.toMillis(CARBON_RETRY_MINUTES)
        );
    }

    // Allows tests to configure shorter API refresh intervals.
    // Both delays are measured in milliseconds.
    public LiveMonitoringService(
            Consumer<LiveMonitorSnapshot> onSnapshot,
            Supplier<LiveMonitorSnapshot> snapshotSupplier,
            CarbonIntensityService carbonIntensityService,
            long refreshDelayMillis,
            long retryDelayMillis
    ) {

        // Validate the supplied refresh intervals.
        if (refreshDelayMillis <= 0 || retryDelayMillis <= 0) {
            throw new IllegalArgumentException(
                    "Carbon intensity refresh delays must be positive."
            );
        }

        // Store the monitoring callback and data providers.
        this.onSnapshot = java.util.Objects.requireNonNull(onSnapshot);
        this.snapshotSupplier = java.util.Objects.requireNonNull(snapshotSupplier);
        this.carbonIntensityService = java.util.Objects.requireNonNull(carbonIntensityService);

        // Store the configurable refresh delays.
        this.carbonRefreshDelayMillis = refreshDelayMillis;
        this.carbonRetryDelayMillis = retryDelayMillis;

        // Keep hardware polling on its dedicated background thread.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "EcoScan-LiveMonitor");
            thread.setDaemon(true);
            return thread;
        });

        // Keep NESO requests separate from hardware polling.
        carbonIntensityScheduler =
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = new Thread(r, "EcoScan-CarbonIntensity");
                    thread.setDaemon(true);
                    return thread;
                });
    }



    private final Consumer<LiveMonitorSnapshot> onSnapshot;
    // Track estimated emissions throughout the monitoring session.
    private final CarbonEmissionsTracker emissionsTracker = new CarbonEmissionsTracker(new CarbonEmissionsCalculator());


    // Manages the current carbon intensity and offline fallback.
    // The service is supplied by the constructor so tests can use
    // simulated API responses instead of real network requests.
    private final CarbonIntensityService carbonIntensityService;



    // Applies the latest carbon intensity to future emissions calculations.
    // This method does not make a network request.
    private void applyCurrentCarbonIntensity() {

        // Retrieve the most recently stored intensity reading.
        CarbonIntensityService.IntensityReading reading = carbonIntensityService.getCurrentReading();

        // Update the emissions tracker with the selected intensity.
        // Previously accumulated emissions remain unchanged.
        emissionsTracker.updateCarbonIntensity(reading.gramsPerKwh());
    }

    // Refreshes the carbon intensity and schedules the next API request.
    // Successful requests refresh every 30 minutes; failures retry after 1 minute.
    // This method runs exclusively on the carbon intensity background scheduler.
    private void refreshAndScheduleCarbonIntensity() {

        try {
            // Retrieve the latest NESO intensity or use the offline fallback.
            carbonIntensityService.refresh();

            // Apply the selected intensity to future emissions calculations.
            applyCurrentCarbonIntensity();

            // Read the result of the refresh.
            CarbonIntensityService.IntensityReading reading = carbonIntensityService.getCurrentReading();

            // Report the updated value and source for integration testing.
            System.out.printf( "Carbon intensity updated: %.1f g/kWh (Source: %s)%n", reading.gramsPerKwh(), reading.source() );

            // Choose the next refresh delay based on the API result.
            // Successful requests use the normal interval.
            // Failed requests use the shorter retry interval.
            long delayMillis = reading.source() == CarbonIntensityService.Source.API ? carbonRefreshDelayMillis : carbonRetryDelayMillis;

            // Avoid scheduling another refresh after monitoring stops.
            if (!carbonIntensityScheduler.isShutdown()) {
                // Schedule the next request using the selected delay.
                // The scheduler runs only one carbon intensity task at a time.
                carbonIntensityScheduler.schedule( this::refreshAndScheduleCarbonIntensity, delayMillis, TimeUnit.MILLISECONDS );
            }

        } catch (java.util.concurrent.RejectedExecutionException exception) {

            // Shutdown can occur between checking the scheduler and scheduling.
            // Ignore this expected condition when EcoScan is closing.
            if (!carbonIntensityScheduler.isShutdown()) {
                System.err.println( "Carbon intensity scheduling failed: " + exception.getMessage() );
            }
        }
    }

    // Starts periodic hardware power monitoring.
    public void start() {

        // Avoid scheduling duplicate monitoring tasks.
        if (started) { return; }
        started = true;

        // Start the first carbon intensity request immediately.
        // The refresh method schedules subsequent requests dynamically.
        carbonIntensityScheduler.schedule( this::refreshAndScheduleCarbonIntensity, 0, TimeUnit.SECONDS );

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


        // Cancel pending carbon intensity retries and interrupt an active
        // HTTP request so it cannot delay application shutdown.
        carbonIntensityScheduler.shutdownNow();


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
