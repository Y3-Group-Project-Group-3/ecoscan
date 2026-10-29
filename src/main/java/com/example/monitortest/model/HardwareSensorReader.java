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



        /*
         * Debug GPU sensors exposed by LibreHardwareMonitor.
         * This helps identify the correct power and temperature readings.
         */
        for (var hardware : computer.getHardware()) {

            if (hardware.getName().toLowerCase().contains("nvidia")) {

                System.out.println("GPU Hardware: " + hardware.getName());

                for (var sensor : hardware.getSensors()) {

                    System.out.println(
                            "Sensor: " + sensor.getName()
                                    + " | Type: " + sensor.getSensorType()
                                    + " | Value: " + sensor.getValue()
                    );
                }
            }
        }


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



    /**
     * Retrieves GPU package power consumption from a hardware snapshot.
     *
     * @param computer LibreHardwareMonitor snapshot
     * @return GPU package power in watts, or NaN if unavailable
     */
    public double getGpuPower(Computer computer) {

        // Search the snapshot for a GPU package power sensor.
        for (var hardware : computer.getHardware()) {

            // Only examine NVIDIA GPU hardware.
            if (!hardware.getName().toLowerCase().contains("nvidia")) {
                continue;
            }

            for (var sensor : hardware.getSensors()) {

                // Match the exact power sensor rather than percentage-based loads.
                if (sensor.getName().equalsIgnoreCase("GPU Package")
                        && sensor.getSensorType().toString().equalsIgnoreCase("Power")) {

                    return sensor.getValue();
                }
            }
        }

        // An unavailable reading must not be interpreted as zero watts.
        return Double.NaN;
    }


    /**
     * Retrieves the GPU core temperature from a hardware snapshot.
     *
     * @param computer LibreHardwareMonitor snapshot
     * @return GPU core temperature in Celsius, or NaN if unavailable
     */
    public double getGpuTemperature(Computer computer) {

        // Search the snapshot for NVIDIA GPU hardware.
        for (var hardware : computer.getHardware()) {

            if (!hardware.getName().toLowerCase().contains("nvidia")) {
                continue;
            }

            // Find the GPU Core temperature sensor.
            for (var sensor : hardware.getSensors()) {

                if (sensor.getName().equalsIgnoreCase("GPU Core")
                        && sensor.getSensorType().toString()
                        .equalsIgnoreCase("Temperature")) {

                    return sensor.getValue();
                }
            }
        }

        // Return NaN if the GPU temperature cannot be retrieved.
        return Double.NaN;
    }


}
