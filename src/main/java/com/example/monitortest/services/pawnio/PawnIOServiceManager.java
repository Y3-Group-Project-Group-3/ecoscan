package com.example.monitortest.services.pawnio;

import com.example.monitortest.services.startup.ApplicationElevationManager;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Coordinates PawnIO driver service management.
 * Responsible for handling driver status, requesting user approval and coordinating service startup.
 * Uses PawnIOService for Windows service operations and ApplicationElevationManager for privilege checks.
 */
public class PawnIOServiceManager {

    private final PawnIOService pawnIOService;
    private final ApplicationElevationManager elevationManager;

    /**
     * Creates a manager for coordinating PawnIO service operations.
     *
     * @param pawnIOService handles Windows driver service operations
     * @param elevationManager handles administrator privilege checks
     */
    public PawnIOServiceManager( PawnIOService pawnIOService, ApplicationElevationManager elevationManager) {
        this.pawnIOService = pawnIOService;
        this.elevationManager = elevationManager;
    }

    /**
     * Asks the user whether they want to start the PawnIO driver.
     *
     * This method should only be called when PawnIO is installed
     * but its Windows service is stopped.
     *
     * @return true if the user approves starting PawnIO,
     *         false if they decline or close the dialog
     */
    public boolean requestServiceStartApproval() {

        /*
         * Ask for explicit permission before attempting to  start the PawnIO Windows driver service
         * Declining allows EcoScan to continue operating  with limited hardware sensor access.
         */
        int choice = JOptionPane.showConfirmDialog(
                null,
                "PawnIO is installed but its driver is not running.\n"
                        + "Would you like EcoScan to start it?",
                "PawnIO Driver Inactive",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Return true only when the user explicitly selects Yes.
        return choice == JOptionPane.YES_OPTION;
    }

    /**
     * Attempts to start the PawnIO service after receiving user approval.
     * Checks administrator privileges before starting the service.
     * The service operation runs in the background to keep the Swing interface responsive.
     */
    public void startService() {

        // Starting the PawnIO Windows driver requires administrator privileges.
        if (!elevationManager.isRunningAsAdministrator()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Starting PawnIO requires administrator privileges.\n"
                            + "Please restart EcoScan as administrator to enable "
                            + "additional hardware sensors.",
                    "Administrator Privileges Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        // Start PawnIO on a background thread so the Swing interface remains responsive.
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            /**
             * Attempts to start the PawnIO Windows service.
             * @return true if the service starts successfully
             */
            @Override
            protected Boolean doInBackground() {
                return pawnIOService.startPawnIOService();
            }

            /**
             * Reports the service-start result on Swing's
             * Event Dispatch Thread.
             */
            @Override
            protected void done() {
                try {
                    boolean started = get();

                    JOptionPane.showMessageDialog(
                            null,
                            started
                                    ? "PawnIO started successfully."
                                    : "PawnIO could not be started. Some hardware sensors may be unavailable.",
                            "PawnIO Service",
                            started
                                    ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.WARNING_MESSAGE
                    );

                } catch (Exception e) {

                    // Report unexpected failures without crashing EcoScan.
                    System.err.println("PawnIO start failed: " + e.getMessage());
                }
            }
        };

        // Begin the background service-start operation.
        worker.execute();
    }
}