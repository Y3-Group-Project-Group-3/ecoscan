package com.example.monitortest.ui.results.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.AppTheme.FONT_FAMILY;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MAIN;
import static com.example.monitortest.ui.theme.AppTheme.TEXT_MUTED;
import static com.example.monitortest.ui.theme.UIUtility.*;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static com.example.monitortest.ui.results.components.StatCard.statCard;
import static java.awt.Component.LEFT_ALIGNMENT;

public class ImpactSection {

    private JLabel energyValue, energyUnit, co2Value, co2Unit, footprintTitle;
    private JToggleButton[] timeButtons;

    // test values - will come from the calculation class
    private double energyKwhPerDay = 2.76;   // energy consumption (kWh per day)
    private double co2GramsPerDay = 571;     // CO2 emissions (g per day)
    private String timescale = "Day";

    // energy, CO2, lifespan + timescale buttons
    public JPanel createImpactSection() {
        JPanel section = column();

        JPanel top = row();
        JPanel titles = column();
//        titles.add(label("ENVIRONMENTAL IMPACT", 12, Font.BOLD, GREEN));
        titles.add(label("ENVIRONMENTAL IMPACT", Font.BOLD, 12, GREEN));
        titles.add(Box.createVerticalStrut(4));
//        footprintTitle = label("Your daily footprint", 20, Font.BOLD, TEXT_MAIN);
        footprintTitle = label("Your daily footprint", Font.BOLD, 20, TEXT_MAIN);
        titles.add(footprintTitle);
        top.add(titles, BorderLayout.WEST);

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
            b.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(8, 16, 8, 16));
            b.setSelected(s.equals(timescale));
            // when clicked - recalculate energy + CO2 for this timescale
            b.addActionListener(e -> { timescale = s; updateImpactValues(); });
            group.add(b);
            timeButtons[i] = b;
            toggle.add(b);
        }
        top.add(wrapBottom(toggle), BorderLayout.EAST);
        section.add(top);
        section.add(Box.createVerticalStrut(14));

        energyValue = label("", Font.BOLD, 26, TEXT_MAIN);
        energyUnit = label("", Font.PLAIN, 12, TEXT_MUTED);
        co2Value = label("", Font.BOLD, 26, TEXT_MAIN);
        co2Unit = label("", Font.PLAIN, 12, TEXT_MUTED);

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(LEFT_ALIGNMENT);
        // data: energy consumption
        cards.add(statCard("⚡", new Color(0xEEF2FF), "ENERGY CONSUMPTION", energyValue, energyUnit, "Typical use"));
        // data: CO2 emissions (per year shown on the right)
        cards.add(statCard("☁", new Color(0xF3EEFF), "CO₂ EMISSIONS", co2Value, co2Unit, "209 kg/year"));
        // data: estimated lifespan
        cards.add(statCard("📅", new Color(0xFFF4E8), "ESTIMATED LIFESPAN",
                label("3–5", Font.BOLD, 26, TEXT_MAIN),
                label("years", Font.PLAIN, 12, TEXT_MUTED), ""));
        section.add(cards);

        updateImpactValues();
        return section;
    }

    // changes energy + CO2 numbers when a timescale button is pressed
    // (maybe move this maths into the calculation class later)
    private void updateImpactValues() {
        double factor;
        String per;
        switch (timescale) {
            case "Second": factor = 1.0 / 86400; per = "second"; break;
            case "Hour":   factor = 1.0 / 24;    per = "hour";   break;
            case "Month":  factor = 30;          per = "month";  break;
            case "Year":   factor = 365;         per = "year";   break;
            default:       factor = 1;           per = "day";
        }
        footprintTitle.setText("Your " + (per.equals("day") ? "daily" : per + "ly") + " footprint");

        double kwh = energyKwhPerDay * factor;
        double grams = co2GramsPerDay * factor;
        if (kwh < 0.01) { energyValue.setText(format(kwh * 1000)); energyUnit.setText("Wh/" + per); }
        else            { energyValue.setText(format(kwh));        energyUnit.setText("kWh/" + per); }
        if (grams >= 1000) { co2Value.setText(format(grams / 1000)); co2Unit.setText("kg/" + per); }
        else               { co2Value.setText(format(grams));        co2Unit.setText("g/" + per); }

        for (JToggleButton b : timeButtons) {
            b.setBackground(b.isSelected() ? GREEN : CARD_BG);
            b.setForeground(b.isSelected() ? Color.WHITE : TEXT_MUTED);
        }
    }
}
