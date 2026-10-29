# stop-backend.ps1 - Gracefully stop all BiblioTech Microservices

$ports = @(8080, 8081, 8082, 8083, 8084, 8761)

Write-Host "Stopping BiblioTech microservices on ports: $($ports -join ', ')..." -ForegroundColor Yellow

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($pidToKill in $pids) {
            try {
                $proc = Get-Process -Id $pidToKill -ErrorAction Stop
                Write-Host "Stopping process $($proc.ProcessName) (PID: $pidToKill) on port $port..." -ForegroundColor Cyan
                Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
            } catch {
                # Already stopped
            }
        }
    } else {
        Write-Host "Port $port: No process active." -ForegroundColor DarkGray
    }
}

Write-Host "All BiblioTech services stopped." -ForegroundColor Green
