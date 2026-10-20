package com.example.monitortest.views.sections;

import com.example.monitortest.views.results.components.ComparisonBar;
import static com.example.monitortest.theme.UIUtility.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.*;
import static com.example.monitortest.theme.AppTheme.BORDER_COLOR;
import static com.example.monitortest.theme.AppTheme.GREEN;
import static com.example.monitortest.theme.AppTheme.GREY_BAR;
import static com.example.monitortest.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.theme.UIUtility.label;
import static java.awt.Component.LEFT_ALIGNMENT;

public class ComparisonSection {


    public static JPanel createComparisonSection() {

        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 160)
        );

        // Heading and description
        JPanel heading = column();

        heading.add(label(
                "COMPARISON",
                Font.BOLD,
                12,
                GREEN
        ));

        heading.add(Box.createVerticalStrut(4));

        heading.add(label(
                "<html>Better than <span style='color:#2E7D4F'>68%</span> of similar Home PCs.</html>",
                Font.BOLD,
                20,
                TEXT_MAIN
        ));

        section.add(heading, BorderLayout.NORTH);

        // Comparison rows
        JPanel box = new JPanel(new GridLayout(0, 1, 0, 10));

        box.setBackground(CARD_BG);

        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        box.add(comparisonRow("This PC", 78, true));
        box.add(comparisonRow("Similar PC", 62, false));
        box.add(comparisonRow("Laptop", 71, false));

        section.add(box, BorderLayout.CENTER);

        return section;
    }


    public static JPanel comparisonRow(String name, int score, boolean thisPc) {
        JPanel line = new JPanel(new BorderLayout(14, 0));
        line.setOpaque(false);
        JPanel labels = new JPanel(new GridLayout(1, 2));

        labels.setOpaque(false);
        labels.setPreferredSize(new Dimension(140, 18));
        labels.add(label(
                name,
                thisPc ? Font.BOLD : Font.PLAIN,
                13,
                thisPc ? TEXT_MAIN : TEXT_MUTED
        ));

        labels.add(label(
                String.valueOf(score),
                Font.BOLD,
                13,
                thisPc ? GREEN : TEXT_MUTED
        ));
        line.add(labels, BorderLayout.WEST);
        line.add(
                new ComparisonBar(
                        score,
                        thisPc ? GREEN : GREY_BAR),
                BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            System.out.println("Row size: " + line.getSize());
            System.out.println("Labels size: " + labels.getSize());
            System.out.println("Labels visible: " + labels.isVisible());
        });
        return line;
    }
}
