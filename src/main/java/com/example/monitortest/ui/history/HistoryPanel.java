package com.example.monitortest.ui.history;

import com.example.monitortest.ui.history.sections.HistoryActionsSection;
import com.example.monitortest.ui.history.sections.HistoryHeaderSection;
import com.example.monitortest.ui.history.sections.HistoryListSection;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HistoryPanel extends JPanel {

    // Theme Colors
    // TODO Move these into the apptheme class at some point
    private static final Color BG_COLOR = new Color(0xF5F5FA);
    private static final Color TEXT_MAIN = new Color(0x1E1E1E);
    private static final Color BORDER_COLOR = new Color(0xE1E1E1);

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        add(createMainContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createMainContentPanel() {

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        main.add( new HistoryHeaderSection(), BorderLayout.NORTH);

        // List and action buttons
        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);

        // List of scans (from data/scan_history.csv)
        HistoryListSection listSection = new HistoryListSection();
        center.add( listSection, BorderLayout.CENTER );

        // Buttons
        HistoryActionsSection actionsSection = new HistoryActionsSection();
        center.add( actionsSection, BorderLayout.SOUTH );

        // Delete Selected
        actionsSection.getDeleteButton().addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Delete the selected scans?", "Delete scans",
                    JOptionPane.OK_CANCEL_OPTION);

            if (answer == JOptionPane.OK_OPTION) {
                listSection.deleteSelected();
            }
        });

        main.add(center, BorderLayout.CENTER);

        return main;
    }
}