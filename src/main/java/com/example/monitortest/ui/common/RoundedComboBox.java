
package com.example.monitortest.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable rounded dropdown for EcoScan.
// Retains standard Swing selection behaviour with custom styling.
public class RoundedComboBox extends JComboBox<String> {

    // Match the corner radius of other EcoScan controls.
    private static final int CORNER_ARC = 12;

    // Creates a dropdown containing the supplied options.
    public RoundedComboBox(String[] options) {
        super(options);

        // Allow this component to draw its own rounded background.
        setOpaque(false);
        setBackground(Color.WHITE);
        setForeground(TEXT_MAIN);
        setFont(getFont().deriveFont(Font.BOLD, 12f));

        // Replace the native dropdown arrow and button appearance.
        setUI(new BasicComboBoxUI() {

            @Override
            protected JButton createArrowButton() {
                return createRoundedArrowButton();
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {

                // The enclosing RoundedComboBox paints the background.
                // Prevent Swing from drawing a rectangular one over it.
            }
        });

        // Keep the selected value transparent so the rounded
        // background remains visible behind the text.
        setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

                // Only the closed dropdown needs a transparent renderer.
                // Popup entries retain their normal selection colours.
                setOpaque(index != -1);

                return this;
            }
        });

        // Repaint the border when keyboard focus changes.
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

    // Creates a transparent arrow button with a simple chevron.
    private JButton createRoundedArrowButton() {

        JButton arrow = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

                // Draw a small downward chevron.
                g2.setColor(isEnabled() ? TEXT_MAIN : TEXT_MUTED);
                g2.setStroke(new BasicStroke( 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int x = getWidth() / 2;
                int y = getHeight() / 2;

                g2.drawLine(x - 4, y - 2, x, y + 2);
                g2.drawLine(x, y + 2, x + 4, y - 2);

                g2.dispose();
            }
        };

        // Prevent native rectangular button painting.
        arrow.setContentAreaFilled(false);
        arrow.setBorderPainted(false);
        arrow.setFocusPainted(false);
        arrow.setOpaque(false);
        arrow.setPreferredSize(new Dimension(28, 28));
        arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return arrow;
    }

    // Draws the rounded white background.
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC );

        g2.dispose();

        // Let Swing draw the selected value and dropdown arrow.
        super.paintComponent(g);
    }

    // Draws the border using EcoScan's theme colours.
    @Override
    protected void paintBorder(Graphics g) {

        Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

        // Highlight the dropdown when it has focus.
        g2.setColor(hasFocus() ? GREEN : BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));

        g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, CORNER_ARC, CORNER_ARC);

        g2.dispose();
    }
}
