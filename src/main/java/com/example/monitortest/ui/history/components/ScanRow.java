
package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.common.OutlinedActionButton;
import com.example.monitortest.ui.theme.UIUtility;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Displays an individual saved hardware scan.
// Contains the scan date, name, and View action.
public class ScanRow extends JPanel {

    private final JButton viewButton;

    // Tracks the row's selection and mouse hover state.
    private boolean selected = false;
    private boolean hovered = false;

    // Colours used to distinguish hovered and selected rows.
    private static final Color HOVER_COLOR = new Color(0xF7F8FA);
    private static final Color SELECTED_COLOR = new Color(0xEAF5EE);

    // Creates a three-column row matching the History table header.
    public ScanRow(String date, String name) {

        setLayout(new GridLayout(1, 3, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        setPreferredSize(new Dimension(700, 54));
        setBackground(Color.WHITE);

        // Match the spacing used by HistoryTableHeader.
        setBorder(new EmptyBorder(10, 18, 10, 18));

        Font font = UIUtility.font( "SansSerif", Font.PLAIN, FONT_LABEL_SIZE );

        // Display the scan information.
        add(createCell(date, font));
        add(createCell(name, font));

        // Place the View button in the final column.
        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0) );
        buttonPanel.setOpaque(false);

        viewButton = new OutlinedActionButton("View");
        viewButton.setIcon( FontIcon.of(FontAwesomeSolid.EYE, 13, GREEN) );
        viewButton.setIconTextGap(6);
        viewButton.setPreferredSize(new Dimension(88, 32));

        buttonPanel.add(viewButton);
        add(buttonPanel);

        // Keep hover highlighting consistent when moving
        // between the row and its View button.
        MouseAdapter hoverListener = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                updateBackground();
            }

            @Override
            public void mouseExited(MouseEvent e) {

                // Check whether the pointer remains inside this row.
                hovered = getMousePosition(true) != null;
                updateBackground();
            }
        };

        addMouseListener(hoverListener);
        viewButton.addMouseListener(hoverListener);
    }

    // Creates an individual text cell with shared application styling.
    private JLabel createCell(String text, Font font) {

        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_MAIN);

        return label;
    }

    // Updates the row background based on selection and hover state.
    // Selected rows take priority over hover highlighting.
    private void updateBackground() {

        if (selected) {
            setBackground(SELECTED_COLOR);
        } else if (hovered) {
            setBackground(HOVER_COLOR);
        } else {
            setBackground(Color.WHITE);
        }
    }

    // Returns the View button so HistoryListSection can attach navigation.
    public JButton getViewButton() {
        return viewButton;
    }

    // Returns whether this row is currently selected.
    public boolean isSelected() {
        return selected;
    }

    // Updates selection and immediately refreshes the row appearance.
    public void setSelected(boolean selected) {
        this.selected = selected;
        updateBackground();
    }
}
