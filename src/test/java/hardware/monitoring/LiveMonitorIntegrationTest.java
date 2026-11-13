package hardware.monitoring;

import com.example.monitortest.hardware.detection.HardwareSensorReader;
import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsCalculator;
import com.example.monitortest.hardware.monitoring.emissions.CarbonEmissionsTracker;

import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for the Live Monitor calculation pipeline.
// Simulated sensors allow repeatable testing without physical hardware.
class LiveMonitorIntegrationTest {

    // Creates a simulated CPU Package power sensor.
    private Computer createCpuOnlyComputer(double powerWatts) {

        // Provide the CPU power measurement.
        Sensor cpuSensor = new Sensor();
        cpuSensor.setName("CPU Package");
        cpuSensor.setSensorType("Power");
        cpuSensor.setValue(powerWatts);

        // Associate the sensor with CPU hardware.
        Hardware cpu = new Hardware();
        cpu.setName("Intel Core i5");
        cpu.setHardwareType("Cpu");
        cpu.setSensors(List.of(cpuSensor));

        // Construct a complete hardware snapshot.
        // No GPU is present, simulating unavailable GPU power.
        Computer computer = new Computer();
        computer.setHardware(List.of(cpu));

        return computer;
    }


    // Creates a simulated computer with both CPU and NVIDIA GPU power sensors.
    // Allows the integration tests to control both readings independently.
    private Computer createCpuGpuComputer( double cpuPower, double gpuPower) {

        // Create the CPU Package power sensor.
        Sensor cpuSensor = new Sensor();
        cpuSensor.setName("CPU Package");
        cpuSensor.setSensorType("Power");
        cpuSensor.setValue(cpuPower);

        // Associate the CPU sensor with CPU hardware.
        Hardware cpu = new Hardware();
        cpu.setName("Intel Core i5");
        cpu.setHardwareType("Cpu");
        cpu.setSensors(List.of(cpuSensor));

        // Create a GPU Package power sensor.
        Sensor gpuSensor = new Sensor();
        gpuSensor.setName("GPU Package");
        gpuSensor.setSensorType("Power");
        gpuSensor.setValue(gpuPower);

        // Associate the GPU sensor with NVIDIA GPU hardware.
        Hardware gpu = new Hardware();
        gpu.setName("NVIDIA GeForce RTX");
        gpu.setHardwareType("GpuNvidia");
        gpu.setSensors(List.of(gpuSensor));

        // Build a hardware snapshot containing both devices.
        Computer computer = new Computer();
        computer.setHardware(List.of(cpu, gpu));

        return computer;
    }


    // Converts a simulated hardware snapshot into a LiveMonitorSnapshot.
    // Uses the production sensor reader to retrieve CPU and GPU power.
    private LiveMonitorSnapshot readPowerSnapshot( HardwareSensorReader reader, Computer computer) {

        // Retrieve CPU Package power from the simulated CPU hardware.
        double cpuPower = reader.getSensorValue( computer, "Cpu", "Power", "CPU Package" );

        // Retrieve GPU power using the reader's supported sensor names.
        double gpuPower = reader.getGpuPower(computer);

        // Pass the retrieved readings to the Live Monitor model.
        return new LiveMonitorSnapshot(cpuPower, gpuPower);
    }



    // LIV-IT-001
    // Verify that CPU sensor data reaches the emissions tracker even when no GPU power sensor exists.
    @Test
    @DisplayName("LIV-IT-001 - Accumulates CO2 emissions from CPU power when the GPU sensor is missing")
    void accumulatesCpuEmissionsWithoutGpuSensor() {

        // Create the hardware sensor reader and emissions tracker.
        HardwareSensorReader reader = new HardwareSensorReader();

        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );

        // Simulate CPU hardware consuming 20 watts.
        Computer computer = createCpuOnlyComputer(20.0);

        // Retrieve the CPU Package sensor using the actual reader.
        double cpuPower = reader.getSensorValue( computer, "Cpu", "Power", "CPU Package" );

        // Attempt to retrieve GPU power from the same snapshot.
        double gpuPower = reader.getGpuPower(computer);

        // Verify that CPU power is available but GPU power is not.
        assertEquals(20.0, cpuPower, 0.000001);
        assertTrue(Double.isNaN(gpuPower));

        // Construct the Live Monitor snapshot from both readings.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(cpuPower, gpuPower);

        // Verify that missing GPU power does not invalidate CPU power.
        assertEquals(20.0, snapshot.totalPower(), 0.000001);

        // Establish the first measurement at 1 second.
        tracker.addSnapshot(snapshot, 1000L);

        // Simulate another identical measurement 60 seconds later.
        tracker.addSnapshot(snapshot, 61000L);

        // Expected: (20 W * 60 s / 3,600,000) * 200 gCO2e/kWh.
        double expectedEmissions = 0.0666666667;

        // Verify that the complete calculation pipeline works.
        assertEquals( expectedEmissions, tracker.getTotalEmissionsGrams(), 0.000001 );
    }


    // LIV-IT-002
    // Verify that valid CPU and GPU sensor readings are combined and used to calculate emissions over a monitoring interval.
    @Test
    @DisplayName("LIV-IT-002 - Accumulates CO₂ emissions using combined CPU and GPU power")
    void accumulatesCombinedCpuAndGpuEmissions() {

        // Create the sensor reader and emissions tracker.
        HardwareSensorReader reader = new HardwareSensorReader();

        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );

        // Simulate CPU power of 50 W and GPU power of 30 W.
        Computer computer = createCpuGpuComputer(50.0, 30.0);

        // Retrieve CPU Package Power from the simulated hardware.
        double cpuPower = reader.getSensorValue( computer, "Cpu", "Power", "CPU Package" );

        // Retrieve GPU Package Power from the simulated NVIDIA GPU.
        double gpuPower = reader.getGpuPower(computer);

        // Verify that the expected individual readings were retrieved.
        assertEquals(50.0, cpuPower, 0.000001);
        assertEquals(30.0, gpuPower, 0.000001);

        // Create the snapshot using the readings from the sensor reader.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(cpuPower, gpuPower);

        // Both readings should produce a combined total of 80 W.
        assertEquals(80.0, snapshot.totalPower(), 0.000001);

        // Establish the first measurement.
        tracker.addSnapshot(snapshot, 1000L);

        // Simulate another measurement 60 seconds later.
        tracker.addSnapshot(snapshot, 61000L);

        // Expected: (80 W * 60 s / 3,600,000) * 200 gCO2e/kWh.
        double expectedEmissions = 0.2666666667;

        // Verify that the accumulated emissions match the expected result.
        assertEquals( expectedEmissions, tracker.getTotalEmissionsGrams(), 0.000001 );
    }


    // LIV-IT-003
    // Verify that the emissions tracker handles a complete sensor outage and resumes accumulating emissions after valid readings return.
    @Test
    @DisplayName("LIV-IT-003 - Resumes CO₂ emissions tracking after temporary sensor failure")
    void resumesTrackingAfterSensorRecovery() {

        // Create the production sensor reader and emissions tracker.
        HardwareSensorReader reader = new HardwareSensorReader();

        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );

        // Create a valid hardware snapshot containing 60 W total power.
        Computer validComputer = createCpuGpuComputer(50.0, 10.0);

        // Create a hardware snapshot where both readings are unavailable.
        Computer unavailableComputer = createCpuGpuComputer( Double.NaN, Double.NaN );

        // Convert the simulated hardware into Live Monitor snapshots.
        LiveMonitorSnapshot validSnapshot = readPowerSnapshot(reader, validComputer);

        LiveMonitorSnapshot unavailableSnapshot = readPowerSnapshot(reader, unavailableComputer);

        // Verify that valid hardware produces the expected total power.
        assertEquals(60.0, validSnapshot.totalPower(), 0.000001);

        // Verify that the unavailable hardware produces no valid total.
        assertTrue(Double.isNaN(unavailableSnapshot.totalPower()));

        // Step 1: Establish an initial valid measurement.
        tracker.addSnapshot(validSnapshot, 1000L);

        // Step 2: Both sensors fail after 60 seconds.
        // The tracker should discard its previous measurement baseline.
        tracker.addSnapshot(unavailableSnapshot, 61000L);

        // Step 3: Sensors recover after another 60 seconds.
        // This measurement creates a new baseline.
        tracker.addSnapshot(validSnapshot, 121000L);

        // No emissions should have accumulated across invalid intervals.
        assertEquals( 0.0, tracker.getTotalEmissionsGrams(), 0.000001 );

        // Step 4: Another valid measurement arrives 60 seconds later.
        // The tracker can now calculate emissions for the valid interval.
        tracker.addSnapshot(validSnapshot, 181000L);

        // Expected: (60 W * 60 s / 3,600,000) * 200 gCO2e/kWh.
        double expectedEmissions = 0.2;

        // Verify that emissions resume accumulating after recovery.
        assertEquals( expectedEmissions, tracker.getTotalEmissionsGrams(), 0.000001 );
    }


    // LIV-IT-004
    // Verify that negative hardware sensor readings cannot produce invalid emissions or corrupt the accumulated emissions total.
    @Test
    @DisplayName("LIV-IT-004 - Rejects negative hardware power readings without accumulating emissions")
    void rejectsNegativeHardwarePowerReadings() {

        // Create the production hardware sensor reader.
        HardwareSensorReader reader = new HardwareSensorReader();

        // Create a new emissions tracker with the default carbon intensity.
        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );

        // Create valid simulated hardware with 60 W combined power.
        Computer validComputer = createCpuGpuComputer(50.0, 10.0);

        // Create invalid hardware where both power sensors report negatives.
        Computer invalidComputer = createCpuGpuComputer(-20.0, -10.0);

        // Convert the simulated hardware into monitoring snapshots.
        LiveMonitorSnapshot validSnapshot = readPowerSnapshot(reader, validComputer);
        LiveMonitorSnapshot invalidSnapshot = readPowerSnapshot(reader, invalidComputer);

        // Confirm that the valid hardware produces the expected power.
        assertEquals(60.0, validSnapshot.totalPower(), 0.000001);

        // Verify that invalid power measurements are rejected.
        assertTrue( Double.isNaN(invalidSnapshot.totalPower()) );

        // Step 1: Establish the initial valid measurement baseline.
        tracker.addSnapshot(validSnapshot, 1000L);

        // Step 2: Both sensors report invalid readings after 60 seconds.
        tracker.addSnapshot(invalidSnapshot, 61000L);

        // Step 3: Valid hardware readings return after another minute.
        // This should establish a new baseline without adding emissions.
        tracker.addSnapshot(validSnapshot, 121000L);

        // No valid measurement interval has been completed.
        // The accumulated emissions should remain zero.
        assertEquals( 0.0, tracker.getTotalEmissionsGrams(), 0.000001 );
    }


    // LIV-IT-005
    // Verify that the Live Monitor correctly accumulates emissions across several intervals with changing CPU and GPU power consumption.
    @Test
    @DisplayName("LIV-IT-005 - Accumulates correct CO2 emissions across changing power intervals")
    void accumulatesEmissionsAcrossChangingPowerIntervals() {

        // Create the production sensor reader and emissions tracker.
        HardwareSensorReader reader = new HardwareSensorReader();

        CarbonEmissionsTracker tracker = new CarbonEmissionsTracker( new CarbonEmissionsCalculator() );

        // Create four simulated hardware snapshots with changing power.
        Computer computer1 = createCpuGpuComputer(40.0, 10.0);
        Computer computer2 = createCpuGpuComputer(60.0, 20.0);
        Computer computer3 = createCpuGpuComputer(80.0, 30.0);
        Computer computer4 = createCpuGpuComputer(50.0, 10.0);

        // Convert each hardware reading into a monitoring snapshot.
        LiveMonitorSnapshot snapshot1 = readPowerSnapshot(reader, computer1);
        LiveMonitorSnapshot snapshot2 = readPowerSnapshot(reader, computer2);
        LiveMonitorSnapshot snapshot3 = readPowerSnapshot(reader, computer3);
        LiveMonitorSnapshot snapshot4 = readPowerSnapshot(reader, computer4);

        // Verify that the power totals match the simulated hardware.
        assertEquals(50.0, snapshot1.totalPower(), 0.000001);
        assertEquals(80.0, snapshot2.totalPower(), 0.000001);
        assertEquals(110.0, snapshot3.totalPower(), 0.000001);
        assertEquals(60.0, snapshot4.totalPower(), 0.000001);

        // Establish the initial monitoring baseline at 1 second.
        tracker.addSnapshot(snapshot1, 1000L);

        // First interval: 50 W to 80 W over 30 seconds.
        tracker.addSnapshot(snapshot2, 31000L);

        // Second interval: 80 W to 110 W over 30 seconds.
        tracker.addSnapshot(snapshot3, 61000L);

        // Third interval: 110 W to 60 W over 30 seconds.
        tracker.addSnapshot(snapshot4, 91000L);

        // Calculate the expected emissions independently.
        // The tracker uses average power for each interval.
        double interval1 = (65.0 * 30.0 / 3_600_000.0) * 200.0;
        double interval2 = (95.0 * 30.0 / 3_600_000.0) * 200.0;
        double interval3 = (85.0 * 30.0 / 3_600_000.0) * 200.0;

        // Sum the emissions produced across all three intervals.
        double expectedEmissions = interval1 + interval2 + interval3;

        // Verify that the complete monitoring pipeline accumulated correctly.
        assertEquals( expectedEmissions, tracker.getTotalEmissionsGrams(), 0.000001 );
    }




}
