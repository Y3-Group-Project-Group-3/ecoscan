package com.example.monitortest.model.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PawnIOInstaller {

    /**
     * Locates the PawnIO installer in either the development project
     * or the packaged EcoScan application's installers directory.
     *
     * This method only checks whether the installer exists.
     * It does not execute, install, or verify the installer.
     *
     * @return The installer's absolute Path if found, otherwise null.
     */
    public Path findPawnIOInstaller() {

        /*
         * First check the development project's installers directory.
         * This preserves the existing behaviour when running in IntelliJ.
         */
        Path developmentInstaller = Path.of("installers", "PawnIO_setup.exe");

        if (Files.isRegularFile(developmentInstaller)) {
            return developmentInstaller.toAbsolutePath();
        }

        /*
         * In a jpackage application image, the java.home property normally points to EcoScan/runtime.
         * Its parent is the EcoScan application directory.
         */
        Path applicationDirectory = Path.of( System.getProperty("java.home") ).getParent();

        if (applicationDirectory != null) {

            /*
             * Look for the installer in EcoScan/app/installers.
             */
            Path packagedInstaller = applicationDirectory
                    .resolve("app")
                    .resolve("installers")
                    .resolve("PawnIO_setup.exe");

            if (Files.isRegularFile(packagedInstaller)) {
                return packagedInstaller.toAbsolutePath();
            }
        }

        // Neither installer location contains the expected file.
        return null;
    }

    /**
     * Verifies that the PawnIO installer matches the expected SHA-256 hash on the pawn io release github
     * https://github.com/namazso/PawnIO.Setup/releases?utm_source=chatgpt.com
     *
     * This helps detect an installer that has been corrupted or modified.
     *
     * @param installerPath The location of the installer executable.
     * @return true if the checksum matches, otherwise false.
     */
    public boolean verifyPawnIOInstaller(Path installerPath) {

        // Expected checksum from the official PawnIO release.
        // Only use this value after confirming the complete hash on GitHub.
//        String expectedHash = "1F519A22E47187F70A1379A48CA604981C4FCF694F4E65B734AA74A9FBA3032";
        String expectedHash = "1f519a22e47187f70a1379a48ca604981c4fcf694f4e65b734aaa74a9fba3032";

        // Reject missing or invalid installer paths.
        if (installerPath == null || !Files.isRegularFile(installerPath)) {
            return false;
        }

        try {
            // Create a SHA-256 digest and calculate the file's checksum.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(installerPath);
            byte[] hashBytes = digest.digest(fileBytes);

            // Convert the calculated checksum into hexadecimal text.
            String actualHash = HexFormat.of().formatHex(hashBytes);

            // Compare the calculated checksum against the expected value.
            return actualHash.equalsIgnoreCase(expectedHash);

        } catch (IOException | NoSuchAlgorithmException e) {

            // Verification fails if the file cannot be read
            // or the hashing algorithm is unavailable.
            return false;
        }
    }

    /**
     * Starts the verified PawnIO installer with administrator privileges.
     *
     * This method must only be called after the user has explicitly
     * agreed to install PawnIO.
     *
     * It starts a PowerShell process that requests Windows UAC
     * elevation and waits for the installer to finish.
     *
     * @param installerPath The location of PawnIO_setup.exe.
     * @return The PowerShell process managing installation.
     * @throws IOException If the installer is invalid or cannot be launched.
     */
    public Process launchPawnIOInstaller(Path installerPath) throws IOException {

        // Prevent execution on operating systems other than Windows.
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            throw new IOException("PawnIO installation requires Windows.");
        }

        // Refuse to execute an installer that fails verification.
        if (!verifyPawnIOInstaller(installerPath)) {
            throw new IOException("PawnIO installer verification failed.");
        }

        /*
         * Convert the installer location into an absolute path.
         *
         * PowerShell uses single quotes around file paths.
         * Any single quotes within the path must be escaped by doubling them.
         */
        String safePath = installerPath.toAbsolutePath().toString().replace("'", "''");

        /*
         * Launch the verified PawnIO installer with administrator privileges.
         *
         * Return the installer's exit code when execution completes.
         * If Windows UAC is cancelled, return a dedicated exit code
         * so EcoScan can distinguish cancellation from other failures.
         */
        String command =
                "try { "
                        + "$p = Start-Process -FilePath '" + safePath + "' "
                        + "-ArgumentList '-install' "
                        + "-Verb RunAs -Wait -PassThru -ErrorAction Stop; "
                        + "exit $p.ExitCode; "
                        + "if ($errorCode -eq 1223) { "
                        + "[Console]::Error.WriteLine('PawnIO installation cancelled by user.'); "
                        + "exit 1223; "
                        + "} "
                        + "[Console]::Error.WriteLine($_.Exception.Message); "
                        + "exit 1; "
                        + "}";

        /*
         * Launch PowerShell without elevating the Java application.
         * The PawnIO installer requests administrator privileges through Windows UAC.
         * Combine standard output and error output into one strem so installation failures can be diagnosed.
         */
        return new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",command).redirectErrorStream(true).start();
    }

}
