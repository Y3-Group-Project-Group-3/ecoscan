package com.example.monitortest.emissions;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for EcoScan's carbon emissions calculator.
// Uses known values instead of live hardware measurements.
class CarbonEmissionsCalculatorTest {

    // Test 1: Verify that a valid power reading produces the expected amount of carbon emissions.
    @Test
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
    void rejectsUnavailablePowerReadings() {

        // Create a calculator using the default configuration.
        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // NaN represents an unavailable hardware sensor.
        double actual = calculator.calculateEmissions( Double.NaN, 60.0 );

        // Confirm the calculator rejects the invalid reading.
        assertTrue(Double.isNaN(actual));
    }
}
