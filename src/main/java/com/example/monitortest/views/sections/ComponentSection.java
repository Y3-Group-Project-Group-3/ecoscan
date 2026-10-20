package com.example.monitortest.views.sections;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.*;
import static com.example.monitortest.theme.AppTheme.BORDER_COLOR;
import static com.example.monitortest.theme.UIUtility.column;
import static com.example.monitortest.theme.UIUtility.label;
import static com.example.monitortest.views.results.components.ComponentRow.componentRow;
import static java.awt.Component.LEFT_ALIGNMENT;

public class ComponentSection {

    // list of components, click a row to open details
    public static JPanel createComponentSection() {
        JPanel section = column();
        section.add(label("COMPONENT BREAKDOWN", Font.BOLD, 12, GREEN));
        section.add(Box.createVerticalStrut(4));
        section.add(label("Hardware assessment", Font.BOLD, 20, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD_BG);
        list.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        list.setAlignmentX(LEFT_ALIGNMENT);

        // data: components from the scanner CSV + their assessment
        list.add(componentRow(list, "CPU", "Intel Core i5-12400F", "EFFICIENT",
                "Intel Core i5-12400F", "Low impact · 65 W", "~3 years · 4–6 years", "Suitable for intended use", false));
        list.add(componentRow(list, "RAM", "16 GB DDR4", "EFFICIENT",
                "16 GB DDR4 · 2 modules", "Low impact · 6 W", "~4 years · 3–5 years", "Suitable for intended use", true));
        list.add(componentRow(list, "GPU", "2 detected", "HIGH IMPACT",
                "2 GPUs detected", "High impact", "~4 years", "Main contributor to energy use", false));
        list.add(componentRow(list, "Storage", "2 detected", "AGEING",
                "2 drives detected", "Low impact", "~6 years", "Older drive may need replacing", false));
        list.add(componentRow(list, "Operating System", "", "SUPPORTED",
                "Windows", "–", "–", "Supported", false));
        section.add(list);
        return section;
    }

}
