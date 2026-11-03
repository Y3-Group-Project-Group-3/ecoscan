package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.results.components.ScoreRing;
import com.example.monitortest.ui.results.components.StatusBadge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.GREEN;
import static com.example.monitortest.ui.theme.AppTheme.GREEN_LIGHT;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static java.awt.Component.LEFT_ALIGNMENT;

public class ScoreRecommendationSection {

    // eco score card + recommendation card
    public static JPanel createScoreAndRecommendation() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        // eco score
        JPanel scoreCard = card();
        scoreCard.setLayout(new BorderLayout(0, 10));
//        scoreCard.add(label("ECO SCORE", 12, Font.BOLD, GREEN), BorderLayout.NORTH);
        scoreCard.add(label("ECO SCORE", Font.BOLD, FONT_LABEL, GREEN), BorderLayout.NORTH);
        JPanel scoreBody = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        scoreBody.setOpaque(false);
        scoreBody.add(new ScoreRing(78));                       // data: eco score 0-100
        scoreBody.add(StatusBadge.badge("GOOD"));                           // data: score rating
        scoreCard.add(scoreBody, BorderLayout.CENTER);
        // data: short description of the score
//        scoreCard.add(label("<html><div style='width:260px'>Strong overall efficiency for a desktop in this category.</div></html>", 12, Font.PLAIN, TEXT_MUTED), BorderLayout.SOUTH);
        scoreCard.add(label("<html><div style='width:260px'>Strong overall efficiency for a desktop in this category.</div></html>", Font.PLAIN, FONT_LABEL, TEXT_MUTED), BorderLayout.SOUTH);
        c.gridx = 0; c.weightx = 0.35; c.insets = new Insets(0, 0, 0, 8);
        panel.add(scoreCard, c);

        // recommendation
        JPanel recoCard = card();
        recoCard.setLayout(new BoxLayout(recoCard, BoxLayout.Y_AXIS));
        JPanel recoTop = row();
//        recoTop.add(label("RECOMMENDATION", 12, Font.BOLD, TEXT_MAIN), BorderLayout.WEST);
        recoTop.add(label("RECOMMENDATION", Font.BOLD, FONT_LABEL, TEXT_MAIN), BorderLayout.WEST);
        // data: which option is active (KEEP / UPGRADE / REPLACE)
        recoTop.add(wrap(recommendationTabs("KEEP")), BorderLayout.EAST);
        recoCard.add(recoTop);
//        recoCard.add(label("KEEP", 40, Font.BOLD, GREEN));      // data: recommendation
        recoCard.add(label("KEEP", Font.BOLD, FONT_DISPLAY, GREEN));
        recoCard.add(Box.createVerticalStrut(6));
        // data: reason for the recommendation
//        recoCard.add(label("<html><div style='width:390px'>Your computer is still suitable for its intended use. "
//                        + "Replacing it now may create more environmental impact than continuing to use it.</div></html>",
//                13, Font.PLAIN, TEXT_MUTED));
        recoCard.add(label(
                "<html><div style='width:390px'>Your computer is still suitable for its intended use. "
                        + "Replacing it now may create more environmental impact than continuing to use it.</div></html>",
                Font.PLAIN, FONT_BODY, TEXT_MUTED
        ));
        recoCard.add(Box.createVerticalStrut(12));
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(GREEN_LIGHT);
        note.setBorder(new EmptyBorder(10, 14, 10, 14));
        note.setAlignmentX(LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));   // room for two lines of FONT_LABEL
        // data: CO2 saved by keeping the PC
//        JLabel noteText = label("<html><div style='width:370px'>Keeping this PC could avoid approximately <b>180 kg CO₂e</b> from manufacturing a replacement.</div></html>",
//                12, Font.PLAIN, TEXT_MAIN);
        JLabel noteText = label(
                "<html><div style='width:370px'>Keeping this PC could avoid approximately <b>180 kg CO₂e</b> from manufacturing a replacement.</div></html>",
                Font.PLAIN, FONT_LABEL, TEXT_MAIN
        );
        noteText.setIcon(loadIcon("icons/leaf_green.png", 14));   // leaf icon from figma
        noteText.setIconTextGap(8);
        note.add(noteText);
        recoCard.add(note);
        c.gridx = 1; c.weightx = 0.65; c.insets = new Insets(0, 8, 0, 0);
        panel.add(recoCard, c);
        return panel;
    }

    private static JPanel recommendationTabs(String active) {
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tabs.setOpaque(false);
        tabs.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        for (String option : new String[]{"KEEP", "UPGRADE", "REPLACE"}) {
//            JLabel tab = label(option, 11, Font.BOLD, option.equals(active) ? GREEN : TEXT_MUTED);
            JLabel tab = label(option, Font.BOLD, FONT_SMALL, option.equals(active) ? GREEN : TEXT_MUTED);
            tab.setOpaque(true);
            tab.setBackground(option.equals(active) ? GREEN_LIGHT : CARD_BG);
            tab.setBorder(new EmptyBorder(6, 12, 6, 12));
            tabs.add(tab);
        }
        return tabs;
    }

}
