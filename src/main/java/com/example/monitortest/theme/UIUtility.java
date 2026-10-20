package com.example.monitortest.theme;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

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


    /** A JLabel with the given font and colour, centred when used in a vertical BoxLayout. */
    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }


//    scrollable()
//    /** Wraps a page in a scroll pane with no border, so short windows can still reach everything. */
//    static JScrollPane scrollable(JComponent page) {
//        JScrollPane scroll = new JScrollPane(page);
//        scroll.setBorder(null);
//        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        scroll.getViewport().setBackground(AppTheme.BG_COLOR);
//        scroll.getVerticalScrollBar().setUnitIncrement(16);
//        return scroll;
//    }
}
