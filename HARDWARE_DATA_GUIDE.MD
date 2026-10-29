# EcoScan — Hardware Data Access Guide

## Overview

EcoScan retrieves hardware specifications and available sensor readings, storing the results in a `HardwareInfo` object.

Other application components should access hardware information through this object rather than triggering additional scans or communicating directly with hardware monitoring libraries.

The results represent a snapshot of the hardware at the time of scanning.

## 1. Accessing HardwareInfo

After a hardware scan completes, EcoScan passes a HardwareInfo object to the results interface.

UI components should receive and reuse this object rather than initiating additional hardware scans.

For example, a component could expose an update method:

```java
import com.example.monitortest.hardware.model.HardwareInfo;
import com.example.monitortest.hardware.model.MemoryModule;
import com.example.monitortest.hardware.model.StorageDevice;
```

Other UI components can receive the same object:

```java
public void updateHardwareInformation(HardwareInfo info) {
    String cpuName = info.getCpuName();
    double cpuPower = info.getCpuPower();

    // Update the component's UI using these values.
}
```

The latter two are examples of possible integration methods, not existing EcoScan methods.

## 2. CPU Information

| Method | Description | Units |
|---|---|---|
| `info.getCpuName()` | CPU model | Text |
| `info.getCpuTemperature()` | CPU temperature | °C |
| `info.getCpuPower()` | CPU package power reading | W |
| `info.isCpuPowerSimulated()` | Whether CPU power is simulated | Boolean |

CPU temperature and power readings depend on hardware sensor availability.

## 3. GPU Information

| Method | Description | Units |
|---|---|---|
| `info.getGpuName()` | GPU model | Text |
| `info.getGpuVendor()` | GPU manufacturer | Text |
| `info.getGpuVram()` | GPU video memory | Check model units |
| `info.getGpuPower()` | GPU power reading | W |
| `info.getGpuTemperature()` | GPU temperature | °C |

GPU sensor readings may be unavailable on unsupported hardware.

## 4. RAM Information

| Method | Description | Units |
|---|---|---|
| `info.getTotalMemory()` | Total system memory | Check model units |
| `info.getMemoryModules()` | Detected physical RAM modules | List |
| `module.getMemoryType()` | RAM generation | Text |
| `module.getCapacityGB()` | Module capacity | GB |
| `module.getClockSpeedMHz()` | Reported module speed | MHz |

Example:

```java
for (MemoryModule module : info.getMemoryModules()) {
    System.out.println(module.getMemoryType());
    System.out.println(module.getCapacityGB());
    System.out.println(module.getClockSpeedMHz());
}
```

## 5. Storage Information

| Method | Description | Units |
|---|---|---|
| `info.getStorageDevices()` | Detected physical storage drives | List |
| `device.getModel()` | Storage model | Text |
| `device.getCapacityGB()` | Storage capacity | GB |
| `device.getCapacityBytes()` | Storage capacity | Bytes |
| `device.getType()` | SSD, HDD or UNKNOWN | Text |

Example:

```java
for (StorageDevice device : info.getStorageDevices()) {
    System.out.println(device.getModel());
    System.out.println(device.getType());
    System.out.println(device.getCapacityGB());
}
```

## 6. Motherboard Information

| Method | Description |
|---|---|
| `info.getMotherboardManufacturer()` | Motherboard manufacturer |
| `info.getMotherboardModel()` | Motherboard model |
| `info.getMotherboardVersion()` | Motherboard version |

## 7. Power Calculations

| Method | Description | Units |
|---|---|---|
| `info.getTotalMeasuredPower()` | Sum of available CPU and GPU power readings | W |
| `info.getEstimatedRamPower()` | Estimated RAM consumption | W |
| `info.getEstimatedStoragePower()` | Estimated storage consumption | W |
| `info.getCombinedComponentPower()` | Available CPU/GPU readings plus RAM/Storage estimates | W |

Example:

```java
double measuredPower = info.getTotalMeasuredPower();
double combinedPower = info.getCombinedComponentPower();

if (Double.isFinite(combinedPower)) {
    System.out.printf("Combined power: %.1f W%n", combinedPower);
}
```

**Important:** These values represent component-level power subtotals, not total electricity consumption measured at the wall socket.

#### MacOS Info
On macOS, EcoScan supplies fabricated CPU and GPU power readings for front-end development. These values participate in getTotalMeasuredPower() and getCombinedComponentPower() just like numerical readings obtained from Windows sensors.

Consequently, the term "measured" in these method names refers to the application's power-data category, not necessarily to a physical sensor measurement.

## 8. Power Data Completeness

| Method | Description |
|---|---|
| `info.hasCompleteMeasuredPower()` | Both CPU and GPU power readings are available |
| `info.hasCompleteRamPowerEstimate()` | All detected RAM modules have supported estimates |
| `info.hasCompleteStoragePowerEstimate()` | All detected storage devices have supported estimates |

These methods describe data coverage, not measurement accuracy.

## 9. Estimated Power Assumptions

EcoScan currently uses fixed baseline estimates:

| Component           | Estimated power |
|---------------------|----------------:|
| DDR2 module         |           4.0 W |
| DDR3 module         |           3.0 W |
| DDR4 module         |           2.5 W |
| DDR5 module         |           3.5 W |
| LPDDR5 Module (mac) |           2.0 W |
| SSD                 |           2.5 W |
| HDD                 |           5.0 W |

The values are defined in `PowerEstimates.java`.

They are approximate assumptions and should not be presented as actual sensor readings.

## 10. Handling Missing Information

Power calculations may return `Double.NaN` when no supported measurement or estimate is available.

Always validate numeric readings before using them:

```java
double power = info.getTotalMeasuredPower();

if (Double.isFinite(power)) {
    System.out.printf("%.1f W%n", power);
} else {
    System.out.println("Power unavailable");
}
```

The application should distinguish unavailable data from a genuine reading of zero.

## 11. Platform Support

### Windows

EcoScan retrieves hardware specifications and sensor readings using OSHI, LibreHardwareMonitor integration, and Windows-specific storage detection.

CPU and GPU power readings depend on hardware sensor availability. RAM and storage power consumption are calculated using fixed estimates.

### macOS

EcoScan provides simulated hardware information to support development and testing of the Swing user interface.

The current implementation combines real system specifications retrieved through OSHI with fabricated hardware information.

| Component | macOS behaviour |
|---|---|
| CPU name | Retrieved through OSHI |
| CPU power | Simulated using the hardware profile or fallback |
| GPU | Simulated integrated GPU information |
| GPU power | Fabricated power reading |
| RAM capacity | Retrieved through OSHI |
| RAM modules | Simulated LPDDR5 module |
| Storage | Simulated 256 GB SSD |
| RAM power | Fixed estimate based on memory type |
| Storage power | Fixed estimate based on device type |

For the current Apple M3 development configuration, the simulated power breakdown is:

- CPU: 12.0 W
- GPU: 8.0 W
- RAM: 2.0 W
- Storage: 2.5 W
- Combined component power: 24.5 W

These values are intended to provide a complete dataset for UI development. They are not measurements of the Mac's actual power consumption.

The macOS simulation uses the same `HardwareInfo` interface and power calculation methods as the Windows implementation.

This allows front-end components to be developed and tested on either operating system without requiring separate UI implementations.
## 12. Integration Recommendations

- Reuse the `HardwareInfo` object returned by the latest completed scan.
- Avoid triggering additional scans from individual UI components.
- Use the existing power calculation methods rather than implementing duplicate calculations.
- Check for unavailable measurements before displaying or calculating values.
- Clearly distinguish measured and estimated power.
- Remember that a power reading in watts is not itself an energy consumption measurement in kWh.
