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

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;

public class HistoryHeaderSection extends JPanel {

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

        searchField = new JTextField("🔍  Search scans");
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));

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

    public JComboBox<String> getSortCombo() {
        return sortCombo;
    }
}