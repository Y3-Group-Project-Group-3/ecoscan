package com.example.monitortest.views;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.monitortest.views.results.components.*;
import com.example.monitortest.views.sections.*;
import com.example.monitortest.theme.AppTheme.*;

import static com.example.monitortest.theme.AppTheme.*;
import static com.example.monitortest.views.sections.ComparisonSection.createComparisonSection;
import static com.example.monitortest.views.sections.ComponentSection.createComponentSection;
import static com.example.monitortest.views.sections.ResultsHeader.createHeader;
import static com.example.monitortest.views.sections.ResultsHeader.refreshButton;
import static com.example.monitortest.views.sections.ScoreRecommendationSection.createScoreAndRecommendation;

// EcoScan - Results page (view only, test data for now)
public class ResultsPanelNew extends JPanel {

    // temp back button
    private JButton backButton;

    public ResultsPanelNew() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(createMainContentPanel());

        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // temp back button
        backButton = new JButton("Back to Home");
        add(backButton, BorderLayout.SOUTH);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createMainContentPanel() {
        JPanel main = new FitWidthPanel();
        ImpactSection impactSection = new ImpactSection();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(28, 36, 36, 36));

        main.add(createHeader());
        main.add(Box.createVerticalStrut(18));
        main.add(createScoreAndRecommendation());
        main.add(Box.createVerticalStrut(28));
        main.add(impactSection.createImpactSection());
        main.add(Box.createVerticalStrut(28));
        main.add(createComparisonSection());
        main.add(Box.createVerticalStrut(28));

        main.add(createComponentSection());

        main.add(Box.createVerticalStrut(28));
        main.add(HardwareInfoSection.createHardwareInfoSection());

        return main;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

}
