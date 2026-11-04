using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EcoScan.HardwareScanner.Models
{
    // Holds the hardware information collected during a scan.
    // Additional components will be added as the scanner develops.
    public class HardwareData
    {
        // Basic computer information, such as manufacturer and model.
        public SystemInfo System { get; set; } = new SystemInfo();

        // Processor specifications collected through WMI.
        public CpuInfo Cpu { get; set; } = new CpuInfo();

        // Information about the computer's physical memory.
        public MemoryInfo Memory { get; set; } = new MemoryInfo();

        // Stores all graphics adapters detected during the scan.
        public List<GpuInfo> Gpus { get; set; } = new List<GpuInfo>();

        // Stores all physical storage devices detected during the scan.
        public List<StorageInfo> Storage { get; set; } = new List<StorageInfo>();

        // Stores the operating system information.
        public OsInfo Os { get; set; } = new OsInfo();
    }
}

