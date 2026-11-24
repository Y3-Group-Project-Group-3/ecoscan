package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.common.OutlinedActionButton;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class HistoryActionsSection extends JPanel {

    // Hover colours for the delete buttons, which remove saved data.
    private static final Color DELETE_HOVER_BG = new Color(0xFDEEEE);
    private static final Color DELETE_HOVER_ACCENT = new Color(0xC0392B);

    // Every History button uses the same size so both cards line up.
    private static final Dimension BUTTON_SIZE = new Dimension(130, 38);

    private JButton importButton;
    private JButton exportButton;
    private JButton printButton;
    private JButton shareButton;
    private JButton deleteButton;
    private JButton clearButton;

    public HistoryActionsSection() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Arrange the action buttons horizontally without FlowLayout's outer gaps.
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.X_AXIS));
        leftButtons.setOpaque(false);

        // Create the saved scan actions using reusable styling.
        // Import sits first, matching the Live Monitor Sessions card.
        importButton = createActionButton( "Import Scans", FontAwesomeSolid.FILE_IMPORT, false );
        exportButton = createActionButton( "Export Results", FontAwesomeSolid.FILE_EXPORT, false );
        printButton = createActionButton( "Print List", FontAwesomeSolid.PRINT, false );
        shareButton = createActionButton( "Share", FontAwesomeSolid.ENVELOPE, false );

        // Add uniform spacing only between buttons.
        leftButtons.add(importButton);
        leftButtons.add(Box.createHorizontalStrut(10));
        leftButtons.add(exportButton);
        leftButtons.add(Box.createHorizontalStrut(10));
        leftButtons.add(printButton);
        leftButtons.add(Box.createHorizontalStrut(10));
        leftButtons.add(shareButton);

        // Align the delete actions with the right edge of the History cards.
        JPanel rightButtons = new JPanel();
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.X_AXIS));
        rightButtons.setOpaque(false);

        // Delete Selected removes the chosen rows, Clear Scans removes every saved scan.
        deleteButton = createActionButton( "Delete Selected", FontAwesomeSolid.TRASH_ALT, true );
        clearButton = createActionButton( "Clear Scans", FontAwesomeSolid.TRASH_ALT, true );

        // Keep both delete actions together on the right edge of the History cards.
        rightButtons.add(deleteButton);
        rightButtons.add(Box.createHorizontalStrut(10));
        rightButtons.add(clearButton);

        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);

    }


    // Creates a consistently styled action button for the History Page.
    // Destructive actions receive a red hover effect instead of green.
    // Shared with the Sessions card so every History button looks the same.
    public static JButton createActionButton( String text, FontAwesomeSolid icon, boolean destructive) {

        // Reuse EcoScan's shared rounded secondary-action button.
        OutlinedActionButton button = new OutlinedActionButton(text);

        // Display a consistent FontAwesome icon beside the label.
        button.setIcon( FontIcon.of(icon, 13, destructive ? TEXT_MUTED : GREEN) );
        button.setDisabledIcon( FontIcon.of(icon, 13, TEXT_MUTED) );
        button.setIconTextGap(8);

        // Apply the red hover treatment only to destructive actions.
        if (destructive) {
            button.setHoverColors( DELETE_HOVER_BG, DELETE_HOVER_ACCENT );
        }

        // Use the same width and height for every History button.
        button.setPreferredSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);

        return button;
    }


    public JButton getImportButton() {
        return importButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    public JButton getPrintButton() {
        return printButton;
    }

    public JButton getShareButton() {
        return shareButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

}