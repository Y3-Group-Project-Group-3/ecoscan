package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.createPillButton;
import com.example.monitortest.ui.common.OutlinedActionButton;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

public class HistoryActionsSection extends JPanel {

    // Hover colours for the delete buttons, which remove saved data.
    private static final Color DELETE_HOVER_BG = new Color(0xFDEEEE);
    private static final Color DELETE_HOVER_ACCENT = new Color(0xC0392B);

    private JButton importButton;
    private JButton exportButton;
    private JButton printButton;
    private JButton deleteButton;
    private JButton deleteAllButton;

    public HistoryActionsSection() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel leftButtons = new JPanel( new FlowLayout(FlowLayout.LEFT, 10, 0) );
        leftButtons.setOpaque(false);

        // Create the standard History actions using reusable styling.
        importButton = createActionButton( "Import Results", FontAwesomeSolid.FILE_IMPORT, false );
        exportButton = createActionButton( "Export Results", FontAwesomeSolid.FILE_EXPORT, false );
        printButton = createActionButton( "Print List", FontAwesomeSolid.PRINT, false );

        leftButtons.add(importButton);
        leftButtons.add(exportButton);
        leftButtons.add(printButton);

        JPanel rightButtons = new JPanel( new FlowLayout(FlowLayout.RIGHT, 10, 0) );
        rightButtons.setOpaque(false);

        // Use a red hover treatment for actions that remove saved scans.
        deleteAllButton = createActionButton( "Delete All", FontAwesomeSolid.TRASH_ALT, true );
        deleteButton = createActionButton( "Delete Selected", FontAwesomeSolid.TRASH_ALT, true);

        rightButtons.add(deleteAllButton);
        rightButtons.add(deleteButton);

        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);

    }


    // Creates a consistently styled action button for the History Page.
    // Destructive actions receive a red hover effect instead of green.
    private JButton createActionButton( String text, FontAwesomeSolid icon, boolean destructive) {

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

        // Match the height used by other EcoScan secondary actions.
        button.setPreferredSize( new Dimension(button.getPreferredSize().width + 20, 38) );

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

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }
}