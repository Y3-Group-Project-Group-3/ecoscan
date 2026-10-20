package com.example.monitortest.views;

import com.example.monitortest.views.sections.HistoryActionsSection;
import com.example.monitortest.views.sections.HistoryHeaderSection;
import com.example.monitortest.views.sections.HistoryListSection;

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

        // ...
        center.add( new HistoryListSection(), BorderLayout.CENTER );

        // ...
        center.add( new HistoryActionsSection(), BorderLayout.SOUTH );

        // ...
        main.add(center, BorderLayout.CENTER);

        return main;
    }

}