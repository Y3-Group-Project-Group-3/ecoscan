package com.example.monitortest.ui.loading.components;
import com.example.monitortest.ui.common.GraphicsUtils;
import com.example.monitortest.ui.common.LineIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Displays a hardware icon inside a rounded square.
 * The icon colour and background can change with scan status.
 */
public class IconBox extends JComponent {

    private final LineIcon icon;

    public IconBox(LineIcon icon) {
        this.icon = icon;

        setPreferredSize(new Dimension(28, 28));
        setMaximumSize(getPreferredSize());
        setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    // Updates the colour of the icon.
    public void setIconColor(Color color) {
        icon.setColor(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create an independent graphics context with smooth rendering.
        Graphics2D g2 = GraphicsUtils.createSmoothTextGraphics(g);

        // Draw the rounded background.
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float( 0, 0, getWidth(), getHeight(), 12, 12 ));

        // Centre the hardware icon within the box.
        icon.paintIcon( this, g2, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2 );

        g2.dispose();
    }
}
