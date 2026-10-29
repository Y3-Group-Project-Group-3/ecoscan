# EcoScan — Hardware Data Access Guide

## Overview

EcoScan retrieves hardware specifications and available sensor readings, storing the results in a `HardwareInfo` object.

Other application components should access hardware information through this object rather than triggering additional scans or communicating directly with hardware monitoring libraries.

The results represent a snapshot of the hardware at the time of scanning.

## 1. Accessing HardwareInfo

After a hardware scan completes, the application receives a `HardwareInfo` object.

For example, inside the existing scan completion workflow:

```java
HardwareInfo info = hardwareInfoService.getHardwareInfo();

ComponentSection.updateComponentSection(info);
```

Other UI components can receive the same object:

```java
EnergyUsageSection.updateEnergyUsage(info);
ApplianceComparisonSection.updateComparison(info);
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

## 8. Power Data Completeness

| Method | Description |
|---|---|
| `info.hasCompleteMeasuredPower()` | Both CPU and GPU power readings are available |
| `info.hasCompleteRamPowerEstimate()` | All detected RAM modules have supported estimates |
| `info.hasCompleteStoragePowerEstimate()` | All detected storage devices have supported estimates |

These methods describe data coverage, not measurement accuracy.

## 9. Estimated Power Assumptions

EcoScan currently uses fixed baseline estimates:

| Component | Estimated power |
|---|---:|
| DDR2 module | 4.0 W |
| DDR3 module | 3.0 W |
| DDR4 module | 2.5 W |
| DDR5 module | 3.5 W |
| SSD | 2.5 W |
| HDD | 5.0 W |

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

**Windows:** Hardware information is retrieved using OSHI, LibreHardwareMonitor integration and Windows-specific storage detection.

**macOS:** The current implementation may use simulated readings or placeholder hardware data.

Consumers should not assume that every reported power value is a physical sensor measurement.

## 12. Integration Recommendations

- Reuse the `HardwareInfo` object returned by the latest completed scan.
- Avoid triggering additional scans from individual UI components.
- Use the existing power calculation methods rather than implementing duplicate calculations.
- Check for unavailable measurements before displaying or calculating values.
- Clearly distinguish measured and estimated power.
- Remember that a power reading in watts is not itself an energy consumption measurement in kWh.
