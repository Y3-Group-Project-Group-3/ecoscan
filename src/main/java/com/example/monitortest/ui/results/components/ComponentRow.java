package com.example.monitortest.ui.results.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.label;

public class ComponentRow {


    // Creates a standard component row without any additional details.
    public static JPanel componentRow(
            JPanel list,
            String name,
            String summary,
            String status,
            String spec,
            String impact,
            String age,
            String assessment,
            boolean openAtStart
    ) {
        // Delegate to the extended method without an additional details panel.
        return componentRow(
                list, name, summary, status,
                spec, impact, age, assessment,
                openAtStart, null
        );
    }



    // Creates an expandable component row with optional additional details.
    public static JPanel componentRow(
            JPanel list,
            String name,
            String summary,
            String status,
            String spec,
            String impact,
            String age,
            String assessment,
            boolean openAtStart,
            JPanel additionalDetails
    ) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);

        // Keep a subtle divider between components without outlining each row.
        item.setBorder(BorderFactory.createMatteBorder( 0, 0, 1, 0, BORDER_COLOR ));

        // Give each component header a consistent white background.
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        header.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

//        JLabel nameLabel = label(name, 14, Font.BOLD, TEXT_MAIN);
        JLabel nameLabel = label(name, Font.BOLD, FONT_CARD_TITLE_SIZE, TEXT_MAIN);

        nameLabel.setPreferredSize(new Dimension(220, 24));   // sized to fit FONT_CARD_TITLE
        left.add(nameLabel);
//        left.add(label(summary, 13, Font.PLAIN, TEXT_MUTED));
        left.add(label(summary, Font.PLAIN, FONT_BODY_SIZE, TEXT_MUTED));
        header.add(left, BorderLayout.WEST);

        // Use Font Awesome icons for expanding and collapsing rows.
        JLabel arrow = new JLabel(
                FontIcon.of( openAtStart ? FontAwesomeSolid.CHEVRON_UP : FontAwesomeSolid.CHEVRON_DOWN, 14, TEXT_MUTED )
        );

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Only create a badge when a meaningful status is provided.
        if (status != null && !status.isBlank() && !status.equals("-")) {
            right.add(StatusBadge.badge(status));
        }

        // Always retain the expand/collapse arrow.
        right.add(arrow);

        header.add(right, BorderLayout.EAST);
        item.add(header, BorderLayout.NORTH);

        // details (hidden until clicked)
        JPanel details = new JPanel(new GridLayout(2, 4, 20, 4));

        // Use a subtle background and divider for expanded specifications.
        details.setBackground(new Color(0xF8FAF9));

        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 18, 14, 18)
        ));


        // Display hardware specifications and power measurements.
        details.add(label("Detected specifications", Font.PLAIN, FONT_SMALL_SIZE, TEXT_MUTED));
        details.add(label("Power at scan", Font.PLAIN, FONT_SMALL_SIZE, TEXT_MUTED));
        details.add(label("Additional information", Font.PLAIN, FONT_SMALL_SIZE, TEXT_MUTED));
        // Covers both measured power shares and estimated power contributions.
        details.add(label("Power contribution", Font.PLAIN, FONT_SMALL_SIZE, TEXT_MUTED));


        details.add(label(spec, Font.BOLD, FONT_BODY_SIZE, TEXT_MAIN));
        details.add(label(impact, Font.BOLD, FONT_BODY_SIZE, TEXT_MAIN)); // data: power use (W) of this part
        details.add(label(age, Font.BOLD, FONT_BODY_SIZE, TEXT_MAIN));
        details.add(label(assessment, Font.BOLD, FONT_BODY_SIZE, TEXT_MAIN));


        // Group the standard specifications and optional extra details together.
        JPanel expandableContent = new JPanel();
        expandableContent.setLayout(new BoxLayout( expandableContent, BoxLayout.Y_AXIS ));
        expandableContent.setOpaque(false);

        // Keep the existing four-column specification grid.
        details.setAlignmentX(Component.LEFT_ALIGNMENT);
        expandableContent.add(details);

        // Add component-specific details beneath the standard grid.
        if (additionalDetails != null) {
            additionalDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
            expandableContent.add(additionalDetails);
        }

        // Expand or collapse both sections as a single unit.
        expandableContent.setVisible(openAtStart);
        item.add(expandableContent, BorderLayout.CENTER);


        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Toggle the complete details area, including any additional rows.
                expandableContent.setVisible(!expandableContent.isVisible());

                // Update the arrow to reflect the expanded state.
                arrow.setIcon(FontIcon.of(
                        expandableContent.isVisible() ? FontAwesomeSolid.CHEVRON_UP : FontAwesomeSolid.CHEVRON_DOWN, 14, TEXT_MUTED
                ));

                list.revalidate();
                list.repaint();
            }
        });
        return item;
    }
}
