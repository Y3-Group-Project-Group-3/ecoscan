package com.example.monitortest.views.results.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.theme.UIUtility.label;

public class StatusBadge {

    public static JLabel badge(String text) {
        Color bg, fg;
        fg = switch (text) {
            case "HIGH IMPACT" -> {
                bg = new Color(0xFEE2E2);
                yield new Color(0xB91C1C);
            }
            case "AGEING" -> {
                bg = new Color(0xFFEDD5);
                yield new Color(0xC26B00);
            }
            default -> {
                bg = new Color(0xDCFCE7);
                yield new Color(0x166534);
            }
        };
//        JLabel l = label(text, 10, Font.BOLD, fg);
        JLabel l = label(text, Font.BOLD, 10, fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

}
