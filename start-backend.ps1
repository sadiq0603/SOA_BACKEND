# start-backend.ps1 - Start all BiblioTech Microservices in correct order

$rootDir = $PSScriptRoot
if (-not $rootDir) { $rootDir = (Get-Location).Path }
$logDir = Join-Path $rootDir "runtime-logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Starting BiblioTech Microservices Architecture        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Service definitions: Name, Port, JarPath
$services = @(
    @{ Name = "eureka-server";  Port = 8761; Jar = "eureka-server\target\eureka-server-1.0.0.jar"; Delay = 10 },
    @{ Name = "auth-service";   Port = 8084; Jar = "auth-service\target\auth-service-1.0.0.jar";   Delay = 8 },
    @{ Name = "book-service";   Port = 8081; Jar = "book-service\target\book-service-1.0.0.jar";   Delay = 8 },
    @{ Name = "rental-service"; Port = 8082; Jar = "rental-service\target\rental-service-1.0.0.jar"; Delay = 8 },
    @{ Name = "fine-service";   Port = 8083; Jar = "fine-service\target\fine-service-1.0.0.jar";   Delay = 8 },
    @{ Name = "api-gateway";    Port = 8080; Jar = "api-gateway\target\api-gateway-1.0.0.jar";    Delay = 8 }
)

foreach ($svc in $services) {
    $jarFullPath = Join-Path $rootDir $svc.Jar
    if (-not (Test-Path $jarFullPath)) {
        Write-Host "ERROR: Jar file not found: $jarFullPath" -ForegroundColor Red
        Write-Host "Please run 'mvn clean package -DskipTests' first in each service folder." -ForegroundColor Yellow
        exit 1
    }

    # Check if already running on port
    $conn = Get-NetTCPConnection -LocalPort $svc.Port -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "Service '$($svc.Name)' is already running on port $($svc.Port)." -ForegroundColor Green
        continue
    }

    Write-Host "Starting $($svc.Name) on port $($svc.Port)..." -ForegroundColor Yellow
    $stdout = Join-Path $logDir "$($svc.Name).out.log"
    $stderr = Join-Path $logDir "$($svc.Name).err.log"

    $process = Start-Process -FilePath "java" `
        -ArgumentList "-jar", "`"$jarFullPath`"" `
        -WorkingDirectory (Split-Path $jarFullPath) `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru

    Write-Host "  Process started (PID: $($process.Id)). Waiting $($svc.Delay)s..." -ForegroundColor DarkGray
    Start-Sleep -Seconds $svc.Delay
}

Write-Host "`nWaiting 10 seconds for Eureka registrations to settle..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Health Check Status                                    " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$allHealthy = $true
foreach ($svc in $services) {
    try {
        $healthUrl = "http://localhost:$($svc.Port)/actuator/health"
        $resp = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 5 -ErrorAction Stop
        if ($resp.status -eq "UP") {
            Write-Host "[UP]      $($svc.Name.PadRight(18)) http://localhost:$($svc.Port)" -ForegroundColor Green
        } else {
            Write-Host "[$($resp.status)] $($svc.Name.PadRight(18)) http://localhost:$($svc.Port)" -ForegroundColor Yellow
            $allHealthy = $false
        }
    } catch {
        Write-Host "[DOWN]    $($svc.Name.PadRight(18)) http://localhost:$($svc.Port) - $($_.Exception.Message)" -ForegroundColor Red
        $allHealthy = $false
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
if ($allHealthy) {
    Write-Host " All 6 microservices are successfully running and healthy!" -ForegroundColor Green
    Write-Host " API Gateway:   http://localhost:8080" -ForegroundColor Cyan
    Write-Host " Eureka Server: http://localhost:8761" -ForegroundColor Cyan
} else {
    Write-Host " Some services may still be initializing. Re-run .\check-backend.ps1 in a few seconds." -ForegroundColor Yellow
}
