package com.example.monitortest.emissions;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;

import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests the accumulation of estimated carbon emissions over time.
// All sensor readings and timestamps are simulated for repeatable results.
class CarbonEmissionsTrackerTest {

    // Creates a fresh emissions tracker for each test.
    // The calculator uses the default carbon intensity of 200 gCO2e/kWh.
    private CarbonEmissionsTracker createTracker() {
        return new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );
    }

    // LIV-UT-015
    // A single measurement cannot establish how long power was consumed.
    @Test
    @DisplayName("LIV-UT-015 - Initial power snapshot does not accumulate emissions")
    void initialSnapshotDoesNotAccumulateEmissions() {

        CarbonEmissionsTracker tracker = createTracker();

        // Establish the initial power measurement at 1 second.
        tracker.addSnapshot( new LiveMonitorSnapshot(50.0, 20.0), 1000L );

        // No time interval has elapsed between readings yet.
        assertEquals( 0.0, tracker.getTotalEmissionsGrams(), 0.000001 );
    }

    // LIV-UT-016
    // Verify emissions accumulated using constant power over one minute.
    @Test
    @DisplayName("LIV-UT-016 - Accumulates correct CO2 emissions at constant power")
    void accumulatesEmissionsAtConstantPower() {

        CarbonEmissionsTracker tracker = createTracker();

        // Total power remains 100 W throughout the interval.
        tracker.addSnapshot( new LiveMonitorSnapshot(70.0, 30.0), 1000L );

        // Simulate another measurement 60 seconds later.
        tracker.addSnapshot( new LiveMonitorSnapshot(70.0, 30.0), 61000L );

        // Expected: (100 * 60 / 3,600,000) * 200.
        assertEquals( 0.3333333333, tracker.getTotalEmissionsGrams(), 0.000001 );
    }

    // LIV-UT-017
    // Verify the tracker averages consecutive power readings.
    @Test
    @DisplayName("LIV-UT-017 - Uses average power between consecutive measurements")
    void usesAveragePowerBetweenMeasurements() {

        CarbonEmissionsTracker tracker = createTracker();

        // First snapshot: 50 W.
        tracker.addSnapshot( new LiveMonitorSnapshot(40.0, 10.0), 1000L );

        // Second snapshot: 100 W, measured 60 seconds later.
        tracker.addSnapshot( new LiveMonitorSnapshot(80.0, 20.0), 61000L );

        // Average power = (50 + 100) / 2 = 75 W.
        // Expected: (75 * 60 / 3,600,000) * 200.
        assertEquals( 0.25, tracker.getTotalEmissionsGrams(), 0.000001 );
    }

    // LIV-UT-018
    // Reproduce the Intel laptop scenario where GPU power is unavailable.
    @Test
    @DisplayName("LIV-UT-018 - Continues accumulating CO2 emissions when GPU power is unavailable")
    void accumulatesEmissionsWithoutGpuPower() {

        CarbonEmissionsTracker tracker = createTracker();

        // Both snapshots have valid CPU power but missing GPU power.
        tracker.addSnapshot( new LiveMonitorSnapshot(20.0, Double.NaN), 1000L );

        tracker.addSnapshot(new LiveMonitorSnapshot(20.0, Double.NaN), 61000L );

        // Expected: (20 * 60 / 3,600,000) * 200.
        assertEquals( 0.0666666667, tracker.getTotalEmissionsGrams(), 0.000001 );
    }

    // LIV-UT-019
    // Verify that unavailable sensor data does not create fake emissions.
    @Test
    @DisplayName("LIV-UT-019 - Skips invalid intervals when both power sensors are unavailable")
    void skipsIntervalsWithUnavailablePower() {

        CarbonEmissionsTracker tracker = createTracker();

        // Establish an initial valid measurement.
        tracker.addSnapshot( new LiveMonitorSnapshot(50.0, 10.0), 1000L );

        // Both sensors become unavailable.
        tracker.addSnapshot( new LiveMonitorSnapshot(Double.NaN, Double.NaN), 61000L );

        // Monitoring recovers with a new valid measurement.
        tracker.addSnapshot( new LiveMonitorSnapshot(50.0, 10.0), 121000L );

        // The invalid interval must not produce emissions.
        // The recovered snapshot establishes a new baseline.
        assertEquals( 0.0, tracker.getTotalEmissionsGrams(), 0.000001 );
    }

    // LIV-UT-020
    // Verify that emissions are accumulated across multiple intervals.
    @Test
    @DisplayName("LIV-UT-020 - Accumulates CO2 emissions across multiple monitoring intervals")
    void accumulatesEmissionsAcrossMultipleIntervals() {

        CarbonEmissionsTracker tracker = createTracker();

        // Simulate a device consuming 100 W continuously.
        tracker.addSnapshot( new LiveMonitorSnapshot(70.0, 30.0), 1000L );

        // First interval: 30 seconds.
        tracker.addSnapshot( new LiveMonitorSnapshot(70.0, 30.0), 31000L );

        // Second interval: another 30 seconds.
        tracker.addSnapshot( new LiveMonitorSnapshot(70.0, 30.0), 61000L );

        // Total duration = 60 seconds at 100 W.
        assertEquals( 0.3333333333, tracker.getTotalEmissionsGrams(), 0.000001 );
    }


    // LIV-UT-051
    // Verify that updating the tracker's carbon intensity changes
    // future calculations without modifying previously calculated values.
    @Test
    @DisplayName("LIV-UT-051 - Updates carbon intensity for future calculations")
    void updatesCarbonIntensityForFutureCalculations() {

        // Create a calculator using EcoScan's original intensity.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator(200.0);

        // Create a tracker that uses the same calculator.
        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker(calculator);

        // Calculate emissions for 100 W over one hour.
        // At 200 g/kWh, the expected result is 20 grams.
        double firstPeriodEmissions = calculator.calculateEmissions(100.0, 3600.0);
        assertEquals(20.0, firstPeriodEmissions, 0.001);

        // Update the intensity through the tracker.
        tracker.updateCarbonIntensity(125.0);

        // Verify that the shared calculator received the new intensity.
        assertEquals(125.0, calculator.getCarbonIntensity(), 0.001);

        // Calculate emissions for another hour at the new intensity.
        double secondPeriodEmissions = calculator.calculateEmissions(100.0, 3600.0);
        assertEquals(12.5, secondPeriodEmissions, 0.001);

        // Verify that the previously calculated amount is unchanged.
        assertEquals(20.0, firstPeriodEmissions, 0.001);

        // Combine the two periods without recalculating the first.
        double combinedEmissions = firstPeriodEmissions + secondPeriodEmissions;
        assertEquals(32.5, combinedEmissions, 0.001);
    }


    // LIV-UT-052
    // Verify that changing carbon intensity affects future snapshot
    // intervals without recalculating previously accumulated emissions.
    @Test
    @DisplayName("LIV-UT-052 - Preserves accumulated emissions after intensity update")
    void preservesAccumulatedEmissionsAfterIntensityUpdate() {

        // Create the calculator with EcoScan's original intensity.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator(200.0);

        // Create a tracker to accumulate emissions across snapshots.
        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker(calculator);

        // Simulate a constant 100 W device using CPU and GPU readings.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(60.0, 40.0);

        // Establish the initial measurement at 1,000 milliseconds.
        tracker.addSnapshot(snapshot, 1_000L);

        // Record another snapshot one hour later.
        tracker.addSnapshot(snapshot, 3_601_000L);

        // Verify that the first hour accumulated 20 grams.
        assertEquals( 20.0, tracker.getTotalEmissionsGrams(), 0.001 );

        // Update the carbon intensity to simulate a new NESO reading.
        tracker.updateCarbonIntensity(125.0);

        // Verify the previous total remains unchanged immediately.
        assertEquals( 20.0, tracker.getTotalEmissionsGrams(), 0.001 );

        // Record a third snapshot one additional hour later.
        tracker.addSnapshot(snapshot, 7_201_000L);

        // Verify that the second hour adds 12.5 grams,
        // giving a combined total of 32.5 grams.
        assertEquals( 32.5, tracker.getTotalEmissionsGrams(), 0.001 );

        // Verify the average across the two monitored hours.
        assertEquals( 32.5 / 7200.0, tracker.getAverageEmissionsPerSecond(), 0.000001 );
    }


}
