package com.example.monitortest.ui.results.components;

import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.common.InfoIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static java.awt.Component.LEFT_ALIGNMENT;

public class StatCard {


    // Preserves existing cards that do not require an information tooltip.
    public static JPanel statCard(
            String icon, Color iconBg, String title,
            JLabel value, JLabel unit, String sideText) {

        return createStatCard(icon, iconBg, title, value, unit, sideText, null, null);
    }

    // Builds the shared card layout, including an optional information icon.
    private static JPanel createStatCard(
            String icon, Color iconBg, String title,
            JLabel value, JLabel unit, String sideText,
            String tooltipTitle, String tooltipDescription) {

        // Create a rounded statistic card using EcoScan's shared component.
        JPanel card = new RoundedPanel(CARD_BG, BORDER_COLOR, 16);
        // Preserve the existing card layout.
        card.setLayout(new BorderLayout());
        // Preserve spacing between the card contents and its rounded border.
        card.setBorder(new EmptyBorder(24, 28, 24, 28));


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

        // Arrange the title, description and power/statistic value vertically.
        JPanel text = column();

        // Arrange the title and optional information icon horizontally.
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = label(title, Font.BOLD, AppTheme.FONT_BADGE_SIZE, TEXT_MUTED);
        titleRow.add(titleLabel);

        // Display the information icon only when tooltip content is supplied.
        if (tooltipTitle != null && tooltipDescription != null) {
            titleRow.add(new InfoIcon(tooltipTitle, tooltipDescription));
        }

        text.add(titleRow);

        // Display the description directly beneath the title.
        text.add(Box.createVerticalStrut(4));

        // Keep the description and optional information icon on one line.
        JPanel descriptionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        descriptionRow.setOpaque(false);
        descriptionRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descriptionLabel = label( sideText, Font.PLAIN, AppTheme.FONT_SMALL_SIZE, TEXT_MUTED );

        descriptionRow.add(descriptionLabel);

        // Display the CO₂e information icon beside the description.
        if (tooltipTitle != null && tooltipDescription != null) {
            descriptionRow.add(new InfoIcon(tooltipTitle, tooltipDescription));
        }

        text.add(descriptionRow);

        // Add spacing before the main statistic.
        text.add(Box.createVerticalStrut(8));

        // Keep the statistic and its unit together on one line.
        JPanel valueLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valueLine.setOpaque(false);
        valueLine.setAlignmentX(LEFT_ALIGNMENT);

        valueLine.add(value);
        valueLine.add(unit);

        text.add(valueLine);

        // Position the information column to the right of the icon.
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // Creates a statistic card with an optional information tooltip.
    public static JPanel statCard(
            String icon, Color iconBg, String title,
            JLabel value, JLabel unit, String sideText,
            String tooltipTitle, String tooltipDescription) {

        return createStatCard(
                icon, iconBg, title, value, unit, sideText,
                tooltipTitle, tooltipDescription
        );
    }

}
