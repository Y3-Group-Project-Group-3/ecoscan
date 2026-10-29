package com.example.monitortest.model;

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
            if (hardware.getHardwareType() == null ||
                    !hardware.getHardwareType().toString()
                            .equalsIgnoreCase(hardwareType)) {
                continue;
            }

            // Skip devices without sensor information.
            if (hardware.getSensors() == null) {
                continue;
            }

            // Search the sensors belonging to this device.
            for (Sensor sensor : hardware.getSensors()) {

                if (sensor.getSensorType() == null ||
                        sensor.getName() == null) {
                    continue;
                }

                // Match both the sensor category and its name.
                boolean matchesType = sensor.getSensorType().toString().equalsIgnoreCase(sensorType);
                boolean matchesName = sensor.getName().equalsIgnoreCase(sensorName);
                if (matchesType && matchesName) {
                    // Return a valid reading from the matching sensor.
                    double value = sensor.getValue();
                    return Double.isFinite(value) ? value : Double.NaN;
                }
            }
        }

        // No matching sensor was found.
        return Double.NaN;
    }
}
