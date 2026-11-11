package com.example.monitortest.ui.loading;

import com.example.monitortest.testsupport.SwingTestSupport;
import com.example.monitortest.ui.loading.components.HardwareRow;
import com.example.monitortest.ui.loading.components.Spinner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for the hardware scan loading page (LoadingPanel).
 *
 * LoadingPanelTest covers the normal behaviour of the page. This class covers:
 *  - unusual input that the normal scan does not usually send (tests 1 to 3), to make sure it is
 *    handled safely instead of crashing the scan or showing the wrong thing on screen, and
 *  - the separation between the page's parts (tests 4 to 7): progress, part status and the
 *    "Now detecting" text are each changed only by their own method, and repeated updates do
 *    not duplicate anything on screen.
 *
 * Every test reads the page the way a user would see it, through its labels, rather than
 * looking at private fields, so the tests do not depend on how LoadingPanel is built inside.
 */
class LoadingPanelEdgeCaseTest {

    // The four hardware parts, in the order their rows appear on the page.
    private static final String[] PARTS = {"CPU", "GPU", "RAM", "Storage"};

    // The status text each hardware row can show.
    private static final String WAITING = "WAITING";
    private static final String DETECTING = "DETECTING...";
    private static final String COMPLETED = "COMPLETED";

    // Run Swing without opening any windows, so the tests also work on build servers.
    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }


    // ---- Edge-case tests ----

    /**
     * Test 1: Part Names And Statuses Are Not Case-Sensitive.
     *
     * The scanner normally sends capitals ("GPU", "DETECTING"), but setPartStatus uses
     * equalsIgnoreCase and toUpperCase so that lower case still works. This is a defensive
     * test: it protects that behaviour if the scanner code ever sends lower case.
     */
    @Test
    @DisplayName("Accepts Part Names And Statuses In Lower Case")
    void acceptsLowerCaseNamesAndStatuses() {
        // Given a fresh loading page (every row starts as WAITING)
        LoadingPanel panel = createPanel();

        // When the GPU row is updated using lower-case text
        panel.setPartStatus("gpu", "detecting");
        flushEdt(); // wait for the update to reach the screen

        // Then the GPU row (index 1) shows the detecting status, as if capitals had been used
        assertEquals(DETECTING, statusOf(panel, 1), "GPU");
    }

    /**
     * Test 2: A Status That HardwareRow Does Not Know Is Ignored Without Crashing.
     *
     * setPartStatus turns the text into a HardwareRow.Status with valueOf(), which throws an
     * IllegalArgumentException for unknown text such as "BROKEN". The panel catches that
     * exception. This test makes sure the catch stays in place.
     */
    @Test
    @DisplayName("Ignores A Status It Does Not Know")
    void ignoresUnknownStatuses() {
        // Given a fresh loading page
        LoadingPanel panel = createPanel();

        // When the CPU row is sent a status that does not exist.
        // The call is run directly on the EDT with onEdt(...): if setPartStatus were called from
        // this test thread it would pass the work to the EDT, and a crash there would never
        // reach this test. Running it on the EDT means any exception makes the test fail.
        assertDoesNotThrow(() -> onEdt(() -> panel.setPartStatus("CPU", "BROKEN")));

        // Then the CPU row (index 0) has not changed and is still waiting
        assertEquals(WAITING, statusOf(panel, 0), "CPU");
    }

    /**
     * Test 3: A Missing (Null) Part Name Shows As Blank Text.
     *
     * setCurrentPart replaces null with an empty string. Without that check, the label's text
     * would be set to null instead of a proper empty value, so a missing name is a case worth
     * protecting.
     */
    @Test
    @DisplayName("Shows A Blank Part Name When Given Null")
    void blankCurrentPartWhenNull() {
        // Given a fresh loading page
        LoadingPanel panel = createPanel();

        // When the current part is set with no name
        assertDoesNotThrow(() -> {
            panel.setCurrentPart(null);
            flushEdt();
        });

        // Then the prefix switches to "Now detecting:" and the part name beside it is empty
        assertEquals("Now detecting:", prefixText(panel));
        assertEquals("", currentPartText(panel));
    }

    /**
     * Test 4: Finishing Parts Does Not Change The Percentage By Itself.
     *
     * LoadingPanel only displays progress. The percentage is worked out by ScanEventListener
     * and passed in through setProgress(). This test confirms the two stay separate: marking
     * every part as DONE must leave the percentage untouched until setProgress is called.
     */
    @Test
    @DisplayName("Finishing Parts Does Not Change The Percentage On Its Own")
    void finishingPartsDoesNotChangeTheProgress() {
        // Given a fresh loading page at 0%
        LoadingPanel panel = createPanel();

        // When every part is marked as finished, without calling setProgress
        for (String part : PARTS) {
            panel.setPartStatus(part, "DONE");
        }
        flushEdt();

        // Then every row shows as completed...
        for (int i = 0; i < PARTS.length; i++) {
            assertEquals(COMPLETED, statusOf(panel, i), PARTS[i] + " row");
        }
        // ...but the percentage is still 0%, because only setProgress may change it
        assertEquals("0%", percentText(panel));
    }


    /**
     * Test 5: Setting The Percentage Does Not Change Any Part's Status.
     *
     * This is the mirror of test 4. The rows are changed only by setPartStatus, so even a
     * finished percentage (100%) must leave every row as it was. If setProgress started marking
     * rows as completed, the page could show parts as done before they had really been scanned.
     */
    @Test
    @DisplayName("Setting The Percentage Does Not Change Any Part's Status")
    void settingProgressDoesNotChangeRowStatuses() {
        // Given a fresh loading page where every row is WAITING
        LoadingPanel panel = createPanel();

        // When the progress is set to 100% without any part being updated
        panel.setProgress(100);
        flushEdt();

        // Then the percentage shows 100%...
        assertEquals("100%", percentText(panel));
        // ...but every row is still waiting, because only setPartStatus may change a row
        for (int i = 0; i < PARTS.length; i++) {
            assertEquals(WAITING, statusOf(panel, i), PARTS[i] + " row");
        }
    }

    /**
     * Test 6: Updating A Part's Status Does Not Change The "Now Detecting" Text.
     *
     * ScanEventListener calls setCurrentPart separately when a part starts detecting. The panel
     * itself must not change the "Now detecting" text when a row's status changes, otherwise
     * the header and the rows could disagree about which part is being scanned.
     */
    @Test
    @DisplayName("Updating A Part's Status Does Not Change The 'Now Detecting' Text")
    void updatingStatusDoesNotChangeCurrentPart() {
        // Given a loading page that says it is currently detecting the CPU
        LoadingPanel panel = createPanel();
        panel.setCurrentPart("CPU");
        flushEdt();

        // When a different part (RAM) changes status
        panel.setPartStatus("RAM", "DETECTING");
        flushEdt();

        // Then the RAM row updates...
        assertEquals(DETECTING, statusOf(panel, 2), "RAM");
        // ...but the header still names the CPU, because only setCurrentPart may change it
        assertEquals("CPU", currentPartText(panel));
    }

    /**
     * Test 7: Sending The Same Status Twice Shows It Only Once.
     *
     * The scanner could report the same status more than once (for example two DETECTING
     * messages for the GPU). Each row must still show a single status label and, while
     * detecting, a single spinner, rather than stacking duplicates on screen.
     */
    @Test
    @DisplayName("Sending The Same Status Twice Shows It Only Once")
    void repeatedStatusIsNotDuplicated() {
        // Given a fresh loading page
        LoadingPanel panel = createPanel();

        // When the GPU is reported as detecting twice in a row
        panel.setPartStatus("GPU", "DETECTING");
        panel.setPartStatus("GPU", "DETECTING");
        flushEdt();

        // Then the page shows exactly one "DETECTING..." label and one spinner
        int detectingLabels = onEdt(() -> countLabels(panel, DETECTING));
        int spinners = onEdt(() -> findAll(panel, Spinner.class).size());
        assertEquals(1, detectingLabels, "DETECTING... labels");
        assertEquals(1, spinners, "spinners");
    }


    // ---- Helpers that read the page through its labels ----

    // Creates the loading page on the Swing event thread, as the app does.
    private static LoadingPanel createPanel() {
        return onEdt(LoadingPanel::new);
    }

    // The status text (WAITING, DETECTING... or COMPLETED) shown by the row at the given index.
    private static String statusOf(LoadingPanel panel, int rowIndex) {
        return onEdt(() -> {
            HardwareRow row = findAll(panel, HardwareRow.class).get(rowIndex);
            for (JLabel label : findAll(row, JLabel.class)) {
                String text = label.getText();
                if (WAITING.equals(text) || DETECTING.equals(text) || COMPLETED.equals(text)) {
                    return text;
                }
            }
            return null;
        });
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
            List<JLabel> labels = findAll(prefix.getParent(), JLabel.class);
            for (JLabel label : labels) {
                if (label != prefix) {
                    return label.getText();
                }
            }
            return null;
        });
    }
}
