package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;

import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.history.components.SessionRow;
import com.example.monitortest.ui.history.components.HistoryTableHeader;
import com.example.monitortest.ui.history.components.HistorySectionTitleBar;
import com.example.monitortest.ui.history.components.HistorySearchBar;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

// Displays saved Live Monitor sessions and their recorded CO2 emissions.
// Handles loading, searching, importing, exporting, printing and deleting session history.
public class SessionListSection extends RoundedPanel {

    private JPanel listItemsPanel;

    // Links each displayed row to the session it represents.
    private final Map<SessionRow, SessionRecord> rows = new LinkedHashMap<>();

    // Removes every saved session after the user confirms.
    private JButton clearButton;

    // Removes the selected sessions after the user confirms.
    private JButton deleteButton;

    // Imports sessions from a CSV file chosen by the user.
    private JButton importButton;

    // Exports the saved sessions to a CSV file chosen by the user.
    private JButton exportButton;

    // Saves the selected or visible sessions as a PDF.
    private JButton printButton;

    // Searches and sorts the sessions in this card only.
    private HistorySearchBar searchBar;

    // Text currently typed into the sessions search box.
    private String searchText = "";

    // Displays the newest sessions first unless the user chooses oldest first.
    private boolean newestFirst = true;

    // Tracks whether there are any saved sessions, ignoring the search filter.
    private boolean hasStoredSessions = false;

    // Retrieves saved sessions from the sessions CSV file.
    private final SessionHistoryCsvRepository sessionRepository = new SessionHistoryCsvRepository();

    // Creates the session history card using EcoScan's existing design.
    public SessionListSection() {

        // Reuse the same white fill, border and corner radius.
        super(Color.WHITE, BORDER_COLOR, 16);

        // Preserve the RoundedPanel as the outer card.
        setLayout(new BorderLayout());

        // Keep the session components away from the rounded corners.
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));


        // Display the title bar above the column headings.
        JPanel top = new JPanel(new BorderLayout());

        top.setOpaque(false);

        // Create the search box and sort dropdown for the sessions.
        searchBar = new HistorySearchBar("Search sessions");

        // Filter the sessions whenever the search text changes.
        searchBar.getSearchField().getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                setSearchText( searchBar.getSearchText() );
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSearchText( searchBar.getSearchText() );
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setSearchText( searchBar.getSearchText() );
            }
        });

        // Re-sort the sessions whenever a different sort option is chosen.
        // The first option is "Newest first", the second is "Oldest first".
        searchBar.getSortCombo().addActionListener(e -> {
            setNewestFirst( searchBar.getSortCombo().getSelectedIndex() == 0 );
        });

        // Reuse the common title bar with the search and sort on the right.
        top.add( new HistorySectionTitleBar( "Live Monitor Sessions", searchBar ), BorderLayout.NORTH );

        // Display the session columns using the shared header.
        // The third column shows each session's average emissions per second.
        top.add( new HistoryTableHeader("DATE", "TOTAL EMISSIONS", "AVG. EMISSIONS"), BorderLayout.SOUTH );

        // Keep the title and column headings inside the card padding.
        contentPanel.add(top, BorderLayout.NORTH);

        listItemsPanel = new JPanel();
        listItemsPanel.setLayout( new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS) );
        listItemsPanel.setBackground(Color.WHITE);

        // Instruction text in the same place as the scans card, so both cards match.
        JLabel infoLabel = new JLabel( "Select one or more rows (Cmd/Ctrl-click) to delete or print them." );
        infoLabel.setFont(UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE));
        infoLabel.setForeground(TEXT_MUTED);
        infoLabel.setBorder(new EmptyBorder(12, 18, 16, 18));

        // Use the same buttons as the scans card so both cards match.
        // Created before refreshing sessions because refreshSessions() updates them.
        importButton = HistoryActionsSection.createActionButton( "Import Sessions", FontAwesomeSolid.FILE_IMPORT, false );
        exportButton = HistoryActionsSection.createActionButton( "Export Sessions", FontAwesomeSolid.FILE_EXPORT, false );
        printButton = HistoryActionsSection.createActionButton( "Print List", FontAwesomeSolid.PRINT, false );
        deleteButton = HistoryActionsSection.createActionButton( "Delete Selected", FontAwesomeSolid.TRASH_ALT, true );
        clearButton = HistoryActionsSection.createActionButton( "Clear Sessions", FontAwesomeSolid.TRASH_ALT, true );

        deleteButton.addActionListener(e -> deleteSelectedSessions());
        clearButton.addActionListener(e -> clearSessions());

        refreshSessions();

        // Use the shared scroll pane configuration for rounded cards.
        JScrollPane scrollPane = UIUtility.createCardScrollPane(listItemsPanel);

        // Keep the scrollable session rows inside the rounded card.
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Import, Export and Print sit on the left, matching the scans card.
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.X_AXIS));
        leftButtons.setOpaque(false);
        leftButtons.add(importButton);
        leftButtons.add(Box.createHorizontalStrut(10));
        leftButtons.add(exportButton);
        leftButtons.add(Box.createHorizontalStrut(10));
        leftButtons.add(printButton);

        // Keep both delete actions together on the right edge of the card.
        JPanel rightButtons = new JPanel();
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.X_AXIS));
        rightButtons.setOpaque(false);
        rightButtons.add(deleteButton);
        rightButtons.add(Box.createHorizontalStrut(10));
        rightButtons.add(clearButton);

        // Line the buttons up with the card's inner padding, the same as the scans card.
        JPanel actionsBar = new JPanel(new BorderLayout());
        actionsBar.setOpaque(false);
        actionsBar.setBorder(new EmptyBorder(0, 10, 10, 10));
        actionsBar.add(leftButtons, BorderLayout.WEST);
        actionsBar.add(rightButtons, BorderLayout.EAST);

        // Keep the instruction text and session actions inside the rounded card.
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.add(infoLabel, BorderLayout.NORTH);
        footerPanel.add(actionsBar, BorderLayout.SOUTH);

        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        // Attach the complete session layout to the outer card.
        add(contentPanel, BorderLayout.CENTER);
    }

    // Reloads saved sessions and updates the Sessions table.
    // Can also be called when the user returns to the History Page.
    public void refreshSessions() {

        // Remove the previously displayed rows before rebuilding the list.
        listItemsPanel.removeAll();
        rows.clear();

        // Tracks whether there are any sessions the user could clear or export.
        hasStoredSessions = false;

        try {

            // Load the saved sessions from EcoScan's sessions CSV file.
            List<SessionRecord> sessions = sessionRepository.loadAll();

            // Sort the sessions by date using the order chosen in the sort dropdown.
            Comparator<SessionRecord> byDate = Comparator.comparing(SessionRecord::getStartedAt);
            sessions.sort( newestFirst ? byDate.reversed() : byDate );

            hasStoredSessions = !sessions.isEmpty();

            // Only keep the sessions that match the current search text.
            sessions.removeIf( session -> !matchesSearch(session) );

            // Show a helpful message when no sessions have been saved yet,
            // or when no saved sessions match the search.
            if (sessions.isEmpty()) {

                String message = hasStoredSessions ? "No saved sessions match your search." : "No monitoring sessions recorded yet. A session is saved when EcoScan closes.";

                JLabel emptyLabel = new JLabel( message );
                emptyLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
                listItemsPanel.add(emptyLabel);

            } else {

                // Create one reusable SessionRow for every saved session.
                for (SessionRecord session : sessions) {

                    SessionRow row = new SessionRow(session);

                    // Allow the row to be selected for deletion.
                    addSelection(row);

                    // Remember which session this row represents.
                    rows.put(row, session);

                    listItemsPanel.add(row);
                }
            }

        } catch (IOException e) {

            // Display a loading error without modifying the saved CSV.
            JLabel errorLabel = new JLabel( "Unable to load saved monitoring sessions." );

            errorLabel.setForeground(Color.RED);
            errorLabel.setBorder(new EmptyBorder(15, 15, 15, 15));

            listItemsPanel.add(errorLabel);

            // Print the underlying error for debugging.
            e.printStackTrace();
        }

        // Only allow clearing and exporting when there are sessions.
        clearButton.setEnabled(hasStoredSessions);
        exportButton.setEnabled(hasStoredSessions);

        // Only allow printing when there are sessions on screen.
        printButton.setEnabled( !rows.isEmpty() );

        // Nothing is selected after reloading the rows.
        deleteButton.setEnabled(false);

        // Update the Swing layout after changing the displayed rows.
        listItemsPanel.revalidate();
        listItemsPanel.repaint();
    }

    /*************************** Sorting and Searching Sessions ***********************/
    // Updates the search text and reloads the Sessions table.
    public void setSearchText(String text) {
        this.searchText = text.trim().toLowerCase();
        refreshSessions();
    }

    // Updates the sort order and reloads the Sessions table.
    public void setNewestFirst(boolean newestFirst) {
        this.newestFirst = newestFirst;
        refreshSessions();
    }

    // Returns true when the session's date or emissions contain the search text.
    private boolean matchesSearch(SessionRecord session) {

        // Every session matches when the search box is empty.
        if (searchText.isEmpty()) {
            return true;
        }

        String date = session.getStartedAt().format(SessionRow.DATE_FORMAT).toLowerCase();
        String emissions = SessionRow.formatEmissions( session.getTotalEmissionsGrams() ).toLowerCase();

        return date.contains(searchText) || emissions.contains(searchText);
    }

    /***************** Selecting Sessions ******************************************/
    // Makes a session row selectable.
    // A single click selects one row, Cmd/Ctrl-click selects several rows.
    private void addSelection(SessionRow row) {

        row.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                // Cmd (Mac) or Ctrl (Windows) allows more than one row to be selected.
                boolean multiSelect = e.isMetaDown() || e.isControlDown();

                // A normal click replaces the previous selection.
                if (!multiSelect) {
                    for (SessionRow other : rows.keySet()) {
                        other.setSelected(false);
                    }
                }

                // Select the clicked row, or toggle it during multi-select.
                row.setSelected( !row.isSelected() || !multiSelect );

                // Only allow Delete Selected when a row is selected.
                deleteButton.setEnabled( hasSelection() );
            }
        });
    }

    // Returns the sessions for the currently selected rows.
    private List<SessionRecord> getSelectedSessions() {

        List<SessionRecord> selected = new ArrayList<>();

        for (Map.Entry<SessionRow, SessionRecord> entry : rows.entrySet()) {

            if (entry.getKey().isSelected()) {
                selected.add(entry.getValue());
            }
        }

        return selected;
    }

    // Returns true when at least one session row is selected.
    public boolean hasSelection() {
        return !getSelectedSessions().isEmpty();
    }

    // Returns true when there are saved sessions, even if the search hides them.
    public boolean hasStoredSessions() {
        return hasStoredSessions;
    }

    // Returns the sessions to include in the PDF, as Date and Total Emissions rows.
    // Uses the selected rows, or every visible row when nothing is selected.
    public List<String[]> getPrintRows() {

        boolean printSelectedOnly = hasSelection();

        List<String[]> printRows = new ArrayList<>();

        // Keep the rows in the same order as the table on screen.
        for (Map.Entry<SessionRow, SessionRecord> entry : rows.entrySet()) {

            if (!printSelectedOnly || entry.getKey().isSelected()) {

                SessionRecord session = entry.getValue();
                printRows.add( new String[]{
                        session.getStartedAt().format(SessionRow.DATE_FORMAT),
                        SessionRow.formatEmissions( session.getTotalEmissionsGrams() )
                } );
            }
        }

        return printRows;
    }

    // Imports sessions from a CSV file chosen by the user.
    // Returns the number of sessions imported.
    public int importSessions(Path importPath) throws IOException {

        // Add the imported sessions to EcoScan's session history.
        int imported = sessionRepository.importFrom(importPath);

        // Rebuild the Sessions table to include the imported sessions.
        refreshSessions();

        return imported;
    }

    // Copies the saved sessions to a CSV file chosen by the user.
    public void exportSessions(Path exportPath) throws IOException {
        sessionRepository.exportTo(exportPath);
    }

    /******************** DELETE SESSIONS **************************************/
    // Asks the user to confirm, then deletes the selected sessions.
    private void deleteSelectedSessions() {

        List<SessionRecord> selected = getSelectedSessions();

        // Do not attempt deletion when nothing is selected.
        if (selected.isEmpty()) {
            return;
        }

        // Confirm before removing the sessions from the saved history.
        int answer = JOptionPane.showConfirmDialog( this,
                "Delete the selected sessions?", "Delete sessions",
                JOptionPane.OK_CANCEL_OPTION );

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            // Remove the selected sessions from the sessions CSV file.
            sessionRepository.deleteSessions(selected);

        } catch (IOException e) {

            // Inform the user that the sessions could not be deleted.
            JOptionPane.showMessageDialog( this, "Unable to delete the selected sessions.", "Delete sessions", JOptionPane.ERROR_MESSAGE );

            // Preserve the exception for debugging.
            e.printStackTrace();

            // Leave the existing session list unchanged after failure.
            return;
        }

        // Refresh the Sessions table after successful deletion.
        refreshSessions();
    }

    // Asks the user to confirm, then deletes every saved session.
    private void clearSessions() {

        // Confirm before removing every session from the saved history.
        int answer = JOptionPane.showConfirmDialog( this,
                "Delete all saved sessions? This cannot be undone.",
                "Clear sessions", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE );

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            // Remove the sessions CSV file.
            sessionRepository.deleteAll();


        } catch (IOException e) {

            // Inform the user that saved sessions could not be cleared.
            JOptionPane.showMessageDialog( this, "Unable to clear the saved sessions.", "Clear sessions", JOptionPane.ERROR_MESSAGE );

            // Preserve the exception for debugging.
            e.printStackTrace();

            // Leave the existing session list unchanged after failure.
            return;
        }

        // Refresh the Sessions table after successful deletion.
        refreshSessions();

    }

    // Returns the Import Sessions button so the History Page can handle the file dialog.
    public JButton getImportButton() {
        return importButton;
    }

    // Returns the Export Sessions button so the History Page can handle the file dialog.
    public JButton getExportButton() {
        return exportButton;
    }

    // Returns the Print List button so the History Page can handle the save dialog.
    public JButton getPrintButton() {
        return printButton;
    }
}