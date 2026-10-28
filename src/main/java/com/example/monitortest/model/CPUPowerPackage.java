
package com.example.monitortest.model;

import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.manager.powershell.PowerShellOperations;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

public class CPUPowerPackage {

    /**
     * Retrieves the CPU's package power consumption in watts
     * using the LibreHardwareMonitor Java wrapper.
     *
     * @return CPU package power in watts, or Double.NaN if
     *         the reading cannot be retrieved.
     */
    public double getCPUPackagePower() {

        // STEP 1: Identify the operating system.

        // Identify the current operating system.
        String operatingSystem = System.getProperty("os.name", "");

        // LibreHardwareMonitor's PowerShell integration is Windows-only.
        // On macOS and Linux, report the sensor as unavailable.
        if (!operatingSystem.toLowerCase(java.util.Locale.ROOT).startsWith("windows")) {
            System.out.println("CPU package power unavailable: non-Windows operating system.");
            return Double.NaN;
        }


        // STEP 2: Configure LibreHardwareMonitor.

        // Retrieve the library's configuration instance and enable CPU hardware monitoring.
        // This tells LibreHardwareMonitor to collect CPU sensor information when performing a scan.
        ComputerConfig config = ComputerConfig.getInstance().setCpuEnabled(true);


        // STEP 3: Retrieve raw PowerShell output (DEBUGGING).

        // The Java wrapper uses PowerShell to interact with the underlying LibreHardwareMonitor .NET library.
        // This call retrieves the raw JSON response containing hardware and sensor information.
        // IMPORTANT: This is for debugging and performs an additional hardware query. It can be removed once
        // the integration is working reliably.
        String rawJson = PowerShellOperations.GET.getComputerJsonData(config);

        // Print the raw response to the console.
        // If rawJson is null, print "NULL".
        // If it contains no visible text, print "EMPTY".
        // Otherwise, print the actual JSON response.
        //
        // This helps diagnose PowerShell execution DLL loading, and hardware retrieval problems.
        System.out.println("=== RAW POWERSHELL OUTPUT ===");
        System.out.println( rawJson == null ? "NULL" : rawJson.isBlank() ? "EMPTY" : rawJson );
        System.out.println("=============================");


        // STEP 4: Retrieve the computer's hardware information.

        // Create a LibreHardwareMonitor manager using the CPU-enabled configuration.
        // getComputer() returns a Computer object containing the hardware information retrieved by the wrapper.
        Computer computer = LibreHardwareManager.createInstance(config).getComputer();


        // STEP 5: Check whether hardware was retrieved.

        // If the library could not retrieve a hardware list, report the issue and return Double.NaN.
        //
        // Possible causes include unavailable dependencies, insufficient hardware access, or retrieval errors.
        if (computer.getHardware() == null) {
            System.out.println( "No hardware information retrieved." );
            return Double.NaN;
        }


        // STEP 6: Iterate through the detected hardware.

        // A computer may contain multiple hardware categories, such as CPU, GPU, motherboard, and memory.
        //
        // Examine each hardware object to find CPU hardware.
        for (Hardware hardware : computer.getHardware()) {

            // Ignore any hardware that is not classified as a CPU.
            // continue skips the remaining code in this iteration and moves to the next hardware object.
            if (!hardware.getHardwareType().toString().equalsIgnoreCase("Cpu")) {
                continue;
            }

            // STEP 7: Iterate through the CPU sensors.

            // A CPU may expose several different sensor types, including temperature, load, clock speed,
            // voltage, and power consumption.
            for (Sensor sensor : hardware.getSensors()) {

                // DEBUGGING:
                // Print each sensor's name, type, and value.
                // This helps identify the exact sensor names used by different CPU models.
                System.out.println(
                        "Sensor: " + sensor.getName()
                                + " | Type: " + sensor.getSensorType()
                                + " | Value: " + sensor.getValue()
                );


                // STEP 8: Identify the CPU package power sensor.

                // Check whether the sensor reports power.
                // equalsIgnoreCase allows differences in uppercase and lowercase characters.
                boolean isPowerSensor = sensor.getSensorType().toString().equalsIgnoreCase("Power");

                // Check whether the sensor's name matches either of the expected package power names.
                //
                // Different hardware configurations may expose slightly different sensor names.
                boolean isPackageSensor = sensor.getName().equalsIgnoreCase(
                        "Package") || sensor.getName().equalsIgnoreCase("CPU Package");


                // STEP 9: Read the matching sensor.

                // Only proceed if BOTH conditions are true:
                //
                // 1. The sensor measures power.
                // 2. The sensor represents CPU package power.
                if (isPowerSensor && isPackageSensor) {

                    // Retrieve the sensor's current reading.
                    // For a Power sensor, the value is in watts.
                    double value = sensor.getValue();

                    // Print the reading for debugging.
                    System.out.println( "CPU Package Power: " + value);


                    // STEP 10: Validate the reading.

                    // Double.isFinite() rejects NaN and positive/negative infinity.
                    //
                    // value >= 0 rejects negative power readings.
                    //
                    // NOTE: This currently accepts 0.0 W. During EcoScan testing, a zero reading
                    // sometimes indicated inaccessible sensors.
                    if (Double.isFinite(value) && value >= 0) {

                        // A matching, accepted reading has been found, so return it immediately.
                        return value;
                    }
                }
            }
        }


        // STEP 11: Handle unavailable CPU package power.

        // Execution reaches this point if no matching sensor produced an accepted reading.
        // This may mean the sensor is missing, unsupported or returning an invalid value.
        System.out.println( "CPU package power sensor not found." );

        // Return NaN so the rest of EcoScan can recognise
        // that CPU package power is unavailable.
        return Double.NaN;
    }
}