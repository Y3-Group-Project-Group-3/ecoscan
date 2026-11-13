package com.example.monitortest.emissions;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for EcoScan's carbon emissions calculator.
// Uses known values instead of live hardware measurements.
class CarbonEmissionsCalculatorTest {

    // Test 1: Verify that a valid power reading produces the expected amount of carbon emissions.
    @Test
    @DisplayName("LIV-UT-001 — Calculates correct CO₂ emissions for 100 W over 60 seconds")
    void calculatesEmissionsCorrectly() {

        // Create the calculator with the default carbon intensity of 200 gCO2e/kWh.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Simulate 100 watts of power consumption for 60 seconds.
        double actual = calculator.calculateEmissions(100.0, 60.0);

        // Independently calculate the expected result.
        double expected = 0.3333333333;

        // Compare the values within a small floating-point tolerance.
        assertEquals(expected, actual, 0.000001);
    }

    // Test 2: Verify that an unavailable power sensor produces NaN instead of an incorrect emissions estimate.
    @Test
    @DisplayName("LIV-UT-002 — Returns NaN when the power sensor reading is unavailable")
    void rejectsUnavailablePowerReadings() {

        // Create a calculator using the default configuration.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // NaN represents an unavailable hardware sensor.
        double actual = calculator.calculateEmissions( Double.NaN, 60.0 );

        // Confirm the calculator rejects the invalid reading.
        assertTrue(Double.isNaN(actual));
    }


    // Test 3: Verify that no power consumption produces no carbon emissions.
    @Test
    @DisplayName("LIV-UT-003 — Returns zero CO2 emissions when device power consumption is zero")
    void returnsZeroEmissionsForZeroPower() {

        // Create the calculator with its default carbon intensity.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Simulate a device using no power for 60 seconds.
        double actual = calculator.calculateEmissions(0.0, 60.0);

        // No electricity consumption should produce zero emissions.
        assertEquals(0.0, actual, 0.000001);
    }

    // Test 4: Verify that negative hardware power readings are rejected.
    @Test
    @DisplayName("LIV-UT-004 — Returns NaN when the power sensor reports a negative value")
    void rejectsNegativePowerReadings() {

        // Create the calculator with the default configuration.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Negative electrical power consumption is invalid for this model.
        double actual = calculator.calculateEmissions(-10.0, 60.0);

        // The calculator should reject the invalid reading.
        assertTrue(Double.isNaN(actual));
    }

    // Test 5: Verify that a custom carbon intensity affects the result.
    @Test
    @DisplayName("LIV-UT-005 — Uses the configured carbon intensity when calculating CO2 emissions")
    void usesConfiguredCarbonIntensity() {

        // Create a calculator using 100 gCO2e/kWh instead of the default 200.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator(100.0);

        // Simulate a device consuming 100 W for 60 seconds.
        double actual = calculator.calculateEmissions(100.0, 60.0);

        // Expected: (100 * 60 / 3,600,000) * 100.
        double expected = 0.1666666667;

        // Verify that the configurable intensity is applied correctly.
        assertEquals(expected, actual, 0.000001);
    }


    // LIV-UT-006
    // Verify that an invalid negative monitoring duration is rejected.
    @Test
    @DisplayName("LIV-UT-006 — Returns NaN when the monitoring duration is negative")
    void rejectsNegativeDuration() {

        // Create a calculator using the default carbon intensity.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Attempt to calculate emissions over an invalid time interval.
        double actual = calculator.calculateEmissions(100.0, -60.0);

        // An invalid duration must not produce an emissions estimate.
        assertTrue(Double.isNaN(actual));
    }

    // LIV-UT-007
    // Verify that an invalid carbon intensity cannot be configured.
    @Test
    @DisplayName("LIV-UT-007 — Rejects negative carbon intensity configuration")
    void rejectsNegativeCarbonIntensity() {

        // Create the calculator with a valid initial configuration.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Setting a negative intensity should raise an exception.
        assertThrows( IllegalArgumentException.class, () -> calculator.setCarbonIntensity(-50.0) );
    }

    // LIV-UT-008
    // Verify the time required to reach a specified emissions milestone.
    @Test
    @DisplayName("LIV-UT-008 — Calculates the correct time to reach a CO2 emissions milestone")
    void calculatesTimeToEmissionsMilestone() {

        // Use the default carbon intensity of 200 gCO2e/kWh.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // At 100 W, 1 gram of CO2e requires 180 seconds.
        double actual = calculator.calculateTimeToEmissions( 100.0, 1.0 );

        // Expected: (1 * 3,600,000) / (100 * 200) = 180 seconds.
        assertEquals(180.0, actual, 0.000001);
    }

    // LIV-UT-009
    // Verify that a milestone cannot be reached with zero power.
    @Test
    @DisplayName("LIV-UT-009 — Returns NaN when calculating milestone time with zero power")
    void rejectsZeroPowerForMilestone() {

        // Create a calculator with the default carbon intensity.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Zero power cannot accumulate positive emissions.
        double actual = calculator.calculateTimeToEmissions( 0.0, 1.0 );

        // The milestone calculation should report an invalid result.
        assertTrue(Double.isNaN(actual));
    }


}


