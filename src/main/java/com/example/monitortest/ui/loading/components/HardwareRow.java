package com.example.monitortest.ui.loading.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.theme.UIUtility.font;
import static com.example.monitortest.ui.theme.UIUtility.smooth;
import com.example.monitortest.ui.common.LineIcon;


/**
 * Represents a hardware component being scanned.
 * Updates its appearance according to the detection status.
 */
public class HardwareRow extends JPanel {

    public enum Status {
        WAITING,
        DETECTING,
        DONE
    }

    private final boolean first, last;
    private final IconBox iconBox;
    private final JPanel statusPanel = new JPanel();
    private final Spinner spinner = new Spinner();
    private Status status;

    public HardwareRow(String name, String iconName, boolean first, boolean last) {
        super(new BorderLayout());
        this.first = first;
        this.last = last;
        setOpaque(false);
        setBorder(new EmptyBorder(8, 14, 8, 14));
        setPreferredSize(new Dimension(400, 52));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        iconBox = new IconBox(new LineIcon(iconName, 16, TEXT_BODY).withStroke(1.8f));
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(font(Font.BOLD, FONT_LABEL_SIZE));
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

    // Updates the row's appearance and spinner; must be called on the Swing EDT.
    public void setStatus(Status newStatus) {
        if (newStatus == status) return;
        status = newStatus;
        // Stop any existing animation before changing the row's status.
        spinner.stop();
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

    // Stops the spinner when the row is no longer active.
    public void stopSpinner() {
        spinner.stop();
    }

    private static JLabel statusLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, FONT_BADGE_SIZE));
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