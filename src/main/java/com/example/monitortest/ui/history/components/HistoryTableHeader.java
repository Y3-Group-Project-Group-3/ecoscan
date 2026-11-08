
package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.theme.UIUtility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Reusable column header for tables on the History Page.
// Accepts any number of column titles while maintaining consistent styling.
public class HistoryTableHeader extends JPanel {

    // Creates a transparent header with evenly spaced columns.
    public HistoryTableHeader(String... columnTitles) {

        // Match the number of grid columns to the supplied titles.
        setLayout(new GridLayout(1, columnTitles.length, 10, 0));

        // Allow RoundedPanel to draw the card background.
        setOpaque(false);

        // Match the horizontal spacing used by the History table rows.
        setBorder(new EmptyBorder(12, 18, 12, 18));

        // Create a consistently styled label for each column heading.
        for (String title : columnTitles) {
            JLabel label = new JLabel(title);
            // Use EcoScan's existing header typography and colours.
            label.setFont( UIUtility.font( "SansSerif", Font.BOLD, FONT_BADGE_SIZE ) );
            label.setForeground(TEXT_MUTED);
            add(label);
        }
    }
}
