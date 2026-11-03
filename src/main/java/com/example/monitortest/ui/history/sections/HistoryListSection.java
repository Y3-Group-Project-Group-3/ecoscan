package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.history.components.ScanRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class HistoryListSection extends JPanel {

    private JPanel listItemsPanel;

    public HistoryListSection() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(BORDER_COLOR, 1, true));

        add(createListHeaderRow(), BorderLayout.NORTH);

        listItemsPanel = new JPanel();
        listItemsPanel.setLayout(
                new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS)
        );
        listItemsPanel.setBackground(Color.WHITE);

        // Temporary scan records
        listItemsPanel.add(new ScanRow(
                "1 Oct 2026", "My PC", "Home",
                "78/100", "209 kg/year", "KEEP"
        ));

        listItemsPanel.add(new ScanRow(
                "12 Aug 2026", "My PC", "Home",
                "70/100", "214 kg/year", "KEEP"
        ));

        listItemsPanel.add(new ScanRow(
                "5 Jul 2026", "Work PC", "Office",
                "64/100", "258 kg/year", "UPGRADE"
        ));

        listItemsPanel.add(new ScanRow(
                "20 Apr 2026", "Reception PC", "Office",
                "48/100", "290 kg/year", "REPLACE"
        ));

        JScrollPane scrollPane = new JScrollPane(listItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(
                "Select a row to delete it, or choose View to open the full breakdown."
        );

        infoLabel.setFont(UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE));
        infoLabel.setForeground(new Color(75, 85, 99));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(243, 244, 246));
        infoLabel.setBorder(new EmptyBorder(12, 15, 12, 15));

        add(infoLabel, BorderLayout.SOUTH);
    }

    public JPanel createListHeaderRow() {

        JPanel header = new JPanel(new GridLayout(1, 7, 10, 0));
        header.setBackground(new Color(243, 244, 246));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        String[] titles = {
                "DATE", "DEVICE", "USE",
                "SCORE", "CO₂", "RESULT", ""
        };

        for (String title : titles) {
            JLabel label = new JLabel(title);
            label.setFont(UIUtility.font("SansSerif", Font.BOLD, FONT_BADGE_SIZE));
            label.setForeground(new Color(75, 85, 99));
            header.add(label);
        }

        return header;
    }
}