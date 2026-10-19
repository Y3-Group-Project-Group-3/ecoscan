package com.example.monitortest.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.border.EmptyBorder;

public class HomePanelNew extends JPanel {

//    private final JButton viewScanPageButton;

    // ==========================================
    // THEME COLOURS (same naming style as the History screen)
    // ==========================================
    static final Color BG_COLOR = new Color(0xF4F6F9);          // page background
    static final Color SIDEBAR_BG = Color.WHITE;
    static final Color SIDEBAR_BORDER = new Color(0xE7EBF0);
    static final Color SIDEBAR_SELECTED = new Color(0xEAF1FD);  // active nav item
    static final Color SIDEBAR_HOVER = new Color(0xF3F6FA);
    static final Color DIVIDER = new Color(0xEEF1F5);

    static final Color GREEN = new Color(0x16834D);             // main brand green
    static final Color GREEN_LIGHT = new Color(0x2F9A5F);
    static final Color BUTTON_GREEN = new Color(0x3B8B5A);      // Scan Computer button
    static final Color BUTTON_GREEN_HOVER = new Color(0x2F7A4C);

    static final Color TEXT_MAIN = new Color(0x0F172A);         // headings
    static final Color TEXT_BODY = new Color(0x475569);         // normal text
    static final Color TEXT_MUTED = new Color(0x64748B);        // descriptions
    static final Color TEXT_FAINT = new Color(0x94A3B8);        // footnotes, "WAITING"
    static final Color BORDER_COLOR = new Color(0xE3E8EF);
    static final String FONT_FAMILY = "Segoe UI";               // falls back to a default font if missing


    private JButton scanButton;
    private String selectedUse = "Home";

    public HomePanelNew() {
        setLayout(new BorderLayout());
        setBackground(new Color(0xF4F6F9));

        JComponent homePage = createHomePage();

        add(homePage, BorderLayout.CENTER);
    }
//    /** Returns the app font in the given style (Font.PLAIN / Font.BOLD) and size. */
//    static Font font(int style, float size) {
//        return new Font(FONT_FAMILY, style, 12).deriveFont(style, size);
//    }
//    /** A JLabel with the given font and colour, centred when used in a vertical BoxLayout. */
//    static JLabel label(String text, int style, float size, Color color) {
//        JLabel l = new JLabel(text);
//        l.setFont(font(style, size));
//        l.setForeground(color);
//        l.setAlignmentX(Component.CENTER_ALIGNMENT);
//        return l;
//    }


    private JComponent createHomePage() {
        // Your teammate's existing Home page design goes here.
        // Background panel that also paints the two faint decorative circles
        HomeBackground page = new HomeBackground();

        // Everything is stacked in one centred column
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        // ---- Heading block ----
        column.add(label("HARDWARE ASSESSMENT", Font.BOLD, 10, GREEN));
        column.add(Box.createVerticalStrut(8));
        column.add(label("Scan your computer", Font.BOLD, 30, TEXT_MAIN));
        column.add(Box.createVerticalStrut(12));
        // HTML lets the text wrap and centre inside a fixed width
        column.add(label("<html><div style='width:440px; text-align:center;'>EcoScan finds your hardware for you, "
                + "nothing to type in. It analyses your computer's environmental impact and tells you whether "
                + "to keep, upgrade or replace it.</div></html>", Font.PLAIN, 12.5f, TEXT_MUTED));

        // ---- "What do you use it for?" cards ----
        column.add(Box.createVerticalStrut(32));
        column.add(label("What do you use it for?", Font.BOLD, 12, TEXT_MAIN));
        column.add(Box.createVerticalStrut(12));

        String[][] options = {
                {"Home", "Browsing & everyday tasks", "HOME"},
                {"Office", "Documents & productivity", "BRIEFCASE"},
                {"Gaming", "Games & entertainment", "GAMEPAD"},
                {"Creative", "Design, photo & video", "PENCIL"},
        };
        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        cardRow.setOpaque(false);
        ButtonGroup useGroup = new ButtonGroup();   // only one card can be selected at a time
        for (String[] option : options) {
            UsageCard card = new UsageCard(option[0], option[1], option[2]);
            card.setSelected(option[0].equals(selectedUse));
            card.addActionListener(e -> selectedUse = card.getTitle());   // remember the choice
            useGroup.add(card);
            cardRow.add(card);
        }
        column.add(cardRow);

        // ---- Main action ----
        column.add(Box.createVerticalStrut(24));
        scanButton = new PrimaryButton("Scan Computer", new LineIcon("SCAN", 15, Color.WHITE));
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//        scanButton.addActionListener(e -> showScan());
        column.add(scanButton);

        // ---- Privacy note ----
        column.add(Box.createVerticalStrut(14));
        JLabel privacy = label("Only hardware and system information is read. No personal files are accessed.",
                Font.PLAIN, 10.5f, TEXT_MUTED);
        privacy.setIcon(new LineIcon("SHIELD", 13, GREEN));
        privacy.setIconTextGap(6);
        column.add(privacy);

        // ---- "View scan history ->" link ----
//        column.add(Box.createVerticalStrut(8));
//        LinkButton historyLink = new LinkButton("View scan history", new LineIcon("ARROW_RIGHT", 12, GREEN));
//        historyLink.addActionListener(e -> showHistory());
//        column.add(historyLink);

        // Place the column at the top-centre of the page
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(56, 32, 32, 32);
        page.add(column, c);
        return page;
    }

    public JButton getScanButton() {
        return scanButton;
    }

    public String getSelectedUse() {
        return selectedUse;
    }


    // TODO Move to separate class
    /** Home page background: plain colour plus two faint circles (top-right and bottom-left). */
    static class HomeBackground extends ScrollablePanel {
        HomeBackground() {
            super(new GridBagLayout());
            setBackground(BG_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = smooth(g);
            g2.setColor(new Color(232, 237, 246, 190));
            int w = getWidth(), h = getHeight();
            g2.fillOval(w - 125, 45, 250, 250);   // centred on the right edge, half hidden
            g2.fillOval(-70, h - 90, 140, 140);   // centred on the left edge near the bottom
            g2.dispose();
        }
    }
    //TODO Move to separate class
    /** Panel that fills the scroll pane's visible area, so backgrounds reach the bottom of the window. */
    static class ScrollablePanel extends JPanel implements Scrollable {
        ScrollablePanel(LayoutManager layout) { super(layout); }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 160; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() {
            // Stretch to the window height unless the content is taller (then scroll)
            return getParent() instanceof JViewport v && v.getHeight() > getPreferredSize().height;
        }
    }

    // TODO Move to separate class
    /** One selectable "What do you use it for?" card. Works like a radio button inside a ButtonGroup. */
    static class UsageCard extends JToggleButton {
        private final String title, subtitle;
        private final LineIcon icon;
        private boolean hover;

        UsageCard(String title, String subtitle, String iconName) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = new LineIcon(iconName, 16, new Color(0x334155)).withStroke(1.8f);
            setPreferredSize(new Dimension(148, 96));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(title + ": " + subtitle);   // for screen readers
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        String getTitle() { return title; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int w = getWidth(), h = getHeight();
            boolean selected = isSelected();

            // Card background and border (green when selected)
            RoundRectangle2D card = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 18, 18);
            g2.setColor(selected ? new Color(0xF7FBF9) : Color.WHITE);
            g2.fill(card);
            g2.setStroke(new BasicStroke(selected ? 1.5f : 1f));
            g2.setColor(selected ? GREEN : (hover ? new Color(0xCBD5E1) : (isFocusOwner() ? new Color(0x94C9A9) : BORDER_COLOR)));
            g2.draw(card);

            // Icon in a small rounded box
            int boxX = (w - 32) / 2, boxY = 13;
            g2.setColor(new Color(0xEEF2F8));
            g2.fill(new RoundRectangle2D.Float(boxX, boxY, 32, 32, 12, 12));
            icon.setColor(selected ? GREEN : new Color(0x334155));
            icon.paintIcon(this, g2, boxX + 8, boxY + 8);

            // Title and subtitle, centred
            drawCentred(g2, title, font(Font.BOLD, 12.5f), TEXT_MAIN, 64);
            drawCentred(g2, subtitle, font(Font.PLAIN, 9.5f), new Color(0x8A96A8), 80);

            // Green tick badge in the top-right corner
            if (selected) {
                int bx = w - 8 - 17, by = 8;
                g2.setColor(new Color(0x2F8A55));
                g2.fillOval(bx, by, 17, 17);
                new LineIcon("CHECK", 11, Color.WHITE).withStroke(3.2f).paintIcon(this, g2, bx + 3, by + 3);
            }
            g2.dispose();
        }

        private void drawCentred(Graphics2D g2, String text, Font f, Color color, int baseline) {
            g2.setFont(f);
            g2.setColor(color);
            int x = (getWidth() - g2.getFontMetrics().stringWidth(text)) / 2;
            g2.drawString(text, x, baseline);
        }
    }

    // TODO Move to seprate class
    /** Solid green rounded button (Scan Computer). */
    static class PrimaryButton extends JButton {
        private boolean hover;

        PrimaryButton(String text, Icon icon) {
            super(text, icon);
            setIconTextGap(8);
            setFont(font(Font.BOLD, 12.5f));
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(10, 30, 10, 30));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(hover || isFocusOwner() ? BUTTON_GREEN_HOVER : BUTTON_GREEN);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }
    }


    // TODO Move to separate class
    // =====================================================================
    // ICONS
    // Outline icons drawn with Java2D on a 24x24 grid, then scaled to the
    // requested size. To add an icon, add a new "case" in draw().
    // =====================================================================
    static class LineIcon implements Icon {
        private final String name;
        private final int size;
        private Color color;
        private float strokeWidth = 2f;   // in 24x24 grid units

        LineIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        /** Returns this icon with a different line thickness (e.g. thicker for tiny ticks). */
        LineIcon withStroke(float width) {
            this.strokeWidth = width;
            return this;
        }

        void setColor(Color color) { this.color = color; }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g2);
            g2.dispose();
        }

        private void draw(Graphics2D g) {
            switch (name) {
                case "LEAF" -> {
                    Path2D leaf = new Path2D.Double();
                    leaf.moveTo(11, 20);
                    leaf.curveTo(6.5, 20.5, 3, 15.5, 4.8, 11);
                    leaf.curveTo(5.8, 8.4, 7.6, 6.6, 9.8, 6.1);
                    leaf.curveTo(15.5, 5, 17, 4.48, 19, 2);
                    leaf.curveTo(20, 4, 21, 6.18, 21, 10);
                    leaf.curveTo(21, 15.5, 16.22, 20, 11, 20);
                    leaf.closePath();
                    g.draw(leaf);
                    Path2D stem = new Path2D.Double();
                    stem.moveTo(2, 21);
                    stem.curveTo(2, 18, 3.85, 15.64, 7.08, 15);
                    stem.curveTo(9.5, 14.52, 12, 13, 13, 12);
                    g.draw(stem);
                }
                case "HOME" -> polyline(g, true, 3, 10.5, 12, 3, 21, 10.5, 21, 21, 15, 21, 15, 15, 9, 15, 9, 21, 3, 21);
                case "RESULTS" -> {
                    Path2D page = new Path2D.Double();
                    page.moveTo(14, 2);
                    page.lineTo(6, 2);
                    page.quadTo(4, 2, 4, 4);
                    page.lineTo(4, 20);
                    page.quadTo(4, 22, 6, 22);
                    page.lineTo(18, 22);
                    page.quadTo(20, 22, 20, 20);
                    page.lineTo(20, 8);
                    page.closePath();
                    g.draw(page);
                    polyline(g, false, 14, 2, 14, 8, 20, 8);
                    line(g, 8, 13, 16, 13);
                    line(g, 8, 17, 16, 17);
                    line(g, 8, 9, 10, 9);
                }
                case "HISTORY" -> {
                    g.draw(new Arc2D.Double(3, 3, 18, 18, 165, 315, Arc2D.OPEN));
                    polyline(g, false, 3, 3, 3, 8, 8, 8);
                    polyline(g, false, 12, 7, 12, 12, 16, 14);
                }
                case "SCAN" -> {
                    corner(g, 3, 7, 3, 3, 7, 3);
                    corner(g, 17, 3, 21, 3, 21, 7);
                    corner(g, 21, 17, 21, 21, 17, 21);
                    corner(g, 7, 21, 3, 21, 3, 17);
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                    dot(g, 12, 12);
                }
                case "BRIEFCASE" -> {
                    g.draw(new RoundRectangle2D.Double(2, 7, 20, 14, 4, 4));
                    Path2D handle = new Path2D.Double();
                    handle.moveTo(8, 7);
                    handle.lineTo(8, 5);
                    handle.quadTo(8, 3, 10, 3);
                    handle.lineTo(14, 3);
                    handle.quadTo(16, 3, 16, 5);
                    handle.lineTo(16, 7);
                    g.draw(handle);
                    line(g, 2, 13, 22, 13);
                }
                case "GAMEPAD" -> {
                    g.draw(new RoundRectangle2D.Double(2, 7, 20, 11, 9, 9));
                    line(g, 6, 12.5, 10, 12.5);
                    line(g, 8, 10.5, 8, 14.5);
                    dot(g, 15, 11.5);
                    dot(g, 18, 13.5);
                }
                case "PENCIL" -> {
                    polyline(g, true, 16, 3.5, 20.5, 8, 7.5, 21, 2.5, 21.5, 3, 16.5);
                    line(g, 13.5, 6, 18, 10.5);
                }
                case "SHIELD" -> {
                    Path2D shield = new Path2D.Double();
                    shield.moveTo(12, 22);
                    shield.curveTo(12, 22, 20, 18, 20, 12);
                    shield.lineTo(20, 5);
                    shield.lineTo(12, 2);
                    shield.lineTo(4, 5);
                    shield.lineTo(4, 12);
                    shield.curveTo(4, 18, 12, 22, 12, 22);
                    shield.closePath();
                    g.draw(shield);
                    polyline(g, false, 9, 12, 11, 14, 15, 10);
                }
                case "ARROW_RIGHT" -> {
                    line(g, 5, 12, 19, 12);
                    polyline(g, false, 12, 5, 19, 12, 12, 19);
                }
                case "MONITOR" -> {
                    g.draw(new RoundRectangle2D.Double(2, 3, 20, 14, 4, 4));
                    line(g, 8, 21, 16, 21);
                    line(g, 12, 17, 12, 21);
                }
                case "CPU" -> {
                    g.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 4, 4));
                    g.draw(new Rectangle2D.Double(9, 9, 6, 6));
                    for (double p : new double[]{9, 15}) {
                        line(g, p, 1, p, 4);
                        line(g, p, 20, p, 23);
                        line(g, 1, p, 4, p);
                        line(g, 20, p, 23, p);
                    }
                }
                case "RAM" -> {
                    g.draw(new RoundRectangle2D.Double(2, 6, 20, 12, 4, 4));
                    for (double x = 6; x <= 18; x += 4) line(g, x, 10, x, 14);
                    line(g, 6, 18, 6, 21);
                    line(g, 18, 18, 18, 21);
                }
                case "GPU" -> {
                    Path2D card = new Path2D.Double();
                    card.moveTo(2, 6);
                    card.lineTo(19, 6);
                    card.quadTo(21, 6, 21, 8);
                    card.lineTo(21, 16);
                    card.quadTo(21, 18, 19, 18);
                    card.lineTo(2, 18);
                    g.draw(card);
                    line(g, 2, 4, 2, 21);
                    g.draw(new Ellipse2D.Double(10, 9.5, 5, 5));
                    line(g, 6, 10, 6, 14);
                }
                case "STORAGE" -> {
                    g.draw(new Ellipse2D.Double(3, 2, 18, 6));
                    line(g, 3, 5, 3, 19);
                    line(g, 21, 5, 21, 19);
                    g.draw(new Arc2D.Double(3, 9, 18, 6, 180, 180, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(3, 16, 18, 6, 180, 180, Arc2D.OPEN));
                }
                case "OS" -> {
                    g.draw(new RoundRectangle2D.Double(3, 3, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(14, 3, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(14, 14, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(3, 14, 7, 7, 2, 2));
                }
                case "CHECK" -> polyline(g, false, 20, 6, 9, 17, 4, 12);
                case "RESTART" -> {
                    g.draw(new Arc2D.Double(3, 3, 18, 18, 45, 315, Arc2D.OPEN));
                    polyline(g, false, 21, 3, 21, 8, 16, 8);
                }
                case "WARNING" -> {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(10.3, 3.9);
                    triangle.lineTo(1.8, 18);
                    triangle.quadTo(1, 21, 3.5, 21);
                    triangle.lineTo(20.5, 21);
                    triangle.quadTo(23, 21, 22.2, 18);
                    triangle.lineTo(13.7, 3.9);
                    triangle.quadTo(12, 1.4, 10.3, 3.9);
                    triangle.closePath();
                    g.draw(triangle);
                    line(g, 12, 9, 12, 13);
                    dot(g, 12, 17);
                }
                default -> throw new IllegalArgumentException("Unknown icon: " + name);
            }
        }

        // ---- Small drawing helpers ----
        private static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        /** A dot: a tiny line that the round line cap turns into a circle. */
        private static void dot(Graphics2D g, double x, double y) {
            g.draw(new Line2D.Double(x, y, x + 0.01, y));
        }

        /** Connects the given x,y pairs with straight lines; closed = join the last point to the first. */
        private static void polyline(Graphics2D g, boolean closed, double... xy) {
            Path2D p = new Path2D.Double();
            p.moveTo(xy[0], xy[1]);
            for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
            if (closed) p.closePath();
            g.draw(p);
        }

        /** A rounded corner bracket for the scan icon: start -> corner -> end. */
        private static void corner(Graphics2D g, double x1, double y1, double cx, double cy, double x2, double y2) {
            Path2D p = new Path2D.Double();
            p.moveTo(x1, y1);
            p.lineTo(cx + (x1 - cx) / 2, cy + (y1 - cy) / 2);
            p.quadTo(cx, cy, cx + (x2 - cx) / 2, cy + (y2 - cy) / 2);
            p.lineTo(x2, y2);
            g.draw(p);
        }
    }



// TODO Move to separate class
    /** Green rounded square with a white icon (sidebar logo and scanning page badge). */
    static class GradientBadge extends JComponent {
        private final int size, iconSize, radius;
        private final String iconName;

        GradientBadge(int size, String iconName, int iconSize, int radius) {
            this.size = size;
            this.iconName = iconName;
            this.iconSize = iconSize;
            this.radius = radius;
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(getPreferredSize());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            paintBadge(g2, size, iconName, iconSize, radius);
            g2.dispose();
        }

        static void paintBadge(Graphics2D g2, int size, String iconName, int iconSize, int radius) {
            g2.setPaint(new GradientPaint(0, 0, GREEN_LIGHT, size, size, GREEN));
            g2.fill(new RoundRectangle2D.Float(0, 0, size, size, radius * 2, radius * 2));
            int offset = (size - iconSize) / 2;
            new LineIcon(iconName, iconSize, Color.WHITE).paintIcon(null, g2, offset, offset);
        }

        /** Draws the badge into an image, used for the window/taskbar icon. */
        static Image createImage(int size, String iconName) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = smooth(img.getGraphics());
            paintBadge(g2, size, iconName, size * 18 / 34, size * 9 / 34);
            g2.dispose();
            return img;
        }
    }

//    TODO: Move to separate class

    // ==========================================
    // SHARED HELPERS
    // ==========================================

    /** Returns the app font in the given style (Font.PLAIN / Font.BOLD) and size. */
    static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, 12).deriveFont(style, size);
    }

    /** Copy of the Graphics with smooth (anti-aliased) shapes and text. Remember to dispose() it. */
    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Use the operating system's text smoothing if available (sharper on Windows)
        Object desktopHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints instanceof Map<?, ?> hints) g2.addRenderingHints(hints);
        return g2;
    }

    /** A JLabel with the given font and colour, centred when used in a vertical BoxLayout. */
    static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /** Wraps a page in a scroll pane with no border, so short windows can still reach everything. */
    static JScrollPane scrollable(JComponent page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }


}