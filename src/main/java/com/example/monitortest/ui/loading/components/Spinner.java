
package com.example.monitortest.ui.loading.components;

import com.example.monitortest.ui.common.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Animated spinner displayed while a hardware component is being detected.
public class Spinner extends JComponent {

    private final Timer timer;
    private int angle;

    public Spinner() {
        setPreferredSize(new Dimension(12, 12));
        setMaximumSize(getPreferredSize());

        // Rotate the spinner approximately every 30 milliseconds.
        timer = new Timer(30, e -> {
            angle = (angle + 12) % 360;
            repaint();
        });
    }

    // Starts the spinner animation.
    public void start() { timer.start(); }
    // Stops the spinner animation.
    public void stop() { timer.stop(); }

    @Override
    protected void paintComponent(Graphics g) {
        // Create an independent graphics context with smooth rendering.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);

        // Draw a rotating arc to indicate active detection.
        g2.setColor(TEXT_FAINT);
        g2.setStroke(new BasicStroke( 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
        g2.draw(new Arc2D.Float( 1.5f, 1.5f, 9, 9, -angle, 270, Arc2D.OPEN ));

        g2.dispose();
    }
}
