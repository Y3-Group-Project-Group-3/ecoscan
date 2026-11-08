package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.common.GraphicsUtils;
import com.example.monitortest.ui.common.OutlinedActionButton;
import com.example.monitortest.ui.common.VerticalDivider;

import static com.example.monitortest.ui.theme.AppTheme.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;



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

    // Creates a styled Save Scan button using the shared outlined component.
    public static JButton createSaveButton() {

        // Reuse EcoScan's standard rounded secondary-action appearance.
        JButton saveButton = new OutlinedActionButton("Save Scan");

        // Display the application's existing green icon style.
        saveButton.setIcon( FontIcon.of(FontAwesomeSolid.SAVE, 15, GREEN) );

        // Keep the icon muted while saving is unavailable.
        saveButton.setDisabledIcon( FontIcon.of(FontAwesomeSolid.SAVE, 15, TEXT_MUTED) );

        saveButton.setIconTextGap(10);
        saveButton.setPreferredSize(new Dimension(130, 42));

        // Save Scan becomes available when a new scan completes.
        saveButton.setEnabled(false);

        return saveButton;
    }

    // Creates a Refresh button using the shared outlined component.
    public static JButton createRefreshButton() {

        // Reuse the same border, rounding and hover treatment as Save Scan.
        JButton refreshButton = new OutlinedActionButton( "Refresh Hardware Info" );

        // Retain the existing green refresh icon.
        refreshButton.setIcon( FontIcon.of(FontAwesomeSolid.SYNC_ALT, 15, GREEN) );

        refreshButton.setIconTextGap(10);
        refreshButton.setPreferredSize(new Dimension(210, 42));

        return refreshButton;
    }

}