package com.example.monitortest.hardware.monitoring;

import com.example.monitortest.services.startup.ApplicationElevationManager;

import java.util.Locale;

/**
 * Determines whether the current environment supports EcoScan's Windows live hardware monitoring functionality.
 */
public final class LiveMonitorAvailability {

    // Represents the possible availability states.
    public enum Status {
        AVAILABLE,
        WINDOWS_ONLY,
        ADMIN_REQUIRED
    }

    private LiveMonitorAvailability() {
        // Utility class: prevent instantiation.
    }

    /**
     * Checks the operating system and administrator privileges.
     * @return the current availability status
     */
    public static Status check() {

        // Live monitoring currently supports Windows only.
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (!osName.startsWith("windows")) {
            return Status.WINDOWS_ONLY;
        }

        // Check whether the Windows process is elevated.
        ApplicationElevationManager elevationManager = new ApplicationElevationManager();

        if (!elevationManager.isRunningAsAdministrator()) {
            return Status.ADMIN_REQUIRED;
        }

        // The operating system and permissions meet our requirements.
        return Status.AVAILABLE;
    }
}
