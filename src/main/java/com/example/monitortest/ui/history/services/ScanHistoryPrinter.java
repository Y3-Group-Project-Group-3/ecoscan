package com.example.monitortest.ui.history.services;

import com.example.monitortest.ui.history.model.SavedScan;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

// Handles printing saved hardware scan summaries.
// Keeps printing logic separate from the History Page's UI.
public class ScanHistoryPrinter {

    // Format scan dates in the same way as the History Page.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu");

    // Prints the supplied scans in their current display order.
    // The parent component is used to position any error dialogs.
    public void printScans( Collection<SavedScan> scans, Component parent) {

        // Create a printable table containing only Date and Name.
        String[] columns = {"Date", "Name"};
        Object[][] data = new Object[scans.size()][columns.length];

        int index = 0;
        // Convert each saved scan into a printable table row.
        for (SavedScan scan : scans) {
            data[index][0] = scan.getScannedAt().format(DATE_FORMAT);
            data[index][1] = scan.getName();
            index++;
        }

        JTable table = new JTable(data, columns);

        // Size the off-screen table so Swing can print it correctly.
        table.setSize(table.getPreferredSize());
        table.getTableHeader().setSize( table.getTableHeader().getPreferredSize() );

        try {

            // Open the system print dialog and fit the table to page width.
            table.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("EcoScan - Previous Scans"),
                    new MessageFormat("Page {0}")
            );

        } catch (PrinterException e) {

            // Report a printing failure without affecting saved scans.
            JOptionPane.showMessageDialog( parent, "Unable to print the saved scans.", "Print scans", JOptionPane.ERROR_MESSAGE );
            // Retain diagnostic information for development.
            e.printStackTrace();
        }
    }
}
