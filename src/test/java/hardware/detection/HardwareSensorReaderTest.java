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
}

