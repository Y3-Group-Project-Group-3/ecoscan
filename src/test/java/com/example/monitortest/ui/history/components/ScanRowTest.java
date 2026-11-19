package com.example.monitortest.ui.history.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the saved scan rows on the History page.
class ScanRowTest {

    // HIS-UT-017
    // Verify that selecting a row highlights it in light green.
    @Test
    @DisplayName("HIS-UT-017 - Highlights a scan row green when it is selected")
    void highlightsSelectedRow() {

        // Create a row for a saved hardware scan.
        ScanRow row = new ScanRow("6 Nov 2026", "Brandon scan");

        // Select the row as if the user had clicked it.
        row.setSelected(true);

        // The row should be selected and use the light green selection colour.
        assertTrue(row.isSelected());
        assertEquals(new Color(0xEAF5EE), row.getBackground());
    }
}