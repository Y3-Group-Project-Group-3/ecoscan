package com.example.monitortest.ui.loading;

import com.example.monitortest.testsupport.SwingTestSupport;
import com.example.monitortest.ui.loading.components.GradientProgressBar;
import com.example.monitortest.ui.loading.components.HardwareRow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Tests for the scanning screen: progress, the part being detected, per-component status and the warning.
class LoadingPanelTest {

    private static final String[] PARTS = {"CPU", "GPU", "RAM", "Storage"};

    private static final String WAITING = "WAITING";
    private static final String DETECTING = "DETECTING...";
    private static final String COMPLETED = "COMPLETED";

    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }

    private static LoadingPanel createPanel() {
        return onEdt(LoadingPanel::new);
    }


    // ---- Helpers that read the screen through its labels ----

    private static List<HardwareRow> rows(LoadingPanel panel) {
        return findAll(panel, HardwareRow.class);
    }

    // The status text shown by a hardware row (WAITING, DETECTING... or COMPLETED).
    private static String statusOf(HardwareRow row) {
        for (JLabel label : findAll(row, JLabel.class)) {
            String text = label.getText();
            if (WAITING.equals(text) || DETECTING.equals(text) || COMPLETED.equals(text)) {
                return text;
            }
        }
        return null;
    }

    private static String statusOf(LoadingPanel panel, int rowIndex) {
        return onEdt(() -> statusOf(rows(panel).get(rowIndex)));
    }

    // The percentage label, found by its "NN%" text.
    private static String percentText(LoadingPanel panel) {
        return onEdt(() -> {
            for (JLabel label : findAll(panel, JLabel.class)) {
                if (label.getText() != null && label.getText().matches("\\d+%")) {
                    return label.getText();
                }
            }
            return null;
        });
    }

    // The label that reads "Waiting for scan" before a scan and "Now detecting:" during one.
    private static JLabel prefixLabel(LoadingPanel panel) {
        JLabel prefix = findLabel(panel, "Waiting for scan");
        return prefix != null ? prefix : findLabel(panel, "Now detecting:");
    }

    private static String prefixText(LoadingPanel panel) {
        return onEdt(() -> prefixLabel(panel).getText());
    }

    // The label beside the prefix that names the part being detected.
    private static String currentPartText(LoadingPanel panel) {
        return onEdt(() -> {
            JLabel prefix = prefixLabel(panel);
            for (JLabel label : findAll(prefix.getParent(), JLabel.class)) {
                if (label != prefix) {
                    return label.getText();
                }
            }
            return null;
        });
    }

    private static boolean warningVisible(LoadingPanel panel) {
        return onEdt(() -> {
            JLabel title = findLabel(panel, "Warning");
            return title != null && isVisibleWithin(title, panel);
        });
    }

    private static void assertAllRowsAre(LoadingPanel panel, String expected) {
        for (int i = 0; i < PARTS.length; i++) {
            assertEquals(expected, statusOf(panel, i), PARTS[i] + " row");
        }
    }


    // ---- Initial state ----

    @Test
    @DisplayName("Starts in the waiting state")
    void startsWaiting() {
        LoadingPanel panel = createPanel();

        assertEquals("Waiting for scan", prefixText(panel));
        assertEquals("", currentPartText(panel));
        assertEquals("0%", percentText(panel));
        assertAllRowsAre(panel, WAITING);
        assertFalse(warningVisible(panel), "the warning should be hidden until a problem is reported");
    }


    // ---- Progress ----

    @Test
    @DisplayName("Shows the percentage it is given")
    void showsPercentage() {
        LoadingPanel panel = createPanel();

        panel.setProgress(42);
        flushEdt();

        assertEquals("42%", percentText(panel));
    }

    @Test
    @DisplayName("Clamps values above 100 and below 0")
    void clampsOutOfRangeValues() {
        LoadingPanel panel = createPanel();

        panel.setProgress(150);
        flushEdt();
        assertEquals("100%", percentText(panel));

        panel.setProgress(-5);
        flushEdt();
        assertEquals("0%", percentText(panel));

        panel.setProgress(Integer.MAX_VALUE);
        flushEdt();
        assertEquals("100%", percentText(panel));

        panel.setProgress(Integer.MIN_VALUE);
        flushEdt();
        assertEquals("0%", percentText(panel));
    }


    // ---- Current part ----

    @Test
    @DisplayName("Names the part being detected")
    void namesTheCurrentPart() {
        LoadingPanel panel = createPanel();

        panel.setCurrentPart("GPU");
        flushEdt();

        assertEquals("Now detecting:", prefixText(panel));
        assertEquals("GPU", currentPartText(panel));

        panel.setCurrentPart("RAM");
        flushEdt();

        assertEquals("RAM", currentPartText(panel));
    }


    // ---- Component status rows ----

    @Test
    @DisplayName("Updates only the row for the named part")
    void updatesTheMatchingRowOnly() {
        LoadingPanel panel = createPanel();

        panel.setPartStatus("GPU", "DETECTING");
        flushEdt();

        assertEquals(WAITING, statusOf(panel, 0), "CPU");
        assertEquals(DETECTING, statusOf(panel, 1), "GPU");
        assertEquals(WAITING, statusOf(panel, 2), "RAM");
        assertEquals(WAITING, statusOf(panel, 3), "Storage");
    }

    @Test
    @DisplayName("A part moves from waiting to detecting to completed")
    void partMovesThroughAllStatuses() {
        LoadingPanel panel = createPanel();

        panel.setPartStatus("CPU", "DETECTING");
        flushEdt();
        assertEquals(DETECTING, statusOf(panel, 0));

        panel.setPartStatus("CPU", "DONE");
        flushEdt();
        assertEquals(COMPLETED, statusOf(panel, 0));
    }

    @Test
    @DisplayName("Ignores a part it does not know")
    void ignoresUnknownParts() {
        LoadingPanel panel = createPanel();

        assertDoesNotThrow(() -> {
            panel.setPartStatus("Fan", "DONE");
            flushEdt();
        });

        assertAllRowsAre(panel, WAITING);
    }

    @Test
    @DisplayName("Ignores null arguments")
    void ignoresNulls() {
        LoadingPanel panel = createPanel();

        assertDoesNotThrow(() -> {
            panel.setPartStatus(null, "DONE");
            panel.setPartStatus("CPU", null);
            panel.setPartStatus(null, null);
            flushEdt();
        });

        assertAllRowsAre(panel, WAITING);
    }


    // ---- Warning ----

    @Test
    @DisplayName("Shows the warning when the backend reports a problem")
    void showsTheWarning() {
        LoadingPanel panel = createPanel();

        panel.showWarning(true);
        flushEdt();

        assertTrue(warningVisible(panel));
        assertNotNull(findLabelContaining(panel, "Some hardware information could not be retrieved."));
    }

    @Test
    @DisplayName("Hides the warning again")
    void hidesTheWarning() {
        LoadingPanel panel = createPanel();

        panel.showWarning(true);
        panel.showWarning(false);
        flushEdt();

        assertFalse(warningVisible(panel));
    }


    // ---- Reset ----

    @Test
    @DisplayName("Reset returns every part of the screen to its starting state")
    void resetRestoresTheStartingState() {
        LoadingPanel panel = createPanel();

        panel.setProgress(75);
        panel.setCurrentPart("Storage");
        panel.setPartStatus("CPU", "DONE");
        panel.setPartStatus("GPU", "DONE");
        panel.setPartStatus("RAM", "DETECTING");
        panel.showWarning(true);
        flushEdt();

        panel.resetProgress();
        flushEdt();

        assertEquals("0%", percentText(panel));
        assertEquals("Waiting for scan", prefixText(panel));
        assertEquals("", currentPartText(panel));
        assertAllRowsAre(panel, WAITING);
        assertFalse(warningVisible(panel));
    }


    // ---- Threading ----

    @Test
    @DisplayName("Updates sent from a background thread are applied on the EDT")
    void backgroundThreadUpdatesAreApplied() {
        LoadingPanel panel = createPanel();

        // The scan runs off the EDT, so the panel has to marshal updates itself.
        assertFalse(SwingUtilities.isEventDispatchThread());

        panel.setProgress(60);
        panel.setCurrentPart("RAM");
        panel.setPartStatus("RAM", "DETECTING");
        panel.showWarning(true);
        flushEdt();

        assertEquals("60%", percentText(panel));
        assertEquals("RAM", currentPartText(panel));
        assertEquals(DETECTING, statusOf(panel, 2));
        assertTrue(warningVisible(panel));

        panel.resetProgress();
        flushEdt();

        assertEquals("0%", percentText(panel));
    }


    // ---- A whole scan ----

    @Test
    @DisplayName("Follows a full scan the way ScanEventListener drives it")
    void followsAFullScan() {
        LoadingPanel panel = createPanel();

        // The same messages ScanHardware sends: each part reports DETECTING then DONE.
        int completed = 0;
        for (int i = 0; i < PARTS.length; i++) {
            panel.setPartStatus(PARTS[i], "DETECTING");
            panel.setCurrentPart(PARTS[i]);
            flushEdt();

            assertEquals(DETECTING, statusOf(panel, i), PARTS[i] + " detecting");
            assertEquals(PARTS[i], currentPartText(panel));

            panel.setPartStatus(PARTS[i], "DONE");
            completed++;
            panel.setProgress(completed * 100 / PARTS.length);
            flushEdt();

            assertEquals(COMPLETED, statusOf(panel, i), PARTS[i] + " done");
            assertEquals(completed * 25 + "%", percentText(panel));
        }

        assertAllRowsAre(panel, COMPLETED);
        assertEquals("100%", percentText(panel));
    }

}
