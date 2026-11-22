package com.example.monitortest.ui.history.services;

import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.ui.history.components.SessionRow;
import com.example.monitortest.ui.history.model.SavedScan;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Turns saved hardware scans into the summary rows and detail pages used in the scans PDF.
// Uses the same power and CO2e calculations as the Results Page.
public final class ScanPdfReport {

    // Column headings for the summary table on the first page.
    public static final String[] SUMMARY_HEADINGS = {"Date", "Name", "CPU", "GPU", "Power", "CO2e per hour"};

    // Formats scan dates for the summary table and the detail pages.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm");

    // Time periods matching the Results Page.
    private static final double SECONDS_PER_HOUR = 3600;
    private static final double SECONDS_PER_DAY = 86400;
    private static final double SECONDS_PER_MONTH = 30 * 86400.0;
    private static final double SECONDS_PER_YEAR = 365 * 86400.0;

    // Works out CO2e in the same way as the Results Page.
    private static final CarbonEmissionsCalculator CALCULATOR = new CarbonEmissionsCalculator();

    // Prevent instances of this utility class.
    private ScanPdfReport() {
    }


    // Creates one summary row for each scan.
    public static List<String[]> summaryRows(List<SavedScan> scans) {

        List<String[]> rows = new ArrayList<>();

        for (SavedScan scan : scans) {

            HardwareInfo info = scan.getHardwareInfo();
            double power = scanPower(scan);

            rows.add( new String[]{
                    scan.getScannedAt().format(DATE_FORMAT),
                    scan.getName(),
                    textOrUnknown(info.getCpuName()),
                    textOrUnknown(info.getGpuName()),
                    formatPower(power),
                    formatEmissions(power, SECONDS_PER_HOUR)
            } );
        }

        return rows;
    }


    // Creates one detail page for each scan, matching what the Results Page shows.
    public static List<HistoryPdfWriter.DetailPage> detailPages(List<SavedScan> scans) {

        List<HistoryPdfWriter.DetailPage> pages = new ArrayList<>();

        for (SavedScan scan : scans) {
            pages.add( new HistoryPdfWriter.DetailPage( scan.getName(), detailLines(scan) ) );
        }

        return pages;
    }


    // Builds the labels and values for one scan's detail page.
    private static List<String[]> detailLines(SavedScan scan) {

        HardwareInfo info = scan.getHardwareInfo();
        double power = scanPower(scan);

        List<String[]> lines = new ArrayList<>();

        // When the scan was taken and which power mode it used.
        section(lines, "Scan");
        line(lines, "Date", scan.getScannedAt().format(DATE_TIME_FORMAT));
        line(lines, "Power mode", scan.isIncludeEstimates() ? "Measured + estimates" : "Measured only");

        // Projected impact, matching the Results Page.
        section(lines, "Power and emissions");
        line(lines, "Scanned component power", formatPower(power));
        line(lines, "Energy per hour", Double.isFinite(power) ? String.format(Locale.UK, "%.4f kWh", power / 1000) : "Not available");
        line(lines, "CO2e per hour", formatEmissions(power, SECONDS_PER_HOUR));
        line(lines, "CO2e per day", formatEmissions(power, SECONDS_PER_DAY));
        line(lines, "CO2e per month (30 days)", formatEmissions(power, SECONDS_PER_MONTH));
        line(lines, "CO2e per year", formatEmissions(power, SECONDS_PER_YEAR));

        // Processor details.
        section(lines, "Processor");
        line(lines, "Name", textOrUnknown(info.getCpuName()));
        line(lines, "Power", formatPower(info.getCpuPower()) + (info.isCpuPowerSimulated() ? " (simulated)" : ""));
        line(lines, "Temperature", isAvailable(info.getCpuTemperature()) ? String.format(Locale.UK, "%.1f °C", info.getCpuTemperature()) : "Not available");

        // Graphics card details.
        section(lines, "Graphics");
        line(lines, "Name", textOrUnknown(info.getGpuName()));
        line(lines, "Vendor", textOrUnknown(info.getGpuVendor()));
        line(lines, "Memory (VRAM)", formatGB(info.getGpuVram()));
        line(lines, "Power", formatPower(info.getGpuPower()));

        // Memory details, with one line for each RAM module.
        section(lines, "Memory");
        line(lines, "Total memory", formatGB(info.getTotalMemory()));

        List<MemoryModule> modules = info.getMemoryModules();

        for (int i = 0; i < modules.size(); i++) {

            MemoryModule module = modules.get(i);

            line(lines, "Module " + (i + 1), String.format(Locale.UK, "%.1f GB %s, %.0f MHz, %s",
                    module.getCapacityGB(), textOrUnknown(module.getMemoryType()),
                    module.getClockSpeedMHz(), textOrUnknown(module.getManufacturer())));
        }

        // Only show estimated power when the scan was saved with estimates included.
        if (scan.isIncludeEstimates()) {
            line(lines, "Estimated power", formatPower(info.getEstimatedRamPower()));
        }

        // Storage details, with one line for each drive.
        section(lines, "Storage");

        List<StorageDevice> devices = info.getStorageDevices();

        for (int i = 0; i < devices.size(); i++) {

            StorageDevice device = devices.get(i);

            line(lines, "Drive " + (i + 1), String.format(Locale.UK, "%s, %.0f GB, %s",
                    textOrUnknown(device.getModel()), device.getCapacityGB(), textOrUnknown(device.getType())));
        }

        if (devices.isEmpty()) {
            line(lines, "Drives", "Not available");
        }

        if (scan.isIncludeEstimates()) {
            line(lines, "Estimated power", formatPower(info.getEstimatedStoragePower()));
        }

        // Motherboard details.
        section(lines, "Motherboard");
        line(lines, "Manufacturer", textOrUnknown(info.getMotherboardManufacturer()));
        line(lines, "Model", textOrUnknown(info.getMotherboardModel()));
        line(lines, "Version", textOrUnknown(info.getMotherboardVersion()));

        return lines;
    }


    // Returns the scan's power for the mode it was saved in, matching the Results Page.
    private static double scanPower(SavedScan scan) {

        HardwareInfo info = scan.getHardwareInfo();

        return scan.isIncludeEstimates() ? info.getCombinedComponentPower() : info.getTotalMeasuredPower();
    }


    // Adds a section heading to the detail page.
    private static void section(List<String[]> lines, String heading) {
        lines.add( new String[]{ heading, null } );
    }


    // Adds a label and value to the detail page.
    private static void line(List<String[]> lines, String label, String value) {
        lines.add( new String[]{ label, value } );
    }


    // Returns true when a reading was taken.
    private static boolean isAvailable(double value) {
        return Double.isFinite(value) && value >= 0;
    }


    // Formats power in watts, or explains when it is missing.
    private static String formatPower(double watts) {
        return isAvailable(watts) ? String.format(Locale.UK, "%.1f W", watts) : "Not available";
    }


    // Formats the CO2e for a period using the same g / kg units as the History Page.
    private static String formatEmissions(double watts, double seconds) {
        return isAvailable(watts) ? SessionRow.formatEmissions( CALCULATOR.calculateEmissions(watts, seconds) ) : "Not available";
    }


    // Converts bytes to gigabytes, or explains when the size is missing.
    private static String formatGB(long bytes) {
        return bytes > 0 ? String.format(Locale.UK, "%.1f GB", bytes / (1024.0 * 1024 * 1024)) : "Not available";
    }


    // Shows "Unknown" instead of a blank or missing value.
    private static String textOrUnknown(String text) {
        return (text == null || text.isBlank()) ? "Unknown" : text;
    }
}