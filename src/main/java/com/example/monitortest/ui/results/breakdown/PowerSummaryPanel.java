package com.example.monitortest.ui.results.breakdown;

import com.example.monitortest.hardware.model.HardwareInfo;

import javax.swing.*;
import java.awt.*;

/**
 * Displays measured and estimated component power totals.
 * Manages the selected power calculation mode.
 */
public class PowerSummaryPanel extends JPanel {

    private HardwareInfo currentHardwareInfo;

    private boolean includeEstimates = false;

    private final JLabel totalPowerLabel;



    /**
     * Creates the power summary with its calculation mode controls.
     */
    public PowerSummaryPanel() {


        // Arrange the power controls vertically.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Keep this panel aligned with the left edge of its parent.
        setAlignmentX(Component.LEFT_ALIGNMENT);


        // Create the power calculation mode controls.
        JRadioButton measuredOnlyButton = new JRadioButton("Measured only", true);
        JRadioButton combinedButton = new JRadioButton("Measured + estimates");

        measuredOnlyButton.setOpaque(false);
        combinedButton.setOpaque(false);

        // Ensure only one calculation mode can be selected.
        ButtonGroup powerModeGroup = new ButtonGroup();
        powerModeGroup.add(measuredOnlyButton);
        powerModeGroup.add(combinedButton);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

        togglePanel.setOpaque(false);
        togglePanel.add(measuredOnlyButton);
        togglePanel.add(combinedButton);
        // Keep the radio buttons aligned with the left edge.
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create the total power display.
        totalPowerLabel = new JLabel( "Total Measured Power: Not retrieved yet" );
        totalPowerLabel.setFont( totalPowerLabel.getFont().deriveFont(Font.BOLD, 14f));

        // Update the summary when the selected mode changes.
        measuredOnlyButton.addActionListener(e -> {
            includeEstimates = false;
            updatePowerSummary();
        });

        combinedButton.addActionListener(e -> {
            includeEstimates = true;
            updatePowerSummary();
        });

        // Prevent the toggle controls from taking unnecessary vertical space.
        togglePanel.setMaximumSize(new Dimension( Integer.MAX_VALUE, togglePanel.getPreferredSize().height ));

        add(togglePanel);
        add(Box.createVerticalStrut(8));
        add(totalPowerLabel);

    }

    /**
     * Updates the power summary using the latest hardware scan.
     */
    public void updateHardwareInfo(HardwareInfo info) {

        currentHardwareInfo = info;
        updatePowerSummary();
    }

    /**
     * Displays the appropriate power subtotal and completeness label.
     */
    private void updatePowerSummary() {

        if (currentHardwareInfo == null) {
            return;
        }

        double measuredPower = currentHardwareInfo.getTotalMeasuredPower();
        boolean completeMeasuredPower = currentHardwareInfo.hasCompleteMeasuredPower();

        // Measured-only mode requires at least one valid sensor reading.
        if (!includeEstimates) {

            if (!Double.isFinite(measuredPower)) {
                totalPowerLabel.setText("Measured Power: Not available");
                return;
            }

            String powerLabel = completeMeasuredPower
                    ? "Total Measured Power"
                    : "Partial Measured Power";

            totalPowerLabel.setText(String.format( "%s: %.1f W", powerLabel, measuredPower ));
            return;
        }

        // Combined mode includes available CPU/GPU readings and RAM/Storage estimates.
        double combinedPower = currentHardwareInfo.getCombinedComponentPower();

        if (!Double.isFinite(combinedPower)) {
            totalPowerLabel.setText("Component Power: Not available");
            return;
        }

        // Determine whether all supported component categories are covered.
        boolean completeEstimate =
                completeMeasuredPower && currentHardwareInfo.hasCompleteRamPowerEstimate() && currentHardwareInfo.hasCompleteStoragePowerEstimate();

        String powerLabel = completeEstimate ? "Combined Estimated Component Power" : "Partial Combined Component Power";

        totalPowerLabel.setText(String.format( "%s: %.1f W", powerLabel, combinedPower ));
    }
}
