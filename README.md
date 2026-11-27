>> The following is AI generated. It was prompted and edited by Chris Dunne and is accurate to the project.
>> It's also not final, there's a few things left to be completed.

# EcoScan

EcoScan is a desktop application designed to help users understand the environmental impact of their computers. It analyses device hardware, provides information about emissions and component longevity, and helps users make more informed choices about everyday computer use and future hardware upgrades.

## Who is EcoScan for?

EcoScan is intended for people who want to learn more about their own computer hardware, its estimated energy-related emissions, and the potential environmental impact of replacing or retaining hardware.

## Main features

- **Hardware Scan:** View information about the computer's CPU, GPU, memory, motherboard and storage.
- **Results:** Review hardware scan results and the application's environmental-impact information.
- **Live Monitor:** See CPU and GPU power readings, live graphs, estimated carbon emissions, and comparisons.
- **Electricity carbon intensity:** Retrieve the latest available Great Britain grid carbon-intensity reading from the NESO Carbon Intensity API. When the API is unavailable, EcoScan uses an offline fallback estimate.
- **History:** Save and review hardware scans and completed Live Monitor sessions; search, sort, import, export, print and share supported records.

Carbon-emissions figures are **estimates**, not direct measurements of the computer's entire environmental footprint. Sensor availability and accuracy vary by hardware. The NESO reading represents Great Britain's electricity grid, rather than a device-specific electricity source.

## Download and run — Windows (recommended)

# DO NOT FORGET THIS FOR THE LOVE OF GOD
**Release:** `v1.0.0` (release publication pending)

**Portable ZIP:** `EcoScan-v1.0.0-Windows.zip`

1. Download the ZIP from the repository's **Releases** page once the release is published.
2. Right-click the ZIP and select **Extract All**. Extract the *whole archive* to a normal folder, such as Documents or Desktop.
3. Open the extracted `EcoScan` folder.
4. Double-click `EcoScan.exe` to launch the application.
5. Follow any first-run requests relating to administrator access or PawnIO installation. Check Windows prompts before approving them.

**Do not run `EcoScan.exe` from inside the ZIP or move it out of its folder.** The accompanying `app` and `runtime` folders must remain alongside the launcher.

The portable Windows release includes its own Java runtime and application libraries. **Java, Maven and IntelliJ are not required to run it.** There is no EcoScan account, sign-in or test login.

### Windows prerequisites

- **Operating system:** 64-bit Windows 11 is the primary supported and tested platform.
- **Permissions:** Administrator privileges are required for access to some hardware sensors and for installing or starting the PawnIO driver. Windows may display a User Account Control (UAC) prompt.
- **Hardware:** CPU/GPU sensor support depends on the device and driver availability. Some readings may display `N/A`.
- **Internet connection:** Optional for basic hardware scans and monitoring; required for live NESO electricity carbon-intensity updates. Offline operation uses the application's fallback carbon-intensity value.

### PawnIO and first-time setup

EcoScan uses LibreHardwareMonitor-related libraries and the **PawnIO** Windows driver to access supported hardware sensors. The portable release includes `app/installers/PawnIO_setup.exe` for the application's existing setup workflow.

On first launch, EcoScan may request administrator elevation and offer to install or start PawnIO if it is missing or stopped. Follow the application's prompts and any Windows installer instructions. A restart or relaunch may be needed after driver setup. If you choose not to grant administrator access, some monitoring functionality may be unavailable.

Only approve expected prompts from software you intentionally obtained from the official project release. Do not disable Windows security features to run EcoScan. PawnIO is a system driver, so installing it changes the computer's driver configuration even though EcoScan itself is portable.

## Using EcoScan

1. **Hardware Scan:** Start a scan and review the detected hardware.
2. **Results:** Inspect your scan information and save a named scan for later reference.
3. **Live Monitor:** Observe CPU/GPU power, graph readings and estimated emissions while EcoScan is running. Use the graph-layout toggle to switch between stacked and side-by-side views.
4. **History:** Review previous scans and monitoring sessions; use the available search, sort, CSV and PDF actions.

A Live Monitor session is recorded when EcoScan closes normally. Its History entry includes the total and average estimated emissions and the most recent recorded carbon intensity at the end of the session. Older records may show **Not recorded** for fields that were not available when they were saved.

EcoScan stores its saved history locally under the user's `EcoScan` directory, typically `%USERPROFILE%\EcoScan`. Back up important history before removing files or using clear/delete actions.

## Build from source (developers)

The following steps are **not required** for users of the portable ZIP.

### Requirements

- **JDK 21** (including `jpackage` when creating a Windows app image)
- **Apache Maven** available on the command line
- **Windows 11** for creating and testing the Windows portable build
- Access to the dependencies in `pom.xml`, including the repository's bundled `maven-repo` used for the project's patched `jLibreHardwareMonitor` dependency

From the repository root, run:

```powershell
# Compile the project and run the full automated test suite.
mvn clean test

# Package the application and copy runtime JAR dependencies to target/dependency.
mvn package
```

The application JAR is created at `target/hardware-monitor-1.0-SNAPSHOT.jar`. The Maven Dependency Plugin copies runtime JARs into `target/dependency/`.

### Build a portable Windows application image

From the repository root, using **PowerShell** and JDK 21's `jpackage`:

```powershell
# Create a clean staging directory.
New-Item -ItemType Directory -Force 'jpackage-build\input' | Out-Null
Get-ChildItem 'jpackage-build\input' | Remove-Item -Recurse -Force

# Stage the application and its runtime Java libraries.
Copy-Item 'target\hardware-monitor-1.0-SNAPSHOT.jar' 'jpackage-build\input\'
Copy-Item 'target\dependency\*.jar' 'jpackage-build\input\'

# Stage the hardware DLLs and the PawnIO installer.
New-Item -ItemType Directory -Force 'jpackage-build\input\lib' | Out-Null
New-Item -ItemType Directory -Force 'jpackage-build\input\installers' | Out-Null
Copy-Item 'lib\*.dll' 'jpackage-build\input\lib\'
Copy-Item 'installers\PawnIO_setup.exe' 'jpackage-build\input\installers\'

# Create a fresh console-free portable application image.
jpackage `
    --type app-image `
    --name EcoScan `
    --input 'jpackage-build\input' `
    --dest 'jpackage-build\output\EcoScan-release' `
    --main-jar 'hardware-monitor-1.0-SNAPSHOT.jar' `
    --main-class 'com.example.monitortest.Main'
```

The output launcher is `jpackage-build\output\EcoScan-release\EcoScan\EcoScan.exe`. If that app-image directory already exists, remove or rename **that previous output** before rerunning `jpackage`.

To create the release archive:

```powershell
# Archive the entire portable application folder, not just EcoScan.exe.
Compress-Archive `
    -Path 'jpackage-build\output\EcoScan-release\EcoScan' `
    -DestinationPath 'jpackage-build\output\EcoScan-v1.0.0-Windows.zip' `
    -Force
```

## Tests and release validation

Automated tests can be run from the repository root with:

```powershell
mvn clean test
```

For a manual release check, **use the actual ZIP**, transfer it to a separate Windows computer if possible, extract it and confirm the following:

- EcoScan opens without a separately installed JDK.
- PawnIO detection, installation/service startup and administrator elevation behave as expected.
- Hardware Scan and Results work on that computer.
- Live Monitor displays any available sensor readings, graphs and carbon-intensity information (or offline fallback).
- Saved scans and completed sessions remain in History after restarting EcoScan.
- Import/export and PDF report functions work, and EcoScan shuts down normally.

The final release should be described as **fresh-machine verified** only after that check has actually been completed. Testing a previously used computer after removing PawnIO is a useful clean-install simulation, but is not identical to testing an entirely unused machine.

## Troubleshooting and limitations

| Symptom | What to check |
| --- | --- |
| EcoScan will not launch | Extract the complete ZIP; keep `EcoScan.exe`, `app` and `runtime` together. |
| Windows requests administrator permission | This is expected for certain hardware-monitoring operations. Verify the prompt before approving it. |
| PawnIO is missing or stopped | Follow EcoScan's first-run driver installation/service prompts; relaunch or restart Windows if needed. |
| A CPU/GPU reading shows `N/A` | Hardware sensors differ between systems; some devices may not expose the requested measurements. |
| Carbon intensity shows offline fallback | Check connectivity to the NESO service. Basic monitoring can still continue offline. |
| Older History sessions show `Not recorded` | These fields were not captured by older EcoScan versions. |
| Windows displays a security warning | Confirm the executable came from this project's official release. Do not disable Windows security controls. |

**Platform note:** The final portable executable is intended for Windows. The source project includes limited macOS demonstration behaviour, but this Windows release is not a macOS application.

## Repository and final release

- **Source repository:** Add the official GitHub repository URL here.
- **Final source version:** Tag `v1.0.0` after release validation; include its corresponding commit hash in the submission.
- **Portable download:** Link to the published `EcoScan-v1.0.0-Windows.zip` GitHub Release asset.
- **Login credentials:** Not applicable — EcoScan has no user login.

# DO NOT FORGET THIS FOR THE LOVE OF GOD
*This README documents the planned v1.0.0 release package. Replace the release placeholders and update the verification status once testing and publication are complete.*
