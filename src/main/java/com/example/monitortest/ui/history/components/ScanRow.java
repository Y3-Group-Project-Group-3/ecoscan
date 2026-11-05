package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.theme.UIUtility;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class ScanRow extends JPanel {

    private JButton viewButton;
    private boolean selected = false;

    // Creates one history row containing the scan date, CO₂ and View button.
    public ScanRow(
            String date,
            String name
    ) {
        setLayout(new GridLayout(1, 4, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setPreferredSize(new Dimension(700, 50));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 15, 8, 15));

        Font font = UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE);

        // Add the scan information in the same order as the header.
        add(createCell(date, font));
        add(createCell(name, font));

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0) );
        buttonPanel.setOpaque(false);

        viewButton = new JButton("View");
        viewButton.setPreferredSize(new Dimension(70, 30));
        buttonPanel.add(viewButton);

        add(buttonPanel);
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
        setBackground(selected ? new Color(0xEAF5EE) : Color.WHITE);
    }
}