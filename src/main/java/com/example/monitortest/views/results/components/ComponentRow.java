package com.example.monitortest.views.results.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.monitortest.theme.AppTheme.*;
import static com.example.monitortest.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.theme.UIUtility.label;

public class ComponentRow {
    public static JPanel componentRow(JPanel list, String name, String summary, String status,
                                String spec, String impact, String age, String assessment, boolean openAtStart) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

//        JLabel nameLabel = label(name, 14, Font.BOLD, TEXT_MAIN);
        JLabel nameLabel = label(name, Font.BOLD, 14, TEXT_MAIN);

        nameLabel.setPreferredSize(new Dimension(200, 20));
        left.add(nameLabel);
//        left.add(label(summary, 13, Font.PLAIN, TEXT_MUTED));
        left.add(label(summary, Font.PLAIN, 13, TEXT_MUTED));
        header.add(left, BorderLayout.WEST);

//        JLabel arrow = label(openAtStart ? "⌃" : "⌄", 14, Font.PLAIN, TEXT_MUTED);
        JLabel arrow = label(openAtStart ? "⌃" : "⌄", Font.PLAIN, 14, TEXT_MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(StatusBadge.badge(status));
        right.add(arrow);
        header.add(right, BorderLayout.EAST);
        item.add(header, BorderLayout.NORTH);

        // details (hidden until clicked)
        JPanel details = new JPanel(new GridLayout(2, 4, 20, 4));
        details.setBackground(new Color(0xF8FAF9));
        details.setBorder(new EmptyBorder(12, 18, 14, 18));


        // Display hardware specifications and power measurements.
        details.add(label("Detected specifications", Font.PLAIN, 11, TEXT_MUTED));
        details.add(label("Power at scan", Font.PLAIN, 11, TEXT_MUTED));
        details.add(label("Additional information", Font.PLAIN, 11, TEXT_MUTED));
        details.add(label("Share of measured power", Font.PLAIN, 11, TEXT_MUTED));


        details.add(label(spec, Font.BOLD, 13, TEXT_MAIN));
        details.add(label(impact, Font.BOLD, 13, TEXT_MAIN)); // data: power use (W) of this part
        details.add(label(age, Font.BOLD, 13, TEXT_MAIN));
        details.add(label(assessment, Font.BOLD, 13, TEXT_MAIN));

        details.setVisible(openAtStart);
        item.add(details, BorderLayout.CENTER);

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                details.setVisible(!details.isVisible());
                arrow.setText(details.isVisible() ? "⌃" : "⌄");
                list.revalidate();
            }
        });
        return item;
    }
}
