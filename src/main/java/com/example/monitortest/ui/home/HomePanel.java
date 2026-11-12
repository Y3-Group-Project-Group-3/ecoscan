package com.example.monitortest.ui.home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import com.example.monitortest.ui.theme.AppTheme;
import com.example.monitortest.ui.theme.UIUtility;
import com.example.monitortest.ui.common.LineIcon;
import com.example.monitortest.ui.common.PrimaryButton;
import com.example.monitortest.ui.common.ScrollablePanel;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;


public class HomePanel extends JPanel {

    // Navigation buttons for the two main home page features.
    private JButton scanButton;
    private JButton liveMonitorButton;
    private static final int MAX_CONTENT_WIDTH = 1000;
    // Minimum available width required for the two-column layout.
    private static final int CARD_STACK_BREAKPOINT = 850;

    private String selectedUse = "Home";

    // temp history page button
    private JButton historyButton;


    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_COLOR);

        // Create the existing responsive home page.
        JComponent homePage = createHomePage();

        // Place the home page inside a scroll pane.
        JScrollPane scrollPane = new JScrollPane(homePage);

        // Only show the vertical scrollbar when the content requires it.
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        // Prevent horizontal scrolling.
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        // Remove the default scroll pane border.
        scrollPane.setBorder(null);

        // Match the viewport background to the home page.
        scrollPane.getViewport().setBackground(AppTheme.BG_COLOR);

        add(scrollPane, BorderLayout.CENTER);
    }


    private JComponent createHomePage() {
        // Background panel that also paints the two faint decorative circles
        HomeBackground page = new HomeBackground();

        // Everything is stacked in one centred column
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        // Allow the main content column to grow horizontally.
        column.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //Debugging
        column.setBackground(AppTheme.TEXT_MAIN);

        // ---- Heading block ----
        // --- hardware assessment label
        JLabel infoLabel = UIUtility.label("HARDWARE ASSESSMENT", Font.BOLD, AppTheme.FONT_LABEL_SIZE, AppTheme.GREEN);
        column.add(UIUtility.alignComponent(infoLabel, FlowLayout.CENTER));

        // Short green underline beneath the label
        column.add(Box.createVerticalStrut(8));
        JPanel underline = new JPanel();
        underline.setBackground(AppTheme.GREEN);
        underline.setPreferredSize(new Dimension(80, 3));
        column.add(UIUtility.alignComponent(underline, FlowLayout.CENTER));

        // --- Scan yo computer label
        column.add(Box.createVerticalStrut(14));
        JLabel pageTitle = UIUtility.label("Explore your computer", Font.BOLD, AppTheme.FONT_TITLE_SIZE, AppTheme.TEXT_MAIN);
        column.add(UIUtility.alignComponent(pageTitle, FlowLayout.CENTER));

        // --- paragraph text
        column.add(Box.createVerticalStrut(12));
        // HTML lets the text wrap and centre inside a fixed width
        JLabel paragraphLabel = UIUtility.label(
                "Choose how you would like to explore your hardware.",
                Font.PLAIN,
                AppTheme.FONT_BODY_SIZE,
                AppTheme.TEXT_MUTED
        );
        column.add(UIUtility.alignComponent(paragraphLabel, FlowLayout.CENTER));


        // ---- Main feature selection cards ----
        column.add(Box.createVerticalStrut(32));

        // Hardware scanning card.
        HomeFeatureCard scanCard = new HomeFeatureCard(
                "Scan Hardware",
                "Discover your computer's hardware and system specifications.",
                "Start Hardware Scan",
                FontAwesomeSolid.MICROCHIP,
                "SCAN_LINE"
        );

        // Live monitoring card.
        HomeFeatureCard monitorCard = new HomeFeatureCard(
                "Live Monitor",
                "View real-time hardware performance, power usage and emissions.",
                "Open Live Monitor",
                FontAwesomeSolid.CHART_LINE,
                "MONITOR_CHART"
        );

        // Keep references to the buttons for the existing navigation system.
        scanButton = scanCard.getActionButton();
        liveMonitorButton = monitorCard.getActionButton();

        // The layout can switch between two columns and two rows.
        GridLayout featureLayout = new GridLayout(1, 2, 24, 0);
        JPanel featureRow = new JPanel(featureLayout);
        featureRow.setOpaque(false);

        featureRow.add(scanCard);
        featureRow.add(monitorCard);

        // Centre the feature cards while limiting their combined width.
        JPanel featureContainer = new JPanel(new GridBagLayout());
        featureContainer.setOpaque(false);

        // Keep the cards at a consistent height.
        featureContainer.setPreferredSize(new Dimension(MAX_CONTENT_WIDTH, 360));
        featureContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        // Constrain the row to a maximum width, even on large windows.
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.add(featureRow, BorderLayout.CENTER);

        cardWrapper.setPreferredSize(new Dimension(MAX_CONTENT_WIDTH, 360));

        // Centre the card wrapper horizontally inside the available space.
        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx = 0;
        cardConstraints.gridy = 0;
        cardConstraints.anchor = GridBagConstraints.CENTER;

        featureContainer.add(cardWrapper, cardConstraints);

        column.add(featureContainer);


        // Adjust the card layout whenever the available width changes.
        featureContainer.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                int availableWidth = featureContainer.getWidth();

                // Switch to a vertical stack when two cards would be too narrow.
                boolean stacked = availableWidth < CARD_STACK_BREAKPOINT;

                if (stacked) {
                    featureLayout.setRows(2);
                    featureLayout.setColumns(1);
                    featureLayout.setHgap(0);
                    featureLayout.setVgap(20);

                } else {
                    featureLayout.setRows(1);
                    featureLayout.setColumns(2);
                    featureLayout.setHgap(24);
                    featureLayout.setVgap(0);
                }

                // Allow the card container to change height
                // Provide enough height for both cards when stacked.
                int cardHeight = stacked ? 740 : 360;
                cardWrapper.setPreferredSize( new Dimension( stacked ? Math.min(500, availableWidth) : MAX_CONTENT_WIDTH, cardHeight ) );
                featureContainer.setPreferredSize( new Dimension(MAX_CONTENT_WIDTH, cardHeight));
                featureContainer.setMaximumSize( new Dimension(Integer.MAX_VALUE, cardHeight) );
                featureContainer.revalidate();

                featureRow.revalidate();
                featureRow.repaint();


            }
        });





        // ---- Privacy note ----
        column.add(Box.createVerticalStrut(14));
        JLabel privacy = UIUtility.label(
                "Only hardware and system information is read. No personal files are accessed.",
                Font.PLAIN,
                AppTheme.FONT_SMALL_SIZE,
                AppTheme.TEXT_MUTED);
        privacy.setIcon(new LineIcon("SHIELD", 13, AppTheme.GREEN));
        privacy.setIconTextGap(6);
        column.add(UIUtility.alignComponent(privacy,FlowLayout.CENTER));

        // ---- "View scan history ->" link ----
//        historyButton = new JButton("View Scan History");
//        column.add(UIUtility.alignComponent(historyButton, FlowLayout.CENTER));


        // Allow the home page content to use the available horizontal space.
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        // Keep consistent spacing around the home page content.
        c.insets = new Insets(60, 32, 32, 32);

        page.add(column, c);


        return page;
    }

    public JButton getScanButton() {
        return scanButton;
    }

    // Allows the window controller to attach navigation to the Live Monitor button.
    public JButton getLiveMonitorButton() {
        return liveMonitorButton;
    }

    // TODO Move to separate class
    /** Home page background: plain colour plus two faint circles (top-right and bottom-left). */
    static class HomeBackground extends ScrollablePanel {
        HomeBackground() {
            super(new GridBagLayout());
            setBackground(AppTheme.BG_COLOR);
        }
    }

}