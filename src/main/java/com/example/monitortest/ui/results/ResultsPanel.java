package com.example.monitortest.ui.results;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.common.FitWidthPanel;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.HardwareInfoSection;
import com.example.monitortest.ui.results.sections.ImpactSection;
import com.example.monitortest.ui.results.components.EmissionProjectionPanel;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.results.sections.ComponentSection.createComponentSection;
import static com.example.monitortest.ui.results.sections.ResultsHeader.createHeader;
import static com.example.monitortest.ui.results.sections.ResultsHeader.refreshButton;

// EcoScan - Results page (view only, test data for now)
public class ResultsPanel extends JPanel {

    // Displays emissions projections based on the latest hardware scan.
    private final EmissionProjectionPanel projectionPanel = new EmissionProjectionPanel();
    // Keep a reference so the environmental impact values can be updated.
    private final ImpactSection impactSection = new ImpactSection();

    // Creates the scan information footer at the bottom of Results.
    private JPanel createScanInfoFooter() {

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        JLabel scanInfo = new JLabel( "Scanned 1 Oct 2026   •   My PC   •   Home use" );

        scanInfo.setForeground(TEXT_MUTED);
        footer.add(scanInfo);

        return footer;
    }

    public ResultsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(createMainContentPanel());

        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Keep the power mode controls visible while the Results page scrolls.
        add(createPowerModeToolbar(), BorderLayout.NORTH);

        add(scroll, BorderLayout.CENTER);


    }

    // Creates a fixed toolbar containing the power calculation mode controls.
    private JPanel createPowerModeToolbar() {

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setBackground(CARD_BG);
        // Add spacing and a subtle divider beneath the fixed toolbar.
        // Add a subtle bottom border while preserving the toolbar padding.
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 36, 12, 36)
        ));

        // Identify the setting that affects the Results page calculations.
        JLabel modeLabel = new JLabel("POWER CALCULATION MODE");
        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 12f));
        modeLabel.setForeground(TEXT_MAIN);

        toolbar.add(modeLabel, BorderLayout.WEST);

        // Group the power mode controls and refresh button together.
        JPanel toolbarActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        toolbarActions.setOpaque(false);

        // Reuse the existing power mode controls.
        JPanel controls = ComponentSection.getPowerModeControls();

        if (controls != null) {
            toolbarActions.add(controls);
        }

        // Add a subtle vertical divider between the power modes and refresh action.
        JPanel divider = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                // Smooth the divider's appearance.
                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));

                // Draw a line half the component's height, centred vertically.
                int centreX = getWidth() / 2;
                int lineHeight = getHeight() / 2;
                int startY = (getHeight() - lineHeight) / 2;

                g2.drawLine( centreX, startY, centreX, startY + lineHeight );

                g2.dispose();
            }
        };

        // Reserve a narrow area for the divider.
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(12, 42));

        toolbarActions.add(divider);

        // Keep the existing refresh button after the divider.
        toolbarActions.add(refreshButton);

        // Move the existing refresh button into the fixed toolbar.
        toolbarActions.add(refreshButton);
        toolbar.add(toolbarActions, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createMainContentPanel() {

        JPanel main = new FitWidthPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Initialise the refresh button without displaying the old Results header.
        createHeader();

        // Display the environmental impact section.
        main.add(impactSection.createImpactSection());
        main.add(Box.createVerticalStrut(28));

        // Keep the scan projection period synchronised with Environmental Impact.
        impactSection.setTimescaleListener(projectionPanel::setTimescale);

        // Display the new emissions milestone projection.
        projectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(projectionPanel);
        main.add(Box.createVerticalStrut(28));

        main.add(createComponentSection());

        // Keep the power summary, emissions projection and environmental impact in sync.
        ComponentSection.setPowerModeListener(includeEstimates -> {
            projectionPanel.setIncludeEstimates(includeEstimates);
            impactSection.setIncludeEstimates(includeEstimates);
        });

        /********* Component Breakdown  **********/
//        main.add(HardwareInfoSection.createHardwareInfoSection());

        /***************** Footer ************/
        // Separate the scan metadata from the main results.
        main.add(Box.createVerticalStrut(24));

        JPanel scanFooter = createScanInfoFooter();
        scanFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(scanFooter);

        return main;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    // Refresh both environmental sections using the latest hardware scan.
    public void updateHardwareInfo(HardwareInfo info) {
        projectionPanel.updateHardwareInfo(info);
        impactSection.updateHardwareInfo(info);
    }
}
