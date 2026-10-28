package com.example.monitortest.model.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class PawnIOService {

    /**
     * Represents the possible results of checking the PawnIO Windows service.
     */
    public enum PawnIOStatus {
        RUNNING,        // The PawnIO service is running.
        STOPPED,        // The service exists but is not running.
        NOT_INSTALLED,  // The PawnIO service was not found.
        CHECK_FAILED,   // An error prevented the status check.
        NOT_WINDOWS     // The operating system is not Windows.
    }

    /**
     * Checks whether the PawnIO service exists and is running.
     * Uses the Windows Service Control utility (sc.exe) to query the PawnIO service and interpret its output.
     * @return A PawnIOStatus describing the result.
     */
    public PawnIOStatus checkPawnIOStatus() {

        // Identify the operating system.
        // PawnIO is Windows-specific, so there is no reason to execute Windows commands on other platforms.
        String os = System.getProperty("os.name").toLowerCase();

        if (!os.contains("win")) {
            return PawnIOStatus.NOT_WINDOWS;
        }

        try {
            /*
             * Execute the Windows Service Control command:
             *
             * sc.exe query PawnIO
             *
             * ProcessBuilder allows Java to execute an external system command.
             *
             * redirectErrorStream(true) combines standard error and standard output into a single stream.
             */
            Process process = new ProcessBuilder( "sc.exe", "query", "PawnIO" ).redirectErrorStream(true).start();

            /*
             * Wait up to five seconds for the command to finish
             * This prevents the application from waiting indefinitely if the external process becomes stuck.
             */
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return PawnIOStatus.CHECK_FAILED;
            }

            /*
             * Read the output returned by the Windows command.
             *
             * readAllBytes() retrieves the process output, and StandardCharsets.UTF_8 converts it into a String so we can examine the service information.
             */
            String output = new String( process.getInputStream().readAllBytes(), StandardCharsets.UTF_8 );

            /*
             * Check whether the command completed successfully.
             *
             * Exit code 0 indicates success.
             * A non-zero exit code indicates an error.
             *
             * Windows error 1060 means the specified service does not exist as an installed service.
             */
            if (process.exitValue() != 0) {
                if (output.contains("1060")) {
                    return PawnIOStatus.NOT_INSTALLED;
                }

                return PawnIOStatus.CHECK_FAILED;
            }

            /*
             * Examine the returned service information.
             *
             * If the output contains RUNNING, PawnIO is active.
             * Otherwise, the service exists but is not running.
             */
            if (output.contains("RUNNING")) {
                return PawnIOStatus.RUNNING;
            }

            return PawnIOStatus.STOPPED;

        } catch (IOException e) {

            // Handle failures when starting or reading the external Windows command.
            return PawnIOStatus.CHECK_FAILED;

        } catch (InterruptedException e) {

            /*
             * Handle interruption while waiting for the process.
             *
             * Restore the thread's interrupted status so other parts of the application can detect the interruption.
             */
            Thread.currentThread().interrupt();
            return PawnIOStatus.CHECK_FAILED;
        }
    }

    /**
     * Attempts to start an existing PawnIO Windows driver.
     *
     * This method does not install PawnIO or request elevation.
     * The calling process must already have the necessary privileges.
     *
     * @return true if PawnIO is running after the start attempt,
     *         otherwise false.
     */
    public boolean startPawnIOService() {

        // Only attempt to start the driver on Windows.
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return false;
        }

        try {
            /*
             * Request Windows to start the registered PawnIO service.
             * The service must already exist on the system.
             */
            Process process = new ProcessBuilder("sc.exe", "start", "PawnIO").redirectErrorStream(true).start();

            /*
             * Wait for the service command to finish before reading its output.
             *
             * Redirect output to the null device so the process cannot block
             * because of an unread output stream.
             */
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }

            /*
             * Check the actual driver status rather than relying only
             * on the exit code from the start command.
             */
            return checkPawnIOStatus() == PawnIOStatus.RUNNING;

        } catch (IOException e) {

            // Windows could not execute or read the service command.
            return false;

        } catch (InterruptedException e) {

            // Preserve the thread's interruption status.
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
