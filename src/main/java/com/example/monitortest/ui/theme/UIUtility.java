package com.example.monitortest.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static java.awt.Component.LEFT_ALIGNMENT;

public final class UIUtility {
    private UIUtility(){}

//    font()
    /** Returns the app font in the given style (Font.PLAIN / Font.BOLD) and size. */
    public static Font font(int style, float size) {
        return new Font(AppTheme.FONT_FAMILY, style, 12).deriveFont(style, size);
    }


//    smooth()
    /** Copy of the Graphics with smooth (anti-aliased) shapes and text. Remember to dispose() it. */
    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Use the operating system's text smoothing if available (sharper on Windows)
        Object desktopHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints instanceof Map<?, ?> hints) g2.addRenderingHints(hints);
        return g2;
    }


//    label()


    /*******
     * These will need to be standardised and merge at some point
     * I think its just the alignment thats different so that should be simple to have as a parameter
     */
    /** A JLabel with the given font and colour, centred when used in a vertical BoxLayout. */
    public static JLabel label(
            String text,
            int style,
            float size,
            Color color)
    {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel label(
            String text,
            int style,
            float size,
            Color color,
            float alignment)
    {
        JLabel label = label(text, style, size, color);
        label.setAlignmentX(alignment);

        return label;
    }

    // RESULTS PANEL
    public static JPanel column() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(20, 22, 20, 22)));
        return p;
    }

    public static JPanel row() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    // keeps small things (like the tab buttons) at their normal height
    public static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    // loads an icon from the icons folder (exported from figma) and resizes it
    public static ImageIcon loadIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() <= 0) {
            System.out.println("Icon not found: " + path);
            return null;
        }
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static String format(double v) {
        if (v >= 100) return String.format("%.0f", v);
        if (v >= 10) return String.format("%.1f", v);
        if (v >= 0.1) return String.format("%.2f", v);
        return String.format("%.4f", v);
    }


    public static JPanel wrapBottom(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.SOUTH);
        return p;
    }


    // Useful Helpers
    public static JPanel alignComponent(JComponent component, int alignment) {

        JPanel panel = new JPanel( new FlowLayout(alignment, 0, 0) );

        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(component);

        return panel;
    }

    // History Panel
    public static JButton createPillButton(String text) {
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

}
