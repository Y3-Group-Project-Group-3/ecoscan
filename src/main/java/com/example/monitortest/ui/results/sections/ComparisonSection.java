package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.results.components.ComparisonBar;
import static com.example.monitortest.ui.theme.UIUtility.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.BORDER_COLOR;
import static com.example.monitortest.ui.theme.AppTheme.GREEN;
import static com.example.monitortest.ui.theme.AppTheme.GREY_BAR;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.label;

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
        heading.add(label( "COMPARISON", Font.BOLD, FONT_LABEL_SIZE, GREEN ));
        heading.add(Box.createVerticalStrut(4));
        heading.add(label(
                "<html>Better than <span style='color:#2E7D4F'>68%</span> of similar Home PCs.</html>",
                Font.BOLD,
                FONT_HEADING_SIZE,
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

        // Cap the height at what the content needs, so larger fonts are never cut off
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));

        return section;
    }


    public static JPanel comparisonRow(String name, int score, boolean thisPc) {
        JPanel line = new JPanel(new BorderLayout(14, 0));
        line.setOpaque(false);
        JPanel labels = new JPanel(new GridLayout(1, 2));

        labels.setOpaque(false);
        labels.setPreferredSize(new Dimension(170, 22));   // sized to fit FONT_BODY
        labels.add(label( name, thisPc ? Font.BOLD : Font.PLAIN, FONT_BODY_SIZE, thisPc ? TEXT_MAIN : TEXT_MUTED ));
        labels.add(label( String.valueOf(score), Font.BOLD, FONT_BODY_SIZE, thisPc ? GREEN : TEXT_MUTED ));
        line.add(labels, BorderLayout.WEST);
        line.add( new ComparisonBar( score, thisPc ? GREEN : GREY_BAR), BorderLayout.CENTER);
        return line;
    }
}