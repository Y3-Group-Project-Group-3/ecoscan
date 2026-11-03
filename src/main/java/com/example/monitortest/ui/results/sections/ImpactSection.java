package com.example.monitortest.ui.results.sections;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;

import com.example.monitortest.ui.loading.components.RoundedPanel;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static com.example.monitortest.ui.results.components.StatCard.statCard;
import static java.awt.Component.LEFT_ALIGNMENT;



public class ImpactSection {

    // Labels displaying the selected power, energy consumption and emissions.
    private JLabel powerValue, powerUnit;
    private JLabel energyValue, energyUnit, co2Value, co2Unit, footprintTitle;
    private JToggleButton[] timeButtons;

    // Stores the latest hardware scan and selected power calculation mode.
    private HardwareInfo hardwareInfo;
    private boolean includeEstimates = false;

    // Shared calculator for estimated CO2e emissions.
    private final CarbonEmissionsCalculator calculator = new CarbonEmissionsCalculator();


    private String timescale = "Hour";

    // Notifies the Results page when the selected projection period changes.
    private Consumer<String> timescaleListener;

    // energy, CO2, lifespan + timescale buttons
    public JPanel createImpactSection() {
        JPanel section = column();

        // Build the main Scan Results heading.
        JPanel top = new JPanel(new BorderLayout(20, 0));
        top.setOpaque(false);

        JPanel titles = column();
        titles.setOpaque(false);

        // Small category label above the main heading.
        titles.add(label("HARDWARE ASSESSMENT", Font.BOLD, FONT_SMALL_SIZE, GREEN));
        titles.add(Box.createVerticalStrut(6));

        // Make Scan Results the main page heading.
        titles.add(label("Scan Results", Font.BOLD, 30, TEXT_MAIN));
        titles.add(Box.createVerticalStrut(6));

        // Briefly describe the information presented on this page.
        titles.add(label( "Your hardware power and environmental impact overview", Font.PLAIN, FONT_SMALL_SIZE, TEXT_MUTED ));

        top.add(titles, BorderLayout.WEST);

        // Initialise the component power labels before creating the cards.
        powerValue = label("", Font.BOLD, FONT_STAT_SIZE, TEXT_MAIN);
        powerUnit = label("W", Font.PLAIN, FONT_LABEL_SIZE, TEXT_MUTED);

        // timescale buttons (second / hour / day / month / year)
        JPanel toggle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toggle.setOpaque(false);
        toggle.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        String[] scales = {"Second", "Hour", "Day", "Month", "Year"};
        timeButtons = new JToggleButton[scales.length];
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < scales.length; i++) {
            String s = scales[i];
            JToggleButton b = new JToggleButton(s);
            b.setFont(font(Font.PLAIN, FONT_LABEL_SIZE));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(8, 16, 8, 16));
            b.setSelected(s.equals(timescale));

            // Change timescale buttons
            b.addActionListener(e -> {
                timescale = s;
                updateImpactValues();

                // Keep the emissions comparison synchronised with the impact cards.
                if (timescaleListener != null) {
                    timescaleListener.accept(timescale);
                }
            });

            group.add(b);
            timeButtons[i] = b;
            toggle.add(b);
        }

        // Create a rounded inset card for the scanned power reading.
        JPanel powerSummary = new RoundedPanel( new Color(0xF4F7FB), BORDER_COLOR,12 );
        powerSummary.setLayout(new BorderLayout(14, 0));
        powerSummary.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Create a softly coloured background for the power icon.
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(new Color(0xE3F4E9));
        iconPanel.setPreferredSize(new Dimension(48, 48));

        // Use EcoScan's existing Font Awesome 6 icon library.
        JLabel powerIcon = new JLabel( FontIcon.of(FontAwesomeSolid.BOLT, 20, GREEN) );
        iconPanel.add(powerIcon);

        // Stack the heading and wattage vertically.
        JPanel powerDetails = new JPanel();
        powerDetails.setLayout(new BoxLayout(powerDetails, BoxLayout.Y_AXIS));
        powerDetails.setOpaque(false);

        JLabel powerTitle = label( "SCANNED COMPONENT POWER", Font.BOLD, FONT_SMALL_SIZE, TEXT_MUTED );

        // Keep the wattage and unit together.
        JPanel powerReading = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        powerReading.setOpaque(false);

        powerReading.add(powerValue);
        powerReading.add(powerUnit);

        // Align the heading and reading to the left.
        powerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        powerReading.setAlignmentX(Component.LEFT_ALIGNMENT);

        powerDetails.add(powerTitle);
        powerDetails.add(Box.createVerticalStrut(6));
        powerDetails.add(powerReading);

        // Assemble the inset card.
        powerSummary.add(iconPanel, BorderLayout.WEST);
        powerSummary.add(powerDetails, BorderLayout.CENTER);

        // Keep the scanned component power on the right side of the header.
        top.add(powerSummary, BorderLayout.EAST);

        // Create a rounded header card using EcoScan's shared component.
        JPanel headerCard = new RoundedPanel(CARD_BG, BORDER_COLOR, 16);
        headerCard.setLayout(new BorderLayout());

        // Preserve the existing internal spacing.
        headerCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        headerCard.add(top, BorderLayout.CENTER);
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(headerCard);

        // Separate the header card from the projected impact section.
        section.add(Box.createVerticalStrut(28));

        // Place the projection heading and time-period selector together.
        JPanel projectionHeader = row();

        // The heading changes when the selected projection period changes.
        footprintTitle = label( "Projected impact per day", Font.BOLD, FONT_HEADING_SIZE, TEXT_MAIN );

        projectionHeader.add(footprintTitle, BorderLayout.WEST);

        // Position the time-period controls beside the projection heading.
        projectionHeader.add(toggle, BorderLayout.EAST);

        section.add(projectionHeader);
        section.add(Box.createVerticalStrut(14));



        energyValue = label("", Font.BOLD, FONT_STAT_SIZE, TEXT_MAIN);
        energyUnit = label("", Font.PLAIN, FONT_LABEL_SIZE, TEXT_MUTED);
        co2Value = label("", Font.BOLD, FONT_STAT_SIZE, TEXT_MAIN);
        co2Unit = label("", Font.PLAIN, FONT_LABEL_SIZE, TEXT_MUTED);

        // Display component power, energy consumption and CO₂e side by side.
        // Keep the two time-dependent projections side by side.
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(LEFT_ALIGNMENT);

        // data: energy consumption
        cards.add(statCard("⚡", new Color(0xEEF2FF), "ENERGY CONSUMPTION", energyValue, energyUnit, "Constant scan power"));
        // data: CO2 emissions (per year shown on the right)
        cards.add(statCard("☁", new Color(0xF3EEFF), "CO₂ EMISSIONS", co2Value, co2Unit, "Projected CO₂e"));

        section.add(cards);

        // Explain the assumptions behind the environmental projections.
        section.add(Box.createVerticalStrut(10));

        JLabel projectionNote = label(
                "<html>Projections assume continuous operation at the power measured during the scan. "
                        + "Actual energy consumption and CO₂e emissions will vary with workload. "
                        + "Values cover the included hardware components, not total wall-socket power.</html>",
                Font.PLAIN,
                FONT_SMALL_SIZE,
                TEXT_MUTED
        );

        projectionNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(projectionNote);

        updateImpactValues();
        return section;
    }

    // Registers a listener for changes to the selected timescale.
    public void setTimescaleListener(Consumer<String> listener) {
        this.timescaleListener = listener;
    }


    // Recalculates energy and CO2e projections for the selected timescale.
    private void updateImpactValues() {

        double durationSeconds;
        String per;

        switch (timescale) {
            case "Second" -> {
                durationSeconds = 1;
                per = "second";
            }
            case "Hour" -> {
                durationSeconds = 3600;
                per = "hour";
            }
            case "Month" -> {
                durationSeconds = 30 * 86400.0;
                per = "month";
            }
            case "Year" -> {
                durationSeconds = 365 * 86400.0;
                per = "year";
            }
            default -> {
                durationSeconds = 86400;
                per = "day";
            }
        }

        // A scan provides a power snapshot, not recorded energy usage.
        footprintTitle.setText("Projected impact per " + per);

        double powerWatts = getSelectedPower();

        // Display the scanned power independently of the selected projection period.
        if (Double.isFinite(powerWatts) && powerWatts >= 0) {
            powerValue.setText(String.format("%.1f", powerWatts));
            powerUnit.setText("W");
        } else {
            powerValue.setText("N/A");
            powerUnit.setText("");
        }

        if (!Double.isFinite(powerWatts) || powerWatts < 0) {
            energyValue.setText("N/A");
            energyUnit.setText("");
            co2Value.setText("N/A");
            co2Unit.setText("");
        } else {

            // Convert constant power consumption into projected energy usage.
            double energyKwh = powerWatts * durationSeconds / 3_600_000.0;

            // Use the same emissions calculator as the Live Monitor.
            double emissionsGrams = calculator.calculateEmissions(powerWatts, durationSeconds);

            if (energyKwh < 0.01) {
                energyValue.setText(format(energyKwh * 1000));
                energyUnit.setText("Wh/" + per);
            } else {
                energyValue.setText(format(energyKwh));
                energyUnit.setText("kWh/" + per);
            }

            if (emissionsGrams >= 1000) {
                co2Value.setText(format(emissionsGrams / 1000));
                co2Unit.setText("kg/" + per);
            } else {
                co2Value.setText(format(emissionsGrams));
                co2Unit.setText("g/" + per);
            }
        }

        // Preserve the existing appearance of the timescale buttons.
        for (JToggleButton button : timeButtons) {
            button.setBackground(button.isSelected() ? GREEN : CARD_BG);
            button.setForeground(button.isSelected() ? Color.WHITE : TEXT_MUTED);
        }
    }



    // Refreshes the environmental projections after a hardware scan.
    public void updateHardwareInfo(HardwareInfo info) {
        this.hardwareInfo = info;
        updateImpactValues();
    }

    // Changes whether estimated RAM and storage power are included.
    public void setIncludeEstimates(boolean includeEstimates) {
        this.includeEstimates = includeEstimates;
        updateImpactValues();
    }

    // Retrieves the power subtotal for the selected calculation mode.
    private double getSelectedPower() {

        if (hardwareInfo == null) {
            return Double.NaN;
        }

        return includeEstimates ? hardwareInfo.getCombinedComponentPower() : hardwareInfo.getTotalMeasuredPower();
    }

}
