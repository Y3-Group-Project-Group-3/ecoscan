package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable information icon that displays an explanatory
 * tooltip when hovered over.
 */
public class InfoIcon extends JComponent {

    private static final int ICON_SIZE = 16;

    private final Color normalColor = new Color(100, 116, 139);
    private final Color hoverColor = new Color(22, 137, 76);

    private boolean hovered = false;

    /**
     * Creates an information icon with a tooltip.
     *
     * @param title Tooltip heading.
     * @param description Tooltip explanation.
     */
    public InfoIcon(String title, String description) {

        setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
        setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));

        // Reuse the existing tooltip utility.
        InfoTooltip.attach(this, title, description);

        // Indicate that additional information is available.
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Update the icon colour when the mouse enters or leaves.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /**
     * Draws a circular information icon without relying
     * on Unicode symbols or external image files.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Smooth the circle and information symbol.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        g2.setColor(hovered ? hoverColor : normalColor);
        g2.setStroke(new BasicStroke(1.5f));

        // Draw the circular outline.
        g2.drawOval(1, 1, 13, 13);

        // Draw the information symbol using simple shapes.
        g2.fillOval(7, 4, 2, 2);
        g2.fillRoundRect(7, 7, 2, 5, 1, 1);

        g2.dispose();
    }
}
