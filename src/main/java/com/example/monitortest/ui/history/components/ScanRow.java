package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.common.OutlinedActionButton;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class ScanRow extends JPanel {

    private JButton viewButton;
    private boolean selected = false;

    // Tracks whether the mouse is currently over the row.
    private boolean hovered = false;

    // Creates one history row containing the scan date, CO₂ and View button.
    public ScanRow( String date, String name ) {

        // Match the three columns used by HistoryListSection:
        // Date, Name and Action.
        setLayout(new GridLayout(1, 3, 10, 0));
        setBackground(Color.WHITE);

        // Give each row enough height for the rounded action button.
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        setPreferredSize(new Dimension(700, 54));

        // Match the padding of the table header.
        setBorder(new EmptyBorder(10, 18, 10, 18));

        Font font = UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE);

        // Add the scan information in the same order as the header.
        add(createCell(date, font));
        add(createCell(name, font));

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0) );
        buttonPanel.setOpaque(false);

        // Reuse the rounded secondary action button.
        viewButton = new OutlinedActionButton("View");

        // Add a consistent icon to indicate the scan can be opened.
        viewButton.setIcon( FontIcon.of(FontAwesomeSolid.EYE, 13, GREEN) );
        viewButton.setIconTextGap(6);

        // Keep the action compact enough for the table row.
        viewButton.setPreferredSize(new Dimension(88, 32));

        buttonPanel.add(viewButton);


        add(buttonPanel);

        // Highlight the row while the mouse is over it.
        // The View button shares the listener so moving onto it keeps the highlight.
        MouseAdapter hoverListener = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                updateBackground();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Only remove the highlight once the mouse has left the whole row.
                hovered = getMousePosition(true) != null;
                updateBackground();
            }
        };

        addMouseListener(hoverListener);
        viewButton.addMouseListener(hoverListener);
    }

    private JLabel createCell(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_MAIN);
        return label;
    }

    public JButton getViewButton() {
        return viewButton;
    }

    // Is this row selected?
    public boolean isSelected() {
        return selected;
    }

    // Selected rows turn light green
    public void setSelected(boolean selected) {
        this.selected = selected;
        updateBackground();
    }

    // Selected rows stay light green, hovered rows turn light grey, others stay white.
    private void updateBackground() {

        if (selected) {
            setBackground(new Color(0xEAF5EE));
        } else if (hovered) {
            setBackground(new Color(0xF7F8FA));
        } else {
            setBackground(Color.WHITE);
        }
    }
}