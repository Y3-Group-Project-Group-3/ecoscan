package com.example.monitortest.ui.history.sections;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;

public class HistoryHeaderSection extends JPanel {

    public HistoryHeaderSection() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = label( "Previous Scans", Font.BOLD, FONT_TITLE_SIZE, TEXT_MAIN );

        // Search and sort controls now sit inside each History card.
        add(title, BorderLayout.WEST);
    }
}