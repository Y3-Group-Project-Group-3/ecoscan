
using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
using EcoScan.HardwareScanner.Models;
using EcoScan.HardwareScanner.Services;
using Scanner = EcoScan.HardwareScanner.Services.HardwareScanner;

namespace EcoScan.HardwareScanner
{
    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();
        }

        private void btnScan_Click(object sender, EventArgs e)
        {
            try
            {
                lblStatus.Text = "Scanning hardware...";

                var scanner = new Scanner();

                // Collect hardware information using WMI.
                // Each method returns data for a specific hardware category.
                var hardwareData = new HardwareData
                {
                    System = scanner.GetSystemInfo(),
                    Cpu = scanner.GetCpuInfo(),
                    Memory = scanner.GetMemoryInfo(),
                    Gpus = scanner.GetGpuInfo(),
                    Storage = scanner.GetStorageInfo(),
                    Os = scanner.GetOsInfo()
                };

                // Display the collected information for testing.
                // This MessageBox will be removed when CSV export is verified.
                string scanResult =
                    $"SYSTEM\n" +
                    $"Manufacturer: {hardwareData.System.Manufacturer}\n" +
                    $"Model: {hardwareData.System.Model}\n" +
                    $"System Type: {hardwareData.System.SystemType}\n\n" +

                    $"CPU\n" +
                    $"Name: {hardwareData.Cpu.Name}\n" +
                    $"Manufacturer: {hardwareData.Cpu.Manufacturer}\n" +
                    $"Cores: {hardwareData.Cpu.Cores}\n" +
                    $"Logical Processors: {hardwareData.Cpu.LogicalProcessors}\n" +
                    $"Max Clock Speed: {hardwareData.Cpu.MaxClockMHz} MHz\n\n" +

                    $"MEMORY\n" +
                 //   $"Total RAM: {hardwareData.Memory.TotalGB} GB\n\n" +
                    $"Total RAM: {hardwareData.Memory.TotalGB} GiB\n\n" +

                    $"GPU\n";

                // Add each detected graphics adapter to the test output.
                if (hardwareData.Gpus.Count == 0)
                {
                    scanResult += "No graphics adapters detected\n";
                }
                else
                {
                    for (int i = 0; i < hardwareData.Gpus.Count; i++)
                    {
                        scanResult +=
                            $"GPU {i}: {hardwareData.Gpus[i].Name}\n";
                    }
                }

                scanResult += "\nSTORAGE\n";

                // Display every physical storage device found during the scan.
                if (hardwareData.Storage.Count == 0)
                {
                    scanResult += "No storage devices detected\n";
                }
                else
                {
                    for (int i = 0; i < hardwareData.Storage.Count; i++)
                    {
                        var drive = hardwareData.Storage[i];

                        scanResult +=
                            $"Drive {i}\n" +
                            $"Model: {drive.Model}\n" +
                            $"Type: {drive.Type}\n" +
                            $"Capacity: {drive.CapacityGB} GB\n";
                    }
                }

                // Display the operating system information.
                scanResult +=
                    $"\nOS\n" +
                    $"Name: {hardwareData.Os.Name}\n" +
                    $"Architecture: {hardwareData.Os.Architecture}\n" +
                    $"Version: {hardwareData.Os.Version}\n";

                MessageBox.Show(
                    scanResult,
                    "Hardware Scan Results",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Information
                );

                // Save the CSV in the current user's LocalAppData folder.
                // This location works without a fixed username or install path.
                string outputPath = Path.Combine(
                    Environment.GetFolderPath(
                        Environment.SpecialFolder.LocalApplicationData),
                    "EcoScan",
                    "system_data.csv"
                );

                // Export the hardware data to CSV.
                // The exporter creates the destination folder if needed.
                var exporter = new CsvExporter();
                string csvPath = exporter.Export(hardwareData, outputPath);

                lblStatus.Text = "Hardware scan completed";

                // Show the exported file location with an option to open its folder.
                ShowExportResult(csvPath);
            }
            catch (Exception ex)
            {
                // Display an error if scanning or CSV export fails.
                lblStatus.Text = "Scan or export failed";

                MessageBox.Show(
                    ex.Message,
                    "Scan Error",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }


        // Shows the CSV export result and allows the user to open its folder.
        // This dialog is used temporarily while testing the scanner.
        private void ShowExportResult(string csvPath)
        {
            using var dialog = new Form
            {
                Text = "CSV Export Completed",
                StartPosition = FormStartPosition.CenterParent,
                FormBorderStyle = FormBorderStyle.FixedDialog,
                MaximizeBox = false,
                MinimizeBox = false,
                ShowInTaskbar = false,
                ClientSize = new Size(460, 175)
            };

            // Display the exported file location.
            var messageLabel = new Label
            {
                Text = "Hardware scan completed successfully.\n\n" +
                       $"CSV file saved to:\n{csvPath}",
                Location = new Point(20, 20),
                Size = new Size(420, 95),
                AutoEllipsis = true
            };

            // Open File Explorer and select the exported CSV file.
            var btnOpenFolder = new Button
            {
                Text = "Open Folder",
                Location = new Point(225, 130),
                Size = new Size(105, 30)
            };

            btnOpenFolder.Click += (sender, e) =>
            {
                try
                {
                    Process.Start(new ProcessStartInfo
                    {
                        FileName = "explorer.exe",
                        Arguments = $"/select,\"{csvPath}\"",
                        UseShellExecute = true
                    });
                }
                catch (Exception ex)
                {
                    MessageBox.Show(
                        ex.Message,
                        "Unable to Open Folder",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                }
            };

            // Close the dialog without opening File Explorer.
            var btnClose = new Button
            {
                Text = "Close",
                Location = new Point(340, 130),
                Size = new Size(100, 30),
                DialogResult = DialogResult.OK
            };

            dialog.Controls.Add(messageLabel);
            dialog.Controls.Add(btnOpenFolder);
            dialog.Controls.Add(btnClose);

            dialog.AcceptButton = btnClose;
            dialog.CancelButton = btnClose;

            dialog.ShowDialog(this);
        }


        private void btnExit_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }
    }
}
