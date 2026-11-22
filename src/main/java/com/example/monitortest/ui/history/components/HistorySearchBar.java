package com.example.monitortest.ui.history.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.common.RoundedTextField;
import com.example.monitortest.ui.common.RoundedComboBox;

// Reusable search box and sort dropdown for the History cards.
// Used by both Saved Hardware Scans and Live Monitor Sessions.
public class HistorySearchBar extends JPanel {

    // Hint text displayed in the search box until the user starts typing.
    private final String searchPlaceholder;

    private JTextField searchField;
    private JComboBox<String> sortCombo;

    public HistorySearchBar(String placeholderText) {

        searchPlaceholder = "🔍  " + placeholderText;

        // Position search and sort controls without extra outside margins.
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);

        // Use the reusable rounded input component.
        searchField = new RoundedTextField(searchPlaceholder);

        // Keep the existing muted search placeholder.
        searchField.setForeground(TEXT_MUTED);

        // Match the height of the nearby sorting dropdown.
        searchField.setPreferredSize(new Dimension(200, 36));


        // Clear the hint text when the user clicks into the search box.
        // Restore the hint text when the search box is left empty.
        searchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(searchPlaceholder)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_MAIN);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setForeground(TEXT_MUTED);
                    searchField.setText(searchPlaceholder);
                }
            }
        });

        // Use EcoScan's rounded dropdown instead of the native Swing appearance.
        sortCombo = new RoundedComboBox( new String[]{"↓↑ Newest first", "↓↑ Oldest first"} );

        // Match the search field's height and the application's typography.
        sortCombo.setPreferredSize(new Dimension(150, 36));

        // Keep a consistent gap between search and sorting controls.
        add(searchField);
        add(Box.createHorizontalStrut(10));
        add(sortCombo);
    }

    public JTextField getSearchField() {
        return searchField;
    }

    // Returns the text typed into the search box, ignoring the hint text.
    public String getSearchText() {

        String text = searchField.getText();

        if (text.equals(searchPlaceholder)) {
            return "";
        }

        return text.trim();
    }

    public JComboBox<String> getSortCombo() {
        return sortCombo;
    }
}