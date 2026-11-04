using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
namespace EcoScan.HardwareScanner.Models
{
    // Stores basic information about the computer.
    public class SystemInfo
    {
        public string Manufacturer { get; set; } = "Unknown";
        public string Model { get; set; } = "Unknown";
        public string SystemType { get; set; } = "Unknown";
    }
}
