package com.example.monitortest.ui.history.components;

import com.example.monitortest.ui.history.model.SessionRecord;
import com.example.monitortest.ui.theme.UIUtility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.example.monitortest.ui.theme.AppTheme.*;

// Displays one recorded Live Monitor session.
// Responsible only for presenting session information, hover and selection styling.
public class SessionRow extends JPanel {

    // Formats the session's original start date and time.
    // Shared with the Sessions card so search matches the date shown on screen.
    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm");

    // Tracks the row's selection and mouse hover state.
    private boolean selected = false;
    private boolean hovered = false;

    // Colours used to distinguish hovered and selected rows.
    private static final Color HOVER_COLOR = new Color(0xF7F8FA);
    private static final Color SELECTED_COLOR = new Color(0xEAF5EE);

    // Creates a row displaying the session date, total emissions and average emissions per second.
    public SessionRow(SessionRecord session) {


        // Display the session date, total emissions, average emissions and most recent carbon intensity across four columns.
        setLayout(new GridLayout(1, 4, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 18, 8, 18));

        // Use the application's standard table typography.
        Font rowFont = UIUtility.font( "SansSerif", Font.PLAIN, FONT_LABEL_SIZE );


        // Display all four recorded session measurements.
        add(createCell(session.getStartedAt().format(DATE_FORMAT), rowFont));
        add(createCell(formatEmissions(session.getTotalEmissionsGrams()), rowFont));
        add(createCell(formatAverage(session), rowFont));
        add(createCell(formatCarbonIntensity(session), rowFont));


        // Highlight the row when the mouse enters or leaves it.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                updateBackground();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                updateBackground();
            }
        });
    }

    // Creates a consistently styled text cell.
    private JLabel createCell(String text, Font font) {

        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_MAIN);

        return label;
    }

    // Updates the row background based on selection and hover state.
    // Selected rows take priority over hover highlighting.
    private void updateBackground() {

        if (selected) {
            setBackground(SELECTED_COLOR);
        } else if (hovered) {
            setBackground(HOVER_COLOR);
        } else {
            setBackground(Color.WHITE);
        }
    }

    // Returns whether this row is currently selected.
    public boolean isSelected() {
        return selected;
    }

    // Updates selection and immediately refreshes the row appearance.
    public void setSelected(boolean selected) {
        this.selected = selected;
        updateBackground();
    }

    // Formats smaller emissions in grams and larger totals in kilograms.
    // Shared with the Sessions card so the average uses the same units as the rows.
    public static String formatEmissions(double grams) {

        if (grams >= 1000) {
            return String.format(Locale.UK, "%.2f kg CO₂e", grams / 1000);
        }

        return String.format(Locale.UK, "%.2f g CO₂e", grams);
    }

    // Formats the session's average emissions per second, matching the Live Monitor.
    // Sessions saved before the length was recorded show "Not recorded".
    public static String formatAverage(SessionRecord session) {

        if (!session.hasDuration()) {
            return "Not recorded";
        }

        return String.format(Locale.UK, "%.6f g CO₂e/s", session.getTotalEmissionsGrams() / session.getDurationSeconds());
    }


    // Formats the most recent carbon intensity recorded for the session.
    // Older sessions without this information display "Not recorded".
    public static String formatCarbonIntensity(SessionRecord session) {

        // Do not invent intensity values for older saved sessions.
        if (!session.hasCarbonIntensity()) {
            return "Not recorded";
        }

        // Display the recorded intensity in grams of CO2 per kilowatt-hour.
        return String.format( Locale.UK, "%.1f g/kWh", session.getCarbonIntensityGramsPerKwh() );
    }

}