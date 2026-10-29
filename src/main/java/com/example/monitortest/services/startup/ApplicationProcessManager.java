
package com.example.monitortest.services.startup;

import java.nio.file.Path;

/**
 * Provides information about the process currently
 * running EcoScan.
 *
 * This information will eventually help us identify
 * the correct executable when restarting the application.
 */
public class ApplicationProcessManager {

    /**
     * Retrieves the executable used to launch the current Java process.
     *
     * During development, this will normally be java.exe from the JDK.
     * In a packaged EcoScan application, it may be EcoScan.exe.
     *
     * This method only identifies the executable.
     * It does not launch or restart any processes.
     *
     * @return The executable path, or null if unavailable.
     */
    public Path getApplicationExecutable() {

        /*
         * ProcessHandle.current() represents the currently running Java process.
         *
         * info().command() retrieves the executable that started that process, if the operating system provides the information.
         */
        return ProcessHandle.current()
                .info()
                .command()
                .map(Path::of)
                .orElse(null);
    }

    /**
     * Checks whether EcoScan is running through its packaged Windows executable.
     * @return true if the current executable is EcoScan.exe, otherwise false.
     */
    public boolean isPackagedApplication() {

        /*
         * Retrieve the executable associated with the current process.
         * Return false if Windows cannot provide its path.
         */
        Path executable = getApplicationExecutable();

        if (executable == null) { return false; }

        /*
         * Compare the executable filename rather than its full path.
         * This allows EcoScan to run from different installation directories.
         */
        return executable.getFileName()
                .toString()
                .equalsIgnoreCase("EcoScan.exe");
    }


    /**
     * Retrieves the arguments used to launch the current Java process.
     *
     * These arguments can help us understand how EcoScan was started during development.
     *
     * @return An array of launch arguments, or an empty array if the information is unavailable.
     */
    public String[] getApplicationLaunchArguments() {

        /*
         * Retrieve information about the currently running process.
         * arguments() returns the process command-line arguments when the operating system makes them available.
         */
        return ProcessHandle.current()
                .info()
                .arguments()
                .orElse(new String[0]);
    }

    /**
     * Retrieves the command line used to launch the current
     * Java process, if the operating system makes it available.
     *
     * This is useful for diagnosing how IntelliJ starts EcoScan.
     *
     * @return The process command line, or a message indicating
     *         that the information is unavailable.
     */
    public String getApplicationCommandLine() {

        /*
         * Retrieve the current process information.
         * commandLine() attempts to return the executable together with its original launch arguments.
         */
        return ProcessHandle.current()
                .info()
                .commandLine()
                .orElse("Command line information unavailable.");
    }




}
