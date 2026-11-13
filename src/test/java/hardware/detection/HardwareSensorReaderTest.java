package hardware.detection;

import com.example.monitortest.hardware.detection.HardwareSensorReader;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for EcoScan's hardware sensor detection.
// Uses simulated jLibreHardwareMonitor models instead of physical hardware.
class HardwareSensorReaderTest {

    private final HardwareSensorReader reader = new HardwareSensorReader();

    // Creates a simulated sensor with a name, category and reading.
    private Sensor createSensor( String name, String sensorType, double value) {

        Sensor sensor = new Sensor();
        sensor.setName(name);
        sensor.setSensorType(sensorType);
        sensor.setValue(value);

        return sensor;
    }

    // Builds a hardware snapshot containing one simulated device.
    // Allows each test to control the hardware type and available sensors.
    private Computer createComputer( String hardwareType, String hardwareName, Sensor... sensors) {

        Hardware hardware = new Hardware();

        hardware.setHardwareType(hardwareType);
        hardware.setName(hardwareName);
        hardware.setSensors(List.of(sensors));

        Computer computer = new Computer();
        computer.setHardware(List.of(hardware));

        return computer;
    }

    // LIV-UT-021
    // Verify the familiar NVIDIA GPU Package power sensor.
    @Test
    @DisplayName("LIV-UT-021 - Detects NVIDIA GPU Package power sensor")
    void detectsNvidiaGpuPackagePower() {

        Computer computer = createComputer(
                "GpuNvidia",
                "NVIDIA GeForce RTX",
                createSensor("GPU Package", "Power", 75.0)
        );

        assertEquals( 75.0, reader.getGpuPower(computer), 0.000001 );
    }

    // LIV-UT-022
    // Verify that Intel's GPU Power naming convention is recognised.
    @Test
    @DisplayName("LIV-UT-022 - Detects Intel GPU Power sensor")
    void detectsIntelGpuPower() {

        Computer computer = createComputer(
                "GpuIntel",
                "Intel Iris Xe Graphics",
                createSensor("GPU Power", "Power", 9.6)
        );

        assertEquals( 9.6, reader.getGpuPower(computer), 0.000001 );
    }

    // LIV-UT-023
    // Verify support for the GPU Core power naming convention.
    @Test
    @DisplayName("LIV-UT-023 - Detects AMD GPU Core power sensor")
    void detectsAmdGpuCorePower() {

        Computer computer = createComputer(
                "GpuAmd",
                "AMD Radeon Graphics",
                createSensor("GPU Core", "Power", 45.0)
        );

        assertEquals( 45.0, reader.getGpuPower(computer), 0.000001 );
    }

    // LIV-UT-024
    // Ensure CPU power cannot accidentally be classified as GPU power.
    @Test
    @DisplayName("LIV-UT-024 - Ignores power sensors belonging to non-GPU hardware")
    void ignoresNonGpuHardware() {

        Computer computer = createComputer(
                "Cpu",
                "Intel Core i5",
                createSensor("GPU Power", "Power", 50.0)
        );

        assertTrue( Double.isNaN(reader.getGpuPower(computer)) );
    }

    // LIV-UT-025
    // Confirm that a GPU load percentage is not treated as watts.
    @Test
    @DisplayName("LIV-UT-025 - Ignores GPU load sensors when retrieving power")
    void ignoresGpuLoadSensors() {

        Computer computer = createComputer(
                "GpuIntel",
                "Intel Iris Xe Graphics",
                createSensor("GPU Power", "Load", 80.0)
        );

        assertTrue( Double.isNaN(reader.getGpuPower(computer)) );
    }

    // LIV-UT-026
    // Negative power consumption is not a usable reading.
    @Test
    @DisplayName("LIV-UT-026 - Rejects negative GPU power readings")
    void rejectsNegativeGpuPower() {

        Computer computer = createComputer(
                "GpuNvidia",
                "NVIDIA GeForce RTX",
                createSensor("GPU Package", "Power", -10.0)
        );

        assertTrue( Double.isNaN(reader.getGpuPower(computer)) );
    }

    // LIV-UT-027
    // A measured zero is different from an unavailable reading.
    @Test
    @DisplayName("LIV-UT-027 - Accepts zero watts as a valid GPU power reading")
    void acceptsZeroGpuPower() {

        Computer computer = createComputer(
                "GpuIntel",
                "Intel Iris Xe Graphics",
                createSensor("GPU Power", "Power", 0.0)
        );

        assertEquals( 0.0, reader.getGpuPower(computer), 0.000001 );
    }

    // LIV-UT-028
    // Missing hardware information must not cause an exception.
    @Test
    @DisplayName("LIV-UT-028 - Returns NaN when no hardware snapshot is available")
    void handlesMissingHardwareSnapshot() {
        assertTrue( Double.isNaN(reader.getGpuPower(null)) );
    }


    // LIV-UT-029
    // Verify that the standard CPU Package power sensor can be retrieved.
    @Test
    @DisplayName("LIV-UT-029 - Retrieves CPU Package power from a valid CPU sensor")
    void retrievesCpuPackagePower() {

        // Create simulated CPU hardware exposing a Package power sensor.
        Computer computer = createComputer(
                "Cpu",
                "Intel Core i5",
                createSensor("Package", "Power", 35.0)
        );

        // Request the exact CPU sensor used by LiveHardwareReader.
        double actual = reader.getSensorValue( computer, "Cpu", "Power", "Package" );

        // Verify that the sensor reading is returned in watts.
        assertEquals(35.0, actual, 0.000001);
    }

    // LIV-UT-030
    // Verify that an alternative CPU Package sensor name is supported.
    @Test
    @DisplayName("LIV-UT-030 - Retrieves CPU power using the alternative CPU Package sensor name")
    void retrievesAlternativeCpuPackagePower() {

        // Some processors expose the sensor as "CPU Package".
        Computer computer = createComputer(
                "Cpu",
                "Intel Core i5",
                createSensor("CPU Package", "Power", 25.0)
        );

        // Request the alternative sensor name explicitly.
        double actual = reader.getSensorValue( computer, "Cpu", "Power", "CPU Package" );

        // Verify that the matching reading is returned.
        assertEquals(25.0, actual, 0.000001);
    }

    // LIV-UT-031
    // Confirm that a load percentage cannot be mistaken for power.
    @Test
    @DisplayName("LIV-UT-031 - Rejects CPU sensors with an incorrect sensor type")
    void rejectsIncorrectCpuSensorType() {

        // A Load sensor is not an electrical power measurement.
        Computer computer = createComputer(
                "Cpu",
                "Intel Core i5",
                createSensor("Package", "Load", 65.0)
        );

        // Request Package as a Power sensor, not a Load sensor.
        double actual = reader.getSensorValue( computer, "Cpu", "Power", "Package" );

        // No matching Power sensor should be available.
        assertTrue(Double.isNaN(actual));
    }

    // LIV-UT-032
    // Verify that the correct sensor is selected when several exist.
    @Test
    @DisplayName("LIV-UT-032 - Selects the requested CPU power sensor when multiple sensors exist")
    void selectsCorrectCpuPowerSensor() {

        // Create CPU hardware with multiple power sensor names.
        Computer computer = createComputer(
                "Cpu",
                "Intel Core i5",
                createSensor("CPU Cores", "Power", 15.0),
                createSensor("Package", "Power", 35.0),
                createSensor("CPU Platform", "Power", 50.0)
        );

        // Request the Package sensor rather than another power reading.
        double actual = reader.getSensorValue( computer, "Cpu", "Power", "Package" );

        // Verify that the requested 35 W sensor was selected.
        assertEquals(35.0, actual, 0.000001);
    }


    // LIV-UT-033
    // Verify that multiple supported GPU sensors can be handled.
    @Test
    @DisplayName("LIV-UT-033 - Retrieves the first valid supported GPU power sensor")
    void retrievesFirstValidGpuPowerSensor() {

        // Simulate a GPU exposing multiple supported power sensors.
        Computer computer = createComputer(
                "GpuNvidia",
                "NVIDIA GeForce RTX",
                createSensor("GPU Package", "Power", 75.0),
                createSensor("GPU Core", "Power", 60.0)
        );

        // The current selection policy uses the first valid match.
        double actual = reader.getGpuPower(computer);

        assertEquals(75.0, actual, 0.000001);
    }

    // LIV-UT-034
    // Verify that invalid readings do not block later usable sensors.
    @Test
    @DisplayName("LIV-UT-034 - Skips invalid GPU power readings and selects the next valid sensor")
    void skipsInvalidGpuPowerSensor() {

        // The first sensor is invalid, but the second is usable.
        Computer computer = createComputer(
                "GpuNvidia",
                "NVIDIA GeForce RTX",
                createSensor("GPU Package", "Power", Double.NaN),
                createSensor("GPU Core", "Power", 60.0)
        );

        // The reader should continue searching after the invalid reading.
        double actual = reader.getGpuPower(computer);

        assertEquals(60.0, actual, 0.000001);
    }


}

