package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.history.components.ScanRow;
import com.example.monitortest.model.ScanHistory;
import com.example.monitortest.model.ScanRecord;

import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class HistoryListSection extends JPanel {

    private JPanel listItemsPanel;
    private final ScanHistory scanHistory = new ScanHistory();

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

        // scan records from scan_history.csv
        showScans(scanHistory.getScans());

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

    public void showScans(List<ScanRecord> scans) {

        listItemsPanel.removeAll();

        for (ScanRecord scan : scans) {
            listItemsPanel.add(new ScanRow(
                    scan.getDate(), scan.getDevice(), scan.getUse(),
                    scan.getScore(), scan.getCo2(), scan.getResult()
            ));
        }

        listItemsPanel.revalidate();
        listItemsPanel.repaint(); }

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