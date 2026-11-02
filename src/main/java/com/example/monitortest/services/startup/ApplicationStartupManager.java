package com.example.monitortest.services.startup;

import java.nio.file.Path;

import com.example.monitortest.services.pawnio.PawnIOInstallationManager;
import com.example.monitortest.services.pawnio.PawnIOService;
import com.example.monitortest.services.pawnio.PawnIOServiceManager;
import com.example.monitortest.ui.window.CreateWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


/**
 * Coordinates the EcoScan application startup process.
 *
 * Responsible for initializing the application, checking administrator privileges, and coordinating the initial
 * PawnIO service and installation workflows.
 *
 * Delegates individual operations to their respective managers.
 */
public class ApplicationStartupManager {

    private final ApplicationElevationManager elevationManager;
    private final ApplicationProcessManager processManager;
    private final PawnIOService pawnIOService;
    private final PawnIOServiceManager serviceManager;
    private final PawnIOInstallationManager installationManager;

    /**
     * Creates the application startup manager.
     *
     * @param elevationManager handles administrator privileges
     * @param processManager handles application process information
     * @param pawnIOService checks the PawnIO Windows driver
     * @param serviceManager coordinates PawnIO service operations
     * @param installationManager coordinates PawnIO installation
     */
    public ApplicationStartupManager(
            ApplicationElevationManager elevationManager,
            ApplicationProcessManager processManager,
            PawnIOService pawnIOService,
            PawnIOServiceManager serviceManager,
            PawnIOInstallationManager installationManager) {

        /*
         * Store the existing components so startup operations
         * can be delegated without creating duplicate instances.
         */
        this.elevationManager = elevationManager;
        this.processManager = processManager;
        this.pawnIOService = pawnIOService;
        this.serviceManager = serviceManager;
        this.installationManager = installationManager;


    }

    /**
     * Checks whether EcoScan is running on Windows.
     *
     * Windows-specific functionality, including UAC elevation
     * and PawnIO driver management, must not run on macOS.
     *
     * @return true if the current operating system is Windows.
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name", "");

        return osName.toLowerCase(java.util.Locale.ROOT).startsWith("windows");
    }

    /**
     * Checks whether EcoScan needs to request administrator privileges.
     *
     * Automatic elevation is only requested when running as the packaged EcoScan executable, not through IntelliJ.
     *
     * @return true if an elevated instance was successfully launched
     *         and the current instance should stop;
     *         false if normal startup should continue
     */
    public boolean handleAdministratorElevation() {

        // Windows administrator elevation is not supported or required on macOS.
        if (!isWindows()) {
            System.out.println("Non-Windows operating system detected. Skipping administrator elevation.");
            return false;
        }

        /*
         * Only request automatic elevation for the packaged application.
         * IntelliJ launches should continue without an elevation request.
         */
        if (processManager.isPackagedApplication() && !elevationManager.isRunningAsAdministrator()) {

            //Retrieve the executable and request Windows UAC elevation.
            Path executable = processManager.getApplicationExecutable();

            boolean elevationRequested =
                    elevationManager.requestAdministratorElevation(executable);

            /*
             * If the elevated application was launched successfully,the current instance should stop.
             * If elevation was declined, continue with limited sensors.
             */
            if (elevationRequested) {
                return true;
            }

            System.out.println( "Administrator elevation was declined or unavailable. " + "Continuing with limited sensor access." );
        }

        // Continue normal application startup.
        return false;
    }

    /**
     * Creates and displays EcoScan's main application window.
     * Ensures that Swing components are initialized on the Event Dispatch Thread.
     * @param afterWindowCreated additional startup operations to execute after the window is created
     */
    public void initializeApplicationWindow(Runnable afterWindowCreated) {

        // Initialize Swing on the Event Dispatch Thread.Once the window is visible, execute the remaining  startup operations.
        SwingUtilities.invokeLater(() -> {

            // Create and display the main EcoScan window.
            CreateWindow window = new CreateWindow();
            window.createAndShowGUI();

            // Continue startup after the window has been created. This preserves the existing initialization order.
            if (afterWindowCreated != null) {
                afterWindowCreated.run();
            }
        });
    }


    /**
     * Checks the PawnIO driver status without blocking Swing's Event Dispatch Thread.
     */
    public void handlePawnIOStatus() {

        // PawnIO is only supported on Windows.
        if (!isWindows()) {
            System.out.println("Non-Windows operating system detected. Skipping PawnIO initialization.");
            return;
        }

        // Check the Windows driver status on a background thread.
        SwingWorker<PawnIOService.PawnIOStatus, Void> worker = new SwingWorker<>() {

            @Override
            protected PawnIOService.PawnIOStatus doInBackground() {
                return pawnIOService.checkPawnIOStatus();
            }

            @Override
            protected void done() {
                try {
                    // Retrieve the completed status check on Swing's EDT.
                    PawnIOService.PawnIOStatus pawnIOStatus = get();

                    System.out.println("PawnIO Status: " + pawnIOStatus);

                    // Request approval if the driver is installed but stopped.
                    if (pawnIOStatus == PawnIOService.PawnIOStatus.STOPPED) {
                        boolean approved = serviceManager.requestServiceStartApproval();

                        if (approved) {
                            serviceManager.startService();
                        }
                    }

                    // Request approval if PawnIO needs installing.
                    else if (pawnIOStatus == PawnIOService.PawnIOStatus.NOT_INSTALLED) {
                        boolean approved = installationManager.requestInstallationApproval();

                        if (approved) {
                            installationManager.startInstallation();
                        }
                    }

                } catch (InterruptedException e) {
                    // Preserve the interruption status.
                    Thread.currentThread().interrupt();

                } catch (java.util.concurrent.ExecutionException e) {
                    // Report an unexpected failure in the background status check.
                    System.err.println("PawnIO status check failed: " + e.getMessage());
                }
            }
        };

        // Start the status check without blocking the interface.
        worker.execute();
    }


    /**
     * Prints diagnostic information about EcoScan's  runtime environment and PawnIO installer.
     * Useful for debugging both IntelliJ launches and packaged application builds.
     */
    public void printStartupDiagnostics() {

        // Windows-specific diagnostics are not applicable on macOS.
        if (!isWindows()) {
            System.out.println("EcoScan running on a non-Windows operating system.");
            System.out.println("Windows-specific diagnostics skipped.");
            return;
        }

        // Locate and verify the bundled PawnIO installer.
        Path installer = installationManager.getInstallerPath();

        System.out.println("PawnIO installer: " + installer);
        System.out.println( "PawnIO installer verified: " + installationManager.isInstallerVerified(installer));

        // Display administrator privileges and information about the running executable.
        System.out.println( "EcoScan running as administrator: " + elevationManager.isRunningAsAdministrator() );
        System.out.println( "EcoScan executable: " + processManager.getApplicationExecutable() );

        // Print application launch arguments.
        String[] launchArguments = processManager.getApplicationLaunchArguments();
        System.out.println("EcoScan launch arguments:");

        for (String argument : launchArguments) { System.out.println("  " + argument); }

        // Display the command line and packaging status.
        System.out.println( "EcoScan command line: " + processManager.getApplicationCommandLine() );
        System.out.println( "EcoScan packaged application: " + processManager.isPackagedApplication());
    }
}