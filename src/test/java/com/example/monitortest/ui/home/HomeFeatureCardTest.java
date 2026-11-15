package com.example.monitortest.ui.home;

import com.example.monitortest.testsupport.SwingTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Tests for a single feature card on the Home page (title, description and action button).
class HomeFeatureCardTest {

    private static final String TITLE = "Scan Hardware";
    private static final String DESCRIPTION = "Discover your computer's hardware and system specifications.";
    private static final String BUTTON_TEXT = "Start Scan";

    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }


    // Creates a HomeFeatureCard with the five required constructor arguments.
    // Swing component creation takes place on the Event Dispatch Thread.
    private static HomeFeatureCard createCard() {
        return onEdt(() -> new HomeFeatureCard(
                TITLE,
                DESCRIPTION,
                BUTTON_TEXT,
                FontAwesomeSolid.MICROCHIP,
                "scan"
        ));
    }


    // Counts how many times the card's action button fires.
    private static AtomicInteger countActions(HomeFeatureCard card) {
        AtomicInteger count = new AtomicInteger();
        onEdt(() -> card.getActionButton().addActionListener(e -> count.incrementAndGet()));
        return count;
    }


    // ---- Content ----

    @Test
    @DisplayName("Shows the title, description and button text it was given")
    void showsTheTextItWasGiven() {
        HomeFeatureCard card = createCard();

        assertNotNull(findLabel(card, TITLE), "title label");
        assertNotNull(findLabelContaining(card, DESCRIPTION), "description label");
        // The Home card appends a navigation arrow to its action label.
        assertEquals(BUTTON_TEXT + "  \u2192", card.getActionButton().getText()
        );
    }

    @Test
    @DisplayName("Title and description set their font explicitly")
    void titleAndDescriptionSetTheirFont() {
        HomeFeatureCard card = createCard();

        JLabel title = findLabel(card, TITLE);
        JLabel description = findLabelContaining(card, DESCRIPTION);

        assertTrue(title.isFontSet(), "title font was never set (Font.getFont returned null)");
        assertTrue(description.isFontSet(), "description font was never set (Font.getFont returned null)");
    }


    // ---- Clicking ----

    @Test
    @DisplayName("Clicking the card background presses the action button")
    void clickingTheCardTriggersTheButton() {
        HomeFeatureCard card = createCard();
        AtomicInteger actions = countActions(card);

        click(card);

        assertEquals(1, actions.get());
    }

    @Test
    @DisplayName("Clicking the card does nothing while the button is disabled")
    void disabledButtonCannotBeTriggeredFromTheCard() {
        // ScanEventListener disables the button while a scan runs.
        // Clicking the card must not start a second scan.
        HomeFeatureCard card = createCard();
        AtomicInteger actions = countActions(card);

        onEdt(() -> card.getActionButton().setEnabled(false));
        click(card);

        assertEquals(0, actions.get());
    }
}
