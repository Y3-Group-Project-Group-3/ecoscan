package hardware.monitoring;

import com.example.monitortest.hardware.monitoring.LiveMonitorSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for LiveMonitorSnapshot.
// Verifies how EcoScan combines available CPU and GPU power readings.
class LiveMonitorSnapshotTest {

    // LIV-UT-010
    // Verify that both available sensors contribute to total power.
    @Test
    @DisplayName("LIV-UT-010 - Combines CPU and GPU power when both sensors are available")
    void combinesCpuAndGpuPower() {

        // Simulate valid readings from both hardware sensors.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(50.0, 30.0);

        // The combined power should be 50 + 30 = 80 watts.
        assertEquals(80.0, snapshot.totalPower(), 0.000001);
    }

    // LIV-UT-011
    // Verify that missing GPU power does not invalidate CPU power.
    @Test
    @DisplayName("LIV-UT-011 - Uses CPU power when the GPU sensor is unavailable")
    void usesCpuPowerWhenGpuUnavailable() {

        // Reproduce the missing GPU reading seen on the Intel laptop.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(9.6, Double.NaN);

        // CPU power should remain available for emissions estimation.
        assertEquals(9.6, snapshot.totalPower(), 0.000001);

        // The original GPU reading must still be marked unavailable.
        assertTrue(Double.isNaN(snapshot.gpuPower()));
    }

    // LIV-UT-012
    // Verify that GPU measurements can be used without CPU measurements.
    @Test
    @DisplayName("LIV-UT-012 - Uses GPU power when the CPU sensor is unavailable")
    void usesGpuPowerWhenCpuUnavailable() {

        // Simulate a missing CPU sensor with a working GPU sensor.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(Double.NaN, 45.0);

        // The available GPU reading should be used.
        assertEquals(45.0, snapshot.totalPower(), 0.000001);

        // The CPU measurement must remain unavailable.
        assertTrue(Double.isNaN(snapshot.cpuPower()));
    }

    // LIV-UT-013
    // Verify that total power is unavailable when both sensors fail.
    @Test
    @DisplayName("LIV-UT-013 - Returns NaN when both CPU and GPU sensors are unavailable")
    void returnsNaNWhenBothSensorsUnavailable() {

        // Simulate a complete loss of power sensor readings.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(Double.NaN, Double.NaN);

        // Do not report zero consumption when no measurements exist.
        assertTrue(Double.isNaN(snapshot.totalPower()));
    }

    // LIV-UT-014
    // Verify that a zero-watt GPU reading is treated as a valid value.
    @Test
    @DisplayName("LIV-UT-014 - Accepts zero GPU power without invalidating CPU power")
    void acceptsZeroGpuPower() {

        // Simulate the zero-watt GPU reading visible in LibreHardwareMonitor.
        LiveMonitorSnapshot snapshot = new LiveMonitorSnapshot(9.6, 0.0);

        // Zero GPU power should not invalidate the CPU measurement.
        assertEquals(9.6, snapshot.totalPower(), 0.000001);

        // Confirm that zero is a finite reading rather than NaN.
        assertTrue(Double.isFinite(snapshot.gpuPower()));
    }
}
