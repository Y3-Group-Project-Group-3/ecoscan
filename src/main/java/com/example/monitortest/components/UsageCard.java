package com.example.monitortest.components;

import com.example.monitortest.theme.AppTheme;
import com.example.monitortest.theme.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** One selectable "What do you use it for?" card. Works like a radio button inside a ButtonGroup. */
public class UsageCard extends JToggleButton {
    private final String title, subtitle;
    private final LineIcon icon;
    private boolean hover;

    public UsageCard(String title, String subtitle, String iconName) {
        this.title = title;
        this.subtitle = subtitle;
        this.icon = new LineIcon(iconName, 16, new Color(0x334155)).withStroke(1.8f);
        setPreferredSize(new Dimension(148, 96));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(title + ": " + subtitle);   // for screen readers
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    public String getTitle() { return title; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = UIUtility.smooth(g);
        int w = getWidth(), h = getHeight();
        boolean selected = isSelected();

        // Card background and border (green when selected)
        RoundRectangle2D card = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 18, 18);
        g2.setColor(selected ? new Color(0xF7FBF9) : Color.WHITE);
        g2.fill(card);
        g2.setStroke(new BasicStroke(selected ? 1.5f : 1f));
        g2.setColor(selected ? AppTheme.GREEN : (hover ? new Color(0xCBD5E1) : (isFocusOwner() ? new Color(0x94C9A9) : AppTheme.BORDER_COLOR)));
        g2.draw(card);

        // Icon in a small rounded box
        int boxX = (w - 32) / 2, boxY = 13;
        g2.setColor(new Color(0xEEF2F8));
        g2.fill(new RoundRectangle2D.Float(boxX, boxY, 32, 32, 12, 12));
        icon.setColor(selected ? AppTheme.GREEN : new Color(0x334155));
        icon.paintIcon(this, g2, boxX + 8, boxY + 8);

        // Title and subtitle, centred
        drawCentred(g2, title, UIUtility.font(Font.BOLD, 12.5f), AppTheme.TEXT_MAIN, 64);
        drawCentred(g2, subtitle, UIUtility.font(Font.PLAIN, 9.5f), new Color(0x8A96A8), 80);

        // Green tick badge in the top-right corner
        if (selected) {
            int bx = w - 8 - 17, by = 8;
            g2.setColor(new Color(0x2F8A55));
            g2.fillOval(bx, by, 17, 17);
            new LineIcon("CHECK", 11, Color.WHITE).withStroke(3.2f).paintIcon(this, g2, bx + 3, by + 3);
        }
        g2.dispose();
    }

    private void drawCentred(Graphics2D g2, String text, Font f, Color color, int baseline) {
        g2.setFont(f);
        g2.setColor(color);
        int x = (getWidth() - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, baseline);
    }
}

