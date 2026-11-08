package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;

import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.history.components.SessionRow;
import com.example.monitortest.ui.history.components.HistoryTableHeader;
import com.example.monitortest.ui.history.components.HistorySectionTitleBar;
import com.example.monitortest.ui.history.components.HistoryClearButton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Displays saved Live Monitor sessions and their recorded CO2 emissions.
// Handles loading, importing and clearing session history.
public class SessionListSection extends RoundedPanel {

    private JPanel listItemsPanel;

    // Removes every saved session after the user confirms.
    private JButton clearButton;

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

        // Create the shared clearing button.
        // Session-specific confirmation and deletion stay in clearSessions().
        clearButton = new HistoryClearButton( "Clear Sessions", this::clearSessions );

        // Reuse the common title bar with its optional right-side action.
        top.add( new HistorySectionTitleBar( "Live Monitor Sessions", clearButton ), BorderLayout.NORTH );

        // Display the session columns using the shared header.
        top.add( new HistoryTableHeader("DATE", "TOTAL EMISSIONS"), BorderLayout.SOUTH );

        // Keep the title and column headings inside the card padding.
        contentPanel.add(top, BorderLayout.NORTH);

        listItemsPanel = new JPanel();
        listItemsPanel.setLayout( new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS) );
        listItemsPanel.setBackground(Color.WHITE);

        // The title bar must be created before refreshing sessions because refreshSessions() updates the Clear Sessions button.
        refreshSessions();

        // Use the shared scroll pane configuration for rounded cards.
        JScrollPane scrollPane = UIUtility.createCardScrollPane(listItemsPanel);

        // Keep the scrollable session rows inside the rounded card.
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Attach the complete session layout to the outer card.
        add(contentPanel, BorderLayout.CENTER);
    }

    // Reloads saved sessions and updates the Sessions table.
    // Can also be called when the user returns to the History Page.
    public void refreshSessions() {

        // Remove the previously displayed rows before rebuilding the list.
        listItemsPanel.removeAll();

        // Tracks whether there are any sessions the user could clear.
        boolean hasSessions = false;

        try {

            // Load the saved sessions from EcoScan's sessions CSV file.
            List<SessionRecord> sessions = sessionRepository.loadAll();

            // Display the most recent sessions first.
            sessions.sort( Comparator.comparing(SessionRecord::getStartedAt).reversed() );

            hasSessions = !sessions.isEmpty();

            // Show a helpful message when no sessions have been saved yet.
            if (sessions.isEmpty()) {
                JLabel emptyLabel = new JLabel( "No monitoring sessions recorded yet. A session is saved when EcoScan closes." );
                emptyLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
                listItemsPanel.add(emptyLabel);

            } else {

                // Create one reusable SessionRow for every saved session.
                for (SessionRecord session : sessions) {
                    listItemsPanel.add(new SessionRow(session));
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

        // Only allow clearing when there are sessions to remove.
        clearButton.setEnabled(hasSessions);

        // Update the Swing layout after changing the displayed rows.
        listItemsPanel.revalidate();
        listItemsPanel.repaint();
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
}