package com.example.monitortest.ui.home;

import com.example.monitortest.testsupport.SwingTestSupport;
import com.example.monitortest.ui.theme.AppTheme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Tests for the Home page: its text, its two feature cards, navigation buttons and responsive layout.
class HomePanelTest {

    // HomePanel switches to a stacked layout when the card area is narrower than this.
    private static final int STACK_BREAKPOINT = 850;

    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }

    private static HomePanel createPanel() {
        return onEdt(HomePanel::new);
    }

    // The GridLayout panel that holds the two feature cards side by side or stacked.
    private static JPanel featureRow(HomePanel panel) {
        for (JPanel candidate : findAll(panel, JPanel.class)) {
            if (candidate.getLayout() instanceof GridLayout) {
                return candidate;
            }
        }
        fail("Could not find the panel that lays out the feature cards");
        return null;
    }

    // The container whose width decides between one and two columns.
    // Structure: featureContainer > cardWrapper > featureRow.
    private static Container featureContainer(HomePanel panel) {
        return featureRow(panel).getParent().getParent();
    }

    private static GridLayout gridOf(HomePanel panel) {
        return (GridLayout) featureRow(panel).getLayout();
    }

    // Resizes the card container directly, which is what the layout listener reacts to.
    private static void resizeFeatureContainer(HomePanel panel, int width) {
        Container container = featureContainer(panel);
        onEdt(() -> container.setSize(width, container.getHeight()));
        flushEdt();
    }


    // ---- Content ----

    @Test
    @DisplayName("Shows the heading text")
    void showsTheHeading() {
        HomePanel panel = createPanel();

        assertNotNull(findLabel(panel, "HARDWARE ASSESSMENT"), "section label");
        assertNotNull(findLabel(panel, "Explore your computer"), "page title");
        assertNotNull(findLabel(panel, "Choose how you would like to explore your hardware."), "intro text");
    }

    @Test
    @DisplayName("Offers a Scan Hardware card and a Live Monitor card")
    void offersTwoFeatureCards() {
        HomePanel panel = createPanel();
        List<HomeFeatureCard> cards = findAll(panel, HomeFeatureCard.class);

        assertEquals(2, cards.size());

        assertNotNull(findLabel(cards.get(0), "Scan Hardware"));
        assertNotNull(findLabelContaining(cards.get(0), "Discover your computer's hardware"));

        assertNotNull(findLabel(cards.get(1), "Live Monitor"));
        assertNotNull(findLabelContaining(cards.get(1), "real-time hardware performance"));
    }


    // ---- Navigation buttons ----

    @Test
    @DisplayName("Exposes the Scan and Live Monitor buttons for navigation")
    void exposesNavigationButtons() {
        HomePanel panel = createPanel();
        List<HomeFeatureCard> cards = findAll(panel, HomeFeatureCard.class);

        assertNotNull(panel.getScanButton());
        assertNotNull(panel.getLiveMonitorButton());
        assertNotSame(panel.getScanButton(), panel.getLiveMonitorButton());

        assertEquals("Start Scan", panel.getScanButton().getText());
        assertEquals("View Monitor", panel.getLiveMonitorButton().getText());

        // Each exposed button belongs to the matching card.
        assertSame(cards.get(0).getActionButton(), panel.getScanButton());
        assertSame(cards.get(1).getActionButton(), panel.getLiveMonitorButton());
    }

    @Test
    @DisplayName("Clicking the Scan Hardware card fires the Scan button listeners only")
    void clickingScanCardStartsScan() {
        HomePanel panel = createPanel();
        AtomicInteger scans = new AtomicInteger();
        AtomicInteger monitors = new AtomicInteger();
        onEdt(() -> {
            panel.getScanButton().addActionListener(e -> scans.incrementAndGet());
            panel.getLiveMonitorButton().addActionListener(e -> monitors.incrementAndGet());
        });

        click(findAll(panel, HomeFeatureCard.class).get(0));

        assertEquals(1, scans.get());
        assertEquals(0, monitors.get());
    }

    @Test
    @DisplayName("Clicking the Live Monitor card fires the Live Monitor button listeners only")
    void clickingMonitorCardOpensMonitor() {
        HomePanel panel = createPanel();
        AtomicInteger scans = new AtomicInteger();
        AtomicInteger monitors = new AtomicInteger();
        onEdt(() -> {
            panel.getScanButton().addActionListener(e -> scans.incrementAndGet());
            panel.getLiveMonitorButton().addActionListener(e -> monitors.incrementAndGet());
        });

        click(findAll(panel, HomeFeatureCard.class).get(1));

        assertEquals(0, scans.get());
        assertEquals(1, monitors.get());
    }


    // ---- Responsive layout ----

    @Test
    @DisplayName("Stacks the cards one pixel below the breakpoint")
    void stacksJustBelowTheBreakpoint() {
        HomePanel panel = createPanel();

        resizeFeatureContainer(panel, STACK_BREAKPOINT - 1);

        GridLayout grid = gridOf(panel);
        assertEquals(2, grid.getRows());
        assertEquals(1, grid.getColumns());
        assertEquals(0, grid.getHgap());
        assertEquals(20, grid.getVgap());
        // Both cards need to fit one above the other.
        assertEquals(840, featureContainer(panel).getPreferredSize().height);
    }

    @Test
    @DisplayName("Returns to two columns after being stacked and then widened")
    void returnsToTwoColumnsWhenWidened() {
        HomePanel panel = createPanel();

        resizeFeatureContainer(panel, 600);
        assertEquals(1, gridOf(panel).getColumns());

        resizeFeatureContainer(panel, 1100);

        GridLayout grid = gridOf(panel);
        assertEquals(1, grid.getRows());
        assertEquals(2, grid.getColumns());
        assertEquals(24, grid.getHgap());
        assertEquals(0, grid.getVgap());
        assertEquals(410, featureContainer(panel).getPreferredSize().height);
    }

}
