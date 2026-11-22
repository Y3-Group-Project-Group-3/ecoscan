package com.example.monitortest.ui.history;

import com.example.monitortest.ui.history.sections.HistoryActionsSection;
import com.example.monitortest.ui.history.sections.HistoryHeaderSection;
import com.example.monitortest.ui.history.sections.HistoryListSection;
import com.example.monitortest.ui.history.sections.SessionListSection;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;
import static com.example.monitortest.ui.theme.AppTheme.*;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class HistoryPanel extends JPanel {

    private final HistoryListSection historyListSection = new HistoryListSection();

    // Displays the total emissions recorded for each Live Monitor session.
    private final SessionListSection sessionListSection = new SessionListSection();


    // Creates the History Page using EcoScan's shared application theme.
    public HistoryPanel() {

        // Arrange the History content within the available page area.
        setLayout(new BorderLayout());

        // Match the background used throughout EcoScan.
        setBackground(BG_COLOR);

        // Add the main History Page layout.
        add(createMainContentPanel(), BorderLayout.CENTER);

        // Allow the page itself to take focus away from the search box and sort dropdown.
        setFocusable(true);

        // Clicking an empty part of the page clears the focus from the search and sort controls.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }



    private JPanel createMainContentPanel() {


        // Build the main content area with consistent spacing.
        JPanel main = new JPanel(new BorderLayout(0, 24));

        // Match the application's main page background.
        main.setBackground(BG_COLOR);

        // Give the History Page consistent outer spacing.
        main.setBorder(new EmptyBorder(32, 36, 32, 36));


        // Header
        // Search and sort are handled inside each card.
        HistoryHeaderSection headerSection = new HistoryHeaderSection();
        main.add( headerSection, BorderLayout.NORTH);

        // Display the saved scans above the Live Monitor sessions.
        JPanel lists = new JPanel(new GridLayout(2, 1, 0, 20));
        lists.setOpaque(false);
        lists.add(historyListSection);
        lists.add(sessionListSection);

        // Display the saved scan action buttons inside the scans card.
        HistoryActionsSection actionsSection = new HistoryActionsSection();
        historyListSection.setActionsBar(actionsSection);

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

        // Remove every saved scan when the Clear Scans button is clicked.
        actionsSection.getClearButton().addActionListener(e -> historyListSection.confirmClearScans());

        // Import saved scans from a CSV file when the Import Scans button is clicked.
        actionsSection.getImportButton().addActionListener(e -> importScans());

        // Export the saved scans to a CSV file when the Export Results button is clicked.
        actionsSection.getExportButton().addActionListener(e -> exportScans());

        // Import Live Monitor sessions from a CSV file when the Import Sessions button is clicked.
        sessionListSection.getImportButton().addActionListener(e -> importSessions());

        // Export Live Monitor sessions to a CSV file when the Export Sessions button is clicked.
        sessionListSection.getExportButton().addActionListener(e -> exportSessions());

        // Print the History list when the Print List button is clicked.
        actionsSection.getPrintButton().addActionListener(e -> {

            // Inform the user when there are no saved scans to print.
            if (!historyListSection.hasStoredScans()) {
                JOptionPane.showMessageDialog( this,
                        "There are no saved scans to print.",
                        "Print scans", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            historyListSection.printScans();
        });

        // Enable each button only when it can be used.
        // Updated whenever the scans or the selected rows change.
        historyListSection.setStateChangeListener( () -> updateButtonStates(actionsSection) );
        updateButtonStates(actionsSection);

        // Add the scans and sessions cards below the header.
        main.add(lists, BorderLayout.CENTER);

        return main;
    }

    // Enables History actions according to stored records and visible selection.
    private void updateButtonStates(HistoryActionsSection actionsSection) {

        // Actions affecting the entire history use the CSV's stored state.
        boolean hasStoredScans = historyListSection.hasStoredScans();
        // Printing operates on the currently displayed scans.
        boolean hasVisibleScans = historyListSection.hasVisibleScans();

        // Deleting selected rows requires an actual row selection.
        actionsSection.getDeleteButton().setEnabled( historyListSection.hasSelection() );

        // Clearing removes every saved scan, so the search filter must not affect it.
        actionsSection.getClearButton().setEnabled(hasStoredScans);

        // This action operates on the complete saved history.
        actionsSection.getExportButton().setEnabled(hasStoredScans);

        // Only permit printing when visible rows are available.
        actionsSection.getPrintButton().setEnabled(hasVisibleScans);
    }

    // Lets the user choose where to save a copy of the scan history as a CSV file.
    private void exportScans() {

        // Inform the user when there are no saved scans to export.
        if (!historyListSection.hasStoredScans()) {
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


    // Lets the user choose a scans CSV file and adds its scans to the Saved Hardware Scans table.
    private void importScans() {

        // Open a file dialog limited to CSV files.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import scans");
        fileChooser.setFileFilter( new FileNameExtensionFilter("CSV files (*.csv)", "csv") );

        // Stop when the user cancels the dialog.
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File importFile = fileChooser.getSelectedFile();

        try {

            // Add the scans to EcoScan's history, then refresh the table.
            int imported = new ScanHistoryCsvRepository().importFrom( importFile.toPath() );
            historyListSection.refreshScans();

            // Tell the user how many scans were added.
            String message = imported == 0
                    ? "No new scans were found in " + importFile.getName() + "."
                    : imported + " scan(s) imported from " + importFile.getName() + ".\nOnly the 10 most recent scans are kept.";

            JOptionPane.showMessageDialog( this, message, "Import scans", JOptionPane.INFORMATION_MESSAGE );

        } catch (IOException ex) {

            // Inform the user that the import could not be completed.
            JOptionPane.showMessageDialog( this,
                    "Unable to import scans from " + importFile.getName() + ".",
                    "Import scans", JOptionPane.ERROR_MESSAGE );

            // Print the underlying error for debugging.
            ex.printStackTrace();
        }
    }


    // Lets the user choose a sessions CSV file and adds its sessions to the Sessions table.
    private void importSessions() {

        // Open a file dialog limited to CSV files.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import sessions");
        fileChooser.setFileFilter( new FileNameExtensionFilter("CSV files (*.csv)", "csv") );

        // Stop when the user cancels the dialog.
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File importFile = fileChooser.getSelectedFile();

        try {

            // Add the sessions to EcoScan's history and refresh the table.
            int imported = sessionListSection.importSessions( importFile.toPath() );

            // Tell the user how many sessions were added.
            String message = imported == 0
                    ? "No new sessions were found in " + importFile.getName() + "."
                    : imported + " session(s) imported from " + importFile.getName() + ".";

            // Let the user know older sessions are removed once the limit is reached.
            if (imported > 0) {
                message += "\nOnly the " + SessionHistoryCsvRepository.MAX_SAVED_SESSIONS + " most recent sessions are kept.";
            }

            JOptionPane.showMessageDialog( this, message, "Import sessions", JOptionPane.INFORMATION_MESSAGE );

        } catch (IOException ex) {

            // Inform the user that the import could not be completed.
            JOptionPane.showMessageDialog( this,
                    "Unable to import sessions from " + importFile.getName() + ".",
                    "Import sessions", JOptionPane.ERROR_MESSAGE );

            // Print the underlying error for debugging.
            ex.printStackTrace();
        }
    }


    // Lets the user choose where to save a copy of the session history as a CSV file.
    private void exportSessions() {

        // Inform the user when there are no saved sessions to export.
        if (!sessionListSection.hasStoredSessions()) {
            JOptionPane.showMessageDialog( this,
                    "There are no saved sessions to export.",
                    "Export sessions", JOptionPane.INFORMATION_MESSAGE );
            return;
        }

        // Open a save dialog limited to CSV files.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export sessions");
        fileChooser.setFileFilter( new FileNameExtensionFilter("CSV files (*.csv)", "csv") );
        fileChooser.setSelectedFile( new File("ecoscan-sessions.csv") );

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

            // Copy the saved session history to the chosen file.
            sessionListSection.exportSessions( exportFile.toPath() );

            JOptionPane.showMessageDialog( this,
                    "Sessions exported to " + exportFile.getName(),
                    "Export sessions", JOptionPane.INFORMATION_MESSAGE );

        } catch (IOException ex) {

            // Inform the user that the export could not be completed.
            JOptionPane.showMessageDialog( this,
                    "Unable to export the saved sessions.",
                    "Export sessions", JOptionPane.ERROR_MESSAGE );

            // Print the underlying error for debugging.
            ex.printStackTrace();
        }
    }


    // Reloads saved scans and sessions whenever the History Page is opened.
    public void refreshHistory() {
        historyListSection.refreshScans();
        sessionListSection.refreshSessions();
    }


    // Registers the action used to open a saved scan from History.
    // Passes the listener to the History list component.
    public void setViewScanListener(Consumer<SavedScan> listener) {
        historyListSection.setViewScanListener(listener);
    }

}