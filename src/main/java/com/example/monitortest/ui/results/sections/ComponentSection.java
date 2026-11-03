package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.results.breakdown.ComponentSectionUpdater;
import com.example.monitortest.ui.results.breakdown.PowerSummaryPanel;
import com.example.monitortest.ui.loading.components.RoundedPanel;
import com.example.monitortest.hardware.model.HardwareInfo;


import javax.swing.*;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.column;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ComponentSection {

    // Reference to the Component Breakdown list.
    private JPanel componentList;

    // Track component rows by name instead of position.
    private final Map<String, JPanel> componentRows = new HashMap<>();

    // Manages power calculation controls and totals.
    private PowerSummaryPanel powerSummaryPanel;

    // Updates the displayed hardware components.
    private ComponentSectionUpdater componentSectionUpdater;



    // Builds the initial Component Breakdown with placeholder values.
    // Hardware information is supplied separately after a scan completes.
    public JPanel createComponentSection() {

        JPanel section = column();
        section.add(label("COMPONENT BREAKDOWN", Font.BOLD, FONT_LABEL_SIZE, GREEN));
        section.add(Box.createVerticalStrut(4));
        section.add(label("Hardware assessment", Font.BOLD, FONT_HEADING_SIZE, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        // Clip the expandable rows to the rounded container.
        JPanel list = new RoundedPanel(CARD_BG, BORDER_COLOR, 16) {

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Prevent expanded row backgrounds from covering rounded corners.
                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

                g2.clip(new java.awt.geom.RoundRectangle2D.Double( 0, 0, getWidth(), getHeight(), 16, 16 ));

                super.paintChildren(g2);
                g2.dispose();
            }
        };

        // Keep a reference so the rows can be refreshed after scanning.
        componentList = list;
        // Remove references to any rows from a previous construction.
        componentRows.clear();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(LEFT_ALIGNMENT);

        // Keep a reference to the list for future hardware updates.
        componentList = list;

        // Configure the component container before the first scan.
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(LEFT_ALIGNMENT);

        // Remove any references to previously constructed component rows.
        componentRows.clear();

        // Add the initially empty component list to the section.
        // The updater will populate it after a successful scan.
        section.add(list);

        /******************** Power Summary  **********************************/
        // Create the power summary and align it with the component list.
        powerSummaryPanel = new PowerSummaryPanel();

        // Match the horizontal alignment of the component breakdown.
        powerSummaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add spacing between the component rows and the power summary.
        section.add(Box.createVerticalStrut(16));
        section.add(powerSummaryPanel);

        // Connect the updater to the existing component rows and power summary.
        componentSectionUpdater = new ComponentSectionUpdater(componentList, componentRows, powerSummaryPanel);

        /******************** END **********************************/
        return section;
    }


    // Passes completed hardware scan results to the Component Breakdown updater.
    public void updateComponentSection(HardwareInfo info) {

        // Preserve the existing public interface used by ScanEventListener.
        if (componentSectionUpdater != null && info != null) {
            componentSectionUpdater.updateComponentSection(info);
        }
    }

    // Allows the Results page to respond to power calculation mode changes.
    public void setPowerModeListener(Consumer<Boolean> listener) {
        if (powerSummaryPanel != null) {
            powerSummaryPanel.setPowerModeListener(listener);
        }
    }

    // Provides access to the existing power mode controls.
    // Allows ResultsPanel to display them in its fixed toolbar.
    public JPanel getPowerModeControls() {

        if (powerSummaryPanel == null) {
            return null;
        }

        return powerSummaryPanel.getPowerModeControls();
    }

}