
package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.theme.UIUtility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable title bar for sections on the History Page.
// Supports a section heading and an optional action button.
public class HistorySectionTitleBar extends JPanel {

    // Creates a title bar with an optional action on the right.
    public HistorySectionTitleBar(String titleText, JButton actionButton) {

        // Use the same layout and spacing across History sections.
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 8, 18));

        // Display the section heading.
        JLabel title = new JLabel(titleText);
        title.setFont( UIUtility.font("SansSerif", Font.BOLD, FONT_LABEL_SIZE) );
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(12, 15, 4, 15));

        add(title, BorderLayout.WEST);

        // Only include an action button when one is supplied.
        if (actionButton != null) {
            add(actionButton, BorderLayout.EAST);
        }
    }
}
