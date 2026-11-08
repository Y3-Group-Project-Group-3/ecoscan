package com.example.monitortest.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable rounded text field for EcoScan forms and search controls.
// Provides consistent backgrounds, borders and focus styling.
public class RoundedTextField extends JTextField {

    // Match the rounded corners used by EcoScan's action buttons.
    private static final int CORNER_ARC = 12;

    // Creates a rounded text field containing the supplied text.
    public RoundedTextField(String text) {
        super(text);

        // Allow the custom painter to draw the complete background.
        setOpaque(false);

        // Reserve space between the text and rounded edges.
        setBorder(new EmptyBorder(8, 12, 8, 12));

        // Use the application's standard text styling.
        setFont(getFont().deriveFont(Font.PLAIN, 12f));

        // Refresh the border when keyboard focus changes.
        addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
    }

    // Draws the white rounded input background.
    @Override
    protected void paintComponent(Graphics g) {

        // Use an independent graphics context with anti-aliasing.
        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC );

        g2.dispose();

        // Let Swing render the text and caret normally.
        super.paintComponent(g);
    }

    // Draws a subtle outline that turns green when focused.
    @Override
    protected void paintBorder(Graphics g) {

        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        // Highlight the field when the user is typing.
        g2.setColor(hasFocus() ? GREEN : BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));

        g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC );

        g2.dispose();
    }
}
