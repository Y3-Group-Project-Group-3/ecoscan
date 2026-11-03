package com.example.monitortest.ui.results.sections;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;

public class ResultsHeader {

    public static JButton refreshButton;

    // title + scan info
    public static JPanel createHeader() {
        JPanel header = row();
        refreshButton = new JButton("Refresh Hardware Info");

        JPanel left = column();

        left.add(label("SCAN RESULTS", Font.BOLD, FONT_LABEL_SIZE, GREEN));
        left.add(Box.createVerticalStrut(4));
        left.add(label("Environmental assessment", Font.BOLD, FONT_TITLE_SIZE, TEXT_MAIN));

        header.add(left, BorderLayout.WEST);

        // data: scan date, PC name and intended use (from Home page)
        JLabel scanInfo = label("📅  Scanned 1 Oct 2026   •   My PC   •   Home use", Font.PLAIN, FONT_BODY_SIZE, TEXT_MUTED);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(scanInfo, BorderLayout.SOUTH);

        right.add(refreshButton, BorderLayout.NORTH);
        header.add(right, BorderLayout.EAST);

        return header;
    }

}
