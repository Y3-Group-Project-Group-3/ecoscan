package com.example.monitortest.hardware.monitoring;

import com.example.monitortest.services.startup.ApplicationElevationManager;

import java.util.Locale;

/**
 * Determines whether the current environment supports EcoScan's Windows live hardware monitoring functionality.
 */
public final class LiveMonitorAvailability {


    // Represents the monitoring capabilities available on this device.
    public enum Status {

        // Real hardware monitoring is available on Windows.
        AVAILABLE,

        // macOS can display the Live Monitor using simulated data.
        MAC_DEMO,

        // Live monitoring is unsupported on this operating system.
        WINDOWS_ONLY,

        // Windows requires administrator privileges for hardware sensors.
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


        // macOS uses simulated data rather than real hardware monitoring.
        if (osName.startsWith("mac")) {
            return Status.MAC_DEMO;
        }

        // Other unsupported operating systems retain the existing message.
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
