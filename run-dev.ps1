param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $root 'backend-api'
$frontendPath = Join-Path $root 'frontend-web'

function Test-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Thiếu lệnh '$Name'. Hãy cài đặt trước khi chạy script."
    }
}

$backendCommand = "Set-Location '$backendPath'; mvn spring-boot:run"
$frontendCommand = "Set-Location '$frontendPath'; npm run dev"

if ($DryRun) {
    Write-Host "[DryRun] Sẽ chạy backend:"
    Write-Host $backendCommand
    Write-Host "[DryRun] Sẽ chạy frontend:"
    Write-Host $frontendCommand
    exit 0
}

try {
    Test-Command 'mvn'
    Test-Command 'npm'
}
catch {
    Write-Error $_
    exit 1
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Thiếu lệnh 'mvn'. Hãy cài Maven trước khi chạy script này."
    exit 1
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "Thiếu lệnh 'npm'. Hãy cài Node.js trước khi chạy script này."
    exit 1
}

Write-Host "Đang khởi động backend..."
Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $backendCommand

Write-Host "Đang khởi động frontend..."
Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $frontendCommand

Write-Host "Hoàn tất."
Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
Write-Host "Lưu ý: đảm bảo PostgreSQL đang chạy hoặc Docker compose đã được lên trước khi backend khởi động."
