package com.example.monitortest.ui.home;

import javax.swing.*;
import java.awt.*;

import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.common.LineIcon;
import com.example.monitortest.ui.common.PrimaryButton;
import com.example.monitortest.ui.common.ScrollablePanel;
import com.example.monitortest.ui.common.UsageCard;


public class HomePanel extends JPanel {

    private JButton scanButton;
    private String selectedUse = "Home";

    // temp history page button
    private JButton historyButton;

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF4F6F9));

        JComponent homePage = createHomePage();

        add(homePage, BorderLayout.CENTER);
    }

    private JComponent createHomePage() {
        // Background panel that also paints the two faint decorative circles
        HomeBackground page = new HomeBackground();

        // Everything is stacked in one centred column
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        //Debugging
        column.setBackground(AppTheme.TEXT_MAIN);

        // ---- Heading block ----
        // --- hardware assessment label
        JLabel infoLabel = UIUtility.label("HARDWARE ASSESSMENT", Font.BOLD, AppTheme.FONT_LABEL, AppTheme.GREEN);
        column.add(UIUtility.alignComponent(infoLabel, FlowLayout.CENTER));

        // --- Scan yo computer label
        column.add(Box.createVerticalStrut(8));
        JLabel pageTitle = UIUtility.label("Scan your computer", Font.BOLD, AppTheme.FONT_TITLE, AppTheme.TEXT_MAIN);
        column.add(UIUtility.alignComponent(pageTitle, FlowLayout.CENTER));

        // --- paragraph text
        column.add(Box.createVerticalStrut(12));
        // HTML lets the text wrap and centre inside a fixed width
        JLabel paragraphLabel =  UIUtility.label("<html><div style='width:440px; text-align:center;'>EcoScan finds your hardware for you, "
                + "nothing to type in. It analyses your computer's environmental impact and tells you whether "
                + "to keep, upgrade or replace it.</div></html>", Font.PLAIN, AppTheme.FONT_BODY, AppTheme.TEXT_MUTED);
        column.add(UIUtility.alignComponent(paragraphLabel, FlowLayout.CENTER));

        // ---- "What do you use it for?" cards ----
        column.add(Box.createVerticalStrut(32));
        JLabel questionLabel = UIUtility.label("What do you use it for?", Font.BOLD, AppTheme.FONT_LABEL, AppTheme.TEXT_MAIN);
        column.add(UIUtility.alignComponent(questionLabel, FlowLayout.CENTER));
        column.add(Box.createVerticalStrut(12));

        String[][] options = {
                {"Home", "Browsing & everyday tasks", "HOME"},
                {"Office", "Documents & productivity", "BRIEFCASE"},
                {"Gaming", "Games & entertainment", "GAMEPAD"},
                {"Creative", "Design, photo & video", "PENCIL"},
        };
        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        cardRow.setOpaque(false);
        ButtonGroup useGroup = new ButtonGroup();   // only one card can be selected at a time
        for (String[] option : options) {
            UsageCard card = new UsageCard(option[0], option[1], option[2]);
            card.setSelected(option[0].equals(selectedUse));
            card.addActionListener(e -> selectedUse = card.getTitle());   // remember the choice
            useGroup.add(card);
            cardRow.add(card);
        }
        column.add(cardRow);

        // ---- Main action ----
        // Kept close to the cards (20 px) so the button reads as the next step after choosing a usage type
        column.add(Box.createVerticalStrut(20));
        scanButton = new PrimaryButton("Scan Computer", new LineIcon("SCAN", 15, Color.WHITE));

        // Fixed size makes the button more prominent and keeps it in exactly the same place,
        // whichever usage card is selected (the cards only repaint, they never resize)
        Dimension scanButtonSize = new Dimension(260, 50);
        scanButton.setPreferredSize(scanButtonSize);
        scanButton.setMinimumSize(scanButtonSize);
        scanButton.setMaximumSize(scanButtonSize);

        // Centred directly beneath the four usage cards
        column.add(UIUtility.alignComponent(scanButton, FlowLayout.CENTER));

        // ---- Privacy note ----
        column.add(Box.createVerticalStrut(14));
        JLabel privacy = UIUtility.label("Only hardware and system information is read. No personal files are accessed.", Font.PLAIN, AppTheme.FONT_SMALL, AppTheme.TEXT_MUTED);
        privacy.setIcon(new LineIcon("SHIELD", 13, AppTheme.GREEN));
        privacy.setIconTextGap(6);
        column.add(UIUtility.alignComponent(privacy,FlowLayout.CENTER));

        // ---- "View scan history ->" link ----
        historyButton = new JButton("View Scan History");
        column.add(UIUtility.alignComponent(historyButton, FlowLayout.CENTER));

        // Place the column at the top-centre of the page.
        // The top inset (80 px, previously 56 px) moves the whole group slightly lower for better visual balance
        // without changing how its elements line up with each other.
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(80, 32, 32, 32);
        page.add(column, c);


        // Debugging - shows every element with border, useful for alignment
//        UIUtility.showDebugBorders(page);

        return page;
    }

    public JButton getScanButton() {
        return scanButton;
    }

    // TODO Move to separate class
    /** Home page background: plain colour plus two faint circles (top-right and bottom-left). */
    static class HomeBackground extends ScrollablePanel {
        HomeBackground() {
            super(new GridBagLayout());
            setBackground(AppTheme.BG_COLOR);
        }
    }
    public JButton getHistoryButton() {
        return historyButton;
    }
}