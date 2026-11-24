package hardware.monitoring;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import com.example.monitortest.hardware.monitoring.emissions.CarbonIntensityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for the background Live Monitor service.
// Simulated readings prevent dependency on actual device hardware.
class LiveMonitoringServiceTest {

    // LIV-IT-006
    // Verify that starting the service delivers a hardware snapshot.
    @Test
    @DisplayName("LIV-IT-006 - Delivers a hardware snapshot when monitoring starts")
    void deliversSnapshotWhenMonitoringStarts() throws InterruptedException {

        // Create a simulated power measurement.
        LiveMonitorSnapshot expected = new LiveMonitorSnapshot(50.0, 30.0);

        // Store the snapshot received by the callback.
        AtomicReference<LiveMonitorSnapshot> received = new AtomicReference<>();

        // Allow the test to detect when the callback executes.
        CountDownLatch callbackReceived = new CountDownLatch(1);

        // Create the service using a simulated hardware supplier.
        LiveMonitoringService service = new LiveMonitoringService(
                        // Callback invoked when a reading is available.
                        snapshot -> {
                            received.set(snapshot);
                            callbackReceived.countDown();
                        },
                        // Simulated hardware reader.
                        () -> expected
                );

        try {

            // Begin background monitoring.
            service.start();

            // Wait for the first callback, with a timeout to prevent hangs.
            boolean completed = callbackReceived.await( 3, TimeUnit.SECONDS );

            // Verify that a snapshot was delivered.
            assertTrue( completed, "Monitoring did not deliver a snapshot within the timeout." );

            // Verify that the callback received the expected values.
            assertNotNull(received.get());
            assertEquals( 50.0, received.get().cpuPower(), 0.000001 );
            assertEquals( 30.0, received.get().gpuPower(), 0.000001 );

        } finally {

            // Always stop the background thread, even if an assertion fails.
            service.stop();
        }
    }


    // LIV-IT-007
    // Verify that stopping the monitoring service terminates its
    // background worker and prevents additional scheduled callbacks.
    @Test
    @DisplayName("LIV-IT-007 - Stops background monitoring and terminates the scheduler")
    void stopsBackgroundMonitoringSuccessfully() throws InterruptedException {

        // Count the number of snapshots delivered by the service.
        AtomicInteger callbackCount = new AtomicInteger(0);

        // Signal when the first snapshot has been delivered.
        CountDownLatch firstSnapshotReceived = new CountDownLatch(1);

        // Create a service using simulated power measurements.
        LiveMonitoringService service = new LiveMonitoringService(

                        // Record each callback invocation.
                        snapshot -> {
                            callbackCount.incrementAndGet();
                            firstSnapshotReceived.countDown();
                        },

                        // Return the same valid reading for each poll.
                        () -> new LiveMonitorSnapshot(50.0, 30.0)
                );

        try {

            // Begin background monitoring.
            service.start();

            // Confirm that at least one reading was delivered.
            boolean received = firstSnapshotReceived.await( 3, TimeUnit.SECONDS );

            assertTrue( received, "No snapshot was received before stopping monitoring." );

            // Stop scheduling new monitoring tasks.
            service.stop();

            // Wait for the background executor to finish.
            boolean terminated = service.awaitTermination( 3, TimeUnit.SECONDS );

            // Verify that the scheduler terminated successfully.
            assertTrue( terminated, "The monitoring scheduler did not terminate." );

            // Record the number of completed callbacks after termination.
            int countAfterStop = callbackCount.get();

            // Wait longer than the normal two-second polling interval.
            Thread.sleep(2500);

            // Verify that no additional callbacks were delivered.
            assertEquals( countAfterStop, callbackCount.get(), "Monitoring continued after the scheduler terminated." );

        } finally {
            // Always release background resources even if the test fails.
            service.stop();
        }
    }


    // LIV-IT-008
    // Verify that calling start() repeatedly does not create multiple background monitoring tasks.
    @Test
    @DisplayName("LIV-IT-008 - Prevents duplicate monitoring tasks when start is called multiple times")
    void preventsDuplicateMonitoringTasks() throws InterruptedException {

        // Count how many times the simulated hardware reader executes.
        AtomicInteger readingCount = new AtomicInteger(0);

        // Signal when the first hardware reading has completed.
        CountDownLatch firstReadingReceived = new CountDownLatch(1);

        // Create a monitoring service using simulated hardware data.
        LiveMonitoringService service = new LiveMonitoringService(

                        // Notify the test when a snapshot reaches the callback.
                        snapshot -> firstReadingReceived.countDown(),
                        // Count each sensor retrieval performed by the service.
                        () -> {
                            readingCount.incrementAndGet();
                            return new LiveMonitorSnapshot(50.0, 30.0);
                        }
                );

        try {

            // Start monitoring for the first time.
            service.start();

            // Wait until the first scheduled reading has been delivered.
            boolean received = firstReadingReceived.await( 3, TimeUnit.SECONDS );

            // Verify that monitoring started successfully.
            assertTrue( received, "The first monitoring snapshot was not received." );

            // Confirm that the initial reading was requested once.
            assertEquals(1, readingCount.get());

            // Attempt to start the same monitoring service again.
            service.start();

            // Allow a short period for an incorrectly scheduled duplicate task to execute its immediate first reading.
            Thread.sleep(500);

            // A second start request should not cause another immediate read.
            assertEquals( 1, readingCount.get(), "Calling start twice created a duplicate monitoring task." );

        } finally {
            // Always stop the service to release background resources.
            service.stop();
            // Wait for the scheduled worker to terminate.
            service.awaitTermination(3, TimeUnit.SECONDS);
        }
    }


    // LIV-IT-009
    // Verify that a temporary hardware retrieval exception does not
    // terminate the monitoring service or prevent subsequent readings.
    @Test
    @DisplayName("LIV-IT-009 - Recovers from a sensor retrieval exception and continues monitoring")
    void continuesMonitoringAfterSensorException()
            throws InterruptedException {

        // Count how many times the simulated hardware reader is called.
        AtomicInteger readingAttempts = new AtomicInteger(0);

        // Signal when a valid snapshot reaches the monitoring callback.
        CountDownLatch successfulReadingReceived = new CountDownLatch(1);

        // Store the successfully delivered snapshot for verification.
        AtomicReference<LiveMonitorSnapshot> receivedSnapshot = new AtomicReference<>();

        // Create the service using a simulated, occasionally failing reader.
        LiveMonitoringService service = new LiveMonitoringService(

                        // Callback: record successful sensor readings.
                        snapshot -> {
                            receivedSnapshot.set(snapshot);
                            successfulReadingReceived.countDown();
                        },

                        // Simulated hardware reader.
                        () -> {

                            // Track each attempt to retrieve hardware data.
                            int attempt = readingAttempts.incrementAndGet();

                            // Simulate a temporary failure on the first attempt.
                            if (attempt == 1) {
                                throw new IllegalStateException( "Simulated hardware sensor failure" );
                            }

                            // Return valid power readings on subsequent attempts.
                            return new LiveMonitorSnapshot(50.0, 30.0);
                        }
                );

        try {

            // Start the background monitoring service.
            service.start();

            // Wait for monitoring to recover from the first failed reading.
            // The normal polling interval is approximately two seconds.
            boolean recovered = successfulReadingReceived.await( 7, TimeUnit.SECONDS );

            // Verify that a successful callback occurred after the failure.
            assertTrue( recovered, "Monitoring did not recover after the sensor exception." );

            // Confirm that the hardware reader was called at least twice.
            assertTrue( readingAttempts.get() >= 2, "The service did not retry after the initial failure." );

            // Confirm that the recovered reading was delivered.
            assertNotNull(receivedSnapshot.get());

            // Verify the CPU power reading.
            assertEquals( 50.0, receivedSnapshot.get().cpuPower(), 0.000001 );
            // Verify the GPU power reading.
            assertEquals( 30.0, receivedSnapshot.get().gpuPower(), 0.000001 );

        } finally {
            // Stop monitoring regardless of whether the assertions succeed.
            service.stop();
            // Wait for the monitoring executor to finish.
            assertTrue( service.awaitTermination(3, TimeUnit.SECONDS), "The monitoring scheduler did not terminate." );
        }
    }

    // LIV-IT-010
    // Verify that the monitoring service automatically accumulates carbon emissions from consecutive hardware power readings.
    @Test
    @DisplayName("LIV-IT-010 - Automatically accumulates CO₂ emissions from consecutive monitoring snapshots")
    void automaticallyAccumulatesCarbonEmissions() throws InterruptedException {

        // Count how many monitoring snapshots have been delivered.
        AtomicInteger snapshotCount = new AtomicInteger(0);

        // Wait until the service has delivered two valid snapshots.
        CountDownLatch twoSnapshotsReceived = new CountDownLatch(2);

        // Create the monitoring service using simulated hardware readings.
        LiveMonitoringService service = new LiveMonitoringService(
                        // Record every completed monitoring callback.
                        snapshot -> {
                            snapshotCount.incrementAndGet();
                            twoSnapshotsReceived.countDown();
                        },
                        // Simulate constant CPU and GPU power consumption.
                        () -> new LiveMonitorSnapshot(70.0, 30.0)
                );

        try {

            // Start the real background monitoring scheduler.
            service.start();

            // Wait for two consecutive snapshots to be processed.
            // The service normally waits two seconds between readings.
            boolean received = twoSnapshotsReceived.await( 7, TimeUnit.SECONDS );

            // Verify that the service delivered both measurements.
            assertTrue( received, "The service did not deliver two monitoring snapshots." );

            // Confirm that at least two snapshots were delivered.
            assertTrue( snapshotCount.get() >= 2, "Insufficient snapshots were processed." );

            // Retrieve the emissions accumulated by the service.
            // The tracker is updated before each callback is invoked.
            double totalEmissions = service.getEmissionsTracker().getTotalEmissionsGrams();

            // Verify that monitoring produced a valid numerical result.
            assertTrue( Double.isFinite(totalEmissions), "Accumulated emissions must be a finite value." );

            // Verify that electricity consumption produced emissions.
            assertTrue( totalEmissions > 0.0, "Emissions should increase after consecutive valid readings." );

        } finally {
            // Always stop background monitoring after the test.
            service.stop();
            // Confirm the monitoring worker terminates successfully.
            assertTrue( service.awaitTermination(3, TimeUnit.SECONDS), "The monitoring scheduler did not terminate." );
        }
    }



    // LIV-IT-011
    // Verify that hardware monitoring callbacks execute on a background thread rather than blocking Swing's Event Dispatch Thread.
    @Test
    @DisplayName("LIV-IT-011 - Executes monitoring callbacks outside the Swing Event Dispatch Thread")
    void executesMonitoringCallbacksOnBackgroundThread() throws InterruptedException {

        // Record whether the callback was executed on Swing's EDT.
        AtomicBoolean callbackOnEdt = new AtomicBoolean(true);

        // Signal when the first monitoring callback has executed.
        CountDownLatch callbackReceived = new CountDownLatch(1);

        // Use simulated hardware readings to avoid physical sensor access.
        LiveMonitoringService service = new LiveMonitoringService(

                        // Inspect the thread executing the monitoring callback.
                        snapshot -> {
                            callbackOnEdt.set( SwingUtilities.isEventDispatchThread() );
                            // Notify the test that the callback has completed.
                            callbackReceived.countDown();
                        },

                        // Simulate a valid CPU and GPU power measurement.
                        () -> new LiveMonitorSnapshot(50.0, 30.0)
                );

        try {

            // Start the background monitoring scheduler.
            service.start();

            // Wait for the first callback to execute.
            boolean received = callbackReceived.await( 3, TimeUnit.SECONDS );

            // Verify that the callback actually executed.
            assertTrue( received, "The monitoring callback was not executed." );

            // The monitoring callback should not run on Swing's EDT.
            assertFalse( callbackOnEdt.get(), "The monitoring callback executed on the Swing EDT." );

        } finally {
            // Always stop the monitoring service after the test.
            service.stop();
            // Allow the background scheduler to terminate.
            service.awaitTermination(3, TimeUnit.SECONDS);
        }
    }


    // LIV-UT-053
    // Verify that a failed carbon intensity request can recover
    // when a subsequent simulated API request succeeds.
    @Test
    @DisplayName("LIV-UT-053 - Recovers from fallback to API intensity")
    void recoversFromFallbackToApiIntensity() throws Exception {

        // Count simulated API requests.
        AtomicInteger requests = new AtomicInteger();

        // First request fails; the second succeeds.
        CarbonIntensityService intensityService =
                new CarbonIntensityService(() -> {
                    if (requests.incrementAndGet() == 1) {
                        throw new IOException("Simulated connection failure");
                    }
                    return 125.0;
                });

        // Create the monitoring service with simulated hardware.
        LiveMonitoringService monitoringService =
                new LiveMonitoringService(
                        snapshot -> {},
                        () -> new LiveMonitorSnapshot(60.0, 40.0),
                        intensityService
                );

        try {
            // Simulate the initial failed API request.
            intensityService.refresh();

            // Verify that the fallback is selected.
            assertEquals( CarbonIntensityService.Source.FALLBACK, monitoringService.getCurrentCarbonIntensity().source() );
            assertEquals( 200.0, monitoringService.getCurrentCarbonIntensity().gramsPerKwh(), 0.001 );

            // Simulate the next successful API request.
            intensityService.refresh();

            // Verify that the service has recovered.
            assertEquals( CarbonIntensityService.Source.API, monitoringService.getCurrentCarbonIntensity().source() );
            assertEquals( 125.0, monitoringService.getCurrentCarbonIntensity().gramsPerKwh(), 0.001 );

            // Verify that exactly two API requests occurred.
            assertEquals(2, requests.get());

        } finally {
            // Release both background schedulers.
            monitoringService.stop();
            assertTrue( monitoringService.awaitTermination(5, TimeUnit.SECONDS) );
        }
    }


    // LIV-UT-054
    // Verify that the background scheduler retries a failed API request and automatically recovers when the next request succeeds.
    @Test
    @DisplayName("LIV-UT-054 - Automatically retries failed API requests and recovers")
    void automaticallyRetriesAndRecoversFromApiFailure() throws InterruptedException {

        // Count how many simulated API requests have occurred.
        AtomicInteger requests = new AtomicInteger();

        // Signal when the second API request has been reached.
        CountDownLatch secondRequest = new CountDownLatch(1);

        // Simulate a failed initial request followed by a successful retry.
        CarbonIntensityService intensityService = new CarbonIntensityService(() -> {

                    // Count the current request.
                    int requestNumber = requests.incrementAndGet();

                    // Simulate an unavailable internet connection.
                    if (requestNumber == 1) {
                        throw new IOException("Simulated connection failure");
                    }

                    // Signal that the scheduler automatically retried.
                    secondRequest.countDown();

                    // Simulate a successful NESO response.
                    return 125.0;
                });

        // Create the monitoring service using simulated hardware and API data.
        // Successful refreshes wait 5 seconds; failures retry after 50 ms.
        LiveMonitoringService monitoringService =
                new LiveMonitoringService(
                        snapshot -> {},
                        () -> new LiveMonitorSnapshot(60.0, 40.0),
                        intensityService,
                        5000,
                        50
                );

        try {
            // Start both background schedulers.
            monitoringService.start();

            // Verify that a second API request happens automatically.
            assertTrue( secondRequest.await(5, TimeUnit.SECONDS), "The scheduler should retry after the initial API failure." );

            // Allow the background worker to finish applying its result.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (monitoringService.getCurrentCarbonIntensity().source()
                    != CarbonIntensityService.Source.API
                    && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }

            // Retrieve the final carbon intensity reading.
            CarbonIntensityService.IntensityReading reading = monitoringService.getCurrentCarbonIntensity();

            // Verify that the successful retry restored API data.
            assertEquals( CarbonIntensityService.Source.API, reading.source() );

            // Verify that the recovered intensity was stored correctly.
            assertEquals( 125.0, reading.gramsPerKwh(), 0.001 );

            // Verify that the simulated provider was called twice.
            assertEquals( 2, requests.get() );

        } finally {

            // Stop both schedulers even if an assertion fails.
            monitoringService.stop();

            // Verify that the monitoring service terminates safely.
            assertTrue( monitoringService.awaitTermination(5, TimeUnit.SECONDS), "Both monitoring schedulers should terminate." );
        }
    }


    // LIV-UT-055
    // Verify that a recovered API reading is applied to the
    // emissions tracker after the background scheduler retries.
    @Test
    @DisplayName("LIV-UT-055 - Applies recovered API intensity to emissions tracker")
    void appliesRecoveredApiIntensityToTracker() throws InterruptedException {

        // Count simulated API requests.
        AtomicInteger requests = new AtomicInteger();

        // Simulate an initial failure followed by a valid NESO reading.
        CarbonIntensityService intensityService = new CarbonIntensityService(() -> {

                    // Fail the first request to trigger fallback and retry.
                    if (requests.incrementAndGet() == 1) {
                        throw new IOException("Simulated connection failure");
                    }

                    // Return a valid intensity for subsequent requests.
                    return 125.0;
                });

        // Create the monitoring service with simulated hardware readings.
        // Use a short retry interval to keep the test fast.
        LiveMonitoringService monitoringService =
                new LiveMonitoringService(
                        snapshot -> {},
                        () -> new LiveMonitorSnapshot(60.0, 40.0),
                        intensityService,
                        5000,
                        50
                );

        try {
            // Verify that the tracker initially uses the fallback.
            assertEquals( 200.0, monitoringService.getEmissionsTracker().getCarbonIntensity(), 0.001 );

            // Start hardware monitoring and background API refreshes.
            monitoringService.start();

            // Allow up to five seconds for the scheduled retry to succeed.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            // Wait until the recovered value reaches the tracker.
            while (monitoringService.getEmissionsTracker().getCarbonIntensity() != 125.0 && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }

            // Verify that the API recovery occurred.
            assertEquals( CarbonIntensityService.Source.API, monitoringService.getCurrentCarbonIntensity().source() );

            // Verify that the recovered intensity was applied to the tracker.
            assertEquals( 125.0, monitoringService.getEmissionsTracker().getCarbonIntensity(), 0.001 );

            // Verify that at least two API requests were performed.
            assertTrue( requests.get() >= 2, "The scheduler should retry after the failed request." );

        } finally {

            // Shut down both background schedulers after the test.
            monitoringService.stop();

            // Verify that the monitoring service terminates cleanly.
            assertTrue( monitoringService.awaitTermination(5, TimeUnit.SECONDS), "Both background schedulers should terminate." );
        }
    }


    // LIV-UT-056
    // Verify that stopping the monitoring service prevents
    // any further scheduled carbon intensity API requests.
    @Test
    @DisplayName("LIV-UT-056 - Cancels carbon intensity retries after shutdown")
    void cancelsCarbonIntensityRetriesAfterShutdown() throws InterruptedException {

        // Track the number of simulated API requests.
        AtomicInteger requests = new AtomicInteger();

        // Signal when the initial API request has completed its failure.
        CountDownLatch firstRequest = new CountDownLatch(1);

        // Simulate an unavailable NESO API on every request.
        CarbonIntensityService intensityService = new CarbonIntensityService(() -> {
                    // Record the API request.
                    requests.incrementAndGet();

                    // Report that the first request has started.
                    firstRequest.countDown();

                    // Simulate an API connection failure.
                    throw new IOException("Simulated API unavailable");
                });

        // Use simulated hardware and a short API retry interval.
        LiveMonitoringService monitoringService =
                new LiveMonitoringService(
                        snapshot -> {},
                        () -> new LiveMonitorSnapshot(60.0, 40.0),
                        intensityService,
                        5000,
                        200
                );

        try {
            // Begin hardware monitoring and API scheduling.
            monitoringService.start();

            // Verify that the initial API request was attempted.
            assertTrue( firstRequest.await(5, TimeUnit.SECONDS), "The initial API request should execute." );

            // Stop both schedulers before the next API retry.
            monitoringService.stop();

            // Verify both schedulers have terminated.
            assertTrue( monitoringService.awaitTermination(5, TimeUnit.SECONDS), "Both schedulers should terminate after shutdown." );

            // Store the number of API requests after shutdown.
            int requestsAfterShutdown = requests.get();

            // Wait beyond the configured 200 ms retry interval.
            Thread.sleep(350);

            // Confirm that no additional API requests were executed.
            assertEquals( requestsAfterShutdown, requests.get(), "No API requests should occur after shutdown." );

        } finally {
            // Ensure both schedulers are stopped if an assertion fails.
            monitoringService.stop();
        }
    }




}
