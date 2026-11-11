package com.example.monitortest.ui.loading.components;

import com.example.monitortest.testsupport.SwingTestSupport;
import com.example.monitortest.ui.loading.components.HardwareRow.Status;
import com.example.monitortest.ui.theme.AppTheme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Tests for one hardware row on the loading screen (name, icon and status).
class HardwareRowTest {

    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }

    private static HardwareRow createRow(boolean first, boolean last) {
        return onEdt(() -> new HardwareRow("CPU", "CPU", first, last));
    }

    // Counts the status labels, which should always be exactly one.
    private static int statusLabelCount(HardwareRow row) {
        return countLabels(row, "WAITING") + countLabels(row, "DETECTING...") + countLabels(row, "COMPLETED");
    }

    private static void setStatus(HardwareRow row, Status status) {
        onEdt(() -> row.setStatus(status));
    }


    @Test
    @DisplayName("Shows its name and starts as waiting")
    void startsWaiting() {
        HardwareRow row = createRow(true, false);

        assertNotNull(findLabel(row, "CPU"));
        assertNotNull(findLabel(row, "WAITING"));
        assertEquals(1, statusLabelCount(row));
        assertEquals(0, findAll(row, Spinner.class).size(), "no spinner while waiting");
    }

    @Test
    @DisplayName("Detecting shows the label and a spinner")
    void detectingShowsSpinner() {
        HardwareRow row = createRow(false, false);

        setStatus(row, Status.DETECTING);

        assertNotNull(findLabel(row, "DETECTING..."));
        assertEquals(1, statusLabelCount(row));
        assertEquals(1, findAll(row, Spinner.class).size());

        // Stop the animation so the test leaves no running timer behind.
        onEdt(row::stopSpinner);
    }

    @Test
    @DisplayName("Done shows 'COMPLETED' and removes the spinner")
    void doneRemovesSpinner() {
        HardwareRow row = createRow(false, false);

        setStatus(row, Status.DETECTING);
        setStatus(row, Status.DONE);

        assertNotNull(findLabel(row, "COMPLETED"));
        assertEquals(1, statusLabelCount(row));
        assertEquals(0, findAll(row, Spinner.class).size());
    }


    // ---- Painting ----

}
