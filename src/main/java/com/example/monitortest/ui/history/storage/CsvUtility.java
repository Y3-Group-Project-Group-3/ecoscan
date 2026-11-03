
package com.example.monitortest.ui.history.storage;

import java.util.ArrayList;
import java.util.List;

// Provides reusable methods for formatting and parsing CSV records.
// Keeps CSV-specific logic separate from saved scan storage.
public final class CsvUtility {

    // Prevent creation of this utility class.
    private CsvUtility() {
    }

    // Converts multiple field values into one CSV record.
    public static String createRow(String... values) {

        List<String> escapedValues = new ArrayList<>();

        // Escape each field before joining them with commas.
        for (String value : values) {
            escapedValues.add(escapeField(value));
        }

        return String.join(",", escapedValues);
    }

    // Escapes a value so commas and quotation marks are preserved.
    private static String escapeField(String value) {

        // Represent missing text as an empty field.
        if (value == null) {
            return "";
        }

        // Escape quotation marks according to CSV rules.
        String escaped = value.replace("\"", "\"\"");

        // Surround fields containing special characters with quotes.
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    // Parses one complete CSV record into its original field values.
    // The input must already contain the entire record.
    public static List<String> parseRow(String row) {

        if (row == null) {
            throw new IllegalArgumentException("CSV row cannot be null.");
        }

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();

        boolean insideQuotes = false;

        // Read each character while respecting quoted fields.
        for (int i = 0; i < row.length(); i++) {

            char character = row.charAt(i);

            if (character == '"') {

                // Two consecutive quotation marks represent one quote.
                if (insideQuotes && i + 1 < row.length() && row.charAt(i + 1) == '"') {

                    currentValue.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (character == ',' && !insideQuotes) {

                // A comma outside quotes separates two fields.
                values.add(currentValue.toString());
                currentValue.setLength(0);

            } else {
                currentValue.append(character);
            }
        }

        // Reject records with an unclosed quotation mark.
        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "CSV record contains an unclosed quotation mark."
            );
        }

        // Include the final field, even if it is empty.
        values.add(currentValue.toString());





        return values;
    }


}
