package com.example.monitortest.ui.loading;

import com.example.monitortest.ui.common.GradientBadge;
import com.example.monitortest.ui.common.LineIcon;
import com.example.monitortest.ui.common.ScrollablePanel;
import com.example.monitortest.ui.loading.components.GradientProgressBar;
import com.example.monitortest.ui.loading.components.HardwareRow;
import com.example.monitortest.ui.loading.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;
import static com.example.monitortest.ui.theme.UIUtility.label;
import static com.example.monitortest.ui.theme.UIUtility.smooth;

// Loading screen for EcoScan. This panel can be added to the team's main window.
public class LoadingPanel extends JPanel {
    // Hardware components displayed during the scan.
    private static final String[] PARTS = { "CPU", "GPU", "RAM", "Storage" };
    // Matching icons for each hardware component.
    private static final String[] PART_ICONS = { "CPU", "GPU", "RAM", "STORAGE"};

    private JLabel detectingPrefix, detectingPart, percentLabel;
    private GradientProgressBar progressBar;
    private HardwareRow[] rows;
    private JPanel warningWrapper;

    public LoadingPanel() {
        super(new BorderLayout());
        setBackground(BG_COLOR);
        add(scrollable(createScanPage()), BorderLayout.CENTER);
        resetProgress();
    }


    // Resets the loading page to its initial state without starting a scan.
    public void resetProgress() {

        // Ensure all Swing component updates occur on the EDT.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::resetProgress);
            return;
        }

        // Reset the progress bar and percentage display.
        setProgress(0);

        // Return all hardware rows to their waiting state.
        for (HardwareRow row : rows) {
            row.setStatus(HardwareRow.Status.WAITING);
        }

        // Reset the detection labels.
        detectingPrefix.setText("Waiting for scan");
        detectingPart.setText("");

        // Hide any warnings from the previous scan.
        showWarning(false);
    }


    // Displays the percentage received from the backend.
    public void setProgress(int percent) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setProgress(percent));
            return;
        }
        int value = Math.max(0, Math.min(100, percent));
        progressBar.setValue(value);
        percentLabel.setText(value + "%");
    }

    // Displays which part the backend is currently checking.
    public void setCurrentPart(String part) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setCurrentPart(part));
            return;
        }
        detectingPrefix.setText("Now detecting:");
        detectingPart.setText(part == null ? "" : part);
    }


    // Updates the status of a hardware component during scanning.
    public void setPartStatus(String part, String status) {

        // Ensure Swing components are updated on the EDT.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setPartStatus(part, status));
            return;
        }

        // Ignore missing hardware names or statuses.
        if (part == null || status == null) {
            return;
        }

        // Find the hardware row matching the supplied component name.
        for (int i = 0; i < PARTS.length; i++) {

            if (!PARTS[i].equalsIgnoreCase(part)) {
                continue;
            }

            try {
                // Convert the backend status into the HardwareRow status enum.
                HardwareRow.Status newStatus = HardwareRow.Status.valueOf( status.toUpperCase(java.util.Locale.ROOT) );
                // Update the matching hardware row.
                rows[i].setStatus(newStatus);

            } catch (IllegalArgumentException ignored) {
                // Ignore statuses that are not recognised by HardwareRow.
            }
            return;
        }
    }


    // Shows a warning only when the backend reports a problem.
    public void showWarning(boolean visible) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showWarning(visible));
            return;
        }
        warningWrapper.setVisible(visible);
        warningWrapper.revalidate();
        warningWrapper.repaint();
    }


    // Wraps the loading page in a scroll pane when needed.
    private static JScrollPane scrollable(JComponent page) {

        JScrollPane scroll = new JScrollPane(page);

        // Remove the border and prevent horizontal scrolling.
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

        // Match the viewport background to the loading page.
        scroll.getViewport().setBackground(BG_COLOR);

        return scroll;
    }


    // Builds the scanning page
    private JComponent createScanPage() {
        ScrollablePanel page = new ScrollablePanel(new GridBagLayout());
        page.setBackground(BG_COLOR);

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        column.add(Box.createHorizontalStrut(500));

        // Add the loading page header.
        column.add(createHeader());
        column.add(Box.createVerticalStrut(30));

        // Add the progress section.
        column.add(createProgressSection());
        column.add(Box.createVerticalStrut(16));

        // Add the hardware card containing the scan status rows.
        column.add(createHardwareCard());
        column.add(Box.createVerticalStrut(12));

        // Add the informational messages and warning container.
        column.add(createInformationSection());

        for (Component comp : column.getComponents()) {
            if (comp instanceof JComponent jc) jc.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (comp instanceof JPanel p) p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        }

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(48, 32, 32, 32);
        page.add(column, c);
        return page;
    }


    /**
     * Creates the loading page header containing the scan badge,
     * heading and current hardware detection information.
     */
    private JPanel createHeader() {

        // Create the section label.
        JLabel eyebrow = new JLabel("SYSTEM DIAGNOSTIC");
        eyebrow.setFont(font(Font.BOLD, FONT_LABEL));
        eyebrow.setForeground(GREEN);

        // Create the main heading.
        JLabel heading = new JLabel("Scanning your computer");
        heading.setFont(font(Font.BOLD, FONT_TITLE));
        heading.setForeground(TEXT_MAIN);

        // Create the labels that display the currently detected hardware.
        detectingPrefix = new JLabel("Now detecting:");
        detectingPrefix.setFont(font(Font.PLAIN, FONT_SMALL));
        detectingPrefix.setForeground(TEXT_MUTED);

        detectingPart = new JLabel();
        detectingPart.setFont(font(Font.BOLD, FONT_SMALL));
        detectingPart.setForeground(GREEN);

        // Arrange the detection labels horizontally.
        JPanel detectingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detectingRow.setOpaque(false);
        detectingRow.add(detectingPrefix);
        detectingRow.add(Box.createHorizontalStrut(4));
        detectingRow.add(detectingPart);

        // Arrange the heading and detection information vertically.
        JPanel titleText = new JPanel();
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setOpaque(false);

        for (JComponent comp : new JComponent[]{eyebrow, heading, detectingRow}) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleText.add(comp);
        }

        // Position the scan badge beside the heading.
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        titleRow.setOpaque(false);
        titleRow.add(new GradientBadge(48, "SCAN", 24, 12));
        titleRow.add(titleText);

        return titleRow;
    }


    // Creates the progress section containing the caption, percentage and progress bar.
    private JPanel createProgressSection() {

        // Create the progress caption.
        JLabel caption = new JLabel("Hardware scan in progress");
        caption.setFont(font(Font.PLAIN, FONT_SMALL));
        caption.setForeground(TEXT_BODY);
        caption.setVerticalAlignment(SwingConstants.BOTTOM);

        // Create the percentage label updated during scanning.
        percentLabel = new JLabel("0%");
        percentLabel.setFont(font(Font.BOLD, FONT_HEADING));
        percentLabel.setForeground(GREEN);

        // Position the caption and percentage on opposite sides.
        JPanel progressHead = new JPanel(new BorderLayout());
        progressHead.setOpaque(false);
        progressHead.add(caption, BorderLayout.WEST);
        progressHead.add(percentLabel, BorderLayout.EAST);

        // Arrange the heading and progress bar vertically.
        JPanel progressSection = new JPanel();
        progressSection.setLayout(new BoxLayout(progressSection, BoxLayout.Y_AXIS));
        progressSection.setOpaque(false);

        progressSection.add(progressHead);
        progressSection.add(Box.createVerticalStrut(6));

        // Create the animated progress bar.
        progressBar = new GradientProgressBar();
        progressSection.add(progressBar);

        return progressSection;
    }


    // Creates the hardware card containing the components being scanned.
    private RoundedPanel createHardwareCard() {

        // Create the rounded container for the hardware rows.
        RoundedPanel hardwareCard = new RoundedPanel( Color.WHITE, BORDER_COLOR, 20);

        hardwareCard.setLayout(new BoxLayout(hardwareCard, BoxLayout.Y_AXIS));
        hardwareCard.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Create an array to store references to each hardware row.
        rows = new HardwareRow[PARTS.length];

        // Add a row for each hardware component.
        for (int i = 0; i < PARTS.length; i++) {

            rows[i] = new HardwareRow(
                    PARTS[i],
                    PART_ICONS[i],
                    i == 0,
                    i == PARTS.length - 1
            );

            hardwareCard.add(rows[i]);
        }

        return hardwareCard;
    }


    // Creates the informational messages displayed below the hardware card.
    private JPanel createInformationSection() {

        // Create a transparent container for the messages.
        JPanel informationSection = new JPanel();
        informationSection.setLayout(new BoxLayout(informationSection, BoxLayout.Y_AXIS));
        informationSection.setOpaque(false);

        // Explain that the hardware scan runs automatically.
        JLabel hint = label( "Scanning is automatic. No input needed.", Font.PLAIN, FONT_SMALL, TEXT_MUTED, Component.CENTER_ALIGNMENT );

        hint.setIcon(new Dot(6, new Color(0x2563EB)));
        hint.setIconTextGap(7);

        // Add the informational hint above the warning message.
        informationSection.add(hint);
        informationSection.add(createWarning());
        informationSection.add(Box.createVerticalStrut(16));

        // Explain what happens after the scan finishes.
        JLabel footer = label( "The scan results will appear when scanning is complete.", Font.PLAIN, FONT_SMALL, TEXT_FAINT, Component.CENTER_ALIGNMENT );

        informationSection.add(footer);

        // Keep the messages centred within the container.
        for (Component component : informationSection.getComponents()) {
            if (component instanceof JComponent jc) {
                jc.setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }

        return informationSection;
    }






    // Creates the warning message
    private JPanel createWarning() {
        JLabel icon = new JLabel(new LineIcon("WARNING", 17, new Color(0xD97706)));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JLabel title = new JLabel("Warning");
        title.setFont(font(Font.BOLD, FONT_SMALL));
        title.setForeground(new Color(0x78350F));
        JLabel body = new JLabel("<html><div style='width:420px'>Some hardware information could not be retrieved. "
                + "EcoScan will continue with the available information.</div></html>");
        body.setFont(font(Font.PLAIN, FONT_SMALL));
        body.setForeground(new Color(0x92400E));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(body);

        RoundedPanel box = new RoundedPanel(new Color(0xFFF8EB), new Color(0xFCD9A0), 16);
        box.setLayout(new BorderLayout(10, 0));
        box.setBorder(new EmptyBorder(12, 14, 12, 14));
        box.add(icon, BorderLayout.WEST);
        box.add(text, BorderLayout.CENTER);

        warningWrapper = new JPanel(new BorderLayout());
        warningWrapper.setOpaque(false);
        warningWrapper.setBorder(new EmptyBorder(16, 0, 0, 0));
        warningWrapper.add(box);
        warningWrapper.setVisible(false);
        return warningWrapper;
    }


    // Small circular icon displayed beside the informational hint.
    private static class Dot implements Icon {
        private final int size;
        private final Color color;

        Dot(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }
    }


}
