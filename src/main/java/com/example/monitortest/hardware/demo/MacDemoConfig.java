package com.example.monitortest.hardware.demo;

import java.awt.Color;

// Central configuration for EcoScan's macOS demonstration mode.
// All values are simulated and must not be presented as real sensor readings.
public final class MacDemoConfig {

    // Prevent this configuration-only class from being instantiated.
    private MacDemoConfig() {
    }

    // ============================================================
    // HARDWARE PROFILE FALLBACKS
    // Used when a matching macOS JSON profile is unavailable.
    // ============================================================

    public static final double DEFAULT_CPU_POWER_WATTS = 15.0;
    public static final double DEFAULT_GPU_POWER_WATTS = 6.0;

    // Simulated hardware details used by the macOS hardware scan.
    public static final double DEFAULT_CPU_TEMPERATURE_C = 45.0;
    public static final String GPU_NAME = "Apple M3 Integrated GPU";
    public static final String GPU_VENDOR = "Apple";
    public static final String MEMORY_MANUFACTURER = "Apple";
    public static final String MEMORY_TYPE = "LPDDR5";
    public static final long MEMORY_CLOCK_SPEED_HZ = 3_200_000_000L;
    public static final String STORAGE_NAME = "Apple Internal SSD";
    public static final long STORAGE_CAPACITY_BYTES = 256_000_000_000L;
    public static final String STORAGE_TYPE = "SSD";

    // ============================================================
    // LIVE MONITOR DEMONSTRATION
    // ============================================================

    // Duration used to calculate hypothetical carbon emissions.
    public static final double EMISSIONS_DEMO_DURATION_SECONDS = 60.0;

    // Fixed PC position along the comparison ruler.
    // 0.0 = start of the ruler; 1.0 = end of the ruler.
    public static final double COMPARISON_PC_POSITION = 0.20;

    // Position of the first emissions milestone on the demo ruler.
    public static final double FIRST_MILESTONE_POSITION = 0.65;

    // ============================================================
    // LIVE MONITOR APPEARANCE
    // ============================================================

    public static final String LIVE_MONITOR_HEADING = "Live Monitor — Demo Mode (Simulated Data)";

    public static final Color CPU_GRAPH_COLOR = new Color(0x22A065);

    public static final Color GPU_GRAPH_COLOR = new Color(0x94A3B8);
}
