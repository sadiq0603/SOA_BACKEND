# check-backend.ps1 - Health check and Eureka registration status

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   BiblioTech Microservices Status & Eureka Registry      " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$services = @(
    @{ Name = "Eureka Server";   Port = 8761 },
    @{ Name = "API Gateway";     Port = 8080 },
    @{ Name = "Auth Service";    Port = 8084 },
    @{ Name = "Book Service";    Port = 8081 },
    @{ Name = "Rental Service";  Port = 8082 },
    @{ Name = "Fine Service";    Port = 8083 }
)

foreach ($svc in $services) {
    try {
        $healthUrl = "http://localhost:$($svc.Port)/actuator/health"
        $resp = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 3 -ErrorAction Stop
        Write-Host "[UP]      $($svc.Name.PadRight(18)) (Port $($svc.Port)) -> Status: $($resp.status)" -ForegroundColor Green
    } catch {
        Write-Host "[DOWN]    $($svc.Name.PadRight(18)) (Port $($svc.Port)) -> Offline or Unreachable" -ForegroundColor Red
    }
}

Write-Host "`nChecking Eureka Registered Applications..." -ForegroundColor Cyan
try {
    $eurekaApps = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{ "Accept" = "application/json" } -TimeoutSec 4 -ErrorAction Stop
    $apps = $eurekaApps.applications.application
    if ($apps) {
        foreach ($app in $apps) {
            $instances = $app.instance
            $count = if ($instances -is [System.Array]) { $instances.Count } else { 1 }
            Write-Host "  -> Registered: $($app.name.PadRight(18)) ($count instance(s) UP)" -ForegroundColor Green
        }
    } else {
        Write-Host "  No applications registered in Eureka yet." -ForegroundColor Yellow
    }
} catch {
    Write-Host "  Could not query Eureka Server: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host "==========================================================" -ForegroundColor Cyan
