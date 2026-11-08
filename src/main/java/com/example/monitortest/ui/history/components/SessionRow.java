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
// Responsible only for presenting session information and hover styling.
public class SessionRow extends JPanel {

    // Formats the session's original start date and time.
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm");

    // Creates a row displaying the session date and total carbon emissions.
    public SessionRow(SessionRecord session) {

        // Match the two-column layout used by the session table header.
        setLayout(new GridLayout(1, 2, 10, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 18, 8, 18));

        // Use the application's standard table typography.
        Font rowFont = UIUtility.font( "SansSerif", Font.PLAIN, FONT_LABEL_SIZE );

        // Display the session's date and formatted emissions.
        add(createCell(session.getStartedAt().format(DATE_FORMAT), rowFont));
        add(createCell(formatEmissions(session.getTotalEmissionsGrams()), rowFont));

        // Highlight the row when the mouse enters or leaves it.
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(0xF7F8FA));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
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

    // Formats smaller emissions in grams and larger totals in kilograms.
    private String formatEmissions(double grams) {

        if (grams >= 1000) {
            return String.format(Locale.UK, "%.2f kg CO₂e", grams / 1000);
        }

        return String.format(Locale.UK, "%.2f g CO₂e", grams);
    }
}
