package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.model.ScanRecord;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;

public class ScanRow extends JPanel {

    private JButton viewButton;

    private final ScanRecord scan;

    private boolean selected = false;

    public ScanRow(
            String date,
            String device,
            String type,
            String co2
    ) {

        scan = new ScanRecord(date, device, type, co2);

        setLayout(new GridLayout(1, 5, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setPreferredSize(new Dimension(700, 50));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 15, 8, 15));

        Font font = UIUtility.font("SansSerif", Font.PLAIN, FONT_LABEL_SIZE);

        add(createCell(date, font));
        add(createCell(device, font));
        add(createCell(type, font));
        add(createCell(co2, font));

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
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

    public ScanRecord getScan() {return scan;}

    // selected row?
    public boolean isSelected() {return selected;}

    // Selected row turns light green
    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? new Color(0xEAF5EE) : Color.WHITE);}
}