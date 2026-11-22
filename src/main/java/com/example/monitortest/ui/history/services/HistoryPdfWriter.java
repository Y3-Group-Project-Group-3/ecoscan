package com.example.monitortest.ui.history.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Creates a PDF of History records, such as saved scans or sessions.
// Starts with a summary table, followed by an optional detail page for each record.
// Keeps PDF creation separate from the History Page's UI.
public class HistoryPdfWriter {

    // Space around the edge of every page.
    private static final float MARGIN = 50;

    // Height of each table row or detail line.
    private static final float ROW_HEIGHT = 22;

    // Width of the label column on the detail pages.
    private static final float LABEL_WIDTH = 200;

    // Text sizes for the titles and the table.
    private static final float TITLE_SIZE = 18;
    private static final float TEXT_SIZE = 11;

    // Formats the date shown under the title.
    private static final DateTimeFormatter CREATED_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm");

    // Standard PDF fonts, so no font files need to be included with EcoScan.
    private final PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);


    // One extra page describing a single record in detail, such as one saved scan.
    // Each line is a label and a value. A line with no value is shown as a section heading.
    public record DetailPage(String title, List<String[]> lines) { }


    // Writes a PDF with only the summary table.
    public void write(Path pdfPath, String title, String[] headings, List<String[]> rows) throws IOException {
        write(pdfPath, title, headings, rows, List.of());
    }


    // Writes the summary table, then one detail page for each record.
    public void write(Path pdfPath, String title, String[] headings, List<String[]> rows, List<DetailPage> detailPages) throws IOException {

        try (PDDocument document = new PDDocument()) {

            // The summary table always comes first.
            writeTable(document, title, headings, rows);

            // Add the detail pages after the table.
            for (DetailPage detailPage : detailPages) {
                writeDetailPage(document, detailPage);
            }

            // Save the finished PDF to the chosen location.
            document.save(pdfPath.toFile());
        }
    }


    // Writes the title, column headings and rows.
    // Starts a new page whenever the current page is full.
    private void writeTable(PDDocument document, String title, String[] headings, List<String[]> rows) throws IOException {

        // Use landscape pages when there are too many columns for a portrait page.
        PDRectangle pageSize = headings.length > 3
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;

        // Share the page width equally between the columns.
        float tableWidth = pageSize.getWidth() - (MARGIN * 2);
        float columnWidth = tableWidth / headings.length;

        int rowIndex = 0;
        boolean firstPage = true;

        // Always create at least one page, even when there are no rows.
        do {

            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                // Start writing from the top of the page.
                float y = pageSize.getHeight() - MARGIN;

                // Only the first page shows the title and the date it was created.
                if (firstPage) {

                    writeText(content, boldFont, TITLE_SIZE, MARGIN, y, title, tableWidth);
                    y -= ROW_HEIGHT;

                    writeText(content, font, TEXT_SIZE, MARGIN, y, "Created " + LocalDateTime.now().format(CREATED_FORMAT), tableWidth);
                    y -= ROW_HEIGHT * 1.5f;

                    firstPage = false;
                }

                // Show the column headings with a line underneath.
                for (int column = 0; column < headings.length; column++) {
                    writeText(content, boldFont, TEXT_SIZE, MARGIN + column * columnWidth, y, headings[column], columnWidth - 10);
                }

                drawLine(content, y - 8, tableWidth);
                y -= ROW_HEIGHT;

                // Explain an empty table instead of leaving the page blank.
                if (rows.isEmpty()) {
                    writeText(content, font, TEXT_SIZE, MARGIN, y, "No records to show.", tableWidth);
                }

                // Add rows until the page is full.
                while (rowIndex < rows.size() && y > MARGIN) {

                    String[] row = rows.get(rowIndex);

                    for (int column = 0; column < row.length; column++) {
                        writeText(content, font, TEXT_SIZE, MARGIN + column * columnWidth, y, row[column], columnWidth - 10);
                    }

                    y -= ROW_HEIGHT;
                    rowIndex++;
                }
            }

        } while (rowIndex < rows.size());
    }


    // Writes one record's detail page as labels and values.
    // Continues onto another page if there are too many lines.
    private void writeDetailPage(PDDocument document, DetailPage detailPage) throws IOException {

        PDRectangle pageSize = PDRectangle.A4;
        float pageWidth = pageSize.getWidth() - (MARGIN * 2);

        List<String[]> lines = detailPage.lines();

        int lineIndex = 0;
        boolean firstPage = true;

        do {

            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                // Start writing from the top of the page.
                float y = pageSize.getHeight() - MARGIN;

                // Show the record's title with a line underneath.
                String heading = firstPage ? detailPage.title() : detailPage.title() + " (continued)";
                writeText(content, boldFont, TITLE_SIZE, MARGIN, y, heading, pageWidth);
                drawLine(content, y - 10, pageWidth);
                y -= ROW_HEIGHT * 1.5f;

                firstPage = false;

                // Add lines until the page is full.
                while (lineIndex < lines.size() && y > MARGIN) {

                    String[] line = lines.get(lineIndex);

                    if (line[1] == null) {

                        // A line with no value is a section heading, with a small gap above it.
                        y -= 6;
                        writeText(content, boldFont, TEXT_SIZE + 1, MARGIN, y, line[0], pageWidth);

                    } else {

                        // Show the label on the left and its value beside it.
                        writeText(content, font, TEXT_SIZE, MARGIN, y, line[0], LABEL_WIDTH - 10);
                        writeText(content, font, TEXT_SIZE, MARGIN + LABEL_WIDTH, y, line[1], pageWidth - LABEL_WIDTH);
                    }

                    y -= ROW_HEIGHT;
                    lineIndex++;
                }
            }

        } while (lineIndex < lines.size());
    }


    // Writes one piece of text at the given position.
    // Text that is too long for its space is shortened with "...".
    private void writeText(PDPageContentStream content, PDType1Font textFont, float size, float x, float y, String text, float maxWidth) throws IOException {

        String printableText = fitToWidth( makePrintable(text, textFont), textFont, size, maxWidth );

        content.beginText();
        content.setFont(textFont, size);
        content.newLineAtOffset(x, y);
        content.showText(printableText);
        content.endText();
    }


    // Replaces characters the standard PDF fonts cannot show.
    // For example, the subscript 2 in CO₂e becomes a normal 2.
    private String makePrintable(String text, PDType1Font textFont) {

        StringBuilder printable = new StringBuilder();

        for (char character : text.replace('₂', '2').toCharArray()) {

            try {
                // Keep the character when the font can display it.
                textFont.encode(String.valueOf(character));
                printable.append(character);

            } catch (IOException | IllegalArgumentException e) {
                // Show a question mark for anything the font cannot display.
                printable.append('?');
            }
        }

        return printable.toString();
    }


    // Shortens text that would run past its space.
    private String fitToWidth(String text, PDType1Font textFont, float size, float maxWidth) throws IOException {

        // Leave the text as it is when it already fits.
        if (textWidth(text, textFont, size) <= maxWidth) {
            return text;
        }

        String shortened = text;

        // Remove one character at a time until the text and "..." fit.
        while (!shortened.isEmpty() && textWidth(shortened + "...", textFont, size) > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }

        return shortened + "...";
    }


    // Returns how wide the text will be on the page.
    private float textWidth(String text, PDType1Font textFont, float size) throws IOException {
        return textFont.getStringWidth(text) / 1000 * size;
    }


    // Draws a thin line across the page, used under headings.
    private void drawLine(PDPageContentStream content, float y, float width) throws IOException {

        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, y);
        content.lineTo(MARGIN + width, y);
        content.stroke();
    }
}