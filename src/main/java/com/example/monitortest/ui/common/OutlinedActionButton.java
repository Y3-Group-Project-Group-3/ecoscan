
package com.example.monitortest.ui.common;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable rounded outline button for secondary actions.
// Provides consistent backgrounds, borders and hover effects.
public class OutlinedActionButton extends JButton {

    // Rounded corner diameter used throughout the button.
    private static final int CORNER_ARC = 12;

    // Background colour displayed while hovering over an enabled button.
    private static final Color HOVER_BACKGROUND = new Color(240, 249, 244);

    // Creates a styled button with the supplied text.
    public OutlinedActionButton(String text) {
        super(text);

        // Disable Swing's default rectangular button painting.
        setContentAreaFilled(false);
        setBorderPainted(true);
        setFocusPainted(false);
        setOpaque(false);

        // Apply EcoScan's standard secondary-action styling.
        setForeground(TEXT_MAIN);
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Draws the rounded background before Swing draws the text and icon.
    @Override
    protected void paintComponent(Graphics g) {

        // Create an independent graphics context with anti-aliasing.
        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        // Apply the hover colour only when this button is enabled.
        Color background = isEnabled() && getModel().isRollover() ? HOVER_BACKGROUND : Color.WHITE;

        g2.setColor(background);
        g2.fillRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC );

        g2.dispose();

        // Let Swing render the text, icon and disabled appearance.
        super.paintComponent(g);
    }

    // Draws the rounded outline using EcoScan's theme colours.
    @Override
    protected void paintBorder(Graphics g) {

        // Create a smooth graphics context for the button border.
        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        // Highlight the border when the enabled button is hovered.
        Color borderColor = isEnabled() && getModel().isRollover() ? GREEN : BORDER_COLOR;

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC );

        g2.dispose();
    }
}
