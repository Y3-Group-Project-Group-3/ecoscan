package com.example.monitortest.ui.common;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



/**
 * Provides reusable informational tooltips for Swing components.
 *
 * Tooltips can contain a heading and a longer explanation.
 * Swing's built-in HTML support provides basic formatting.
 */
public final class InfoTooltip {

    // Utility class: prevent instances from being created.
    private InfoTooltip() {
    }

    // Applies EcoScan's shared styling to Swing tooltips. Call once when the application starts.
    public static void configureAppearance() {

        // Match the white cards and subtle borders used throughout EcoScan.
        Color background = Color.WHITE;
        Color foreground = new Color(15, 23, 42);
        Color borderColor = new Color(226, 232, 240);

        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        // Configure the default Swing tooltip appearance.
        UIManager.put("ToolTip.background", background);
        UIManager.put("ToolTip.foreground", foreground);
        UIManager.put("ToolTip.border", border);
        UIManager.put("ToolTip.font", new Font("SansSerif", Font.PLAIN, 12));

        // Configure how long tooltips remain visible.
        ToolTipManager tooltipManager = ToolTipManager.sharedInstance();
        // Wait 400 ms before displaying a tooltip.
        tooltipManager.setInitialDelay(400);
        // Keep the tooltip visible for up to 20 seconds.
        tooltipManager.setDismissDelay(20_000);
        // Show another tooltip quickly when moving between information icons.
        tooltipManager.setReshowDelay(200);
    }


    /**
     * Attaches an informational tooltip to a Swing component.
     *
     * @param component Component that displays the tooltip on hover.
     * @param title Heading displayed at the top of the tooltip.
     * @param description Explanation displayed below the heading.
     */
    public static void attach( JComponent component, String title, String description) {

    // Build a formatted tooltip with a consistent width.
    // Format the tooltip heading and explanation.
        String tooltip = """
        <html>
        <div style='width: 280px;'>
            <span style='color: #16894C;'>
                <b>%s</b>
            </span>
            <p style='color: #64748B;'>
                %s
            </p>
        </div>
        </html>
        """.formatted(title, description);


        component.setToolTipText(tooltip);


        // Indicate that this component provides additional information.
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Apply hover styling to text labels.
        if (component instanceof JLabel label) {

            // Preserve the label's original appearance.
            Color originalColor = label.getForeground();

            // EcoScan's green accent colour.
            Color hoverColor = new Color(22, 137, 76);

            // Underline the label to indicate that more information is available.
//            label.setText("<html><u>" + label.getText() + "</u></html>");

            label.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setForeground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setForeground(originalColor);
                }
            });
        }

    }
}
