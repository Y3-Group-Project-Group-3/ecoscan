# EcoScan C# Hardware Scanner

## Overview

This project is a standalone Windows hardware scanner developed as part of the EcoScan group project's technical investigation.

It uses C# and Windows Management Instrumentation (WMI) to retrieve hardware and system information and export the collected data to a CSV file.

The prototype was developed to investigate an alternative hardware detection method and evaluate its potential integration with the main Java EcoScan application.

## Technologies

- C# / .NET 8.0
- Windows Forms
- Windows Management Instrumentation (WMI)
- System.Management NuGet package
- Visual Studio 2022

## Features

The scanner collects information about:

- System manufacturer, model and operating system
- CPU model, manufacturer, core count and logical processors
- Installed physical memory
- GPU information
- Storage devices

The collected information is exported to CSV for further processing or comparison.

## Project Structure

- `Models/` — Data classes representing hardware information.
- `Services/HardwareScanner.cs` — WMI queries and hardware information retrieval.
- `Services/CsvExporter.cs` — CSV file generation.
- `Form1.cs` — Windows Forms user interface.
- `Program.cs` — Application entry point.

## CSV Format

The prototype uses the following structure:

```csv
Category,Instance,Property,Value
```

Each row represents a hardware or system property.

The CSV format was designed to support potential data exchange between the C# scanner and the Java EcoScan application.

## Building the Project

Requirements:

- Windows 10 or Windows 11
- .NET 8 SDK
- Visual Studio 2022 (optional)

Build using:

```powershell
dotnet build EcoScan.HardwareScanner.csproj
```

The application can also be opened using `EcoScan.HardwareScanner.sln` in Visual Studio 2022.

## Integration Status

This is currently an independent research prototype.

The main Java EcoScan application already retrieves hardware information using OSHI and other hardware monitoring mechanisms.

The C# scanner is not currently integrated into the Java application. Its purpose is to provide an alternative Windows-specific hardware detection method and structured CSV export.

Future integration should be considered only if it provides additional functionality or improves hardware detection reliability.
