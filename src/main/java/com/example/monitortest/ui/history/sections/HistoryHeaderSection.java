//package com.example.monitortest.views.sections;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.border.LineBorder;
//import java.awt.*;
//
//import static com.example.monitortest.ui.theme.AppTheme.*;
//
//public class HistoryHeaderSection  extends JPanel{
//
//    private JTextField searchField;
//    private JComboBox<String> sortCombo;
//
//        // header title
//    public HistoryHeaderSection(){
//        JPanel headerPanel = new JPanel(new BorderLayout());
//        headerPanel.setOpaque(false);
//        JLabel title = new JLabel("Previous Scans");
//        title.setFont(new Font("SansSerif", Font.BOLD, 22));
//        title.setForeground(TEXT_MAIN);
//        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
//        controlsPanel.setOpaque(false);
//        JTextField searchField = new JTextField("🔍  Search scans");
//        searchField.setPreferredSize(new Dimension(180, 32));
//        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
//        searchField.setForeground(TEXT_MUTED);
//        searchField.setBorder(BorderFactory.createCompoundBorder(
//                new LineBorder(BORDER_COLOR, 1, true),
//                new EmptyBorder(5, 8, 5, 8)
//        ));
//
//        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"↓↑ Newest first", "↓↑ Oldest first"});
//        sortCombo.setPreferredSize(new Dimension(130, 32));
//        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
//        sortCombo.setBackground(Color.WHITE);
//        controlsPanel.add(searchField);
//        controlsPanel.add(sortCombo);
//        headerPanel.add(title, BorderLayout.WEST);
//        headerPanel.add(controlsPanel, BorderLayout.EAST);
//    }
//
//        // search field
//        public JTextField getSearchField() {
//            return searchField;
//        }
//
//        // sort dropdown
//        public JComboBox<String> getSortCombo() {
//            return sortCombo;
//        }
//
//}


package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;

public class HistoryHeaderSection extends JPanel {

    // Hint text displayed in the search box until the user starts typing.
    private static final String SEARCH_PLACEHOLDER = "🔍  Search scans";

    private JTextField searchField;
    private JComboBox<String> sortCombo;

    public HistoryHeaderSection() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = label(
                "Previous Scans",
                Font.BOLD,
                FONT_TITLE_SIZE,
                TEXT_MAIN
        );

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        controls.setOpaque(false);

        searchField = new JTextField(SEARCH_PLACEHOLDER);
        searchField.setForeground(TEXT_MUTED);
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));

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

        sortCombo = new JComboBox<>(
                new String[]{"↓↑ Newest first", "↓↑ Oldest first"}
        );
        sortCombo.setPreferredSize(new Dimension(130, 32));

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