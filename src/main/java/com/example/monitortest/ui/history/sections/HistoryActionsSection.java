package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.UIUtility.createPillButton;

public class HistoryActionsSection extends JPanel {

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
    }


    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }
}