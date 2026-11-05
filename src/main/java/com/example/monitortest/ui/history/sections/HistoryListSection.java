package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.history.components.ScanRow;

import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
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

    // Links each displayed row to the saved scan it represents.
    private final Map<ScanRow, SavedScan> rows = new LinkedHashMap<>();


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
                "Select one or more rows (Cmd/Ctrl-click) to delete them, or choose View to open the full breakdown."
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
        rows.clear();

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
                    row.getViewButton().addActionListener(e -> {
                        // Forward the selected scan when a listener has been registered.
                        if (viewScanListener != null) {
                            viewScanListener.accept(scan);
                        }
                    });

                    // Allow the row to be selected for deletion.
                    addSelection(row);

                    // Remember which saved scan this row represents.
                    rows.put(row, scan);

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


    // Makes a History row selectable.
    // A single click selects one row, Cmd/Ctrl-click selects several rows.
    private void addSelection(ScanRow row) {

        row.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                // Cmd (Mac) or Ctrl (Windows) allows more than one row to be selected.
                boolean multiSelect = e.isMetaDown() || e.isControlDown();

                // Clear the previous selection for a normal click.
                if (!multiSelect) {
                    for (ScanRow otherRow : rows.keySet()) {
                        otherRow.setSelected(false);
                    }
                }

                // Select the clicked row, or toggle it during multi-select.
                row.setSelected( !row.isSelected() || !multiSelect );
            }
        });
    }


    // Returns true when at least one History row is selected.
    public boolean hasSelection() {

        for (ScanRow row : rows.keySet()) {
            if (row.isSelected()) {
                return true;
            }
        }

        return false;
    }


    // Returns true when the History list contains at least one saved scan.
    public boolean hasScans() {
        return !rows.isEmpty();
    }


    // Deletes the selected hardware scans and refreshes the History table.
    public void deleteSelected() {

        // Collect the ids of every selected saved scan.
        Set<UUID> selectedIds = new HashSet<>();

        for (Map.Entry<ScanRow, SavedScan> entry : rows.entrySet()) {
            if (entry.getKey().isSelected()) {
                selectedIds.add( entry.getValue().getId() );
            }
        }

        // Nothing to delete when no rows are selected.
        if (selectedIds.isEmpty()) {
            return;
        }

        deleteScans( selectedIds, "Unable to delete the selected scans." );
    }


    // Deletes every saved hardware scan and refreshes the History table.
    public void deleteAll() {

        // Collect the ids of every displayed saved scan.
        Set<UUID> allIds = new HashSet<>();

        for (SavedScan scan : rows.values()) {
            allIds.add( scan.getId() );
        }

        deleteScans( allIds, "Unable to delete the saved scans." );
    }


    // Removes the given scans from the CSV file, then reloads the History table.
    // Displays an error message without changing the list if deletion fails.
    private void deleteScans(Set<UUID> ids, String errorMessage) {

        try {

            // Remove the scans from EcoScan's CSV repository.
            scanRepository.deleteByIds(ids);

        } catch (IOException e) {

            // Inform the user that the saved history could not be updated.
            JOptionPane.showMessageDialog( this, errorMessage, "Delete scans", JOptionPane.ERROR_MESSAGE );

            // Print the underlying error for debugging.
            e.printStackTrace();
        }

        // Rebuild the History table from the updated CSV.
        refreshScans();
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