package com.example.monitortest.ui.loading.components;

import com.example.monitortest.testsupport.SwingTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static com.example.monitortest.testsupport.SwingTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

// Tests for the progress bar, which animates towards the value it is given.
// The displayed value is private, so these tests read it from the painted pixels.
class GradientProgressBarTest {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 8;
    private static final int MIDDLE = HEIGHT / 2;

    // Colours the bar paints.
    private static final int TRACK = 0xFFE2E8F0;     // empty part of the bar
    private static final int IN_PROGRESS = 0xFF94A3B8; // fill while the scan is under 100%

    private static final long ANIMATION_TIMEOUT = 5000;

    @BeforeAll
    static void headless() {
        SwingTestSupport.useHeadlessMode();
    }

    private static GradientProgressBar createBar() {
        GradientProgressBar bar = onEdt(GradientProgressBar::new);
        onEdt(() -> bar.setSize(WIDTH, HEIGHT));
        return bar;
    }

    private static int pixelAt(GradientProgressBar bar, int x) {
        BufferedImage image = paintToImage(bar);
        return image.getRGB(x, MIDDLE);
    }

    private static void setValue(GradientProgressBar bar, int value) {
        onEdt(() -> bar.setValue(value));
    }

    // A filled 100% bar is green: its green channel clearly beats its red channel.
    private static boolean isGreen(int argb) {
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        return green > red + 40;
    }

    private static boolean waitForFull(GradientProgressBar bar) {
        return waitUntil(() -> isGreen(pixelAt(bar, WIDTH - 12)), ANIMATION_TIMEOUT);
    }


    @Test
    @DisplayName("Starts empty")
    void startsEmpty() {
        GradientProgressBar bar = createBar();

        assertEquals(TRACK, pixelAt(bar, 10));
        assertEquals(TRACK, pixelAt(bar, WIDTH / 2));
        assertEquals(TRACK, pixelAt(bar, WIDTH - 12));
    }

    @Test
    @DisplayName("Turns green once it reaches 100%")
    void turnsGreenAtOneHundred() {
        GradientProgressBar bar = createBar();

        setValue(bar, 100);

        assertTrue(waitForFull(bar), "the bar should fill completely and turn green");
        assertTrue(isGreen(pixelAt(bar, WIDTH / 2)), "the middle should be green too");
    }

    @Test
    @DisplayName("A value of 0 empties the bar immediately")
    void zeroResetsImmediately() {
        GradientProgressBar bar = createBar();

        setValue(bar, 100);
        assertTrue(waitForFull(bar));

        setValue(bar, 0);

        // No waiting here: a new scan must start from an empty bar straight away.
        assertEquals(TRACK, pixelAt(bar, 10));
        assertEquals(TRACK, pixelAt(bar, WIDTH / 2));
        assertEquals(TRACK, pixelAt(bar, WIDTH - 12));
    }

}
