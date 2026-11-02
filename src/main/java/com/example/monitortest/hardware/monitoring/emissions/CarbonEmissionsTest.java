package com.example.monitortest.hardware.monitoring.emissions;

import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;

public class CarbonEmissionsTest {

    public static void main(String[] args) {

        CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();

        // Simulate 100 W of power consumption over 60 seconds.
        double emissions = calculator.calculateEmissions(100.0, 60.0);

        System.out.printf("Carbon intensity: %.1f g CO2e/kWh%n",
                calculator.getCarbonIntensity());

        System.out.printf("Estimated emissions: %.4f g CO2e%n",
                emissions);
    }
}