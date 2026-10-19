package com.example.monitortest.views;

import com.example.monitortest.model.HardwareInfo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// EcoScan - Results page (view only, test data for now)
public class ResultsPanelNew extends JPanel {

    // colours from figma
    private static final Color BG_COLOR = new Color(0xF4F6F8);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE3E7EB);
    private static final Color GREEN = new Color(0x2E7D4F);
    private static final Color GREEN_LIGHT = new Color(0xEAF5EE);
    private static final Color TEXT_MAIN = new Color(0x111827);
    private static final Color TEXT_MUTED = new Color(0x6B7280);
    private static final Color TRACK = new Color(0xE5E7EB);
    private static final Color GREY_BAR = new Color(0xA3ABB8);

    private static final String FONT = "Segoe UI";

    // labels that change when new data comes in
    private JLabel energyValue, energyUnit, co2Value, co2Unit, footprintTitle;
    private JToggleButton[] timeButtons;

    // test values - will come from the calculation class
    private double energyKwhPerDay = 2.76;   // energy consumption (kWh per day)
    private double co2GramsPerDay = 571;     // CO2 emissions (g per day)
    private String timescale = "Day";

    // temp back button
    private JButton backButton;

    // Power Info Labels
    private JLabel cpuLabel;
    private JLabel memoryLabel;
    private JLabel cpuPowerLabel;
    private JButton refreshButton;

    public ResultsPanelNew() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(createMainContentPanel());

        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // temp back button
        backButton = new JButton("Back to Home");
        add(backButton, BorderLayout.SOUTH);

        add(scroll, BorderLayout.CENTER);
    }


    // ---------- MAIN CONTENT ----------
    private JPanel createMainContentPanel() {
        JPanel main = new FitWidthPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(28, 36, 36, 36));

        main.add(createHeader());
        main.add(Box.createVerticalStrut(18));
        main.add(createScoreAndRecommendation());
        main.add(Box.createVerticalStrut(28));
        main.add(createImpactSection());
        main.add(Box.createVerticalStrut(28));
        main.add(createComparisonSection());
        main.add(Box.createVerticalStrut(28));

        main.add(createComponentSection());

        main.add(Box.createVerticalStrut(28));
        main.add(createHardwareInfoSection());

        return main;
    }

    // title + scan info
    private JPanel createHeader() {
        JPanel header = row();
        refreshButton = new JButton("Refresh Hardware Info");
        JPanel left = column();
        left.add(label("SCAN RESULTS", 12, Font.BOLD, GREEN));
        left.add(Box.createVerticalStrut(4));
        left.add(label("Environmental assessment", 28, Font.BOLD, TEXT_MAIN));
        header.add(left, BorderLayout.WEST);

        // data: scan date, PC name and intended use (from Home page)
        JLabel scanInfo = label("📅  Scanned 1 Oct 2026   •   My PC   •   Home use", 13, Font.PLAIN, TEXT_MUTED);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(scanInfo, BorderLayout.SOUTH);

        right.add(refreshButton, BorderLayout.NORTH);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // eco score card + recommendation card
    private JPanel createScoreAndRecommendation() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        // eco score
        JPanel scoreCard = card();
        scoreCard.setLayout(new BorderLayout(0, 10));
        scoreCard.add(label("ECO SCORE", 12, Font.BOLD, GREEN), BorderLayout.NORTH);
        JPanel scoreBody = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        scoreBody.setOpaque(false);
        scoreBody.add(new ScoreRing(78));                       // data: eco score 0-100
        scoreBody.add(badge("GOOD"));                           // data: score rating
        scoreCard.add(scoreBody, BorderLayout.CENTER);
        // data: short description of the score
        scoreCard.add(label("<html><div style='width:260px'>Strong overall efficiency for a desktop in this category.</div></html>", 12, Font.PLAIN, TEXT_MUTED), BorderLayout.SOUTH);
        c.gridx = 0; c.weightx = 0.35; c.insets = new Insets(0, 0, 0, 8);
        panel.add(scoreCard, c);

        // recommendation
        JPanel recoCard = card();
        recoCard.setLayout(new BoxLayout(recoCard, BoxLayout.Y_AXIS));
        JPanel recoTop = row();
        recoTop.add(label("RECOMMENDATION", 12, Font.BOLD, TEXT_MAIN), BorderLayout.WEST);
        // data: which option is active (KEEP / UPGRADE / REPLACE)
        recoTop.add(wrap(recommendationTabs("KEEP")), BorderLayout.EAST);
        recoCard.add(recoTop);
        recoCard.add(label("KEEP", 40, Font.BOLD, GREEN));      // data: recommendation
        recoCard.add(Box.createVerticalStrut(6));
        // data: reason for the recommendation
        recoCard.add(label("<html><div style='width:390px'>Your computer is still suitable for its intended use. "
                        + "Replacing it now may create more environmental impact than continuing to use it.</div></html>",
                13, Font.PLAIN, TEXT_MUTED));
        recoCard.add(Box.createVerticalStrut(12));
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(GREEN_LIGHT);
        note.setBorder(new EmptyBorder(10, 14, 10, 14));
        note.setAlignmentX(LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        // data: CO2 saved by keeping the PC
        JLabel noteText = label("<html><div style='width:370px'>Keeping this PC could avoid approximately <b>180 kg CO₂e</b> from manufacturing a replacement.</div></html>",
                12, Font.PLAIN, TEXT_MAIN);
        noteText.setIcon(loadIcon("icons/leaf_green.png", 14));   // leaf icon from figma
        noteText.setIconTextGap(8);
        note.add(noteText);
        recoCard.add(note);
        c.gridx = 1; c.weightx = 0.65; c.insets = new Insets(0, 8, 0, 0);
        panel.add(recoCard, c);
        return panel;
    }

    private JPanel recommendationTabs(String active) {
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tabs.setOpaque(false);
        tabs.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        for (String option : new String[]{"KEEP", "UPGRADE", "REPLACE"}) {
            JLabel tab = label(option, 11, Font.BOLD, option.equals(active) ? GREEN : TEXT_MUTED);
            tab.setOpaque(true);
            tab.setBackground(option.equals(active) ? GREEN_LIGHT : CARD_BG);
            tab.setBorder(new EmptyBorder(6, 12, 6, 12));
            tabs.add(tab);
        }
        return tabs;
    }

    // energy, CO2, lifespan + timescale buttons
    private JPanel createImpactSection() {
        JPanel section = column();

        JPanel top = row();
        JPanel titles = column();
        titles.add(label("ENVIRONMENTAL IMPACT", 12, Font.BOLD, GREEN));
        titles.add(Box.createVerticalStrut(4));
        footprintTitle = label("Your daily footprint", 20, Font.BOLD, TEXT_MAIN);
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
            b.setFont(new Font(FONT, Font.PLAIN, 12));
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

        energyValue = label("", 26, Font.BOLD, TEXT_MAIN);
        energyUnit = label("", 12, Font.PLAIN, TEXT_MUTED);
        co2Value = label("", 26, Font.BOLD, TEXT_MAIN);
        co2Unit = label("", 12, Font.PLAIN, TEXT_MUTED);

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(LEFT_ALIGNMENT);
        // data: energy consumption
        cards.add(statCard("⚡", new Color(0xEEF2FF), "ENERGY CONSUMPTION", energyValue, energyUnit, "Typical use"));
        // data: CO2 emissions (per year shown on the right)
        cards.add(statCard("☁", new Color(0xF3EEFF), "CO₂ EMISSIONS", co2Value, co2Unit, "209 kg/year"));
        // data: estimated lifespan
        cards.add(statCard("📅", new Color(0xFFF4E8), "ESTIMATED LIFESPAN",
                label("3–5", 26, Font.BOLD, TEXT_MAIN), label("years", 12, Font.PLAIN, TEXT_MUTED), ""));
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

    // this PC vs similar PC vs laptop
    private JPanel createComparisonSection() {
        JPanel section = column();
        section.add(label("COMPARISON", 12, Font.BOLD, GREEN));
        section.add(Box.createVerticalStrut(4));
        // data: % better than similar PCs + intended use
        section.add(label("<html>Better than <span style='color:#2E7D4F'>68%</span> of similar Home PCs.</html>",
                20, Font.BOLD, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        JPanel box = new JPanel(new GridLayout(0, 1, 0, 10));
        box.setBackground(CARD_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(18, 20, 18, 20)));
        box.setAlignmentX(LEFT_ALIGNMENT);
        // data: comparison scores
        box.add(comparisonRow("This PC", 78, true));
        box.add(comparisonRow("Similar PC", 62, false));
        box.add(comparisonRow("Laptop", 71, false));
        section.add(box);
        return section;
    }

    private JPanel comparisonRow(String name, int score, boolean thisPc) {
        JPanel line = new JPanel(new BorderLayout(14, 0));
        line.setOpaque(false);
        JPanel labels = new JPanel(new GridLayout(1, 2));
        labels.setOpaque(false);
        labels.setPreferredSize(new Dimension(140, 18));
        labels.add(label(name, 13, thisPc ? Font.BOLD : Font.PLAIN, thisPc ? TEXT_MAIN : TEXT_MUTED));
        labels.add(label(String.valueOf(score), 13, Font.BOLD, thisPc ? GREEN : TEXT_MUTED));
        line.add(labels, BorderLayout.WEST);
        line.add(new Bar(score, thisPc ? GREEN : GREY_BAR), BorderLayout.CENTER);
        return line;
    }

    // list of components, click a row to open details
    private JPanel createComponentSection() {
        JPanel section = column();
        section.add(label("COMPONENT BREAKDOWN", 12, Font.BOLD, GREEN));
        section.add(Box.createVerticalStrut(4));
        section.add(label("Hardware assessment", 20, Font.BOLD, TEXT_MAIN));
        section.add(Box.createVerticalStrut(14));

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(CARD_BG);
        list.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        list.setAlignmentX(LEFT_ALIGNMENT);

        // data: components from the scanner CSV + their assessment
        list.add(componentRow(list, "CPU", "Intel Core i5-12400F", "EFFICIENT",
                "Intel Core i5-12400F", "Low impact · 65 W", "~3 years · 4–6 years", "Suitable for intended use", false));
        list.add(componentRow(list, "RAM", "16 GB DDR4", "EFFICIENT",
                "16 GB DDR4 · 2 modules", "Low impact · 6 W", "~4 years · 3–5 years", "Suitable for intended use", true));
        list.add(componentRow(list, "GPU", "2 detected", "HIGH IMPACT",
                "2 GPUs detected", "High impact", "~4 years", "Main contributor to energy use", false));
        list.add(componentRow(list, "Storage", "2 detected", "AGEING",
                "2 drives detected", "Low impact", "~6 years", "Older drive may need replacing", false));
        list.add(componentRow(list, "Operating System", "", "SUPPORTED",
                "Windows", "–", "–", "Supported", false));
        section.add(list);
        return section;
    }

    private JPanel componentRow(JPanel list, String name, String summary, String status,
                                String spec, String impact, String age, String assessment, boolean openAtStart) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel nameLabel = label(name, 14, Font.BOLD, TEXT_MAIN);
        nameLabel.setPreferredSize(new Dimension(200, 20));
        left.add(nameLabel);
        left.add(label(summary, 13, Font.PLAIN, TEXT_MUTED));
        header.add(left, BorderLayout.WEST);

        JLabel arrow = label(openAtStart ? "⌃" : "⌄", 14, Font.PLAIN, TEXT_MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(badge(status));
        right.add(arrow);
        header.add(right, BorderLayout.EAST);
        item.add(header, BorderLayout.NORTH);

        // details (hidden until clicked)
        JPanel details = new JPanel(new GridLayout(2, 4, 20, 4));
        details.setBackground(new Color(0xF8FAF9));
        details.setBorder(new EmptyBorder(12, 18, 14, 18));
        details.add(label("Detected spec", 11, Font.PLAIN, TEXT_MUTED));
        details.add(label("Efficiency / impact", 11, Font.PLAIN, TEXT_MUTED));
        details.add(label("Age / lifespan", 11, Font.PLAIN, TEXT_MUTED));
        details.add(label("Assessment", 11, Font.PLAIN, TEXT_MUTED));
        details.add(label(spec, 13, Font.BOLD, TEXT_MAIN));
        details.add(label(impact, 13, Font.BOLD, TEXT_MAIN));     // data: power use (W) of this part
        details.add(label(age, 13, Font.BOLD, TEXT_MAIN));
        details.add(label(assessment, 13, Font.BOLD, TEXT_MAIN));
        details.setVisible(openAtStart);
        item.add(details, BorderLayout.CENTER);

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                details.setVisible(!details.isVisible());
                arrow.setText(details.isVisible() ? "⌃" : "⌄");
                list.revalidate();
            }
        });
        return item;
    }

    // ---------- SMALL HELPERS ----------

    // panel that stretches to the window width (stops sideways scrolling)
    private static class FitWidthPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 200; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // keeps small things (like the tab buttons) at their normal height
    private JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JPanel wrapBottom(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.SOUTH);
        return p;
    }

    // loads an icon from the icons folder (exported from figma) and resizes it
    private ImageIcon loadIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() <= 0) {
            System.out.println("Icon not found: " + path);
            return null;
        }
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    // eco score circle
    private static class ScoreRing extends JComponent {
        private final int score;
        ScoreRing(int score) { this.score = score; setPreferredSize(new Dimension(116, 116)); }
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()) - 12;
            g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(TRACK);
            g.drawOval(6, 6, s, s);
            g.setColor(GREEN);
            g.drawArc(6, 6, s, s, 90, -(int) (360 * score / 100.0));
            g.setColor(TEXT_MAIN);
            g.setFont(new Font(FONT, Font.BOLD, 32));
            FontMetrics fm = g.getFontMetrics();
            String t = String.valueOf(score);
            g.drawString(t, 6 + (s - fm.stringWidth(t)) / 2, 6 + s / 2 + 6);
            g.setColor(TEXT_MUTED);
            g.setFont(new Font(FONT, Font.PLAIN, 11));
            fm = g.getFontMetrics();
            g.drawString("/ 100", 6 + (s - fm.stringWidth("/ 100")) / 2, 6 + s / 2 + 24);
            g.dispose();
        }
    }

    // bar for the comparison
    private static class Bar extends JComponent {
        private final int value; private final Color color;
        Bar(int value, Color color) { this.value = value; this.color = color; setPreferredSize(new Dimension(100, 18)); }
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = getHeight() / 2 - 4;
            g.setColor(TRACK);
            g.fillRoundRect(0, y, getWidth(), 8, 8, 8);
            g.setColor(color);
            g.fillRoundRect(0, y, (int) (getWidth() * value / 100.0), 8, 8, 8);
            g.dispose();
        }
    }

    private JPanel statCard(String icon, Color iconBg, String title, JLabel value, JLabel unit, String sideText) {
        JPanel card = card();
        card.setLayout(new BorderLayout(16, 0));
        JLabel ic = new JLabel(icon, SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        ic.setOpaque(true);
        ic.setBackground(iconBg);
        ic.setPreferredSize(new Dimension(44, 44));
        JPanel icWrap = new JPanel(new GridBagLayout());
        icWrap.setOpaque(false);
        icWrap.add(ic);
        card.add(icWrap, BorderLayout.WEST);

        JPanel text = column();
        JPanel top = row();
        top.add(label(title, 10, Font.BOLD, TEXT_MUTED), BorderLayout.WEST);
        top.add(label(sideText, 11, Font.PLAIN, TEXT_MUTED), BorderLayout.EAST);
        text.add(top);
        text.add(Box.createVerticalStrut(6));
        JPanel valueLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valueLine.setOpaque(false);
        valueLine.setAlignmentX(LEFT_ALIGNMENT);
        valueLine.add(value);
        valueLine.add(unit);
        text.add(valueLine);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // coloured status label (GOOD, EFFICIENT, HIGH IMPACT...)
    private JLabel badge(String text) {
        Color bg, fg;
        switch (text) {
            case "HIGH IMPACT": bg = new Color(0xFEE2E2); fg = new Color(0xB91C1C); break;
            case "AGEING":      bg = new Color(0xFFEDD5); fg = new Color(0xC26B00); break;
            default:            bg = new Color(0xDCFCE7); fg = new Color(0x166534);
        }
        JLabel l = label(text, 10, Font.BOLD, fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(20, 22, 20, 22)));
        return p;
    }

    private JPanel row() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JPanel column() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, style, size));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private String format(double v) {
        if (v >= 100) return String.format("%.0f", v);
        if (v >= 10) return String.format("%.1f", v);
        if (v >= 0.1) return String.format("%.2f", v);
        return String.format("%.4f", v);
    }

    private JPanel createHardwareInfoSection() {
        JPanel section = column();

        section.add(label(
                "DETECTED HARDWARE",
                12,
                Font.BOLD,
                GREEN
        ));

        section.add(Box.createVerticalStrut(14));

        JPanel hardwareCard = card();
        hardwareCard.setLayout(new BoxLayout(hardwareCard, BoxLayout.Y_AXIS));

        cpuLabel = label("CPU: Not retrieved yet", 13, Font.PLAIN, TEXT_MAIN);
        memoryLabel = label("Memory: Not retrieved yet", 13, Font.PLAIN, TEXT_MAIN);
        cpuPowerLabel = label("CPU Power: Not retrieved yet", 13, Font.PLAIN, TEXT_MAIN);

        hardwareCard.add(cpuLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(memoryLabel);
        hardwareCard.add(Box.createVerticalStrut(8));
        hardwareCard.add(cpuPowerLabel);

        section.add(hardwareCard);

        return section;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void updateHardwareInfo(HardwareInfo info) {

        cpuLabel.setText("CPU: " + info.getCpuName());

        if (Double.isNaN(info.getCpuPower())) {
            cpuPowerLabel.setText("CPU Power: Not supported");
        } else {
            cpuPowerLabel.setText(
                    String.format("CPU Power: %.1f W", info.getCpuPower())
            );
        }

        memoryLabel.setText(
                String.format(
                        "Total Memory: %.2f GB",
                        info.getTotalMemory() / (1024.0 * 1024 * 1024)
                )
        );
    }
}
