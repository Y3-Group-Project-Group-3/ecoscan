package com.example.monitortest.ui.results;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.ui.common.FitWidthPanel;
import com.example.monitortest.ui.history.model.SavedScan;
import com.example.monitortest.ui.history.storage.ScanHistoryCsvRepository;
import com.example.monitortest.ui.results.sections.ComponentSection;
import com.example.monitortest.ui.results.sections.ImpactSection;
import com.example.monitortest.ui.results.components.EmissionProjectionPanel;
import com.example.monitortest.ui.results.sections.ResultsToolbar;
import static com.example.monitortest.ui.theme.AppTheme.*;
import com.example.monitortest.ui.history.model.SavedScan;

import java.util.List;

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

    // Preserves the power mode selected for the latest hardware scan.
    private boolean latestScanIncludeEstimates = false;

    // Distinguishes historical results from the latest hardware scan.
    private boolean viewingSavedScan = false;

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

            // Preserve mode changes only when displaying the latest scan.
            // Historical scan selections must not overwrite this preference.
            // switching calculation modes while viewing history won't overwrite the latest scan's preferred mode.
            if (!viewingSavedScan) {
                latestScanIncludeEstimates = includeEstimates;
            }

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



    // Displays hardware information across all Results Page sections.
    // Used by both newly completed scans and saved historical scans.
    private void displayHardwareInfo(HardwareInfo info) {

        // Update the hardware component breakdown and power summary.
        componentSection.updateComponentSection(info);

        // Update the environmental impact and emissions projections.
        projectionPanel.updateHardwareInfo(info);
        impactSection.updateHardwareInfo(info);
    }


    /********************************* DISPLAY SAVED SCAN  ***************************/
    // Displays a saved hardware scan without performing another scan.
    // Preserves the latest completed scan for later restoration.
    public void displaySavedScan(SavedScan savedScan) {

        // Prevent a missing saved record from being displayed.
        if (savedScan == null) {
            throw new IllegalArgumentException( "Saved scan cannot be null." );
        }

        // Enter historical viewing mode before changing the power controls.
        // This protects the latest scan's preferred calculation mode.
        viewingSavedScan = true;

        // Restore the power mode that was selected when this scan was saved.
        componentSection.setIncludeEstimates(savedScan.isIncludeEstimates());

        // Display the original hardware information from CSV.
        displayHardwareInfo(savedScan.getHardwareInfo());

        // Show the saved scan's original timestamp and user-defined name.
        String formattedTime = savedScan.getScannedAt().format(SCAN_TIME_FORMAT);
        scanInfoLabel.setText( "Saved Scan: " + savedScan.getName() + " | Scanned " + formattedTime );

        // Prevent saving a historical record as if it were a new scan.
        saveButton.setEnabled(false);
    }

    /********************************* SHOW LATEST SCAN AFTER VIEWING HISTORY ***************************/
    // Restores the latest completed hardware scan after viewing history.
    // Does not rescan hardware or replace the original scan timestamp.
    public void restoreLatestScan() {

        // Avoid modifying the Results Page when no latest scan exists.
        if (currentHardwareInfo == null || currentScanTime == null) {
            return;
        }

        // Return to the normal hardware scan viewing mode.
        viewingSavedScan = false;

        // Restore the power calculation mode used by the latest scan.
        componentSection.setIncludeEstimates(latestScanIncludeEstimates);

        // Restore the latest hardware information and calculations.
        displayHardwareInfo(currentHardwareInfo);

        // Restore the latest scan's original timestamp.
        updateScanInfoFooter();

        // The latest completed scan is eligible to be saved.
        saveButton.setEnabled(true);
    }






    /*********************** UPDATE PAGE ***********************************/
    // Displays a newly completed hardware scan.
    // The latest scan is retained independently of any historical scans.
    public void updateHardwareInfo(HardwareInfo info) throws IOException {

        // A new hardware scan replaces the historical viewing state.
        viewingSavedScan = false;

        // The newly completed scan adopts the currently selected power mode.
        latestScanIncludeEstimates = includeEstimates;

        // Preserve the latest completed hardware scan and timestamp.
        currentHardwareInfo = info;
        currentScanTime = LocalDateTime.now();

        // Display the newly collected hardware information.
        displayHardwareInfo(info);

        // Update the footer using the new scan's timestamp.
        updateScanInfoFooter();

        // A new scan is eligible to be saved.
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