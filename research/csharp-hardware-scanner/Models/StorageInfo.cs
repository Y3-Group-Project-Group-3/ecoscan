using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EcoScan.HardwareScanner.Models
{
    // Stores information about a physical storage device.
    // Each detected drive will have its own StorageInfo object.
    public class StorageInfo
    {
        public string Model { get; set; } = "Unknown";

        public string Type { get; set; } = "Unknown";

        public double? CapacityGB { get; set; }
    }
}
