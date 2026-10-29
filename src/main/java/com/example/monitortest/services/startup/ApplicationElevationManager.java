package com.example.monitortest.services.startup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Used for filesystem paths and process timeouts.
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;


/**
 * Handles Windows administrator privilege checks and eventually, requests to restart EcoScan with elevation.
 *
 * This class is separate from PawnIOManager because
 * administrator privileges concern the application itself,
 * rather than the PawnIO driver specifically.
 */
public class ApplicationElevationManager {

    /**
     * Represents the outcome of requesting an elevated application launch.
     */
    public enum ElevationResult {
        STARTED,       // Windows accepted the elevated launch request.
        CANCELLED,     // The user declined the UAC prompt.
        FAILED,        // An error prevented the launch.
        ALREADY_ADMIN, // EcoScan is already elevated.
        UNSUPPORTED    // Not running as a packaged Windows executable.
    }


    /**
     * Checks whether the current EcoScan process is running with elevated administrator privileges on Windows.
     * Uses the Windows "whoami" utility to examine the security groups and integrity level of the process.
     *
     * @return true if running at high integrity, otherwise false.
     */
    public boolean isRunningAsAdministrator() {

        // Administrator elevation checks only apply to Windows.
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return false;
        }

        try {
            /*
             * Run the Windows command "whoami /groups".
             * This displays the security groups and integrity levels associated with the current process.
             */
            Process process = new ProcessBuilder("whoami.exe", "/groups").redirectErrorStream(true).start();

            /*
             * Read the output while the command runs, then wait for it to finish.
             * The command normally returns a small amount of data.
             */
            String output = new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8);

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            /*
             * S-1-16-12288 identifies the Windows High Mandatory Integrity Level, normally used by elevated processes.             *
             * Check the command succeeded before inspecting its output.
             */
            return process.exitValue() == 0 && output.contains("S-1-16-12288");

        } catch (IOException e) {

            // The Windows command could not be executed or read.
            return false;

        } catch (InterruptedException e) {

            // Preserve the interruption signal for the calling thread.
            Thread.currentThread().interrupt();
            return false;
        }
    }


    /**
     * Requests administrator privileges by launching the specified executable
     * through Windows User Account Control (UAC).
     *
     * @param executable the path to the executable to launch with elevation
     * @return true if Windows accepted the launch request, otherwise false
     */
    public boolean requestAdministratorElevation(Path executable) {

        /*
         * Validate the executable before attempting elevation.
         * Only Windows supports this elevation mechanism.
         */
        if (!System.getProperty("os.name").toLowerCase().contains("win")
                || executable == null
                || !Files.isRegularFile(executable)) {
            return false;
        }

        try {
            /*
             * Pass the executable path through an environment variable.
             * This avoids inserting the path directly into PowerShell code
             * and handles paths containing spaces or special characters.
             */
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    /*
                     * Attempt to relaunch EcoScan with administrator privileges.
                     * If the user cancels UAC, PowerShell catches the error and * exits with code 1 without printing the exception.
                     * A successful elevation request exits with code 0.
                     */
                    "-Command",
                    "try { " +
                            "    $ErrorActionPreference = 'Stop'; " +
                            "    Start-Process -FilePath $env:ECOSCAN_ELEVATION_TARGET -Verb RunAs; " +
                            "    exit 0; " +
                            "} catch { " +
                            "    exit 1; " +
                            "}"
            );

            /*
             * Supply the executable path to the child PowerShell process.
             * The packaged EcoScan.exe will be the elevation target.
             */
            processBuilder.environment().put(
                    "ECOSCAN_ELEVATION_TARGET",
                    executable.toAbsolutePath().toString()
            );

            Process process = processBuilder.inheritIO().start();

            /*
             * Wait for PowerShell to complete the launch request.
             * A successful exit indicates that Windows accepted the request,
             * not that the elevated application finished running.
             */
            return process.waitFor() == 0;

        } catch (IOException e) {

            // PowerShell could not be started.
            return false;

        } catch (InterruptedException e) {

            // Preserve the interruption signal.
            Thread.currentThread().interrupt();
            return false;
        }
    }

}