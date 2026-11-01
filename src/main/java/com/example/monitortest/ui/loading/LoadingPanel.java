package com.example.monitortest.ui.loading;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
    private Runnable onCancel, onRestart;

    public LoadingPanel() {
        super(new BorderLayout());
        setBackground(BG_COLOR);
        add(scrollable(createScanPage()), BorderLayout.CENTER);
        resetProgress();
    }


    /**
     * Starts the visual loading animation.
     * Does not start the actual hardware scan.
     */
    public void startLoadingAnimation() {
        SwingUtilities.invokeLater(() -> progressBar.startAnimation());
    }

    /**
     * Stops the visual loading animation.
     * Does not interrupt the hardware scan.
     */
    public void stopLoadingAnimation() {
        SwingUtilities.invokeLater(() -> progressBar.stopAnimation());
    }


    // The main app can decide what the buttons should do.
    public void setOnCancel(Runnable action) { onCancel = action; }
    public void setOnRestart(Runnable action) { onRestart = action; }

    // Clears the display. It does not start a scan.
    public void resetProgress() {

        // Stop any previous animation and reset the progress bar.
        progressBar.stopAnimation();
        progressBar.setValue(0);

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::resetProgress);
            return;
        }
        setProgress(0);
        for (HardwareRow row : rows) row.setStatus(HardwareRow.Status.WAITING);
        detectingPrefix.setText("Waiting for scan");
        detectingPart.setText("");
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

    // The backend sets the status of each hardware row.
    public void setPartStatus(String part, String status) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setPartStatus(part, status));
            return;
        }
        if (part == null || status == null) return;
        for (int i = 0; i < PARTS.length; i++) {
            if (PARTS[i].equalsIgnoreCase(part)) {
                try {
                    rows[i].setStatus(HardwareRow.Status.valueOf(status.toUpperCase(java.util.Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    // Ignore an unknown status.
                }
                return;
            }
        }
    }

    // Changes the heading when the backend finishes.
    public void showComplete() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::showComplete);
            return;
        }
        detectingPrefix.setText("Scan complete.");
        detectingPart.setText("Opening results...");
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

    // Stops the small loading animations when the page is closed.
    public void stopAnimations() {
        if (rows != null) for (HardwareRow row : rows) row.stopSpinner();
    }

    // Colours used in the app
    static final Color BG_COLOR = new Color(0xF4F6F9);
    static final Color SIDEBAR_BG = Color.WHITE;
    static final Color SIDEBAR_BORDER = new Color(0xE7EBF0);
    static final Color SIDEBAR_SELECTED = new Color(0xEAF1FD);
    static final Color SIDEBAR_HOVER = new Color(0xF3F6FA);
    static final Color DIVIDER = new Color(0xEEF1F5);

    static final Color GREEN = new Color(0x16834D);
    static final Color GREEN_LIGHT = new Color(0x2F9A5F);
    static final Color BUTTON_GREEN = new Color(0x3B8B5A);
    static final Color BUTTON_GREEN_HOVER = new Color(0x2F7A4C);

    static final Color TEXT_MAIN = new Color(0x0F172A);
    static final Color TEXT_BODY = new Color(0x475569);
    static final Color TEXT_MUTED = new Color(0x64748B);
    static final Color TEXT_FAINT = new Color(0x94A3B8);
    static final Color BORDER_COLOR = new Color(0xE3E8EF);

    // Font used for text
    static final String FONT_FAMILY = "Segoe UI";

    // Sets the font style and size
    static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, 12).deriveFont(style, size);
    }


    // Makes shapes and text look smoother
    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Object desktopHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints instanceof Map<?, ?> hints) g2.addRenderingHints(hints);
        return g2;
    }


    // Creates a text label
    static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }


    // Allows the loading page to scroll when needed
    static JScrollPane scrollable(JComponent page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
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

        JLabel eyebrow = new JLabel("SYSTEM DIAGNOSTIC");
        eyebrow.setFont(font(Font.BOLD, 10));
        eyebrow.setForeground(GREEN);
        JLabel heading = new JLabel("Scanning your computer");
        heading.setFont(font(Font.BOLD, 24));
        heading.setForeground(TEXT_MAIN);
        detectingPrefix = new JLabel("Now detecting:");
        detectingPrefix.setFont(font(Font.PLAIN, 11.5f));
        detectingPrefix.setForeground(TEXT_MUTED);
        detectingPart = new JLabel();
        detectingPart.setFont(font(Font.BOLD, 11.5f));
        detectingPart.setForeground(GREEN);

        JPanel detectingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detectingRow.setOpaque(false);
        detectingRow.add(detectingPrefix);
        detectingRow.add(Box.createHorizontalStrut(4));
        detectingRow.add(detectingPart);

        JPanel titleText = new JPanel();
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setOpaque(false);
        for (JComponent comp : new JComponent[]{eyebrow, heading, detectingRow}) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleText.add(comp);
        }

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        titleRow.setOpaque(false);
        titleRow.add(new GradientBadge(48, "SCAN", 24, 12));
        titleRow.add(titleText);
        column.add(titleRow);
        column.add(Box.createVerticalStrut(30));

        JLabel caption = new JLabel("Hardware scan in progress");
        caption.setFont(font(Font.PLAIN, 11.5f));
        caption.setForeground(TEXT_BODY);
        caption.setVerticalAlignment(SwingConstants.BOTTOM);
        percentLabel = new JLabel("0%");
        percentLabel.setFont(font(Font.BOLD, 19));
        percentLabel.setForeground(GREEN);

        JPanel progressHead = new JPanel(new BorderLayout());
        progressHead.setOpaque(false);
        progressHead.add(caption, BorderLayout.WEST);
        progressHead.add(percentLabel, BorderLayout.EAST);
        column.add(progressHead);
        column.add(Box.createVerticalStrut(6));
        progressBar = new GradientProgressBar();
        column.add(progressBar);
        column.add(Box.createVerticalStrut(16));

        RoundedPanel hardwareCard = new RoundedPanel(Color.WHITE, BORDER_COLOR, 20);
        hardwareCard.setLayout(new BoxLayout(hardwareCard, BoxLayout.Y_AXIS));
        hardwareCard.setBorder(new EmptyBorder(1, 1, 1, 1));
        rows = new HardwareRow[PARTS.length];
        for (int i = 0; i < PARTS.length; i++) {
            rows[i] = new HardwareRow(PARTS[i], PART_ICONS[i], i == 0, i == PARTS.length - 1);
            hardwareCard.add(rows[i]);
        }
        column.add(hardwareCard);
        column.add(Box.createVerticalStrut(12));

        JLabel hint = label("Scanning is automatic. No input needed.", Font.PLAIN, 10.5f, TEXT_MUTED);
        hint.setIcon(new Dot(6, new Color(0x2563EB)));
        hint.setIconTextGap(7);
        column.add(hint);

        column.add(createWarning());

        column.add(Box.createVerticalStrut(16));
        OutlineButton cancel = new OutlineButton("Cancel", null);
        cancel.addActionListener(e -> { if (onCancel != null) onCancel.run(); });
        OutlineButton restart = new OutlineButton("Restart Scan", new LineIcon("RESTART", 14, TEXT_MAIN));
        restart.addActionListener(e -> { if (onRestart != null) onRestart.run(); });
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(cancel);
        buttonRow.add(restart);
        column.add(buttonRow);

        column.add(Box.createVerticalStrut(10));
        column.add(label("The scan results will appear when scanning is complete.",
                Font.PLAIN, 10, TEXT_FAINT));

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


    // Creates the warning message
    private JPanel createWarning() {
        JLabel icon = new JLabel(new LineIcon("WARNING", 17, new Color(0xD97706)));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JLabel title = new JLabel("Warning");
        title.setFont(font(Font.BOLD, 11.5f));
        title.setForeground(new Color(0x78350F));
        JLabel body = new JLabel("<html><div style='width:420px'>Some hardware information could not be retrieved. "
                + "EcoScan will continue with the available information.</div></html>");
        body.setFont(font(Font.PLAIN, 11));
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

    static class ScrollablePanel extends JPanel implements Scrollable {
        ScrollablePanel(LayoutManager layout) { super(layout); }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 160; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport v && v.getHeight() > getPreferredSize().height;
        }
    }


    // Draws the home page background
    static class RoundedPanel extends JPanel {
        private final Color fill, border;
        private final int arc;

        RoundedPanel(Color fill, Color border, int arc) {
            this.fill = fill;
            this.border = border;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(fill);
            g2.fill(shape);
            g2.setColor(border);
            g2.draw(shape);
            g2.dispose();
        }
    }


    // Draws the app badge
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


        static Image createImage(int size, String iconName) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = smooth(img.getGraphics());
            paintBadge(g2, size, iconName, size * 18 / 34, size * 9 / 34);
            g2.dispose();
            return img;
        }
    }


    // Buttons in the sidebar
    static class OutlineButton extends JButton {
        private boolean hover;

        OutlineButton(String text, Icon icon) {
            super(text, icon);
            setIconTextGap(8);
            setFont(font(Font.BOLD, 12));
            setForeground(TEXT_MAIN);
            setBorder(new EmptyBorder(8, 15, 8, 15));
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
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(hover ? new Color(0xF8FAFC) : Color.WHITE);
            g2.fill(shape);
            g2.setColor(isFocusOwner() ? GREEN : new Color(0xD9E0E8));
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }


    // Text button used for links
    static class GradientProgressBar extends JComponent {
        private int value;
        // Controls the animated loading indicator.
        private final Timer animationTimer;
        private int animationPosition = 0;

        // The percentage reported by the hardware scan.
        private int targetValue = 0;
        // Smoothly moves the displayed progress towards the target.
        private final Timer smoothProgressTimer;

        void startAnimation() {
            animationTimer.start();
        }

        // Stops the progress bar animation when the scan finishes.
        void stopAnimation() {
            animationTimer.stop();
        }

        GradientProgressBar() {
            setPreferredSize(new Dimension(100, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

            // Updates the animation approximately 30 times per second.
            animationTimer = new Timer(33, e -> {
                animationPosition = (animationPosition + 2) % 100;
                repaint();
            });

            // Animate progress at approximately 60 frames per second.
            smoothProgressTimer = new Timer(16, e -> {

                // Stop the timer once the displayed progress reaches its target.
                if (value >= targetValue) {
                    ((Timer) e.getSource()).stop();
                    return;
                }

                // Advance the displayed progress towards the actual milestone.
                value = Math.min(value + 1, targetValue);

                // Redraw the progress bar.
                repaint();
            });
        }

        /**
         * Sets the target progress percentage.
         * The bar animates towards this value instead of jumping immediately.
         */
        void setValue(int newValue) {

            // Ensure the target remains between 0 and 100%.
            targetValue = Math.max(0, Math.min(100, newValue));

            // A reset to zero should happen immediately.
            if (targetValue == 0) {
                smoothProgressTimer.stop();
                value = 0;
                repaint();
                return;
            }

            // Start animating if the bar has not reached the target.
            if (value < targetValue && !smoothProgressTimer.isRunning()) {
                smoothProgressTimer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);

            int w = getWidth();
            int h = getHeight();

            // Draw the empty progress bar background.
            g2.setColor(new Color(0xE2E8F0));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

            // Calculate the filled width based on actual scan progress.
            int filled = Math.round(w * value / 100f);

            if (filled > 0) {

                // Apply the existing green gradient to the completed portion.
                g2.setPaint(new GradientPaint( 0, 0, GREEN, Math.max(filled, 1), 0, GREEN_LIGHT ));

                // Fill the bar from left to right.
                g2.fill(new RoundRectangle2D.Float( 0, 0, filled, h, h, h ));
            }

            g2.dispose();
        }


    }


    // One item in the hardware scan list
    static class HardwareRow extends JPanel {
        enum Status { WAITING, DETECTING, DONE }

        private final boolean first, last;
        private final IconBox iconBox;
        private final JPanel statusPanel = new JPanel();
        private final Spinner spinner = new Spinner();
        private Status status;

        HardwareRow(String name, String iconName, boolean first, boolean last) {
            super(new BorderLayout());
            this.first = first;
            this.last = last;
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setPreferredSize(new Dimension(400, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            iconBox = new IconBox(new LineIcon(iconName, 16, TEXT_BODY).withStroke(1.8f));
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(font(Font.BOLD, 12));
            nameLabel.setForeground(new Color(0x1E293B));
            Box left = Box.createHorizontalBox();
            left.add(iconBox);
            left.add(Box.createHorizontalStrut(12));
            left.add(nameLabel);

            statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
            statusPanel.setOpaque(false);

            add(left, BorderLayout.WEST);
            add(statusPanel, BorderLayout.EAST);
            setStatus(Status.WAITING);
        }

        void setStatus(Status newStatus) {
            if (newStatus == status) return;
            status = newStatus;
            stopSpinner();
            // Clears the old status before showing the new one
            statusPanel.removeAll();

            switch (newStatus) {
                case DONE -> {
                    statusPanel.add(new JLabel(new LineIcon("CHECK", 12, GREEN).withStroke(2.6f)));
                    statusPanel.add(Box.createHorizontalStrut(6));
                    statusPanel.add(statusLabel("COMPLETED", GREEN));
                    iconBox.setBackground(new Color(0xE9F6EE));
                    iconBox.setIconColor(new Color(0x2F6F4B));
                }
                case DETECTING -> {
                    statusPanel.add(spinner);
                    statusPanel.add(Box.createHorizontalStrut(6));
                    statusPanel.add(statusLabel("DETECTING...", GREEN));
                    spinner.start();
                    iconBox.setBackground(new Color(0xEEF2F7));
                    iconBox.setIconColor(TEXT_BODY);
                }
                case WAITING -> {
                    statusPanel.add(statusLabel("WAITING", TEXT_FAINT));
                    iconBox.setBackground(new Color(0xF1F5F9));
                    iconBox.setIconColor(TEXT_BODY);
                }
            }
            statusPanel.revalidate();
            repaint();
        }

        void stopSpinner() {
            spinner.stop();
        }

        private static JLabel statusLabel(String text, Color color) {
            JLabel l = new JLabel(text);
            l.setFont(font(Font.BOLD, 9.5f));
            l.setForeground(color);
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int w = getWidth(), h = getHeight();

            if (status == Status.DETECTING) {
                g2.setColor(new Color(0xF8FAFC));
                if (first) g2.fill(new RoundRectangle2D.Float(0, 0, w, h + 20, 20, 20));
                else if (last) g2.fill(new RoundRectangle2D.Float(0, -20, w, h + 20, 20, 20));
                else g2.fillRect(0, 0, w, h);
            }
            if (!last) {
                g2.setColor(DIVIDER);
                g2.fillRect(0, h - 1, w, 1);
            }
            g2.dispose();
        }
    }


    // Small box for a hardware icon
    static class IconBox extends JComponent {
        private final LineIcon icon;

        IconBox(LineIcon icon) {
            this.icon = icon;
            setPreferredSize(new Dimension(28, 28));
            setMaximumSize(getPreferredSize());
            setAlignmentY(Component.CENTER_ALIGNMENT);
        }

        void setIconColor(Color color) {
            icon.setColor(color);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            icon.paintIcon(this, g2, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
            g2.dispose();
        }
    }


    // Spinning icon while scanning
    static class Spinner extends JComponent {
        private final Timer timer;
        private int angle;

        Spinner() {
            setPreferredSize(new Dimension(12, 12));
            setMaximumSize(getPreferredSize());
            timer = new Timer(30, e -> {
                angle = (angle + 12) % 360;
                repaint();
            });
        }

        void start() { timer.start(); }
        void stop() { timer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(TEXT_FAINT);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Float(1.5f, 1.5f, 9, 9, -angle, 270, Arc2D.OPEN));
            g2.dispose();
        }
    }


    // Thin line between sections
    static class Dot implements Icon {
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

    // Draws the icons used in the app
    static class LineIcon implements Icon {
        private final String name;
        private final int size;
        private Color color;
        private float strokeWidth = 2f;

        LineIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }


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

        private static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }


        private static void dot(Graphics2D g, double x, double y) {
            g.draw(new Line2D.Double(x, y, x + 0.01, y));
        }


        private static void polyline(Graphics2D g, boolean closed, double... xy) {
            Path2D p = new Path2D.Double();
            p.moveTo(xy[0], xy[1]);
            for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
            if (closed) p.closePath();
            g.draw(p);
        }


        private static void corner(Graphics2D g, double x1, double y1, double cx, double cy, double x2, double y2) {
            Path2D p = new Path2D.Double();
            p.moveTo(x1, y1);
            p.lineTo(cx + (x1 - cx) / 2, cy + (y1 - cy) / 2);
            p.quadTo(cx, cy, cx + (x2 - cx) / 2, cy + (y2 - cy) / 2);
            p.lineTo(x2, y2);
            g.draw(p);
        }
    }
}
