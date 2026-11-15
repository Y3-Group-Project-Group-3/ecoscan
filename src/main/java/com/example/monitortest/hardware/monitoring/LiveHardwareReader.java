package com.example.monitortest.hardware.monitoring;

import com.example.monitortest.hardware.detection.HardwareSnapshotProvider;
import com.example.monitortest.hardware.detection.HardwareSensorReader;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import java.util.function.Supplier;

// Retrieves current CPU and GPU power consumption readings.
public class LiveHardwareReader {

//    private final HardwareSnapshotProvider snapshotProvider;
    // Supplies a hardware snapshot from the real provider or a test fixture.
    private final Supplier<Computer> snapshotSupplier;
    private final HardwareSensorReader sensorReader;

//    public LiveHardwareReader() {
//        // Reuse the existing hardware sensor infrastructure.
//        snapshotProvider = new HardwareSnapshotProvider();
//        sensorReader = new HardwareSensorReader();
//    }


    // Production constructor.
    // Continue retrieving hardware from EcoScan's existing provider.
    public LiveHardwareReader() {
        // Delegate to the testable constructor using real hardware access.
        this(HardwareSnapshotProvider::getSnapshot);
    }

    // Package-private constructor for integration testing.
    // Allows tests to supply simulated hardware without accessing sensors.
    public LiveHardwareReader(Supplier<Computer> snapshotSupplier) {

        // Store the selected hardware snapshot source.
        this.snapshotSupplier = snapshotSupplier;

        // Continue using EcoScan's real sensor selection logic.
        this.sensorReader = new HardwareSensorReader();
    }


    /**
     * Retrieves a new snapshot of CPU and GPU power consumption.
     * @return Power measurements in watts, or NaN if unavailable.
     */
    public LiveMonitorSnapshot readSnapshot() {

        // Retrieve the latest available hardware sensor snapshot.
//        Computer computer = snapshotProvider.getSnapshot();
//        Computer computer = HardwareSnapshotProvider.getSnapshot();

        // Retrieve hardware from the configured snapshot source.
        Computer computer = snapshotSupplier.get();

        // Default to unavailable readings if the snapshot cannot be retrieved.
        if (computer == null) {
            return new LiveMonitorSnapshot(Double.NaN, Double.NaN);
        }

        // Retrieve CPU Package Power.
        double cpuPower = sensorReader.getSensorValue( computer, "Cpu", "Power", "Package" );

        // Some processors use an alternative sensor name.
        if (Double.isNaN(cpuPower)) {
            cpuPower = sensorReader.getSensorValue( computer, "Cpu", "Power", "CPU Package" );
        }

        // Retrieve NVIDIA GPU Package Power.
        double gpuPower = sensorReader.getGpuPower(computer);

        return new LiveMonitorSnapshot(cpuPower, gpuPower);
    }
}
