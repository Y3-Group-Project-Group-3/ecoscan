package com.example.monitortest.ui.history.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for how Live Monitor session emissions are displayed on the History page.
class SessionRowTest {

    // HIS-UT-018
    // Verify that totals under 1000 grams are shown in grams.
    @Test
    @DisplayName("HIS-UT-018 - Shows totals under 1000 g in grams")
    void showsSmallTotalsInGrams() {

        // Format a typical session total of 0.12 grams.
        String actual = SessionRow.formatEmissions(0.12);

        // Small totals should stay in grams.
        assertEquals("0.12 g CO₂e", actual);
    }

    // HIS-UT-019
    // Verify that totals of 1000 grams or more are shown in kilograms.
    @Test
    @DisplayName("HIS-UT-019 - Shows totals of 1000 g or more in kilograms")
    void showsLargeTotalsInKilograms() {

        // Format a session total of 1500 grams.
        String actual = SessionRow.formatEmissions(1500.0);

        // 1500 g should be converted to 1.50 kg.
        assertEquals("1.50 kg CO₂e", actual);
    }
}