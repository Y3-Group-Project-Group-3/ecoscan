package com.example.monitortest.ui.results.breakdown;

import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.hardware.model.HardwareInfo;
import java.util.function.Consumer;

import javax.swing.*;
import java.awt.*;

/**
 * Displays measured and estimated component power totals.
 * Manages the selected power calculation mode.
 */
public class PowerSummaryPanel extends JPanel {

    private HardwareInfo currentHardwareInfo;
    private boolean includeEstimates = false;
    // Notifies other components when the power calculation mode changes.
    private Consumer<Boolean> powerModeListener;

    private final JLabel totalPowerLabel;

    // Holds the power mode controls so they can be displayed in the fixed toolbar.
    private final JPanel togglePanel;


    // Creates the power summary with its calculation mode controls.
    public PowerSummaryPanel() {


        // Arrange the power controls vertically.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Keep this panel aligned with the left edge of its parent.
        setAlignmentX(Component.LEFT_ALIGNMENT);


        // Create the power calculation mode controls.
        JRadioButton measuredOnlyButton = new JRadioButton("Measured only", true);
        JRadioButton combinedButton = new JRadioButton("Measured + estimates");

        // Apply the initial appearance of both power modes.
        stylePowerModeButton(measuredOnlyButton, true);
        stylePowerModeButton(combinedButton, false);

        // Ensure only one calculation mode can be selected.
        ButtonGroup powerModeGroup = new ButtonGroup();
        powerModeGroup.add(measuredOnlyButton);
        powerModeGroup.add(combinedButton);

        // Keep the two options close together for a segmented appearance.
        togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

        togglePanel.setOpaque(false);
        togglePanel.add(measuredOnlyButton);
        togglePanel.add(combinedButton);
        // Keep the radio buttons aligned with the left edge.
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create the total power display.
        totalPowerLabel = new JLabel( "Total Measured Power: Not retrieved yet" );
        totalPowerLabel.setFont( totalPowerLabel.getFont().deriveFont(Font.BOLD, AppTheme.FONT_CARD_TITLE_SIZE));

        // Update the summary when the selected mode changes.
        measuredOnlyButton.addActionListener(e -> {
            includeEstimates = false;
            // Highlight the newly selected mode.
            stylePowerModeButton(measuredOnlyButton, true);
            stylePowerModeButton(combinedButton, false);
            updatePowerSummary();

            // Notify the Results page that measured-only mode was selected.
            if (powerModeListener != null) {
                powerModeListener.accept(false);
            }
        });

        combinedButton.addActionListener(e -> {
            includeEstimates = true;
            // Highlight the newly selected mode.
            stylePowerModeButton(measuredOnlyButton, false);
            stylePowerModeButton(combinedButton, true);
            updatePowerSummary();

            // Notify the Results page that estimates are now included.
            if (powerModeListener != null) {
                powerModeListener.accept(true);
            }
        });

        // Prevent the toggle controls from taking unnecessary vertical space.
        togglePanel.setMaximumSize(new Dimension( Integer.MAX_VALUE, togglePanel.getPreferredSize().height ));



        add(togglePanel);
        add(Box.createVerticalStrut(8));
        add(totalPowerLabel);

    }


    // Updates the power summary using the latest hardware scan.
    public void updateHardwareInfo(HardwareInfo info) {

        currentHardwareInfo = info;
        updatePowerSummary();
    }

    //Displays the appropriate power subtotal and completeness label.
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
            String powerLabel = completeMeasuredPower ? "Total Measured Power" : "Partial Measured Power";
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

    // Registers a listener for changes to the power calculation mode.
    public void setPowerModeListener(Consumer<Boolean> listener) {
        this.powerModeListener = listener;
    }

    // Returns the existing power mode controls for placement in the Results toolbar.
    public JPanel getPowerModeControls() {
        return togglePanel;
    }

    // Applies a rounded, highlighted appearance to each power mode button.
    private void stylePowerModeButton(JRadioButton button, boolean selected) {

        button.setFocusPainted(false);
        // Display a hand cursor when hovering over the power mode buttons.
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        // Remove the default radio indicator.
        button.setIcon(new ImageIcon());
        button.setSelectedIcon(new ImageIcon());

        // Keep the selected text white and the inactive text dark.
        button.setForeground(selected ? Color.WHITE : AppTheme.TEXT_MAIN);

        // Add padding without drawing the default Swing border.
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        // Paint a rounded background behind the button text.
        button.setUI(new javax.swing.plaf.basic.BasicRadioButtonUI() {

            @Override
            public void paint(Graphics g, JComponent c) {

                Graphics2D g2 = (Graphics2D) g.create();

                // Enable smooth rounded corners.
                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

                // Use green for the selected mode and a subtle grey otherwise.
                g2.setColor(selected ? AppTheme.GREEN : new Color(235, 239, 245));
                g2.fillRoundRect( 0, 0, c.getWidth(), c.getHeight(), 14, 14 );
                g2.dispose();

                // Allow Swing to draw the button text.
                super.paint(g, c);
            }
        });
    }
}
