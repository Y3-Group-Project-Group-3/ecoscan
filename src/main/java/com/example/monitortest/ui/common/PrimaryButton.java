package com.example.monitortest.ui.common;

import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.theme.UIUtility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class PrimaryButton extends JButton {

    private boolean hover;

    public PrimaryButton(String text, Icon icon) {
        super(text, icon);

        setIconTextGap(8);
        setFont(UIUtility.font(Font.BOLD, 12.5f));
        setForeground(Color.WHITE);

        setBorder(new EmptyBorder(10, 30, 10, 30));

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = UIUtility.smooth(g);

        if (hover || isFocusOwner()) {
            g2.setColor(AppTheme.BUTTON_GREEN_HOVER);
        } else {
            g2.setColor(AppTheme.BUTTON_GREEN);
        }

        g2.fill(new RoundRectangle2D.Float(
                0, 0,
                getWidth(), getHeight(),
                14, 14
        ));

        g2.dispose();

        super.paintComponent(g);
    }
}