package com.example.monitortest.ui.history.components;

import javax.swing.*;
import java.awt.*;

public class ResultBadge {

    public static JLabel createBadge(String text) {

        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(getBackground());
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 20, 20
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(75, 22));
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));

        switch (text.toUpperCase()) {
            case "KEEP":
                badge.setForeground(new Color(22, 101, 52));
                badge.setBackground(new Color(220, 252, 231));
                break;

            case "UPGRADE":
                badge.setForeground(new Color(194, 107, 0));
                badge.setBackground(new Color(255, 237, 213));
                break;

            case "REPLACE":
                badge.setForeground(new Color(185, 28, 28));
                badge.setBackground(new Color(254, 226, 226));
                break;

            default:
                badge.setForeground(Color.DARK_GRAY);
                badge.setBackground(new Color(245, 245, 245));
        }

        return badge;
    }
}