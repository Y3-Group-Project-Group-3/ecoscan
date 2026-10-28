package com.example.monitortest.model.services;


import javax.swing.JOptionPane;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


/**
 * Coordinates the PawnIO installation workflow.
 * Responsible for requesting user approval, launching the installer in the background, and reporting installation results.
 * Uses PawnIOInstaller for installer operations and PawnIOService for checking the driver status.
 */
public class PawnIOInstallationManager {

    private final PawnIOInstaller pawnIOInstaller;
    private final PawnIOService pawnIOService;


    /**
     * Asks the user whether they want to install PawnIO.
     * @return true if the user approves installation,
     *         false if they decline or close the dialog
     */
    public boolean requestInstallationApproval() {

        /*
         * Display the installation confirmation dialog.
         * PawnIO installation must be explicitly approved before the installer is launched.
         */
        int userChoice = JOptionPane.showConfirmDialog(
                null,
                "PawnIO is not installed.\n"
                        + "EcoScan requires PawnIO to access certain hardware sensors.\n\n"
                        + "Would you like to install PawnIO now?",
                "PawnIO Installation Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Record the user's decision for diagnostic purposes.
        boolean approved = userChoice == JOptionPane.YES_OPTION;
        System.out.println("PawnIO installation accepted: " + approved);

        return approved;
    }

    /**
     * Creates the installation manager using the existing installer and service components.
     *
     * @param pawnIOInstaller handles installer operations
     * @param pawnIOService handles Windows driver service operations
     */
    public PawnIOInstallationManager( PawnIOInstaller pawnIOInstaller, PawnIOService pawnIOService) {
        this.pawnIOInstaller = pawnIOInstaller;
        this.pawnIOService = pawnIOService;

    }

    /**
     * Starts the PawnIO installation workflow.
     * Runs the installation on a background thread to prevent blocking Swing's Event Dispatch Thread.
     */
    public void startInstallation() {

        /*
         * Create a dedicated thread for the installation process and start it asynchronously.
         */
        Thread installationThread = new Thread(this::executeInstallation, "PawnIO-Installer");
        installationThread.start();
    }

    /**
     * Executes the PawnIO installation process.
     *
     * Handles installer execution, result verification, and reporting installation outcomes.
     */
    private void executeInstallation() {

        try {

                // Locate and launch the verified PawnIO installer.
                Path installerPath = pawnIOInstaller.findPawnIOInstaller();
                Process installationProcess = pawnIOInstaller.launchPawnIOInstaller(installerPath);

                /*
                 * Read the combined standard output and error stream.
                 * This captures PowerShell messages, including errors caused by a failed installation or cancelled UAC prompt.
                 * Reading before waitFor() also prevents the process from blocking if its output buffer becomes full.
                 */
                String installationOutput = new String( installationProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8 );

                // Wait for PowerShell to finish and retrieve its exit code.  An exit code of zero normally indicates successful execution.
                int exitCode = installationProcess.waitFor();

                // Display the results for debugging.
                System.out.println("PawnIO installation exit code: " + exitCode);
                System.out.println("PawnIO installation output: " + installationOutput);

                /*
                 * Check the installer process result and report it to the user.
                 * A successful exit code is not sufficient on its own: verify that Windows now recognises the PawnIO service.
                 */
                PawnIOService.PawnIOStatus updatedStatus = pawnIOService.checkPawnIOStatus();

                // Delegate installation result reporting to a dedicated method that displays the appropriate Swing dialog.
                displayInstallationResult(exitCode, installationOutput, updatedStatus);



            } catch (IOException e) {

                /* * Delegate installation error reporting to the  dedicated method. */
                displayInstallationError(e);

        } catch (InterruptedException e) {

            // Preserve the interruption status of the background thread.
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Displays the appropriate result of a PawnIO installation.
     *
     * Handles successful installation, inactive drivers,
     * incomplete installations, UAC cancellation and failures.
     *
     * @param exitCode exit code returned by the installer
     * @param installationOutput output captured from PowerShell
     * @param updatedStatus PawnIO service status after installation
     */
    private void displayInstallationResult( int exitCode, String installationOutput, PawnIOService.PawnIOStatus updatedStatus) {

        /*
         * Ensure all installation result dialogs are displayed on Swing's Event Dispatch Thread.
         */
        // Display the result on Swing's Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {

            /*
             * Detect when the user cancels the Windows UAC prompt.
             * A cancelled installation is not treated as an application error because EcoScan can continue with limited sensors.
             */
            if (exitCode != 0 && installationOutput.toLowerCase(java.util.Locale.ROOT).contains("canceled by the user")) {

                JOptionPane.showMessageDialog(
                        null,
                        "PawnIO installation was cancelled.\n"
                                + "EcoScan will continue with limited hardware sensor access.",
                        "PawnIO Installation Cancelled",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if (exitCode == 0 && updatedStatus == PawnIOService.PawnIOStatus.RUNNING) {

                JOptionPane.showMessageDialog(
                        null,
                        "PawnIO was installed successfully and its driver is running.",
                        "PawnIO Installation Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );

                /*
                 * PawnIO has been installed and Windows recognises its service, but the driver has not started.
                 * Installation succeeded, although some hardware sensors may remain unavailable until the driver starts.
                 */
            }else if (exitCode == 0 && updatedStatus == PawnIOService.PawnIOStatus.STOPPED) {

                JOptionPane.showMessageDialog(
                        null,
                        "PawnIO was installed successfully, but its driver is not running.\n"
                                + "The driver may need to be started before additional "
                                + "hardware sensors become available.",
                        "PawnIO Installed - Driver Inactive",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            /*
             * The installer reported success, but Windows could not confirm that the PawnIO driver was installed correctly.
             *
             * This is treated as an incomplete installation rather than an installer execution failure.
             */
            else if (exitCode == 0) {

                JOptionPane.showMessageDialog(
                        null,
                        "PawnIO installation finished, but its driver could not be verified.\n\n"
                                + "Driver status: " + updatedStatus + "\n\n"
                                + "EcoScan will continue with limited hardware sensor access.",
                        "PawnIO Installation Incomplete",
                        JOptionPane.WARNING_MESSAGE
                );
            }

            /*
             * Handle installation failures that are not caused by the user cancelling the Windows UAC prompt.
             * Include the installer output when available to help identify why the installation was unsuccessful.
             */
            else {
                String errorDetails = installationOutput.isBlank() ? "No additional error information was provided." : installationOutput.trim();

                JOptionPane.showMessageDialog(
                        null,
                        "PawnIO installation could not be completed.\n\n"
                                + "Installer exit code: " + exitCode + "\n"
                                + "Driver status: " + updatedStatus + "\n\n"
                                + "Details:\n" + errorDetails,
                        "PawnIO Installation Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Displays an error when the PawnIO installation process cannot be started or encounters an IOException.
     * @param error the exception describing the failure
     */
    private void displayInstallationError(IOException error) {

        // Record the underlying error for debugging.
        System.err.println( "PawnIO installation failed: " + error.getMessage() );

        /* * Schedule the error dialog on Swing's Event Dispatch Thread to ensure thread-safe interaction with Swing components. */
        SwingUtilities.invokeLater(() -> {

            JOptionPane.showMessageDialog(
                    null,
                    "PawnIO installation could not be started.\n\n" + "Reason: " + error.getMessage(),
                    "PawnIO Installation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * Finds the bundled PawnIO installer.
     * @return path to the installer, if found
     */
    public Path getInstallerPath() {
        return pawnIOInstaller.findPawnIOInstaller();
    }

    /**
     * Checks whether the installer matches its expected checksum.
     * @param installer path to the installer
     * @return true if verification succeeds
     */
    public boolean isInstallerVerified(Path installer) { return pawnIOInstaller.verifyPawnIOInstaller(installer);}
}