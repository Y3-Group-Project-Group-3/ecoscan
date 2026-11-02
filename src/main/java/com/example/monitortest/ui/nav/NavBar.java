
package com.example.monitortest.ui.nav;

import com.example.monitortest.ui.common.GradientBadge;
import com.example.monitortest.ui.common.LineIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;
import static com.example.monitortest.ui.theme.UIUtility.smooth;

public class NavBar extends JPanel {

    private final NavButton homeNav;
    private final NavButton resultsNav;
    private final NavButton historyNav;

    public NavBar(Consumer<String> navigate) {

        // Sidebar configuration
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));
        setBackground(SIDEBAR_BG);
        setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, SIDEBAR_BORDER
        ));

        // Logo text
        JPanel logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setOpaque(false);

        JLabel appTitle = new JLabel("EcoScan");
        appTitle.setFont(font(Font.BOLD, FONT_SUBHEADING));
        appTitle.setForeground(TEXT_MAIN);

        JLabel appSub = new JLabel("APPLICATION");
        appSub.setFont(font(Font.PLAIN, FONT_BADGE));
        appSub.setForeground(new Color(0x8A96A8));

        logoText.add(appTitle);
        logoText.add(appSub);

        // Logo row
        JPanel logoRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );

        logoRow.setOpaque(false);
        logoRow.setBorder(new EmptyBorder(20, 6, 18, 16));

        logoRow.add(new GradientBadge(36, "LEAF", 19, 10));
        logoRow.add(logoText);

        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                logoRow.getPreferredSize().height
        ));

        // Navigation buttons
        homeNav = new NavButton("Home", "HOME");
        resultsNav = new NavButton("Results", "RESULTS");
        historyNav = new NavButton("History", "HISTORY");

        homeNav.addActionListener(e -> navigate.accept("home"));
        resultsNav.addActionListener(e -> navigate.accept("results"));
        historyNav.addActionListener(e -> navigate.accept("history"));

        // Top section
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(logoRow);
        top.add(new Divider());
        top.add(Box.createVerticalStrut(14));
        top.add(homeNav);
        top.add(Box.createVerticalStrut(6));
        top.add(resultsNav);
        top.add(Box.createVerticalStrut(6));
        top.add(historyNav);

        // Version label
        JLabel version = new JLabel(
                "Version 1.0.0",
                new Dot(6, new Color(0x22C55E)),
                SwingConstants.LEFT
        );

        version.setIconTextGap(8);
        version.setFont(font(Font.PLAIN, FONT_SMALL));
        version.setForeground(TEXT_FAINT);
        version.setBorder(new EmptyBorder(12, 16, 14, 16));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bottom section
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        bottom.add(new Divider());
        bottom.add(version);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);

        setActivePage("home");
    }

    public void setActivePage(String page) {
        homeNav.setActive("home".equals(page));
        resultsNav.setActive("results".equals(page));
        historyNav.setActive("history".equals(page));
    }

    // Horizontal divider
    static class Divider extends JComponent {

        Divider() {
            setPreferredSize(new Dimension(10, 1));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(DIVIDER);
            g.fillRect(12, 0, Math.max(0, getWidth() - 24), 1);
        }
    }

    // Small coloured dot
    static class Dot implements Icon {

        private final int size;
        private final Color color;

        Dot(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }
    }

    // Navigation button
    static class NavButton extends JButton {

        private final LineIcon icon;
        private boolean active;
        private boolean hover;

        NavButton(String text, String iconName) {
            super(text);

            icon = new LineIcon(iconName, 16, TEXT_MUTED);

            setIcon(icon);
            setIconTextGap(12);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 22, 0, 22));
            setFont(font(Font.PLAIN, FONT_BODY));
            setForeground(TEXT_BODY);

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            setPreferredSize(new Dimension(200, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;

            setFont(font(
                    active ? Font.BOLD : Font.PLAIN,
                    FONT_BODY
            ));

            setForeground(active ? TEXT_MAIN : TEXT_BODY);
            icon.setColor(active ? GREEN : TEXT_MUTED);

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = smooth(g);

            if (active || hover || isFocusOwner()) {
                g2.setColor(
                        active ? SIDEBAR_SELECTED : SIDEBAR_HOVER
                );

                g2.fill(new RoundRectangle2D.Float(
                        10, 1,
                        getWidth() - 20,
                        getHeight() - 2,
                        16, 16
                ));
            }

            if (active) {
                g2.setColor(GREEN);

                g2.fill(new RoundRectangle2D.Float(
                        -3, 6,
                        6, getHeight() - 12,
                        4, 4
                ));
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
