package com.example.monitortest.views;

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;
import java.awt.geom.*;
import com.example.monitortest.theme.AppTheme;
import com.example.monitortest.theme.UIUtility;
import com.example.monitortest.components.*;


public class HomePanelNew extends JPanel {

    private JButton scanButton;
    private String selectedUse = "Home";

    // temp history page button
    private JButton historyButton;


    public HomePanelNew() {
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

        // ---- Heading block ----
        column.add(UIUtility.label("HARDWARE ASSESSMENT", Font.BOLD, 10, AppTheme.GREEN));
        column.add(Box.createVerticalStrut(8));
        column.add(UIUtility.label("Scan your computer", Font.BOLD, 30, AppTheme.TEXT_MAIN));
        column.add(Box.createVerticalStrut(12));
        // HTML lets the text wrap and centre inside a fixed width
        column.add(UIUtility.label("<html><div style='width:440px; text-align:center;'>EcoScan finds your hardware for you, "
                + "nothing to type in. It analyses your computer's environmental impact and tells you whether "
                + "to keep, upgrade or replace it.</div></html>", Font.PLAIN, 12.5f, AppTheme.TEXT_MUTED));

        // ---- "What do you use it for?" cards ----
        column.add(Box.createVerticalStrut(32));
        column.add(UIUtility.label("What do you use it for?", Font.BOLD, 12, AppTheme.TEXT_MAIN));
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
        column.add(Box.createVerticalStrut(24));
        scanButton = new PrimaryButton("Scan Computer", new LineIcon("SCAN", 15, Color.WHITE));
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//        scanButton.addActionListener(e -> showScan());
        column.add(scanButton);

        // ---- Privacy note ----
        column.add(Box.createVerticalStrut(14));
        JLabel privacy = UIUtility.label("Only hardware and system information is read. No personal files are accessed.",
                Font.PLAIN, 10.5f, AppTheme.TEXT_MUTED);
        privacy.setIcon(new LineIcon("SHIELD", 13, AppTheme.GREEN));
        privacy.setIconTextGap(6);
        column.add(privacy);

        // ---- "View scan history ->" link ----
        historyButton = new JButton("View Scan History");
        historyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        column.add(Box.createVerticalStrut(15));
        column.add(historyButton);

        // Place the column at the top-centre of the page
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(56, 32, 32, 32);
        page.add(column, c);
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