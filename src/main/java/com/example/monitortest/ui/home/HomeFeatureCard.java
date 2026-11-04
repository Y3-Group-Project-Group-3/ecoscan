
package com.example.monitortest.ui.home;

import com.example.monitortest.ui.common.LineIcon;
import com.example.monitortest.ui.common.PrimaryButton;
import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.theme.UIUtility;


import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomeFeatureCard extends JPanel {

    // Size of the light-green circle behind the feature icon, and of the icon itself.
    private static final int ICON_CIRCLE_SIZE = 96;
    private static final int FEATURE_ICON_SIZE = 48;
    // Both card buttons are the same height; their width fits the text, as in the design.
    private static final int BUTTON_HEIGHT = 52;

    private final JButton actionButton;
    // Tracks whether the mouse is hovering over the feature card.
    private boolean hovered = false;

    // Creates a reusable home page card with a title, description and action.
    // Each card receives its own feature-specific icon, plus a white line icon for its button (e.g. "SCAN", "MONITOR").
    public HomeFeatureCard(String title, String description, String buttonText, Ikon featureIcon, String buttonIconName) {

        setLayout(new BorderLayout(0, 16));

        // Allow the custom rounded background to be painted.
        setOpaque(false);

        // Add internal spacing so content does not touch the card edges.
        setBorder(new EmptyBorder(28, 28, 28, 28));

        // Compact feature icon inside a light-green circle (replaces the large illustration).
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(ICON_CIRCLE_SIZE, ICON_CIRCLE_SIZE));
        // Minimum size too, so the circle is never squashed when the window is narrower.
        imagePanel.setMinimumSize(new Dimension(ICON_CIRCLE_SIZE, ICON_CIRCLE_SIZE));

        // Create a scalable icon using the existing Ikonli library.
        FontIcon icon = FontIcon.of(featureIcon, FEATURE_ICON_SIZE, AppTheme.GREEN);

        JLabel imageLabel = new JLabel(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEAF5EE));   // very light tint of the brand green
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        imageLabel.setPreferredSize(new Dimension(ICON_CIRCLE_SIZE, ICON_CIRCLE_SIZE));
        imageLabel.setMinimumSize(new Dimension(ICON_CIRCLE_SIZE, ICON_CIRCLE_SIZE));
        imagePanel.add(imageLabel);

        add(imagePanel, BorderLayout.CENTER);

        // Stack the feature title, description and button vertically.
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        // Font.getFont() reads a system property, not a font, so it left the title in the plain default font.
        titleLabel.setFont(UIUtility.font(Font.BOLD, AppTheme.FONT_HEADING_SIZE + 4));
        titleLabel.setForeground(AppTheme.TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Centre the text within the title label.
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);


        JLabel descriptionLabel = new JLabel( "<html><div style='width:280px; text-align:center;'>" + description + "</div></html>" );
        descriptionLabel.setFont(UIUtility.font(Font.PLAIN, AppTheme.FONT_BODY_SIZE));
        descriptionLabel.setForeground(AppTheme.TEXT_MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Centre the description within its label.
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // White icon on the left, arrow on the right.
        actionButton = new PrimaryButton(buttonText + "  \u2192", new LineIcon(buttonIconName, 20, Color.WHITE));
        Dimension buttonSize = new Dimension(actionButton.getPreferredSize().width, BUTTON_HEIGHT);
        actionButton.setPreferredSize(buttonSize);
        actionButton.setMaximumSize(buttonSize);
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(descriptionLabel);
        detailsPanel.add(Box.createVerticalStrut(20));
        detailsPanel.add(actionButton);

        add(detailsPanel, BorderLayout.SOUTH);

        // Hover behaviour
        MouseAdapter hoverListener = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                updateHoverState();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHoverState();
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                // Only respond to a single left mouse click.
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 1) {
                    return;
                }

                // The JButton already handles its own clicks.
                // Avoid triggering the same navigation action twice.
                if (SwingUtilities.getAncestorOfClass( JButton.class, e.getComponent()) != null || e.getComponent() instanceof JButton) {
                    return;
                }

                // Trigger the existing button action when the card is clicked.
                actionButton.doClick();
            }
        };


        // Register the listener on the card and its contents.
        addHoverListeners(this, hoverListener);

        // Show a pointer cursor anywhere over the feature card.
        applyPointerCursor(this);

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create a separate graphics context for smooth rendering.
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing to smooth the rounded corners.
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        // Draw the card background with rounded corners.
        // Apply a subtle green tint when the card is hovered.
        g2.setColor(hovered ? new Color(245, 252, 247) : Color.WHITE);
        g2.fillRoundRect( 0, 0, getWidth(), getHeight(), 24, 24 );

        // Draw a subtle outline around the card.
        // Highlight the card outline when the mouse is over it.
        g2.setColor(hovered ? AppTheme.GREEN : new Color(220, 225, 230));
        g2.setStroke(new BasicStroke(1f));

        g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, 24, 24 );

        g2.dispose();
    }


    // Exposes the button so HomePanel can connect it to existing navigation.
    public JButton getActionButton() {
        return actionButton;
    }


    // Registers the same mouse listener on a component and its children.
    private void addHoverListeners(Component component, MouseAdapter listener) {
        component.addMouseListener(listener);

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                addHoverListeners(child, listener);
            }
        }
    }

    // Updates the hover state based on the cursor's position within the card.
    private void updateHoverState() {
        Point mousePosition = getMousePosition(true);

        boolean insideCard = mousePosition != null && contains(mousePosition);

        if (hovered != insideCard) {
            hovered = insideCard;
            repaint();
        }
    }


    // Applies the hand cursor to the card and all its child components.
    private void applyPointerCursor(Component component) {

        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Apply the same cursor to nested panels, labels and buttons.
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyPointerCursor(child);
            }
        }
    }


}
