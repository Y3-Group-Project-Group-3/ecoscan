package com.example.monitortest.ui.livemonitor.components;

import java.util.List;

// Provides the shared emissions milestones used by EcoScan's live monitoring and hardware scan results.
public final class EmissionMilestones {

    // Prevent instances of this utility class from being created.
    private EmissionMilestones() {
    }

    // Reference activities ordered by estimated carbon emissions.
    // Values are provisional and may be updated in the future.
    private static final List<EmissionMilestone> MILESTONES = List.of(
            new EmissionMilestone("Google Search", 0.2, "1 search"),
            new EmissionMilestone("Disposable Coffee Cup", 16.0, "1 cup"),
            new EmissionMilestone("Eco-Friendly Light Bulb", 30.0, "4 hours"),
            new EmissionMilestone("Plastic Bag", 33.0, "1 bag"),
            new EmissionMilestone("Cup of Tea", 40.0, "1 cup"),
            new EmissionMilestone("Water Kettle", 50.0, "Typical use"),
            new EmissionMilestone("Banana", 70.0, "1 banana"),
            new EmissionMilestone("Plastic Water Bottle", 83.0, "1 bottle"),
            new EmissionMilestone("Pasta", 117.0, "1 serving, 75 g"),
            new EmissionMilestone("Plant-Based Latte", 140.0, "1 latte"),
            new EmissionMilestone("Standard Light Bulb", 172.0, "4 hours"),
            new EmissionMilestone("Public Transport", 244.0, "20-minute commute"),
            new EmissionMilestone("Washing Machine", 275.0, "Typical cycle"),
            new EmissionMilestone("Dairy Latte", 350.0, "1 latte"),
            new EmissionMilestone("Microwave", 400.0, "Typical use"),
            new EmissionMilestone("Car Commute", 560.0, "10 minutes"),
            new EmissionMilestone("Electric Oven", 675.0, "Typical use"),
            new EmissionMilestone("Tumble Dryer", 1000.0, "Typical cycle"),
            new EmissionMilestone("Hot Shower", 2000.0, "10 minutes"),
            new EmissionMilestone("Beef", 7700.0, "1 serving, 75 g"),
            new EmissionMilestone("London to Paris Flight", 40000.0, "One way, economy"),
            new EmissionMilestone("NY to London Flight", 900000.0, "One way, economy")
    );

    // Returns the shared, unmodifiable list of milestones.
    public static List<EmissionMilestone> getMilestones() {
        return MILESTONES;
    }
}
