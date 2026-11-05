package com.example.monitortest.ui.history.sections;

import com.example.monitortest.ui.theme.UIUtility;

import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.history.storage.SessionHistoryCsvRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Displays the total emissions recorded for each Live Monitor session.
// Kept separate from the saved hardware scans table.
public class SessionListSection extends JPanel {

    private JPanel listItemsPanel;

    // Retrieves saved sessions from the sessions CSV file.
    private final SessionHistoryCsvRepository sessionRepository = new SessionHistoryCsvRepository();

    // Formats session start times for the Sessions table.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm");


    public SessionListSection() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(BORDER_COLOR, 1, true));

        // Display the table title above the column headings.
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(createTitle(), BorderLayout.NORTH);
        top.add(createListHeaderRow(), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        listItemsPanel = new JPanel();
        listItemsPanel.setLayout( new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS) );
        listItemsPanel.setBackground(Color.WHITE);


        // Populate the Sessions table using records saved to CSV.
        refreshSessions();


        JScrollPane scrollPane = new JScrollPane(listItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(scrollPane, BorderLayout.CENTER);
    }


    // Reloads saved sessions and updates the Sessions table.
    // Can also be called when the user returns to the History Page.
    public void refreshSessions() {

        // Remove the previously displayed rows before rebuilding the list.
        listItemsPanel.removeAll();

        try {

            // Load the saved sessions from EcoScan's sessions CSV file.
            List<SessionRecord> sessions = sessionRepository.loadAll();

            // Display the most recent sessions first.
            sessions.sort( Comparator.comparing(SessionRecord::getStartedAt).reversed() );

            // Show a helpful message when no sessions have been saved yet.
            if (sessions.isEmpty()) {
                JLabel emptyLabel = new JLabel( "No monitoring sessions recorded yet. A session is saved when EcoScan closes." );
                emptyLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
                listItemsPanel.add(emptyLabel);

            } else {

                // Create one table row for every saved session.
                for (SessionRecord session : sessions) {
                    listItemsPanel.add( createRow(session) );
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


    // Creates one table row displaying the session's date and total emissions.
    private JPanel createRow(SessionRecord session) {

        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(8, 15, 8, 15));

        Font font = UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE);

        // Add the session information in the same order as the header.
        row.add( createCell( session.getStartedAt().format(DATE_FORMAT), font ) );
        row.add( createCell( formatEmissions(session.getTotalEmissionsGrams()), font ) );

        // Highlight the row in light grey while the mouse is over it.
        row.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(0xF7F8FA));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }
        });

        return row;
    }


    // Displays smaller totals in grams and larger totals in kilograms.
    private String formatEmissions(double grams) {

        if (grams >= 1000) {
            return String.format( Locale.UK, "%.2f kg CO₂e", grams / 1000 );
        }

        return String.format( Locale.UK, "%.2f g CO₂e", grams );
    }


    private JLabel createCell(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_MAIN);
        return label;
    }


    // Creates the title displayed above the Sessions table.
    private JLabel createTitle() {

        JLabel title = new JLabel("Live Monitor Sessions");
        title.setFont(UIUtility.font( "SansSerif", Font.BOLD, FONT_LABEL_SIZE ));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(12, 15, 4, 15));

        return title;
    }


    public JPanel createListHeaderRow() {

        // Create the header with the two session columns.
        JPanel header = new JPanel(new GridLayout(1, 2, 10, 0));
        header.setBackground(new Color(243, 244, 246));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Define the column headings in display order.
        String[] titles = { "DATE", "TOTAL EMISSIONS" };

        // Create and add a label for each column heading.
        for (String title : titles) {
            JLabel label = new JLabel(title);
            label.setFont(UIUtility.font( "SansSerif", Font.BOLD, FONT_BADGE_SIZE ));
            label.setForeground(new Color(75, 85, 99));
            header.add(label);
        }

        return header;
    }

}