package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;
import com.example.monitortest.ui.common.RoundedTextField;

import com.example.monitortest.ui.common.RoundedComboBox;

public class HistoryHeaderSection extends JPanel {

    // Hint text displayed in the search box until the user starts typing.
    private static final String SEARCH_PLACEHOLDER = "🔍  Search scans";

    private JTextField searchField;
    private JComboBox<String> sortCombo;

    public HistoryHeaderSection() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = label( "Previous Scans", Font.BOLD, FONT_TITLE_SIZE, TEXT_MAIN );

        JPanel controls = new JPanel( new FlowLayout(FlowLayout.RIGHT, 10, 0) );
        controls.setOpaque(false);


        // Use the reusable rounded input component.
        searchField = new RoundedTextField(SEARCH_PLACEHOLDER);

        // Keep the existing muted search placeholder.
        searchField.setForeground(TEXT_MUTED);

        // Match the height of the nearby sorting dropdown.
        searchField.setPreferredSize(new Dimension(200, 36));


        // Clear the hint text when the user clicks into the search box.
        // Restore the hint text when the search box is left empty.
        searchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_MAIN);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setForeground(TEXT_MUTED);
                    searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });

        // Use EcoScan's rounded dropdown instead of the native Swing appearance.
        sortCombo = new RoundedComboBox( new String[]{"↓↑ Newest first", "↓↑ Oldest first"} );

        // Match the search field's height.
        sortCombo.setPreferredSize(new Dimension(150, 36));

        // Match the search field's height and the application's typography.
        sortCombo.setPreferredSize(new Dimension(150, 36));

        controls.add(searchField);
        controls.add(sortCombo);

        add(title, BorderLayout.WEST);
        add(controls, BorderLayout.EAST);
    }

    public JTextField getSearchField() {
        return searchField;
    }

    // Returns the text typed into the search box, ignoring the hint text.
    public String getSearchText() {

        String text = searchField.getText();

        if (text.equals(SEARCH_PLACEHOLDER)) {
            return "";
        }

        return text.trim();
    }

    public JComboBox<String> getSortCombo() {
        return sortCombo;
    }
}