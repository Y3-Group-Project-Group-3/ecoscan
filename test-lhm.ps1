$ErrorActionPreference = "Stop"

$libPath = Join-Path (Get-Location).Path "lib"

$dll1 = Join-Path $libPath "HidSharp.dll"
$dll2 = Join-Path $libPath "LibreHardwareMonitorLib.dll"

$memoryDll = "C:\Users\chris\Desktop\libre\System.Memory.dll"

$unsafeDll = "C:\Users\chris\Desktop\libre\System.Runtime.CompilerServices.Unsafe.dll"

Write-Output "Loading: $unsafeDll"
[System.Reflection.Assembly]::LoadFile($unsafeDll) | Out-Null

Write-Output "Loading: $memoryDll"
[System.Reflection.Assembly]::LoadFile($memoryDll) | Out-Null

Write-Output "Loading: $dll1"
[System.Reflection.Assembly]::LoadFile($dll1) | Out-Null

Write-Output "Loading: $dll2"
[System.Reflection.Assembly]::LoadFile($dll2) | Out-Null

$pc = New-Object LibreHardwareMonitor.Hardware.Computer
$pc.IsCpuEnabled = $true

try {
    $pc.Open()

    foreach ($hw in $pc.Hardware) {
        $hw.Update()

        Write-Output "Hardware: $($hw.Name) [$($hw.HardwareType)]"

        foreach ($sensor in $hw.Sensors) {
            Write-Output ("{0} | {1} | {2}" -f `
                $sensor.Name, $sensor.SensorType, $sensor.Value)
        }
    }
}
finally {
    $pc.Close()
}
