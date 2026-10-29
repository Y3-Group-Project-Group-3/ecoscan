package com.example.monitortest.ui.results.components;

import javax.swing.*;
import java.awt.*;
import static com.example.monitortest.ui.theme.AppTheme.TRACK;

public class ComparisonBar extends JComponent {
    private final int value;
    private final Color color;

    public ComparisonBar(int value, Color color) {
        this.value = value;
        this.color = color;

        setPreferredSize(
                new Dimension(100, 18)
        );
    }

    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = getHeight() / 2 - 4;
        g.setColor(TRACK);
        g.fillRoundRect(0, y, getWidth(), 8, 8, 8);
        g.setColor(color);
        g.fillRoundRect(0, y, (int) (getWidth() * value / 100.0), 8, 8, 8);
        g.dispose();
    }
}