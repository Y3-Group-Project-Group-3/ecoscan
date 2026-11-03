package com.example.monitortest.ui.results.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.common.VerticalDivider;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import com.example.monitortest.ui.common.GraphicsUtils;


// Constructs the fixed toolbar displayed at the top of the Results page.
public class ResultsToolbar {

    // Creates the toolbar containing power mode controls and refresh action.
// Builds the toolbar using controls supplied by the Results page.
    public static JPanel createToolbar( JButton refreshButton, JButton saveButton, JPanel powerModeControls) {

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setBackground(CARD_BG);

        // Add a bottom divider while preserving the toolbar padding.
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 36, 12, 36)
        ));

        // Identify the setting that affects the Results calculations.
        JLabel modeLabel = new JLabel("POWER CALCULATION MODE");
        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 12f));
        modeLabel.setForeground(TEXT_MAIN);

        toolbar.add(modeLabel, BorderLayout.WEST);

        // Group the power mode controls and refresh button together.
        JPanel toolbarActions = new JPanel( new FlowLayout(FlowLayout.RIGHT, 12, 0) );
        toolbarActions.setOpaque(false);

        // Display the power mode controls supplied by ResultsPanel.
        if (powerModeControls != null) {
            toolbarActions.add(powerModeControls);
        }

        // Separate toolbar actions using a reusable vertical divider.
        toolbarActions.add(new VerticalDivider(0.5f));

        toolbarActions.add(saveButton);

        // Reuse the existing refresh button.
        toolbarActions.add(refreshButton);

        toolbar.add(toolbarActions, BorderLayout.EAST);

        return toolbar;
    }


    // Creates the button used to save the currently displayed hardware scan.
    public static JButton createSaveButton() {

        // Use a standard Swing button until saving functionality is connected.
        JButton saveButton = new JButton("Save Scan");

        // Match the toolbar's existing button sizing and typography.
        saveButton.setPreferredSize(new Dimension(130, 42));
        saveButton.setFont( saveButton.getFont().deriveFont(Font.BOLD, 12f) );

        // Keep the new button consistent with the application's colours.
        saveButton.setBackground(Color.WHITE);
        saveButton.setForeground(TEXT_MAIN);
        saveButton.setFocusPainted(false);

        // Indicate that the button is interactive.
        saveButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );

        // Saving should only become available after a successful scan.
        saveButton.setEnabled(false);

        return saveButton;
    }



    // Creates the refresh button used by the Results toolbar.
    public static JButton createRefreshButton() {

        JButton refreshButton = new JButton("Refresh Hardware Info") {

            @Override
            protected void paintComponent(Graphics g) {

                // Create a smooth graphics context for the custom button background.
                Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);

                // Apply a subtle green tint when hovering.
                Color background = getModel().isRollover() ? new Color(240, 249, 244) : Color.WHITE;

                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.dispose();

                // Draw the button's text and icon.
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {

                // Create a smooth graphics context for the rounded button border.
                Graphics2D g2 = GraphicsUtils.createSmoothGraphics(g);
                g2.setColor( getModel().isRollover() ? GREEN : BORDER_COLOR );
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, 12, 12 );

                g2.dispose();
            }
        };

        // Configure the refresh icon.
        FontIcon refreshIcon = FontIcon.of( FontAwesomeSolid.SYNC_ALT, 15, GREEN );

        refreshButton.setIcon(refreshIcon);
        refreshButton.setIconTextGap(10);

        // Remove Swing's default rectangular button appearance.
        refreshButton.setContentAreaFilled(false);
        refreshButton.setBorderPainted(true);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(false);

        // Apply the existing Results button typography and sizing.
        refreshButton.setForeground(TEXT_MAIN);
        refreshButton.setFont( refreshButton.getFont().deriveFont(Font.BOLD, 12f));
        refreshButton.setPreferredSize(new Dimension(210, 42));

        // Show a hand cursor to indicate an interactive action.
        refreshButton.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );

        return refreshButton;
    }
}