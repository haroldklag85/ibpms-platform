Write-Host "Monitoring backend..."
while($true) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient('127.0.0.1', 8080)
        if ($tcp.Connected) {
            $tcp.Close()
            break
        }
    } catch {}
    Start-Sleep -Seconds 10
    Write-Host "Still waiting for Spring Boot compilation..."
}
Write-Host "Backend is up! Launching E2E Suite..."
cmd.exe /c "npx playwright test e2e/certification/ --project=authenticated"
