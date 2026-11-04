using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EcoScan.HardwareScanner.Models
{
    // Stores information about a graphics adapter.
    // Each detected GPU will have its own GpuInfo object.
    public class GpuInfo
    {
        public string Name { get; set; } = "Unknown";
    }
}
