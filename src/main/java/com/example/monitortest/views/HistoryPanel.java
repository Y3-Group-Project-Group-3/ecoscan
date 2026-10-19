package com.example.monitortest.views;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


public class HistoryPanel extends JPanel {
    // Theme Colors
    private static final Color BG_COLOR = new Color(0xF5F5FA);
    private static final Color SIDEBAR_BG = new Color(0xFFFFFF);
    private static final Color SIDEBAR_SELECTED = new Color(0xEDF3FF);
    private static final Color TEXT_MAIN = new Color(0x1E1E1E);
    private static final Color TEXT_MUTED = new Color(0x787878);
    private static final Color BORDER_COLOR = new Color(0xE1E1E1);
    private static final Color ROW_SELECTED_BG = new Color(0xF2F2F2);

    // temp back button
    private JButton backButton;

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        add(createMainContentPanel(), BorderLayout.CENTER);

        // temp back button
        backButton = new JButton("Back to Home");
        add(backButton, BorderLayout.SOUTH);
    }


    // ==========================================
    // MAIN CONTENT PANEL
    // ==========================================

    private JPanel createMainContentPanel() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Previous Scans");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);
        JTextField searchField = new JTextField("🔍  Search scans");
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setForeground(TEXT_MUTED);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"↓↑ Newest first", "↓↑ Oldest first"});
        sortCombo.setPreferredSize(new Dimension(130, 32));
        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortCombo.setBackground(Color.WHITE);
        controlsPanel.add(searchField);
        controlsPanel.add(sortCombo);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        // Center Container
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // Custom Container
        JPanel scanListContainer = new JPanel(new BorderLayout());
        scanListContainer.setBackground(Color.WHITE);
        scanListContainer.setBorder(new LineBorder(BORDER_COLOR, 1, true));

        // Header Row
        scanListContainer.add(createListHeaderRow(), BorderLayout.NORTH);

        // Items List Panel
        JPanel listItemsPanel = new JPanel();
        listItemsPanel.setLayout(new BoxLayout(listItemsPanel, BoxLayout.Y_AXIS));
        listItemsPanel.setBackground(Color.WHITE);

        // Add scan rows manually
        listItemsPanel.add(createScanRow("1 Oct 2026", "My PC", "Home", "78/100", "209 kg/year", "KEEP", true));
        listItemsPanel.add(createScanRow("12 Aug 2026", "My PC", "Home", "70/100", "214 kg/year", "KEEP", false));
        listItemsPanel.add(createScanRow("5 Jul 2026", "Work PC", "Office", "64/100", "258 kg/year", "UPGRADE", false));
        listItemsPanel.add(createScanRow("20 Apr 2026", "Reception PC", "Office", "48/100", "290 kg/year", "REPLACE", false));
        JScrollPane scrollPane = new JScrollPane(listItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scanListContainer.add(scrollPane, BorderLayout.CENTER);

        // Info Label at the bottom of the table
        JLabel infoLabel = new JLabel(
                "Select a row to delete it, or choose View to open the full breakdown."
        );

        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(75, 85, 99));
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(243, 244, 246));

        // Darker top border and padding
        infoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, new Color(209, 213, 219)
                ),
                new EmptyBorder(12, 15, 12, 15)
        ));

        scanListContainer.add(infoLabel, BorderLayout.SOUTH);

        // Action Button Bar
        JPanel buttonBar = new JPanel(new BorderLayout());
        buttonBar.setOpaque(false);

        JPanel leftButtons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );
        leftButtons.setOpaque(false);

        leftButtons.add(createPillButton("📥 Import Results"));
        leftButtons.add(createPillButton("💾 Export Results"));
        leftButtons.add(createPillButton("🖨️ Print List"));

        // Right side - keep button at its preferred size
        JPanel rightButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        rightButtons.setOpaque(false);

        JButton deleteBtn = createPillButton("🗑️ Delete Selected");
        rightButtons.add(deleteBtn);

        buttonBar.add(leftButtons, BorderLayout.WEST);
        buttonBar.add(rightButtons, BorderLayout.EAST);

        centerContainer.add(scanListContainer);

        // Add space above the action buttons
        centerContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        centerContainer.add(buttonBar);
        main.add(headerPanel, BorderLayout.NORTH);
        main.add(centerContainer, BorderLayout.CENTER);
        return main;
    }

    // ==========================================
    // CUSTOM LIST ROWS (WITHOUT JTABLE)
    // ==========================================

    private JPanel createListHeaderRow() {
        JPanel header = new JPanel(new GridLayout(1, 7, 10, 0));

        // Darker header background
        header.setBackground(new Color(243, 244, 246));

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, new Color(209, 213, 219)
                ),
                new EmptyBorder(12, 15, 12, 15)
        ));

        String[] titles = {
                "DATE", "DEVICE", "USE", "SCORE", "CO₂", "RESULT", ""
        };

        for (String t : titles) {
            JLabel lbl = new JLabel(t);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            lbl.setForeground(new Color(75, 85, 99));
            header.add(lbl);
        }

        return header;
    }

    private JPanel createScanRow(String date, String device, String use, String score, String co2, String result, boolean isSelected) {
        JPanel row = new JPanel(new GridLayout(1, 7, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setPreferredSize(new Dimension(700, 45));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(8, 15, 8, 15)
        ));

        Font font = new Font("SansSerif", Font.PLAIN, 12);
        JLabel lblDate = new JLabel(date);
        lblDate.setFont(font);
        lblDate.setForeground(TEXT_MAIN);
        JLabel lblDevice = new JLabel(device);
        lblDevice.setFont(font);
        lblDevice.setForeground(TEXT_MAIN);
        JLabel lblUse = new JLabel(use);
        lblUse.setFont(font);
        lblUse.setForeground(TEXT_MAIN);
        JLabel lblScore = new JLabel(score);
        lblScore.setFont(font);
        lblScore.setForeground(TEXT_MAIN);
        JLabel lblCo2 = new JLabel(co2);
        lblCo2.setFont(font);
        lblCo2.setForeground(TEXT_MAIN);
        // Result Badge
        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resultPanel.setOpaque(false);
        resultPanel.add(createBadge(result));
        // View Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        JButton viewBtn = new JButton("View");
        viewBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        viewBtn.setForeground(TEXT_MAIN);
        viewBtn.setBackground(Color.WHITE);
        viewBtn.setFocusPainted(false);
        viewBtn.setPreferredSize(new Dimension(55, 24));
        viewBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        buttonPanel.add(viewBtn);
        row.add(lblDate);
        row.add(lblDevice);
        row.add(lblUse);
        row.add(lblScore);
        row.add(lblCo2);
        row.add(resultPanel);
        row.add(buttonPanel);
        return row;
    }

    private JLabel createBadge(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Rounded background
                g2.setColor(getBackground());
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 20, 20
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(75, 22));
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));

        // Set badge colours
        switch (text.toUpperCase()) {
            case "KEEP":
                badge.setForeground(new Color(22, 101, 52));
                badge.setBackground(new Color(220, 252, 231));
                break;

            case "UPGRADE":
                badge.setForeground(new Color(194, 107, 0));
                badge.setBackground(new Color(255, 237, 213));
                break;

            case "REPLACE":
                badge.setForeground(new Color(185, 28, 28));
                badge.setBackground(new Color(254, 226, 226));
                break;

            default:
                badge.setForeground(TEXT_MAIN);
                badge.setBackground(new Color(245, 245, 245));
                break;
        }

        return badge;
    }

    private JButton createPillButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    public JButton getBackButton() {
        return backButton;
    }
}