package com.example.monitortest.views.results.components;

import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.theme.UIUtility.*;
import static java.awt.Component.LEFT_ALIGNMENT;

public class StatCard {
    public static JPanel statCard(String icon, Color iconBg, String title, JLabel value, JLabel unit, String sideText) {
        JPanel card = card();
        card.setLayout(new BorderLayout(16, 0));
        JLabel ic = new JLabel(icon, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        ic.setOpaque(true);
        ic.setBackground(iconBg);
        ic.setPreferredSize(new Dimension(44, 44));
        JPanel icWrap = new JPanel(new GridBagLayout());
        icWrap.setOpaque(false);
        icWrap.add(ic);
        card.add(icWrap, BorderLayout.WEST);

        JPanel text = column();
        JPanel top = row();
        top.add(label(title, Font.BOLD, 10, TEXT_MUTED), BorderLayout.WEST);
        top.add(label(sideText, Font.PLAIN, 11, TEXT_MUTED), BorderLayout.EAST);
        text.add(top);
        text.add(Box.createVerticalStrut(6));
        JPanel valueLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valueLine.setOpaque(false);
        valueLine.setAlignmentX(LEFT_ALIGNMENT);
        valueLine.add(value);
        valueLine.add(unit);
        text.add(valueLine);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

}
