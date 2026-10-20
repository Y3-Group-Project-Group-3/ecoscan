package com.example.monitortest.views.history.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.*;

public class ScanRow extends JPanel {

    private JButton viewButton;

    public ScanRow(
            String date,
            String device,
            String use,
            String score,
            String co2,
            String result
    ) {
        setLayout(new GridLayout(1, 7, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setPreferredSize(new Dimension(700, 45));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 15, 8, 15));

        Font font = new Font("SansSerif", Font.PLAIN, 12);

        add(createCell(date, font));
        add(createCell(device, font));
        add(createCell(use, font));
        add(createCell(score, font));
        add(createCell(co2, font));

        JPanel resultPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        resultPanel.setOpaque(false);
        resultPanel.add(ResultBadge.createBadge(result));
        add(resultPanel);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        buttonPanel.setOpaque(false);

        viewButton = new JButton("View");
        viewButton.setPreferredSize(new Dimension(55, 24));
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
}