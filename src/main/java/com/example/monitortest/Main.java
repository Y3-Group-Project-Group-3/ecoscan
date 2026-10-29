package com.example.monitortest;

import com.example.monitortest.services.pawnio.PawnIOInstallationManager;
import com.example.monitortest.services.pawnio.PawnIOInstaller;
import com.example.monitortest.services.pawnio.PawnIOService;
import com.example.monitortest.services.pawnio.PawnIOServiceManager;
import com.example.monitortest.services.startup.ApplicationElevationManager;
import com.example.monitortest.services.startup.ApplicationProcessManager;
import com.example.monitortest.services.startup.ApplicationStartupManager;


/**
 * Entry point for the EcoScan desktop application.
 * Initializes the required application services and managers,
 * then delegates startup operations to ApplicationStartupManager.
 */
public class Main{

    public static void main(String[] args) {

        // Create the manager responsible for PawnIO operations.
        PawnIOInstaller pawnIOInstaller = new PawnIOInstaller();

        // Create the service responsible for checking whether the PawnIO Windows driver is installed and running.
        PawnIOService pawnIOService = new PawnIOService();

        //Create the manager responsible for coordinating PawnIO installation and user approval.
        PawnIOInstallationManager installationManager = new PawnIOInstallationManager(pawnIOInstaller, pawnIOService);

        // Create a manager for Windows administrator privilege checks.
        ApplicationElevationManager elevationManager = new ApplicationElevationManager();

        // Create the manager responsible for coordinating PawnIO service operations and user approval
        PawnIOServiceManager serviceManager = new PawnIOServiceManager(pawnIOService, elevationManager);

        // Create a manager for information about the running process.
        ApplicationProcessManager processManager = new ApplicationProcessManager();

        // Create the manager responsible for coordinating EcoScan's application startup sequence.
        ApplicationStartupManager startupManager =
                new ApplicationStartupManager(
                        elevationManager,
                        processManager,
                        pawnIOService,
                        serviceManager,
                        installationManager
                );

        // Stop the current instance if an elevated EcoScan process was launched successfully.
        if (startupManager.handleAdministratorElevation()) {
            return;
        }

        // Initialize the application window and execute the remaining startup operations.
        startupManager.initializeApplicationWindow(() -> {
            // Check PawnIO and coordinate any required operations.
            startupManager.handlePawnIOStatus();
            // Print information about the application environment.
            startupManager.printStartupDiagnostics();
        });
    }
}