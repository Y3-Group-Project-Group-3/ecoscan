
package com.example.monitortest.ui.history.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilityJUnitTest {

    // UT-HIS-007 — CSV Special Characters
    @Test
    void utHis007_preservesSpecialCharacters() {

        // Test commas, quotation marks, line breaks and empty fields.
        List<String> originalValues = List.of(
                "Intel Core i7, 12th Gen",
                "GPU \"NVIDIA RTX\"",
                "First line\nSecond line",
                "Windows\r\nLinux",
                "",
                "Normal value"
        );

        // Convert the original values into one CSV record.
        String csvRow = CsvUtility.createRow(
                originalValues.toArray(new String[0])
        );

        // Parse the CSV record back into individual fields.
        List<String> parsedValues = CsvUtility.parseRow(csvRow);

        // The number of fields must remain unchanged.
        assertEquals(originalValues.size(), parsedValues.size());

        // Every field must match its original value exactly.
        assertEquals(originalValues, parsedValues);

        // Verify that special characters were escaped correctly.
        assertTrue(csvRow.contains("\"Intel Core i7, 12th Gen\""));
        assertTrue(csvRow.contains("\"GPU \"\"NVIDIA RTX\"\"\""));
        assertTrue(csvRow.contains("\"First line\nSecond line\""));
        assertTrue(csvRow.contains("\"Windows\r\nLinux\""));
    }

    // UT-HIS-008 — Malformed CSV Input
    @Test
    void utHis008_rejectsMalformedCsv() {

        // Each record contains an unclosed quoted field.
        List<String> malformedRows = List.of(
                "\"Unclosed field",
                "Valid field,\"Unclosed field",
                "CPU,\"First line\nSecond line"
        );

        for (String malformedRow : malformedRows) {

            // Malformed records must be rejected rather than parsed.
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> CsvUtility.parseRow(malformedRow),
                    "Malformed CSV should be rejected: " + malformedRow
            );

            // Verify that the exception identifies the parsing problem.
            assertEquals(
                    "CSV record contains an unclosed quotation mark.",
                    exception.getMessage()
            );
        }
    }
}
