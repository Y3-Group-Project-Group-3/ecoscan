
package com.example.monitortest.ui.home;

import com.example.monitortest.ui.common.PrimaryButton;
import com.example.monitortest.ui.theme.AppTheme;


import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomeFeatureCard extends JPanel {

    private final JButton actionButton;
    // Tracks whether the mouse is hovering over the feature card.
    private boolean hovered = false;

    // Creates a reusable home page card with a title, description and action.
    // Each card receives its own feature-specific icon.
    public HomeFeatureCard(String title, String description, String buttonText, Ikon featureIcon) {

        setLayout(new BorderLayout(0, 16));

        // Allow the custom rounded background to be painted.
        setOpaque(false);

        // Add internal spacing so content does not touch the card edges.
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // The main image area will be replaced with a feature illustration later.
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setBackground(AppTheme.BG_COLOR);
        imagePanel.setPreferredSize(new Dimension(280, 240));

        // Create a scalable icon using the existing Ikonli library.
        FontIcon icon = FontIcon.of(featureIcon, 110, AppTheme.GREEN);

        JLabel imageLabel = new JLabel(icon);
        imagePanel.add(imageLabel);

        add(imagePanel, BorderLayout.CENTER);

        // Stack the feature title, description and button vertically.
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Font.getFont(AppTheme.FONT_FAMILY));
        titleLabel.setForeground(AppTheme.TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Centre the text within the title label.
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);


        JLabel descriptionLabel = new JLabel( "<html><div style='width:240px; text-align:center;'>" + description + "</div></html>" );
        descriptionLabel.setFont(Font.getFont(AppTheme.FONT_FAMILY));
        descriptionLabel.setForeground(AppTheme.TEXT_MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Centre the description within its label.
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        actionButton = new PrimaryButton(buttonText, null);
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
