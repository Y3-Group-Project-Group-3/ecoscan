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

    // Button colours, matched to the Home screen design.
    private static final Color FILL = new Color(0x189557);
    private static final Color FILL_HOVER = new Color(0x137A47);
    // Corner radius of the button.
    private static final int RADIUS = 12;

    private boolean hover;

    public PrimaryButton(String text, Icon icon) {
        super(text, icon);

        setIconTextGap(8);
        setFont(buttonFont());
        setForeground(Color.WHITE);

        setBorder(new EmptyBorder(0, 40, 0, 40));

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

    // Segoe UI Semibold (as in the design) where Windows has it, otherwise the app's bold font.
    private static Font buttonFont() {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (family.equals("Segoe UI Semibold")) {
                return new Font(family, Font.PLAIN, 12).deriveFont(AppTheme.FONT_CARD_TITLE_SIZE);
            }
        }
        return UIUtility.font(Font.BOLD, AppTheme.FONT_BODY_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create a smooth graphics context for the rounded button background.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);

        // Flat green, a little darker on hover or keyboard focus.
        g2.setColor(hover || isFocusOwner() ? FILL_HOVER : FILL);

        g2.fill(new RoundRectangle2D.Float(
                0, 0,
                getWidth(), getHeight(),
                RADIUS, RADIUS
        ));

        g2.dispose();

        super.paintComponent(g);
    }
}