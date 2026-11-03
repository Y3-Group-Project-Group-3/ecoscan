package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.history.components.ScanRow;
import com.example.monitortest.model.ScanHistory;
import com.example.monitortest.model.ScanRecord;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class HistoryListSection extends JPanel {

    private JPanel listItemsPanel;
    private final ScanHistory scanHistory = new ScanHistory();   // reads/writes the CSV
    private final List<ScanRow> rows = new ArrayList<>();        // rows on screen

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

        // Scan records from data/scan_history.csv
        showScans(scanHistory.getScans());

        JScrollPane scrollPane = new JScrollPane(listItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(scrollPane, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(
                "Select one or more rows (Cmd/Ctrl-click) to delete them."
        );

        infoLabel.setFont(UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE));
        infoLabel.setForeground(new Color(75, 85, 99));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(243, 244, 246));
        infoLabel.setBorder(new EmptyBorder(12, 15, 12, 15));

        add(infoLabel, BorderLayout.SOUTH);
    }

    // Clears the list and shows these scans (reused by search, sort, import)
    public void showScans(List<ScanRecord> scans) {

        listItemsPanel.removeAll();
        rows.clear();

        for (ScanRecord scan : scans) {
            addRow(new ScanRow(
                    scan.getDate(), scan.getDevice(),
                    scan.getType(), scan.getCo2()
            ));
        }

        listItemsPanel.revalidate();
        listItemsPanel.repaint();
    }

    // Adds a row to the list and makes it clickable
    private void addRow(ScanRow row) {
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Cmd-click (Mac) / Ctrl-click (Windows) selects more than one row
                boolean multi = e.isMetaDown() || e.isControlDown();

                if (!multi) {
                    for (ScanRow r : rows) {
                        r.setSelected(false);
                    }
                }
                row.setSelected(!row.isSelected() || !multi);
            }
        });

        rows.add(row);
        listItemsPanel.add(row);
    }

    // Removes the selected rows from the list and the CSV file
    public void deleteSelected() {

        List<ScanRecord> remaining = new ArrayList<>();

        for (ScanRow row : rows) {
            if (!row.isSelected()) {
                remaining.add(row.getScan());
            }
        }

        scanHistory.saveScans(remaining);
        showScans(remaining);
    }

    public JPanel createListHeaderRow() {

        JPanel header = new JPanel(new GridLayout(1, 5, 10, 0));
        header.setBackground(new Color(243, 244, 246));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        String[] titles = {
                "DATE", "DEVICE", "TYPE", "CO₂", ""
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