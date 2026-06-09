param(
    [string]$TestCommand = "test -pl ibpms-core"
)

# Kill any existing conflicts before starting using relative script path
& "$PSScriptRoot\kill_conflicts.ps1"

$monitor = Start-Job -ScriptBlock {
    $myParentId = $args[0]
    
    function Get-Descendants($targetId) {
        $ids = @($targetId)
        $queue = New-Object System.Collections.Queue
        $queue.Enqueue($targetId)
        while ($queue.Count -gt 0) {
            $curr = $queue.Dequeue()
            Get-CimInstance Win32_Process -Filter "ParentProcessId = $curr" | ForEach-Object {
                $childId = $_.ProcessId
                if ($ids -notcontains $childId) {
                    $ids += $childId
                    $queue.Enqueue($childId)
                }
            }
        }
        return $ids
    }
    
    $langServerPids = Get-CimInstance Win32_Process -Filter "Name = 'language_server.exe'" | Select-Object -ExpandProperty ProcessId

    while ($true) {
        # Recalculate our descendants dynamically to allow new JVM forks we spawn
        $allowedIds = Get-Descendants $myParentId
        $allowedIds += Get-Descendants $PID
        
        # Get language server descendants
        $langDescendants = @()
        if ($langServerPids) {
            $langDescendants = Get-Descendants $langServerPids
        }

        Get-CimInstance Win32_Process | ForEach-Object {
            $cmd = $_.CommandLine
            $p_id = $_.ProcessId
            $name = $_.Name
            
            if ($cmd -and ($p_id -notin $allowedIds)) {
                $shouldKill = $false
                
                # Check if it's a descendant of language_server.exe
                $isLangDescendant = $p_id -in $langDescendants
                
                if ($isLangDescendant) {
                    if ($name -match "java|powershell|cmd|node") {
                        if ($name -ne "language_server.exe") {
                            $shouldKill = $true
                        }
                    }
                }
                
                # Also kill any compile/clean commands running anywhere in the system
                if (-not $shouldKill) {
                    if ($cmd.Contains("clean compile") -or 
                        $cmd.Contains("clean package") -or 
                        $cmd.Contains("clean test") -or 
                        $cmd.Contains("spring-boot:run") -or 
                        $cmd.Contains("mvn.cmd compile -DskipTests")) {
                        $shouldKill = $true
                    }
                }
                
                if ($shouldKill) {
                    Write-Output "Killing process: $p_id - $name - $cmd"
                    Stop-Process -Id $p_id -Force -ErrorAction SilentlyContinue
                }
            }
        }
        Start-Sleep -Milliseconds 500
    }
} -ArgumentList $PID

# Run the maven test command
Write-Host "Running Maven command: ..\maven\apache-maven-3.9.6\bin\mvn.cmd $TestCommand"
cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd $TestCommand"

Stop-Job $monitor
$killed = Receive-Job $monitor
if ($killed) {
    Write-Host "Processes killed during test run:"
    $killed | ForEach-Object { Write-Host $_ }
}
Remove-Job $monitor
