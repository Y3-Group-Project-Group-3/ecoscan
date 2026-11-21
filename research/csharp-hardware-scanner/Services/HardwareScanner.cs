
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Management;
using EcoScan.HardwareScanner.Models;

namespace EcoScan.HardwareScanner.Services
{
    public class HardwareScanner
    {

        // Retrieves operating system details using WMI.
        public OsInfo GetOsInfo()
        {
            var osInfo = new OsInfo();

            using var searcher = new ManagementObjectSearcher(
                "SELECT Caption, OSArchitecture, Version " +
                "FROM Win32_OperatingSystem");

            foreach (ManagementObject os in searcher.Get())
            {
                using (os)
                {
                    // Store the Windows name, architecture and version.
                    osInfo.Name =
                        os["Caption"]?.ToString()?.Trim() ?? "Unknown";

                    osInfo.Architecture =
                        os["OSArchitecture"]?.ToString() ?? "Unknown";

                    osInfo.Version =
                        os["Version"]?.ToString() ?? "Unknown";
                }

                break;
            }

            return osInfo;
        }

        // Retrieves the computer manufacturer, model and system type.
        public SystemInfo GetSystemInfo()
        {
            var systemInfo = new SystemInfo();

            using var searcher = new ManagementObjectSearcher(
                "SELECT Manufacturer, Model, SystemType " +
                "FROM Win32_ComputerSystem");

            foreach (ManagementObject system in searcher.Get())
            {
                using (system)
                {
                    systemInfo.Manufacturer =
                        system["Manufacturer"]?.ToString() ?? "Unknown";

                    systemInfo.Model =
                        system["Model"]?.ToString() ?? "Unknown";

                    systemInfo.SystemType =
                        system["SystemType"]?.ToString() ?? "Unknown";
                }

                break;
            }

            return systemInfo;
        }

        // Retrieves the total installed physical memory in gigabytes.


        // Retrieves the total installed physical memory in GiB.
        public MemoryInfo GetMemoryInfo()
        {
            var memoryInfo = new MemoryInfo();
            ulong totalBytes = 0;
            bool memoryFound = false;

            // Retrieve the capacity of each installed memory module.
            using var searcher = new ManagementObjectSearcher(
                "SELECT Capacity FROM Win32_PhysicalMemory");

            foreach (ManagementObject memory in searcher.Get())
            {
                using (memory)
                {
                    // WMI returns the capacity of each module in bytes.
                    if (ulong.TryParse(
                        memory["Capacity"]?.ToString(),
                        out ulong capacity))
                    {
                        totalBytes += capacity;
                        memoryFound = true;
                    }
                }
            }

            // Convert the total capacity from bytes to GiB (1024^3).
            if (memoryFound)
            {
                memoryInfo.TotalGB = Math.Round(
                    totalBytes / 1024.0 / 1024.0 / 1024.0, 2);
            }

            return memoryInfo;
        }



        // Retrieves all graphics adapters detected by Windows.
        public List<GpuInfo> GetGpuInfo()
        {
            var gpuList = new List<GpuInfo>();

            using var searcher = new ManagementObjectSearcher(
                "SELECT Name FROM Win32_VideoController");

            foreach (ManagementObject gpu in searcher.Get())
            {
                using (gpu)
                {
                    // Store each detected graphics adapter separately.
                    gpuList.Add(new GpuInfo
                    {
                        Name = gpu["Name"]?.ToString() ?? "Unknown"
                    });
                }
            }

            return gpuList;
        }

        // Retrieves all physical storage devices detected by Windows.
        public List<StorageInfo> GetStorageInfo()
        {
            var storageList = new List<StorageInfo>();

            // Retrieve drive types separately using the Windows Storage API.
            var driveTypes = GetStorageTypes();

            using var searcher = new ManagementObjectSearcher(
                "SELECT Model, Size, SerialNumber FROM Win32_DiskDrive");

            foreach (ManagementObject disk in searcher.Get())
            {
                using (disk)
                {
                    string serial = NormalizeSerialNumber(
                        disk["SerialNumber"]?.ToString());

                    string driveType = "Unknown";

                    // Match drive types by serial number rather than list order.
                    if (!string.IsNullOrEmpty(serial) &&
                        driveTypes.TryGetValue(serial, out string? type))
                    {
                        driveType = type;
                    }

                    double? capacityGB = null;


                    // Convert drive capacity from bytes to decimal GB.
                    if (ulong.TryParse(
                        disk["Size"]?.ToString(),
                        out ulong totalBytes))
                    {
                        capacityGB = Math.Round(
                            totalBytes / 1000.0 / 1000.0 / 1000.0, 2);
                    }


                    storageList.Add(new StorageInfo
                    {
                        Model = disk["Model"]?.ToString() ?? "Unknown",
                        Type = driveType,
                        CapacityGB = capacityGB
                    });
                }
            }

            return storageList;
        }

        // Reads storage media types from the Windows Storage Management API.
        // A failed query should not prevent basic drive information being collected.
        private static Dictionary<string, string> GetStorageTypes()
        {
            var driveTypes = new Dictionary<string, string>(
                StringComparer.OrdinalIgnoreCase);

            try
            {
                var scope = new ManagementScope(
                    @"\\.\ROOT\Microsoft\Windows\Storage");

                scope.Connect();

                using var searcher = new ManagementObjectSearcher(
                    scope,
                    new ObjectQuery(
                        "SELECT SerialNumber, MediaType FROM MSFT_PhysicalDisk"));

                foreach (ManagementObject disk in searcher.Get())
                {
                    using (disk)
                    {
                        string serial = NormalizeSerialNumber(
                            disk["SerialNumber"]?.ToString());

                        if (string.IsNullOrEmpty(serial))
                            continue;

                        int? mediaType = ConvertToInt(disk["MediaType"]);

                        string type = mediaType switch
                        {
                            3 => "HDD",
                            4 => "SSD",
                            5 => "SCM",
                            _ => "Unknown"
                        };

                        // Avoid assigning a type when duplicate serial numbers
                        // make it impossible to identify a drive reliably.
                        if (!driveTypes.TryAdd(serial, type))
                        {
                            driveTypes[serial] = "Unknown";
                        }
                    }
                }
            }
            catch (ManagementException)
            {
                // Storage type information is optional.
                // Drives will still be listed with an Unknown type.
            }
            catch (UnauthorizedAccessException)
            {
                // Some systems restrict access to storage management data.
            }
            catch (System.Runtime.InteropServices.COMException)
            {
                // The storage management provider may be unavailable.
            }

            return driveTypes;
        }

        // Removes surrounding whitespace from drive serial numbers.
        private static string NormalizeSerialNumber(string? serial)
        {
            return serial?.Trim() ?? string.Empty;
        }

        // Retrieves CPU specifications using WMI.
        public CpuInfo GetCpuInfo()
        {
            // Request only the CPU properties needed for our scanner.
            using var searcher = new ManagementObjectSearcher(
                "SELECT Name, Manufacturer, NumberOfCores, " +
                "NumberOfLogicalProcessors, MaxClockSpeed " +
                "FROM Win32_Processor");

            foreach (ManagementObject cpu in searcher.Get())
            {
                using (cpu)
                {
                    // Store the collected values in a CpuInfo object.
                    // Missing values are handled to avoid conversion errors.
                    return new CpuInfo
                    {
                        Name = cpu["Name"]?.ToString() ?? "Unknown",
                        Manufacturer = cpu["Manufacturer"]?.ToString() ?? "Unknown",
                        Cores = ConvertToInt(cpu["NumberOfCores"]),
                        LogicalProcessors = ConvertToInt(
                            cpu["NumberOfLogicalProcessors"]),
                        MaxClockMHz = ConvertToInt(cpu["MaxClockSpeed"])
                    };
                }
            }

            // Return default values if no CPU information is found.
            return new CpuInfo();
        }

        // Converts WMI values to integers.
        // Returns null when the value is missing or cannot be converted.
        private static int? ConvertToInt(object? value)
        {
            if (value == null)
                return null;

            return int.TryParse(value.ToString(), out int result)
                ? result
                : null;
        }
    }
}
