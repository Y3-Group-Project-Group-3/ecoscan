package com.example.monitortest.ui.results.sections;

import javax.swing.*;
import java.awt.*;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;

public class ResultsHeader {

    public static JButton refreshButton;

    // title + scan info
    public static JPanel createHeader() {
        JPanel header = row();

        // Create a secondary action button that matches EcoScan's toolbar.
        refreshButton = new JButton("Refresh Hardware Info") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                // Smooth the rounded button edges.
                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

                // Use a subtle green tint when hovering over the button.
                Color background = getModel().isRollover() ? new Color(240, 249, 244) : Color.WHITE;

                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.dispose();

                // Draw the existing button text over the custom background.
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

                // Highlight the button border when the mouse hovers over it.
                g2.setColor(getModel().isRollover() ? GREEN : BORDER_COLOR);

                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, 12, 12 );

                g2.dispose();
            }
        };

        // Use the same Font Awesome 6 icon library as the Live Monitor.
        FontIcon refreshIcon = FontIcon.of( FontAwesomeSolid.SYNC_ALT, 15, GREEN );
        // Display the icon to the left of the button text.
        refreshButton.setIcon(refreshIcon);
        refreshButton.setIconTextGap(10);

        // Remove Swing's default rectangular button appearance.
        refreshButton.setContentAreaFilled(false);
        refreshButton.setBorderPainted(true);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(false);

        // Match the text and dimensions of the power-mode controls.
        refreshButton.setForeground(TEXT_MAIN);
        refreshButton.setFont(refreshButton.getFont().deriveFont(Font.BOLD, 12f));
        refreshButton.setPreferredSize(new Dimension(210, 42));

        // Indicate that the button performs an action.
        refreshButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        JPanel left = column();

        left.add(label("SCAN RESULTS", Font.BOLD, FONT_TITLE_SIZE, TEXT_MAIN));

        header.add(left, BorderLayout.WEST);

        return header;
    }

}
