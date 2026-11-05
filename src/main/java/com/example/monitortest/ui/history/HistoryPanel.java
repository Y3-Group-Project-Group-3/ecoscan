package com.example.monitortest.ui.history;

import com.example.monitortest.ui.history.sections.HistoryActionsSection;
import com.example.monitortest.ui.history.sections.HistoryHeaderSection;
import com.example.monitortest.ui.history.sections.HistoryListSection;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class HistoryPanel extends JPanel {

    private final HistoryListSection historyListSection = new HistoryListSection();

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
        HistoryHeaderSection headerSection = new HistoryHeaderSection();
        main.add( headerSection, BorderLayout.NORTH);

        // Filter the History list whenever the search text changes.
        headerSection.getSearchField().getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                historyListSection.setSearchText( headerSection.getSearchText() );
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                historyListSection.setSearchText( headerSection.getSearchText() );
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                historyListSection.setSearchText( headerSection.getSearchText() );
            }
        });

        // Re-sort the History list whenever a different sort option is chosen.
        // The first option is "Newest first", the second is "Oldest first".
        headerSection.getSortCombo().addActionListener(e -> {
            boolean newestFirst = headerSection.getSortCombo().getSelectedIndex() == 0;
            historyListSection.setNewestFirst( newestFirst );
        });

        // List and action buttons
        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);

        // Display the existing History list component.
        center.add(historyListSection, BorderLayout.CENTER);

        // Display the History action buttons.
        HistoryActionsSection actionsSection = new HistoryActionsSection();
        center.add( actionsSection, BorderLayout.SOUTH );

        // Delete the selected scans when the Delete Selected button is clicked.
        actionsSection.getDeleteButton().addActionListener(e -> {

            // Ask the user to select a scan first when nothing is selected.
            if (!historyListSection.hasSelection()) {
                JOptionPane.showMessageDialog( this,
                        "Select one or more scans to delete.",
                        "Delete scans", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            // Confirm before removing the scans from the saved history.
            int answer = JOptionPane.showConfirmDialog( this,
                    "Delete the selected scans?", "Delete scans",
                    JOptionPane.OK_CANCEL_OPTION );

            // Remove the selected scans once the user confirms.
            if (answer == JOptionPane.OK_OPTION) {
                historyListSection.deleteSelected();
            }
        });

        // Delete every saved scan when the Delete All button is clicked.
        actionsSection.getDeleteAllButton().addActionListener(e -> {

            // Inform the user when there are no saved scans to delete.
            if (!historyListSection.hasScans()) {
                JOptionPane.showMessageDialog( this,
                        "There are no saved scans to delete.",
                        "Delete all scans", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            // Confirm before removing every scan from the saved history.
            int answer = JOptionPane.showConfirmDialog( this,
                    "Delete all saved scans? This cannot be undone.",
                    "Delete all scans", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE );

            // Remove every saved scan once the user confirms.
            if (answer == JOptionPane.OK_OPTION) {
                historyListSection.deleteAll();
            }
        });

        // Export the saved scans to a CSV file when the Export Results button is clicked.
        actionsSection.getExportButton().addActionListener(e -> exportScans());

        // Print the History list when the Print List button is clicked.
        actionsSection.getPrintButton().addActionListener(e -> {

            // Inform the user when there are no saved scans to print.
            if (!historyListSection.hasScans()) {
                JOptionPane.showMessageDialog( this,
                        "There are no saved scans to print.",
                        "Print scans", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            historyListSection.printScans();
        });

        // ...
        main.add(center, BorderLayout.CENTER);

        return main;
    }


    // Lets the user choose where to save a copy of the scan history as a CSV file.
    private void exportScans() {

        // Inform the user when there are no saved scans to export.
        if (!historyListSection.hasScans()) {
            JOptionPane.showMessageDialog( this,
                    "There are no saved scans to export.",
                    "Export scans", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        // Open a save dialog limited to CSV files.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export scans");
        fileChooser.setFileFilter( new FileNameExtensionFilter("CSV files (*.csv)", "csv") );
        fileChooser.setSelectedFile( new File("ecoscan-scans.csv") );

        // Stop when the user cancels the dialog.
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Add the .csv extension when the user leaves it out.
        File exportFile = fileChooser.getSelectedFile();

        if (!exportFile.getName().toLowerCase().endsWith(".csv")) {
            exportFile = new File( exportFile.getParentFile(), exportFile.getName() + ".csv" );
        }

        try {

            // Copy the saved scan history to the chosen file.
            new ScanHistoryCsvRepository().exportTo( exportFile.toPath() );

            JOptionPane.showMessageDialog( this,
                    "Scans exported to " + exportFile.getName(),
                    "Export scans", JOptionPane.INFORMATION_MESSAGE );

        } catch (IOException ex) {

            // Inform the user that the export could not be completed.
            JOptionPane.showMessageDialog( this,
                    "Unable to export the saved scans.",
                    "Export scans", JOptionPane.ERROR_MESSAGE );

            // Print the underlying error for debugging.
            ex.printStackTrace();
        }
    }


    // Reloads saved scans whenever the History Page is opened.
    public void refreshHistory() {
        historyListSection.refreshScans();
    }


    // Registers the action used to open a saved scan from History.
    // Passes the listener to the History list component.
    public void setViewScanListener(Consumer<SavedScan> listener) {
        historyListSection.setViewScanListener(listener);
    }

}