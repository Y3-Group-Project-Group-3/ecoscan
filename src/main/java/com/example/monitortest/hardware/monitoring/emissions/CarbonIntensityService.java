package com.example.monitortest.hardware.monitoring.emissions;

import java.io.IOException;

// Manages the carbon intensity currently used by EcoScan.
// Uses NESO data when available and a fixed fallback otherwise.
public class CarbonIntensityService {

    // Preserve EcoScan's existing offline assumption.
    public static final double FALLBACK_INTENSITY = 200.0;

    // Identifies where the current value came from.
    public enum Source {
        API,
        FALLBACK
    }

    // Represents a source of carbon intensity measurements.
    // Allows real API retrieval or simulated responses during testing.
    @FunctionalInterface
    public interface IntensityProvider {
        double fetch() throws IOException, InterruptedException;
    }


    // Store the value and its source together for consistent reads.
    public record IntensityReading(double gramsPerKwh, Source source) {}

    private final IntensityProvider provider;

    // Start with a valid value before any API request occurs.
    private volatile IntensityReading currentReading = new IntensityReading(FALLBACK_INTENSITY, Source.FALLBACK);

    // Production constructor: use the real NESO API client.
    public CarbonIntensityService() {
        this(new CarbonIntensityApiClient()::fetchCurrentCarbonIntensity);
    }

    // Allows a custom intensity provider to be supplied.
    // Used by automated tests to avoid real network requests.
    public CarbonIntensityService(IntensityProvider provider) {
        this.provider = java.util.Objects.requireNonNull(provider);
    }


    // Retrieve the latest intensity from NESO.
    // Call this method on a background thread, never Swing's EDT.
    public void refresh() {

        try {
            // Request the latest intensity from the API provider.
            double intensity = provider.fetch();

            // Reject unexpected values before updating the service.
            if (!Double.isFinite(intensity) || intensity < 0) {
                throw new IOException("Invalid carbon intensity received.");
            }

            // Store the accepted value and identify it as API data.
            currentReading = new IntensityReading(intensity, Source.API);

        } catch (IOException exception) {

            // Continue using the existing offline assumption.
            currentReading = new IntensityReading( FALLBACK_INTENSITY, Source.FALLBACK );
            System.err.println( "Carbon intensity unavailable: " + exception.getMessage() );

        } catch (InterruptedException exception) {

            // Preserve the interruption signal for the calling thread.
            Thread.currentThread().interrupt();

            // Use the fallback when the request is interrupted.
            currentReading = new IntensityReading( FALLBACK_INTENSITY, Source.FALLBACK );
        }
    }

    // Return the latest intensity and its source.
    // Reading this value does not make a network request.
    public IntensityReading getCurrentReading() {
        return currentReading;
    }
}
