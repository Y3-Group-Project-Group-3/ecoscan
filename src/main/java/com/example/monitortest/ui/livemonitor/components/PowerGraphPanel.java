package com.example.monitortest.ui.livemonitor.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Displays recent hardware power consumption measurements.
 * Each instance maintains its own history of readings.
 */
public class PowerGraphPanel extends JPanel {

    // Store a power measurement together with its collection time in a record
    // records are useful here because each reading contains two related values that shouldn't change after creation.
    private record PowerReading(long timestamp, double watts) {}

    // Deque - allows us to efficiently add new readings at one end and remove older readings from the other.
    private final Deque<PowerReading> readings = new ArrayDeque<>();
    private final Color lineColor;

    /********************* MAC Dummy Mode ***********************/
    // Enables a fixed power line instead of time-based measurements.
    private boolean demoMode = false;

    // Stores the static simulated power value.
    private double demoPower = 0.0;


    public PowerGraphPanel(Color lineColor) {

        // Store the colour used when drawing the power graph.
        this.lineColor = lineColor;

        // Set the initial appearance and dimensions.
        setOpaque(false);
        setPreferredSize(new Dimension(600, 200));
    }


    /**
     * Adds a timestamped power reading and removes measurements older than the graph's 60-second history window.
     * Must be called on Swing's Event Dispatch Thread.
     */
    public void addReading(double watts) {

        long now = System.currentTimeMillis();

        // Store the measurement with its timestamp.
        readings.addLast(new PowerReading(now, watts));

        // Remove readings older than 60 seconds.
        while (!readings.isEmpty() && now - readings.peekFirst().timestamp() > 60_000) {
            readings.removeFirst();
        }

        // Redraw the graph with the updated history.
        repaint();
    }


    // Enables a permanent horizontal line for simulated macOS power readings.
    // The value remains visible without requiring repeated measurements.
    public void setDemoPower(double watts) {

        // Reject invalid demonstration values.
        if (!Double.isFinite(watts) || watts < 0) {
            throw new IllegalArgumentException(
                    "Demo power must be a finite, non-negative value."
            );
        }

        // Enable the static demonstration graph.
        demoMode = true;
        demoPower = watts;

        // Redraw the graph using the simulated value.
        repaint();
    }




    // Draws the power consumption graph using the stored readings.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing for smoother graph lines.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        int left = 45;
        int right = getWidth() - 15;
        int top = 15;
        int bottom = getHeight() - 30;

        int graphWidth = right - left;
        int graphHeight = bottom - top;

        if (graphWidth <= 0 || graphHeight <= 0) {
            g2.dispose();
            return;
        }


        // Determine the highest valid power reading in the visible time window.
        double maxPower = 0.0;
        long windowStart = System.currentTimeMillis() - 60_000;

        // Determine the highest power value for the selected graph mode.
        if (demoMode) {
            // Use the fixed simulated power value.
            maxPower = demoPower;
        } else {
            // Preserve the original rolling 60-second Windows behaviour.
            for (PowerReading reading : readings) {

                double watts = reading.watts();
                // Ignore unavailable, invalid or expired readings.
                if (reading.timestamp() >= windowStart && Double.isFinite(watts) && watts >= 0) {
                    maxPower = Math.max(maxPower, watts);
                }
            }
        }


        // Add 20% headroom so the highest reading does not touch the top.
        maxPower *= 1.2;

        // Round upwards to the nearest 10 W for readable axis labels.
        // Use a minimum scale of 10 W when there are no valid readings.
        maxPower = Math.max(10.0, Math.ceil(maxPower / 10.0) * 10.0);

        // Draw horizontal grid lines and wattage labels.
        g2.setFont(getFont().deriveFont(10f));

        for (int i = 0; i <= 4; i++) {
            int y = bottom - (i * graphHeight / 4);
            int watts = (int) Math.round(maxPower * i / 4);

            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(left, y, right, y);

            g2.setColor(new Color(100, 116, 139));
            g2.drawString(watts + " W", 5, y + 4);
        }

        // Draw time markers at 15-second intervals.
        for (int i = 0; i <= 4; i++) {

            int x = left + (i * graphWidth / 4);

            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(x, top, x, bottom);

            // Label the timeline from 60 seconds ago to now.
            String label = i == 4 ? "Now" : "-" + (60 - i * 15) + "s";

            g2.setColor(new Color(100, 116, 139));
            g2.drawString(label, x - 12, bottom + 18);
        }

        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke( 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));


        /*********************** MAC *************************************/
        // Display a permanent horizontal line for macOS demonstration mode.
        if (demoMode) {
            // Convert the simulated wattage into a vertical graph position.
            int y = bottom - (int) Math.round( Math.min(demoPower / maxPower, 1.0) * graphHeight );
            // Draw the fixed line across the complete graph width.
            g2.drawLine(left, y, right, y);
            // Do not draw any time-based measurements in demonstration mode.
            g2.dispose();
            return;
        }


        int previousX = 0;
        int previousY = 0;
        boolean previousValid = false;

        for (PowerReading reading : readings) {

            double value = reading.watts();

            // Missing readings create gaps rather than false zero values.
            if (!Double.isFinite(value) || value < 0) {
                previousValid = false;
                continue;
            }

            // Ignore measurements outside the visible 60-second window.
            if (reading.timestamp() < windowStart) {
                previousValid = false;
                continue;
            }

            // Convert elapsed time into a horizontal graph position.
            double timePosition = (double) (reading.timestamp() - windowStart) / 60_000;
            int x = left + (int) Math.round(timePosition * graphWidth);

            // Convert power consumption into a vertical graph position.
            int y = bottom - (int) Math.round( Math.min(value / maxPower, 1.0) * graphHeight );
            if (previousValid) {
                g2.drawLine(previousX, previousY, x, y);
            }

            previousX = x;
            previousY = y;
            previousValid = true;
        }


        g2.dispose();
    }

}
