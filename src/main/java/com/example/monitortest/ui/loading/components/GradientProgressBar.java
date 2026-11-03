package com.example.monitortest.ui.loading.components;

import com.example.monitortest.ui.common.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static com.example.monitortest.ui.theme.AppTheme.*;

/**
 * Custom progress bar that smoothly animates towards the percentage reported by the hardware scan.
 */
public class GradientProgressBar extends JComponent {

    private int value = 0;
    private int targetValue = 0;

    private final Timer smoothProgressTimer;

    public GradientProgressBar() {
        setPreferredSize(new Dimension(100, 8));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        // Gradually increase the displayed progress towards the target.
        smoothProgressTimer = new Timer(16, e -> {

            // Stop animating when the target percentage is reached.
            if (value >= targetValue) {
                ((Timer) e.getSource()).stop();
                return;
            }

            value = Math.min(value + 1, targetValue);
            repaint();
        });
    }

    /**
     * Updates the target percentage while preserving the smooth animation between progress milestones.
     */
    public void setValue(int newValue) {
        targetValue = Math.max(0, Math.min(100, newValue));

        // Reset immediately when beginning a new scan.
        if (targetValue == 0) {
            smoothProgressTimer.stop();
            value = 0;
            repaint();
            return;
        }

        // Start animating towards the new target.
        if (value < targetValue && !smoothProgressTimer.isRunning()) {
            smoothProgressTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create an independent graphics context with smooth rendering.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);

        int width = getWidth();
        int height = getHeight();

        // Draw the empty progress track.
        g2.setColor(new Color(0xE2E8F0));
        g2.fill(new RoundRectangle2D.Float( 0, 0, width, height, height, height));

        // Calculate the filled width from the displayed percentage.
        int filled = Math.round(width * value / 100f);


        if (filled > 0) {

            // Use grey while scanning and green when fully complete.
            if (value == 100) {
                g2.setPaint(new GradientPaint( 0, 0, GREEN, Math.max(filled, 1), 0, GREEN_LIGHT ));
            } else {
                g2.setColor(new Color(0x94A3B8));
            }

            // Draw the filled portion of the progress bar.
            g2.fill(new RoundRectangle2D.Float( 0, 0, filled, height, height, height ));
        }


        g2.dispose();
    }
}
