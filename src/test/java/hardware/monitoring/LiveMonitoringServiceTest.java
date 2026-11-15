package hardware.monitoring;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.hardware.monitoring.LiveMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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



}
