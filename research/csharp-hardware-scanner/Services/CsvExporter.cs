using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Globalization;
using EcoScan.HardwareScanner.Models;

namespace EcoScan.HardwareScanner.Services
{
    public class CsvExporter
    {
        // Exports the collected hardware information to a UTF-8 CSV file.
        // Returns the file path when the export is completed successfully.
        public string Export(HardwareData hardwareData, string outputPath)
        {
            if (hardwareData == null)
                throw new ArgumentNullException(nameof(hardwareData));

            if (string.IsNullOrWhiteSpace(outputPath))
                throw new ArgumentException(
                    "The output path cannot be empty.",
                    nameof(outputPath));

            var rows = new List<string>
            {
                "Category,Instance,Property,Value"
            };

            // Add metadata to identify the CSV format and scan date.
            AddRow(rows, "Metadata", 0, "SchemaVersion", "1.0");
            AddRow(rows, "Metadata", 0, "ScanDate",
                DateTimeOffset.Now.ToString("o", CultureInfo.InvariantCulture));

            // Add basic computer information.
            AddRow(rows, "System", 0, "Manufacturer",
                hardwareData.System.Manufacturer);

            AddRow(rows, "System", 0, "Model",
                hardwareData.System.Model);

            AddRow(rows, "System", 0, "SystemType",
                hardwareData.System.SystemType);

            // Add processor information.
            AddRow(rows, "CPU", 0, "Manufacturer",
                hardwareData.Cpu.Manufacturer);

            AddRow(rows, "CPU", 0, "Name",
                hardwareData.Cpu.Name);

            AddRow(rows, "CPU", 0, "Cores",
                FormatNumber(hardwareData.Cpu.Cores));

            AddRow(rows, "CPU", 0, "LogicalProcessors",
                FormatNumber(hardwareData.Cpu.LogicalProcessors));

            AddRow(rows, "CPU", 0, "MaxClockMHz",
                FormatNumber(hardwareData.Cpu.MaxClockMHz));

            // Add the total amount of physical memory.
            //AddRow(rows, "RAM", 0, "TotalGB",
            AddRow(rows, "RAM", 0, "TotalGiB",
                FormatNumber(hardwareData.Memory.TotalGB));

            // Export each graphics adapter with its own instance number.
            for (int i = 0; i < hardwareData.Gpus.Count; i++)
            {
                AddRow(rows, "GPU", i, "Name",
                    hardwareData.Gpus[i].Name);
            }

            // Export each physical drive with its own instance number.
            for (int i = 0; i < hardwareData.Storage.Count; i++)
            {
                var drive = hardwareData.Storage[i];

                AddRow(rows, "Storage", i, "Model", drive.Model);
                AddRow(rows, "Storage", i, "Type", drive.Type);
                AddRow(rows, "Storage", i, "CapacityGB",
                    FormatNumber(drive.CapacityGB));
            }

            // Add operating system information.
            AddRow(rows, "OS", 0, "Name",
                hardwareData.Os.Name);

            AddRow(rows, "OS", 0, "Architecture",
                hardwareData.Os.Architecture);

            AddRow(rows, "OS", 0, "Version",
                hardwareData.Os.Version);

            // Create the destination directory if it does not exist.
            string fullPath = Path.GetFullPath(outputPath);
            string? directory = Path.GetDirectoryName(fullPath);

            if (!string.IsNullOrEmpty(directory))
            {
                Directory.CreateDirectory(directory);
            }

            // Write the complete CSV using UTF-8 without a byte order mark.
            // Existing files at the same path will be replaced.
            File.WriteAllLines(
                fullPath,
                rows,
                new UTF8Encoding(false));

            // Verify that the exported file exists.
            if (!File.Exists(fullPath))
            {
                throw new IOException(
                    "The CSV file was not created successfully.");
            }

            return fullPath;
        }

        // Adds one CSV row using the agreed four-column structure.
        private static void AddRow(
            List<string> rows,
            string category,
            int instance,
            string property,
            string? value)
        {
            rows.Add(string.Join(",",
                EscapeCsv(category),
                instance.ToString(CultureInfo.InvariantCulture),
                EscapeCsv(property),
                EscapeCsv(value)));
        }

        // Escapes commas, quotation marks and line breaks in CSV values.
        private static string EscapeCsv(string? value)
        {
            value ??= string.Empty;

            if (value.Contains('"'))
            {
                value = value.Replace("\"", "\"\"");
            }

            if (value.Contains(',') ||
                value.Contains('"') ||
                value.Contains('\n') ||
                value.Contains('\r'))
            {
                return $"\"{value}\"";
            }

            return value;
        }

        // Formats numeric values consistently for the Java CSV reader.
        // Missing values are written as empty fields.
        private static string FormatNumber<T>(T? value)
            where T : struct, IFormattable
        {
            return value.HasValue
                ? value.Value.ToString(
                    null,
                    CultureInfo.InvariantCulture)
                : string.Empty;
        }
    }
}

