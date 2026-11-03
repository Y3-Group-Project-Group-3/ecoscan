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
import java.util.UUID;


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


    /************************************** csv ************************/
    // Save action for the currently displayed hardware scan.
    private final JButton saveButton = ResultsToolbar.createSaveButton();

    // Stores the hardware information currently displayed on the Results Page.
    private HardwareInfo currentHardwareInfo;

    // Preserves the original timestamp of the displayed hardware scan.
    private LocalDateTime currentScanTime;

    // Tracks whether estimated RAM and storage power are included.
    private boolean includeEstimates = false;

    // Handles saving and loading hardware scans from CSV.
    private final ScanHistoryCsvRepository scanRepository = new ScanHistoryCsvRepository();
    /**************************************************************************/


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

        // Use the timestamp associated with the displayed hardware scan.
        LocalDateTime scanTime = currentScanTime;

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

        // Construct the toolbar with both Refresh and Save Scan actions.
        add( ResultsToolbar.createToolbar( refreshButton, saveButton, powerModeControls ), BorderLayout.NORTH );

        add(scroll, BorderLayout.CENTER);

        // Connect the Results sections after their UI components are initialised.
        configureSectionListeners();

        /***************** csv *************************/

        // Connect the Save Scan button to the naming dialog and CSV repository.
        saveButton.addActionListener(e -> {

            // Ask the user to choose a name for the current scan.
            String name = requestScanName();

            // Only save when the user provides a valid name.
            if (name != null) {
                saveCurrentScan(name);
            }
        });

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
            // Preserve the setting for the saved scan.
            this.includeEstimates = includeEstimates;

            // Update the existing Results Page sections.
            projectionPanel.setIncludeEstimates(includeEstimates);
            impactSection.setIncludeEstimates(includeEstimates);
        });
    }


    // Returns the Results toolbar refresh button for listener registration.
    public JButton getRefreshButton() {
        return refreshButton;
    }


    /********************************* CSV *******************************/
    // Returns the Save Scan button for event listener registration.
    public JButton getSaveButton() { return saveButton; }

    // Requests a user-defined name for the currently displayed hardware scan.
    // Returns null when the user cancels the dialog.
    private String requestScanName() {

        while (true) {

            // Display the naming dialog using Swing's built-in input component.
            String name = JOptionPane.showInputDialog( this, "Enter a name for this scan:", "Save Hardware Scan", JOptionPane.PLAIN_MESSAGE );

            // Cancel or closing the dialog means no name was provided.
            if (name == null) { return null; }

            // Remove leading and trailing whitespace.
            name = name.trim();

            // Return the entered name when it is valid.
            if (!name.isEmpty()) { return name; }

            // Explain the validation issue and allow another attempt.
            JOptionPane.showMessageDialog( this, "Please enter a name for the scan.", "Invalid Scan Name", JOptionPane.WARNING_MESSAGE );
        }
    }


    // Saves the currently displayed hardware scan using the user-defined name.
// Displays a confirmation or error message after the operation.
    private void saveCurrentScan(String name) {

        // Prevent saving when no completed hardware scan is available.
        if (currentHardwareInfo == null || currentScanTime == null) {
            JOptionPane.showMessageDialog( this, "No hardware scan is available to save.", "Save Scan", JOptionPane.WARNING_MESSAGE );
            return;
        }

        // Create a saved record using the original scan information.
        SavedScan savedScan = new SavedScan( UUID.randomUUID(), name, currentScanTime, currentHardwareInfo, includeEstimates );

        try {
            // Persist the scan using the existing CSV repository.
            scanRepository.save(savedScan);

            // Confirm that the scan was saved successfully.
            JOptionPane.showMessageDialog( this, "Hardware scan saved successfully as:\n" + name, "Scan Saved", JOptionPane.INFORMATION_MESSAGE );

        } catch (IOException | IllegalArgumentException e) {
            // Explain the failure without closing the Results Page.
            JOptionPane.showMessageDialog( this, "Unable to save the hardware scan:\n" + e.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE );
        }
    }




    /*********************** UPDATE PAGE ***********************************/
    // Refreshes all Results Page sections after a successful hardware scan.
    public void updateHardwareInfo(HardwareInfo info) throws IOException {

        // Preserve the completed scan and its timestamp for saving.
        currentHardwareInfo = info;
        currentScanTime = LocalDateTime.now();

        // Update the Component Breakdown and power summary.
        componentSection.updateComponentSection(info);

        // Update environmental impact and emissions projections.
        projectionPanel.updateHardwareInfo(info);
        impactSection.updateHardwareInfo(info);

        // Display when the latest scan results were applied.
        updateScanInfoFooter();

        // Allow the user to save the successfully completed hardware scan.
        saveButton.setEnabled(true);


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