using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EcoScan.HardwareScanner.Models
{
    public class CpuInfo
    {
        public string Name { get; set; } = "Unknown";
        public string Manufacturer { get; set; } = "Unknown";
        public int? Cores { get; set; }
        public int? LogicalProcessors { get; set; }
        public int? MaxClockMHz { get; set; }
    }
}
