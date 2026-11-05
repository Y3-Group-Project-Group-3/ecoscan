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

public class HistoryActionsSection extends JPanel {

    // Hover colours for the standard History buttons.
    private static final Color HOVER_BG = new Color(0xEEF7F1);
    private static final Color HOVER_ACCENT = new Color(0x2F9A5F);

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

        JPanel leftButtons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        leftButtons.setOpaque(false);

        importButton = createPillButton("📥 Import Results");
        exportButton = createPillButton("💾 Export Results");
        printButton = createPillButton("🖨️ Print List");

        leftButtons.add(importButton);
        leftButtons.add(exportButton);
        leftButtons.add(printButton);

        JPanel rightButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        rightButtons.setOpaque(false);

        deleteAllButton = createPillButton("🗑️ Delete All");
        deleteButton = createPillButton("🗑️ Delete Selected");

        rightButtons.add(deleteAllButton);
        rightButtons.add(deleteButton);

        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);

        // Apply a subtle green hover to the standard buttons.
        addHoverEffect(importButton, HOVER_BG, HOVER_ACCENT);
        addHoverEffect(exportButton, HOVER_BG, HOVER_ACCENT);
        addHoverEffect(printButton, HOVER_BG, HOVER_ACCENT);

        // Use a subtle red hover so the delete buttons stand out as destructive.
        addHoverEffect(deleteAllButton, DELETE_HOVER_BG, DELETE_HOVER_ACCENT);
        addHoverEffect(deleteButton, DELETE_HOVER_BG, DELETE_HOVER_ACCENT);
    }


    // Tints the button's background, border and text while the mouse is over it.
    // Restores the original pill button styling when the mouse leaves.
    private void addHoverEffect(JButton button, Color hoverBackground, Color hoverAccent) {

        // Allow the button's background colour to be painted on every platform.
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBackground);
                button.setForeground(hoverAccent);
                button.setBorder( createButtonBorder(hoverAccent) );
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(TEXT_MAIN);
                button.setBorder( createButtonBorder(BORDER_COLOR) );
            }
        });
    }


    // Creates the pill button border in the given colour.
    // Matches the padding used by UIUtility.createPillButton.
    private Border createButtonBorder(Color colour) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(colour, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        );
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