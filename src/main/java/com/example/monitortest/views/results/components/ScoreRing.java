package com.example.monitortest.views.results.components;

import com.example.monitortest.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.GREEN;

// eco score circle
    public class ScoreRing extends JComponent {

        private final int score;

        public ScoreRing(int score) {
            this.score = score;

            setPreferredSize(
                    new Dimension(116, 116));

        }

        protected void paintComponent(Graphics g0) {

            Graphics2D g = (Graphics2D) g0.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = Math.min(getWidth(), getHeight()) - 12;
            g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(AppTheme.TRACK);
            g.drawOval(6, 6, s, s);
            g.setColor(GREEN);
            g.drawArc(6, 6, s, s, 90, -(int) (360 * score / 100.0));
            g.setColor(AppTheme.TEXT_MAIN);
            g.setFont(new Font(AppTheme.FONT_FAMILY, Font.BOLD, 32));

            FontMetrics fm = g.getFontMetrics();
            String t = String.valueOf(score);

            g.drawString(t, 6 + (s - fm.stringWidth(t)) / 2, 6 + s / 2 + 6);
            g.setColor(AppTheme.TEXT_MUTED);
            g.setFont(new Font(AppTheme.FONT_FAMILY, Font.PLAIN, 11));

            fm = g.getFontMetrics();

            g.drawString("/ 100", 6 + (s - fm.stringWidth("/ 100")) / 2, 6 + s / 2 + 24);

            g.dispose();
        }
    }