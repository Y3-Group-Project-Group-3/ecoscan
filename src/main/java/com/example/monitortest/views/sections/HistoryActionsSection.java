package com.example.monitortest.views.sections;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.theme.UIUtility.createPillButton;

public class HistoryActionsSection extends JPanel {

    private JButton importButton;
    private JButton exportButton;
    private JButton printButton;
    private JButton deleteButton;

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
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        rightButtons.setOpaque(false);

        deleteButton = createPillButton("🗑️ Delete Selected");
        rightButtons.add(deleteButton);

        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);
    }


    public JButton getDeleteButton() {
        return deleteButton;
    }
}