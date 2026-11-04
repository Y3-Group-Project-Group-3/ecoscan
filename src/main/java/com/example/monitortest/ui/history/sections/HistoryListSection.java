package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.history.components.ScanRow;

import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class HistoryListSection extends JPanel {

    private JPanel listItemsPanel;

    // Retrieves saved hardware scans from the CSV history file.
    private final ScanHistoryCsvRepository scanRepository = new ScanHistoryCsvRepository();

    // Formats scan timestamps for the History table.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu");

    // Notifies the parent History Page when a saved scan is selected.
    private Consumer<SavedScan> viewScanListener;


    public HistoryListSection() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(BORDER_COLOR, 1, true));

        add(createListHeaderRow(), BorderLayout.NORTH);

        listItemsPanel = new JPanel();
        listItemsPanel.setLayout( new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS) );
        listItemsPanel.setBackground(Color.WHITE);


        // Populate the History table using records saved to CSV.
        refreshScans();


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


    // Reloads saved hardware scans and updates the History table.
    // Can also be called when the user returns to the History Page.
    public void refreshScans() {

        // Remove the previously displayed rows before rebuilding the list.
        listItemsPanel.removeAll();

        try {

            // Load the saved records from EcoScan's CSV repository.
            List<SavedScan> scans = scanRepository.loadAll();

            // Display the most recent hardware scans first.
            scans.sort( Comparator.comparing(SavedScan::getScannedAt).reversed() );

            // Show a helpful message when no scans have been saved yet.
            if (scans.isEmpty()) {
                JLabel emptyLabel = new JLabel( "No saved hardware scans available." );
                emptyLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
                listItemsPanel.add(emptyLabel);

            } else {

                // Create one table row for every saved hardware scan.
                for (SavedScan scan : scans) {

                    // Format the original scan date for display.
                    String date = scan.getScannedAt().format(DATE_FORMAT);

                    // Create the row displaying the saved scan's date and name.
                    ScanRow row = new ScanRow(date, scan.getName());

                    // Associate this row's View Scan button with its saved record.
                    // Each iteration of the loop refers to a particular SavedScan object.
                    // The button's listener captures that reference, meaning clicking View Scan for Gaming PC
                    // selects the Gaming PC record rather than whichever record was last loaded.
                    row.getViewButton().addActionListener(e -> {
                        // Forward the selected scan when a listener has been registered.
                        if (viewScanListener != null) {
                            viewScanListener.accept(scan);
                        }
                    });

                    // Add the configured row to the History list.
                    listItemsPanel.add(row);

                }
            }

        } catch (IOException | IllegalArgumentException e) {

            // Display a loading error without modifying the saved CSV.
            JLabel errorLabel = new JLabel( "Unable to load saved hardware scans." );

            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(new EmptyBorder(15, 15, 15, 15));

            listItemsPanel.add(errorLabel);

            // Print the underlying error for debugging.
            e.printStackTrace();
        }

        // Update the Swing layout after changing the displayed rows.
        listItemsPanel.revalidate();
        listItemsPanel.repaint();
    }

    public JPanel createListHeaderRow() {

        // Create the header with the three remaining columns.
        JPanel header = new JPanel(new GridLayout(1, 3, 10, 0));
        header.setBackground(new Color(243, 244, 246));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Define the column headings in display order.
        String[] titles = { "DATE", "NAME", "ACTION" };

        // Create and add a label for each column heading.
        for (String title : titles) {
            JLabel label = new JLabel(title);
            label.setFont(UIUtility.font( "SansSerif", Font.BOLD, FONT_BADGE_SIZE ));
            label.setForeground(new Color(75, 85, 99));
            header.add(label);
        }

        return header;
    }


    // Registers the action to perform when the user selects a saved scan.
    public void setViewScanListener(Consumer<SavedScan> listener) {
        this.viewScanListener = listener;
    }


}