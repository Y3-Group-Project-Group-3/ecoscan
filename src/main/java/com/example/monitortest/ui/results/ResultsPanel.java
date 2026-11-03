package com.example.monitortest.ui.results;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.common.FitWidthPanel;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.CsvUtility;
import com.example.monitortest.ui.history.storage.SavedScanCsvMapper;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.ImpactSection;
import com.example.monitortest.ui.results.components.EmissionProjectionPanel;
import com.example.monitortest.ui.results.sections.ResultsToolbar;
import static com.example.monitortest.ui.theme.AppTheme.*;


import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public class ResultsPanel extends JPanel {

    // Displays emissions projections based on the latest hardware scan.
    private final EmissionProjectionPanel projectionPanel = new EmissionProjectionPanel();

    // Keep a reference so the environmental impact values can be updated.
    private final ImpactSection impactSection = new ImpactSection();

    // Owns the Component Breakdown displayed on this Results Page.
    private final ComponentSection componentSection = new ComponentSection();

    // Displays the completion time of the most recent successful scan.
    private final JLabel scanInfoLabel = new JLabel();

    // Formats scan timestamps using the user's local date and time.
    private static final DateTimeFormatter SCAN_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu 'at' HH:mm");

    private final JButton refreshButton = ResultsToolbar.createRefreshButton();


    // Creates the footer displaying information about the latest scan.
    private JPanel createScanInfoFooter() {

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        // Configure the scan information label.
        scanInfoLabel.setForeground(TEXT_MUTED);

        // The timestamp is populated after a successful hardware scan.
        footer.add(scanInfoLabel);

        return footer;
    }


    // Updates the footer when a completed hardware scan is applied.
    private void updateScanInfoFooter() {

        // Capture the current local date and time.
        LocalDateTime scanTime = LocalDateTime.now();

        // Format the timestamp for display in the Results footer.
        String formattedTime = scanTime.format(SCAN_TIME_FORMAT);

        scanInfoLabel.setText("Scanned " + formattedTime);
    }



    public ResultsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(createMainContentPanel());

        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Retrieve the power controls from this Results Page's Component Breakdown.
        JPanel powerModeControls = componentSection.getPowerModeControls();

        // Construct the fixed toolbar using the existing controls and refresh button.
        add( ResultsToolbar.createToolbar(refreshButton, powerModeControls), BorderLayout.NORTH );

        add(scroll, BorderLayout.CENTER);

        // Connect the Results sections after their UI components are initialised.
        configureSectionListeners();
    }

    private JPanel createMainContentPanel() {

        JPanel main = new FitWidthPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_COLOR);
        main.setBorder(new EmptyBorder(28, 36, 36, 36));

        // Display the environmental impact section.
        main.add(impactSection.createImpactSection());
        main.add(Box.createVerticalStrut(28));

        // Display the new emissions milestone projection.
        projectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(projectionPanel);
        main.add(Box.createVerticalStrut(28));

        // Add this Results Page's Component Breakdown.
        main.add(componentSection.createComponentSection());

        /***************** Footer ************/
        // Separate the scan metadata from the main results.
        main.add(Box.createVerticalStrut(24));

        JPanel scanFooter = createScanInfoFooter();
        scanFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(scanFooter);

        return main;
    }


    // Connects interactions between the Results Page sections.
    private void configureSectionListeners() {

        // Synchronise the emissions projection with the selected timescale.
        impactSection.setTimescaleListener(projectionPanel::setTimescale);

        // Keep environmental calculations in sync with the selected power mode.
        componentSection.setPowerModeListener(includeEstimates -> {
            projectionPanel.setIncludeEstimates(includeEstimates);
            impactSection.setIncludeEstimates(includeEstimates);
        });
    }


    // Returns the Results toolbar refresh button for listener registration.
    public JButton getRefreshButton() {
        return refreshButton;
    }


    // Refreshes all Results Page sections after a successful hardware scan.
    public void updateHardwareInfo(HardwareInfo info) throws IOException {

        // Update the Component Breakdown and power summary.
        componentSection.updateComponentSection(info);

        // Update environmental impact and emissions projections.
        projectionPanel.updateHardwareInfo(info);
        impactSection.updateHardwareInfo(info);

        // Display when the latest scan results were applied.
        updateScanInfoFooter();


// TEMPORARY TEST: Confirm that the existing scan history remains readable.
        ScanHistoryCsvRepository repository = new ScanHistoryCsvRepository();

        try {

            // Load the records saved during our previous test.
            List<SavedScan> scans = repository.loadAll();

            System.out.println("Saved scans: " + scans.size());

            // Display the names and dates of the stored records.
            for (SavedScan scan : scans) {
                System.out.println(
                        scan.getName() + " - " + scan.getScannedAt()
                );
            }

        } catch (IOException e) {

            // Report any failure to read the existing CSV file.
            e.printStackTrace();
        }


    }



}