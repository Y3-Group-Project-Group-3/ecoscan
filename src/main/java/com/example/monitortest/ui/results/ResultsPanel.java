package com.example.monitortest.ui.results;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.common.FitWidthPanel;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.ImpactSection;
import com.example.monitortest.ui.results.components.EmissionProjectionPanel;
import com.example.monitortest.ui.results.sections.ResultsToolbar;

import static com.example.monitortest.ui.theme.AppTheme.*;
import static com.example.monitortest.ui.results.sections.ComponentSection.createComponentSection;

public class ResultsPanel extends JPanel {

    // Displays emissions projections based on the latest hardware scan.
    private final EmissionProjectionPanel projectionPanel = new EmissionProjectionPanel();
    // Keep a reference so the environmental impact values can be updated.
    private final ImpactSection impactSection = new ImpactSection();
    private final JButton refreshButton = ResultsToolbar.createRefreshButton();

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

        // Keep the Results toolbar visible while the page scrolls.
        add(ResultsToolbar.createToolbar(refreshButton), BorderLayout.NORTH);

        add(scroll, BorderLayout.CENTER);


    }

    private JPanel createMainContentPanel() {

        JPanel main = new FitWidthPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(28, 36, 36, 36));

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

        /***************** Footer ************/
        // Separate the scan metadata from the main results.
        main.add(Box.createVerticalStrut(24));

        JPanel scanFooter = createScanInfoFooter();
        scanFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(scanFooter);

        return main;
    }

    // Returns the Results toolbar refresh button for listener registration.
    public JButton getRefreshButton() {
        return refreshButton;
    }

    // Refresh both environmental sections using the latest hardware scan.
    public void updateHardwareInfo(HardwareInfo info) {
        projectionPanel.updateHardwareInfo(info);
        impactSection.updateHardwareInfo(info);
    }
}
