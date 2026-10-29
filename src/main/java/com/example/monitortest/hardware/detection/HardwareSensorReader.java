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




    /**
     * Retrieves NVIDIA GPU package power from a hardware snapshot.
     *
     * @param computer LibreHardwareMonitor snapshot.
     * @return GPU package power in watts, or NaN if unavailable.
     */
    public double getGpuPower(Computer computer) {

        // Check whether the hardware snapshot is available.
        if (computer == null || computer.getHardware() == null) {
            return Double.NaN;
        }

        // Search the snapshot for NVIDIA GPU hardware.
        for (Hardware hardware : computer.getHardware()) {

            // Skip devices without a valid name or sensor collection.
            if (hardware.getName() == null || hardware.getSensors() == null) {
                continue;
            }

            // Only examine NVIDIA GPU hardware.
            if (!hardware.getName().toLowerCase().contains("nvidia")) {
                continue;
            }

            // Find the GPU Package power sensor.
            for (Sensor sensor : hardware.getSensors()) {

                if (sensor.getName() == null || sensor.getSensorType() == null) {
                    continue;
                }

                if (sensor.getName().equalsIgnoreCase("GPU Package") && sensor.getSensorType().toString().equalsIgnoreCase("Power")) {
                    // Reject invalid or negative power measurements.
                    double value = sensor.getValue();
                    return Double.isFinite(value) && value >= 0.0 ? value : Double.NaN;
                }
            }
        }

        // No supported GPU power sensor was found.
        return Double.NaN;
    }



}
