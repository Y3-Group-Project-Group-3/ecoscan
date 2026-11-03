package com.example.monitortest.ui.results.sections;

import com.example.monitortest.ui.results.breakdown.ComponentSectionUpdater;
import com.example.monitortest.ui.results.breakdown.PowerSummaryPanel;
import com.example.monitortest.hardware.model.HardwareInfo;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.BORDER_COLOR;
import static com.example.monitortest.ui.theme.UIUtility.column;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static com.example.monitortest.ui.results.components.ComponentRow.componentRow;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ComponentSection {

    // Reference to the existing Component Breakdown list.
    private static JPanel componentList;
    // Track component rows by name instead of their position.
    private static final Map<String, JPanel> componentRows = new HashMap<>();

    // Manages the power calculation mode and displayed power totals.
    private static PowerSummaryPanel powerSummaryPanel;

    // Holds the updater responsible for refreshing the Component Breakdown.
    private static ComponentSectionUpdater componentSectionUpdater;


    // Create the Component Breakdown before scan results are available.
    public static JPanel createComponentSection() {
        return createComponentSection(null);
    }


    // list of components, click a row to open details
    // Build the component breakdown using the supplied scan results.
    public static JPanel createComponentSection(HardwareInfo info) {
        JPanel section = column();
        section.add(label("COMPONENT BREAKDOWN", Font.BOLD, FONT_LABEL_SIZE, GREEN));
        section.add(Box.createVerticalStrut(4));
        section.add(label("Hardware assessment", Font.BOLD, FONT_HEADING_SIZE, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        JPanel list = new JPanel();
        // Keep a reference so the rows can be refreshed after scanning.
        componentList = list;
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD_BG);
        list.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        list.setAlignmentX(LEFT_ALIGNMENT);


        /******************** CPU **********************************/
        // Initial CPU placeholders; actual values are supplied by the updater.
        String cpuName = "Not retrieved yet";
        String cpuPowerText = "Not available";
        String cpuShareText = "Not available";

        JPanel cpuRow = componentRow(
                list, "CPU", cpuName, "MEASURED", cpuName, cpuPowerText, "—", cpuShareText, false);
        list.add(cpuRow);
        componentRows.put("CPU", cpuRow);

        /******************** GPU **********************************/
        // Initial GPU placeholders; actual values are supplied by the updater.
        String gpuName = "Not retrieved yet";
        String gpuPowerText = "Not available";
        String gpuShareText = "Not available";

        JPanel gpuRow = componentRow(
                list, "GPU", gpuName, "MEASURED", gpuName, gpuPowerText, "—", gpuShareText, false
        );
        list.add(gpuRow);
        componentRows.put("GPU", gpuRow);

        /******************** RAM **********************************/
        // Create the RAM row with placeholders until a scan completes.
        JPanel ramRow = componentRow(
                list, "RAM", "Not retrieved yet", "EFFICIENT", "Not retrieved yet", "Not available", "—", "Not available", false
        );
        list.add(ramRow);
        componentRows.put("RAM", ramRow);

        /******************** Storage **********************************/
        // Create storage row with placeholders until a scan completes
        JPanel storageRow = componentRow(
                list, "Storage", "Not retrieved yet", "ESTIMATED", "Not retrieved yet", "Not measured", "—", "—", false
        );
        list.add(storageRow);
        componentRows.put("Storage", storageRow);

        // Add the expandable component rows.
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
    public static void updateComponentSection(HardwareInfo info) {

        // Preserve the existing public interface used by ScanEventListener.
        if (componentSectionUpdater != null && info != null) {
            componentSectionUpdater.updateComponentSection(info);
        }
    }

    // Allows the Results page to respond to power calculation mode changes.
    public static void setPowerModeListener(Consumer<Boolean> listener) {
        if (powerSummaryPanel != null) {
            powerSummaryPanel.setPowerModeListener(listener);
        }
    }

    // Provides access to the existing power mode controls.
    // Allows ResultsPanel to display them in its fixed toolbar.
    public static JPanel getPowerModeControls() {

        if (powerSummaryPanel == null) {
            return null;
        }

        return powerSummaryPanel.getPowerModeControls();
    }

}