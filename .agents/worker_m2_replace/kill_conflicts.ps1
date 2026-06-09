Get-CimInstance Win32_Process | ForEach-Object {
    $cmd = $_.CommandLine
    $p_id = $_.ProcessId
    if ($cmd) {
        $shouldKill = $false
        if ($cmd.Contains("java.exe") -or $cmd.Contains("surefirebooter")) {
            $shouldKill = $true
        } elseif (($cmd.Contains("powershell") -or $cmd.Contains("cmd.exe")) -and 
                  ($cmd.Contains("mvn") -or $cmd.Contains("maven") -or $cmd.Contains("npm") -or $cmd.Contains("test-compile"))) {
            $shouldKill = $true
        }
        
        if ($shouldKill) {
            if ($cmd.Contains("kill_conflicts.ps1") -or $cmd.Contains("worker_m2_replace")) {
                return
            }
            Write-Host "Killing Process: $p_id - $cmd"
            Stop-Process -Id $p_id -Force -ErrorAction SilentlyContinue
        }
    }
}
