using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EcoScan.HardwareScanner.Models
{
    // Stores information about the operating system.
    public class OsInfo
    {
        public string Name { get; set; } = "Unknown";

        public string Architecture { get; set; } = "Unknown";

        public string Version { get; set; } = "Unknown";
    }
}
