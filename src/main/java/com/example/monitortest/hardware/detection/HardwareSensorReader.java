package com.example.monitortest.hardware.detection;

import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

public class HardwareSensorReader {

    /**
     * Searches an existing LibreHardwareMonitor snapshot for a sensor matching the supplied parameters.
     *
     * @param computer Retrieved hardware snapshot.
     * @param hardwareType Hardware category (e.g. Cpu).
     * @param sensorType Sensor category (e.g. Power).
     * @param sensorName Exact sensor name (e.g. Package).
     * @return The sensor reading, or NaN if unavailable.
     */
    public double getSensorValue(
            Computer computer,
            String hardwareType,
            String sensorType,
            String sensorName
    ) {

        // Check that the hardware snapshot exists.
        if (computer == null || computer.getHardware() == null) {
            return Double.NaN;
        }

        // Iterate through the detected hardware devices.
        for (Hardware hardware : computer.getHardware()) {

            // Skip hardware that doesn't match the requested type.
            if (hardware.getHardwareType() == null || !hardware.getHardwareType().toString() .equalsIgnoreCase(hardwareType)) {
                continue;
            }

            // Skip devices without sensor information.
            if (hardware.getSensors() == null) {
                continue;
            }

            // Search the sensors belonging to this device.
            for (Sensor sensor : hardware.getSensors()) {

                if (sensor.getSensorType() == null || sensor.getName() == null) {
                    continue;
                }

                // Match both the sensor category and its name.
                boolean matchesType = sensor.getSensorType().toString().equalsIgnoreCase(sensorType);
                boolean matchesName = sensor.getName().equalsIgnoreCase(sensorName);
                if (matchesType && matchesName) {

                    // Retrieve the matching sensor reading.
                    double value = sensor.getValue();

                    // Reject unavailable or non-finite readings.
                    if (!Double.isFinite(value)) { return Double.NaN; }

                    // Power consumption cannot be negative.
                    if (sensorType.equalsIgnoreCase("Power") && value < 0.0) {
                        return Double.NaN;
                    }

                    return value;

                }
            }
        }
        // No matching sensor was found.
        return Double.NaN;
    }





    // Retrieves GPU power from supported GPU hardware sensors.
    // Supports common NVIDIA, AMD and Intel GPU power sensor names.
    public double getGpuPower(Computer computer) {

        // Return unavailable when the hardware snapshot cannot be read.
        if (computer == null || computer.getHardware() == null) {
            return Double.NaN;
        }

        // Search every GPU rather than restricting detection to NVIDIA.
        for (Hardware hardware : computer.getHardware()) {

            if (hardware.getHardwareType() == null || hardware.getSensors() == null) {
                continue;
            }

            // Only inspect hardware classified as a GPU.
            String hardwareType = hardware.getHardwareType().toString();

            if (!hardwareType.toLowerCase(java.util.Locale.ROOT).startsWith("gpu")) {
                continue;
            }

            // Inspect the power sensors belonging to this GPU.
            for (Sensor sensor : hardware.getSensors()) {

                if (sensor.getSensorType() == null || sensor.getName() == null) {
                    continue;
                }

                // Exclude loads, temperatures and other non-power readings.
                if (!sensor.getSensorType().toString().equalsIgnoreCase("Power")) {
                    continue;
                }

                // Support the common GPU power sensor naming conventions.
                String sensorName = sensor.getName();

                boolean supportedName = sensorName.equalsIgnoreCase("GPU Package") || sensorName.equalsIgnoreCase("GPU Power") || sensorName.equalsIgnoreCase("GPU Core");
                if (!supportedName) {
                    continue;
                }

                // Only accept valid, non-negative readings.
                double value = sensor.getValue();

                if (Double.isFinite(value) && value >= 0.0) {
                    return value;
                }
            }
        }

        // No supported GPU power sensor could be retrieved.
        return Double.NaN;
    }




}
