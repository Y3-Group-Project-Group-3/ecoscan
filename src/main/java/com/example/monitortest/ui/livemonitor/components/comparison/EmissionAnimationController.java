package com.example.monitortest.ui.livemonitor.components.comparison;

import javax.swing.Timer;

// Manages fade, zoom and PC emissions animations for the Live Monitor.
// All animations use Swing timers, so their callbacks run on the EDT.
public final class EmissionAnimationController {

    // Provides the current target scale for the zoom animation.
    private final EmissionScaleController scaleController;

    // Requests a redraw without depending directly on a Swing panel.
    private final Runnable repaintCallback;

    // Current animated values displayed by the comparison panel.
    private float rulerOpacity = 0.0f;
    private double scaleMaximumGrams = 0.02;
    private double displayedEmissionsGrams = 0.0;

    // Latest emissions rate and pending measurement correction.
    private double emissionsRateGramsPerSecond = 0.0;
    private double emissionsCorrectionGrams = 0.0;

    // Tracks elapsed time between PC animation frames.
    private long lastAnimationTime = System.nanoTime();

    // Three independent timers control the ruler's animations.
    private final Timer fadeTimer;
    private final Timer zoomTimer;
    private final Timer pcAnimationTimer;

    // Creates the timers and connects them to the current scale and UI.
    public EmissionAnimationController( EmissionScaleController scaleController, Runnable repaintCallback
    ) {

        this.scaleController = scaleController;
        this.repaintCallback = repaintCallback;

        // Animate the displayed scale towards the controller's target.
        zoomTimer = new Timer(16, e -> {

            double targetScaleMaximumGrams = scaleController.getTargetScaleMaximumGrams();

            // Preserve the original zoom easing factor.
            scaleMaximumGrams += (targetScaleMaximumGrams - scaleMaximumGrams) * 0.08;

            // Finish once the animated scale is close to its target.
            if (Math.abs(targetScaleMaximumGrams - scaleMaximumGrams) < targetScaleMaximumGrams * 0.001) {
                scaleMaximumGrams = targetScaleMaximumGrams;
                ((Timer) e.getSource()).stop();
            }

            repaintCallback.run();
        });

        // Gradually fade the ruler into view after the first reading.
        fadeTimer = new Timer(16, e -> {

            // Preserve the original opacity increment.
            rulerOpacity = Math.min(1.0f, rulerOpacity + 0.032f);

            if (rulerOpacity >= 1.0f) {
                rulerOpacity = 1.0f;
                ((Timer) e.getSource()).stop();
            }

            repaintCallback.run();
        });


        // Continuously advance the PC marker and reconcile measurement differences.
        pcAnimationTimer = new Timer(16, e -> {

            long now = System.nanoTime();

            // Calculate the time elapsed since the previous animation frame.
            double elapsedSeconds = (now - lastAnimationTime) / 1_000_000_000.0;

            lastAnimationTime = now;

            // Prevent large animation jumps after a pause or system suspension.
            elapsedSeconds = Math.max(0.0, Math.min(elapsedSeconds, 0.1));

            // Estimate emissions accumulated since the previous frame.
            displayedEmissionsGrams += emissionsRateGramsPerSecond * elapsedSeconds;

            // Apply a forward-only correction when the marker is behind
            // the latest measured emissions total.
            double correction = Math.max( 0.0, emissionsCorrectionGrams * 0.08 );

            // Gradually move the marker forwards towards the measurement.
            displayedEmissionsGrams += correction;

            // Reduce the remaining correction by the amount applied.
            emissionsCorrectionGrams = Math.max( 0.0, emissionsCorrectionGrams - correction );

            // Redraw the marker using its updated cumulative emissions.
            repaintCallback.run();
        });

    }


    // Receives the latest measured emissions and updates animation state.
    // Subsequent measurement differences are reconciled gradually by the timer.
    public void updateEmissions( double totalEmissionsGrams, double averageGramsPerSecond) {


        // Only use finite, non-negative rates for forward animation.
        emissionsRateGramsPerSecond = Double.isFinite(averageGramsPerSecond) ? Math.max(0.0, averageGramsPerSecond) : 0.0;

        // Initialise the animated PC marker on the first reading.
        if (!pcAnimationTimer.isRunning()) {

            // Start at the measured total rather than animating from zero.
            displayedEmissionsGrams = totalEmissionsGrams;

            // No measurement correction is needed for the initial reading.
            emissionsCorrectionGrams = 0.0;

            // Avoid a large elapsed-time jump on the first animation frame.
            lastAnimationTime = System.nanoTime();

            pcAnimationTimer.start();
            return;
        }


        // Calculate how far the animated marker is behind the measurement.
        // Do not apply negative corrections to cumulative emissions.
        emissionsCorrectionGrams = Math.max( 0.0, totalEmissionsGrams - displayedEmissionsGrams );

        // The timer will gradually reconcile this difference while continuing to accumulate emissions at the current rate.
    }


    // Starts the ruler fade-in animation from transparent.
    public void startFade() {
        rulerOpacity = 0.0f;
        fadeTimer.start();
    }

    // Starts the initial zoom animation, if it is not already running.
    public void startInitialZoom() {
        if (!zoomTimer.isRunning()) {
            zoomTimer.start();
        }
    }

    // Starts zooming when the displayed scale is below the target.
    public void startZoomIfNeeded() {

        double targetScaleMaximumGrams = scaleController.getTargetScaleMaximumGrams();

        if (scaleMaximumGrams < targetScaleMaximumGrams && !zoomTimer.isRunning()) {
            zoomTimer.start();
        }
    }

    // Returns the current animated ruler opacity.
    public float getRulerOpacity() {
        return rulerOpacity;
    }

    // Returns the currently displayed emissions range.
    public double getScaleMaximumGrams() {
        return scaleMaximumGrams;
    }

    // Returns the animated cumulative PC emissions.
    public double getDisplayedEmissionsGrams() {
        return displayedEmissionsGrams;
    }

    // Stops animations when the owning panel no longer needs them.
    public void stop() {
        fadeTimer.stop();
        zoomTimer.stop();
        pcAnimationTimer.stop();
    }
}
