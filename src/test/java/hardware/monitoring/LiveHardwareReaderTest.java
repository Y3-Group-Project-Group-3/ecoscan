package hardware.monitoring;

import com.example.monitortest.hardware.monitoring.LiveHardwareReader;
import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for LiveHardwareReader.
// Verifies that complete monitoring snapshots are assembled correctly from simulated hardware sensor information.
class LiveHardwareReaderTest {

    // LIV-IT-012
    // Verify that the alternative CPU Package power sensor is used when the primary Package sensor cannot be found.
    @Test
    @DisplayName("LIV-IT-012 - Uses alternative CPU Package sensor when the primary sensor is unavailable")
    void usesAlternativeCpuPackageSensor() {

        // Create a simulated CPU power sensor using the alternative name.
        Sensor cpuSensor = new Sensor();

        cpuSensor.setName("CPU Package");
        cpuSensor.setSensorType("Power");
        cpuSensor.setValue(35.0);

        // Associate the sensor with CPU hardware.
        Hardware cpuHardware = new Hardware();

        cpuHardware.setName("Intel Core i5");
        cpuHardware.setHardwareType("Cpu");
        cpuHardware.setSensors(List.of(cpuSensor));

        // Construct a computer containing only the simulated CPU.
        // No GPU power sensor is provided.
        Computer computer = new Computer();
        computer.setHardware(List.of(cpuHardware));

        // Create the real LiveHardwareReader with simulated hardware input.
        LiveHardwareReader reader = new LiveHardwareReader(() -> computer);

        // Retrieve the snapshot through the production reader logic.
        LiveMonitorSnapshot snapshot = reader.readSnapshot();

        // Verify that the alternative CPU sensor was detected.
        assertEquals( 35.0, snapshot.cpuPower(), 0.000001 );

        // Verify that the missing GPU sensor remains unavailable.
        assertTrue( Double.isNaN(snapshot.gpuPower()) );

        // Verify that the available CPU power contributes to total power.
        assertEquals( 35.0, snapshot.totalPower(), 0.000001 );
    }


    // LIV-IT-013
    // Verify that LiveHardwareReader prioritises the primary Package sensor when both supported CPU power sensor names are available.
    @Test
    @DisplayName("LIV-IT-013 - Prioritises the primary CPU Package power sensor when both names exist")
    void prioritisesPrimaryCpuPowerSensor() {

        // Create the primary CPU Package sensor.
        Sensor primarySensor = new Sensor();
        primarySensor.setName("Package");
        primarySensor.setSensorType("Power");
        primarySensor.setValue(45.0);

        // Create an alternative CPU Package sensor with a different value.
        Sensor alternativeSensor = new Sensor();
        alternativeSensor.setName("CPU Package");
        alternativeSensor.setSensorType("Power");
        alternativeSensor.setValue(30.0);

        // Associate both sensors with the same CPU hardware.
        Hardware cpuHardware = new Hardware();
        cpuHardware.setName("Intel Core i5");
        cpuHardware.setHardwareType("Cpu");
        cpuHardware.setSensors( List.of(primarySensor, alternativeSensor) );

        // Construct a hardware snapshot containing the simulated CPU.
        Computer computer = new Computer();
        computer.setHardware(List.of(cpuHardware));

        // Supply the simulated hardware to the production reader.
        LiveHardwareReader reader = new LiveHardwareReader(() -> computer);

        // Retrieve the combined monitoring snapshot.
        LiveMonitorSnapshot snapshot = reader.readSnapshot();

        // The primary Package reading should take precedence.
        assertEquals( 45.0, snapshot.cpuPower(), 0.000001 );

        // Verify that the missing GPU sensor remains unavailable.
        assertTrue( Double.isNaN(snapshot.gpuPower()) );

        // The available CPU reading should determine total power.
        assertEquals( 45.0, snapshot.totalPower(), 0.000001 );
    }


    // LIV-IT-014
    // Verify that LiveHardwareReader safely handles a missing hardware snapshot without throwing an exception.
    @Test
    @DisplayName("LIV-IT-014 - Returns unavailable power readings when the hardware snapshot is missing")
    void handlesMissingHardwareSnapshot() {

        // Simulate a hardware provider that cannot retrieve a snapshot.
        LiveHardwareReader reader = new LiveHardwareReader(() -> null);

        // Attempt to retrieve the monitoring snapshot.
        LiveMonitorSnapshot snapshot = reader.readSnapshot();

        // Verify that the reader still returns a snapshot object.
        assertNotNull(snapshot);

        // CPU power should be marked unavailable.
        assertTrue( Double.isNaN(snapshot.cpuPower()) );

        // GPU power should also be marked unavailable.
        assertTrue( Double.isNaN(snapshot.gpuPower()) );

        // The combined power must remain unavailable.
        assertTrue( Double.isNaN(snapshot.totalPower()) );
    }


}
