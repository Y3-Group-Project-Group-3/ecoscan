package hardware.monitoring.emissions;

import com.example.monitortest.hardware.monitoring.emissions.CarbonIntensityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

// Tests how EcoScan manages carbon intensity values and fallback behaviour.
// Simulated providers keep the tests independent of internet connectivity.
class CarbonIntensityServiceTest {

    // LIV-UT-046
    // Verify that the service starts with EcoScan's existing fallback before requesting any carbon intensity data from NESO.
    @Test
    @DisplayName("LIV-UT-046 - Starts with the default carbon intensity fallback")
    void startsWithDefaultFallback() {

        // Create a simulated provider that would return an API value.
        CarbonIntensityService service = new CarbonIntensityService(() -> 125.0);

        // Read the initial state without triggering an API request.
        CarbonIntensityService.IntensityReading reading = service.getCurrentReading();

        // Verify the existing default intensity is preserved.
        assertEquals( 200.0, reading.gramsPerKwh(), 0.001 );

        // Verify that the service identifies the value as a fallback.
        assertEquals( CarbonIntensityService.Source.FALLBACK, reading.source() );
    }


    // LIV-UT-047
    // Verify that a successful API refresh replaces the default carbon intensity with the value supplied by NESO.
    @Test
    @DisplayName("LIV-UT-047 - Updates carbon intensity after a successful API refresh")
    void updatesIntensityAfterSuccessfulRefresh() {

        // Create a simulated API provider returning a valid intensity.
        CarbonIntensityService service = new CarbonIntensityService(() -> 125.0);

        // Verify that the service initially uses the fallback.
        assertEquals(
                CarbonIntensityService.Source.FALLBACK,
                service.getCurrentReading().source()
        );

        // Simulate a successful API refresh.
        service.refresh();

        // Retrieve the updated reading.
        CarbonIntensityService.IntensityReading reading = service.getCurrentReading();

        // Verify that the new intensity replaces the fallback.
        assertEquals(
                125.0,
                reading.gramsPerKwh(),
                0.001,
                "The service should store the API-provided intensity."
        );

        // Verify that the reading is identified as API data.
        assertEquals(
                CarbonIntensityService.Source.API,
                reading.source(),
                "A successful refresh should identify the source as API."
        );
    }


    // LIV-UT-048
    // Verify that a failed API request restores the default fallback,
    // even when a previous refresh successfully returned live data.
    @Test
    @DisplayName("LIV-UT-048 Uses fallback after an API request failure")
    void usesFallbackAfterApiFailure() {

        // Count requests so the simulated API can return different outcomes.
        java.util.concurrent.atomic.AtomicInteger requests = new java.util.concurrent.atomic.AtomicInteger();

        // First request succeeds; the second simulates a network failure.
        CarbonIntensityService service = new CarbonIntensityService(() -> {

            if (requests.incrementAndGet() == 1) {
                return 125.0;
            }

            throw new IOException("Simulated network failure.");
        });

        // First refresh: accept the API value.
        service.refresh();
        assertEquals( 125.0, service.getCurrentReading().gramsPerKwh(), 0.001 );
        assertEquals( CarbonIntensityService.Source.API, service.getCurrentReading().source() );

        // Second refresh: simulate loss of API connectivity.
        service.refresh();

        // Retrieve the reading after the failed request.
        CarbonIntensityService.IntensityReading reading = service.getCurrentReading();

        // Verify that EcoScan restores its offline assumption.
        assertEquals( CarbonIntensityService.FALLBACK_INTENSITY, reading.gramsPerKwh(), 0.001 );

        // Verify that the reading is identified as a fallback.
        assertEquals( CarbonIntensityService.Source.FALLBACK, reading.source() );
    }


    // LIV-UT-049
    // Verify that invalid API intensity values are rejected
    // before they can be used in emissions calculations.
    @Test
    @DisplayName("LIV-UT-049 - Rejects invalid API intensity values and uses fallback")
    void rejectsInvalidApiIntensityValues() {

        // Define invalid intensity values to test.
        double[] invalidValues = { Double.NaN, Double.POSITIVE_INFINITY, -25.0 };

        // Test each invalid value independently.
        for (double invalidValue : invalidValues) {

            // Simulate an API provider returning the invalid value.
            CarbonIntensityService service = new CarbonIntensityService(() -> invalidValue);

            // Attempt to refresh the carbon intensity.
            service.refresh();

            // Retrieve the resulting intensity and its source.
            CarbonIntensityService.IntensityReading reading = service.getCurrentReading();

            // Verify that the default fallback is used.
            assertEquals(
                    CarbonIntensityService.FALLBACK_INTENSITY,
                    reading.gramsPerKwh(),
                    0.001,
                    "Invalid intensity should trigger the fallback."
            );

            // Verify that the source is identified as a fallback.
            assertEquals(
                    CarbonIntensityService.Source.FALLBACK,
                    reading.source(),
                    "Invalid intensity must not be identified as API data."
            );
        }
    }


    // LIV-UT-050
    // Verify that an interrupted API request restores the fallback and preserves the calling thread's interruption status.
    @Test
    @DisplayName("LIV-UT-050 - Preserves interruption and uses fallback")
    void preservesInterruptionAndUsesFallback() throws Exception {

        // Run this test on a dedicated thread to avoid interrupting
        // JUnit's test execution thread.
        java.util.concurrent.atomic.AtomicReference<CarbonIntensityService.IntensityReading> readingRef = new java.util.concurrent.atomic.AtomicReference<>();

        java.util.concurrent.atomic.AtomicBoolean interruptedRef = new java.util.concurrent.atomic.AtomicBoolean(false);

        // Create a worker that simulates an interrupted API request.
        Thread worker = new Thread(() -> {

            // The simulated provider always reports interruption.
            CarbonIntensityService service =
                    new CarbonIntensityService(() -> {
                        throw new InterruptedException( "Simulated API request interruption." );
                    });

            // Attempt to refresh the carbon intensity.
            service.refresh();

            // Capture the resulting fallback reading.
            readingRef.set(service.getCurrentReading());

            // Capture the worker thread's interruption status.
            interruptedRef.set(
                    Thread.currentThread().isInterrupted()
            );

        }, "EcoScan-CarbonIntensity-Test");

        // Start the simulated API worker.
        worker.start();

        // Wait for the worker to complete.
        worker.join(3000);

        // Verify that the test worker has terminated.
        assertFalse( worker.isAlive(), "The simulated API worker should terminate." );

        // Retrieve the reading produced by the worker.
        CarbonIntensityService.IntensityReading reading = readingRef.get();

        assertNotNull(reading);

        // Verify that the original offline fallback is selected.
        assertEquals( CarbonIntensityService.FALLBACK_INTENSITY, reading.gramsPerKwh(), 0.001 );

        // Verify that the reading is identified as a fallback.
        assertEquals( CarbonIntensityService.Source.FALLBACK, reading.source() );

        // Verify that InterruptedException was handled correctly.
        assertTrue( interruptedRef.get(), "The worker's interrupted status must be preserved." );
    }




}
