param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $root 'backend-api'
$frontendPath = Join-Path $root 'frontend-web'
$composePath = Join-Path $root 'devops-infra\docker-compose.yml'
$localMavenRoot = Join-Path $env:USERPROFILE '.oceanflow-local-maven'
$mavenVersion = '3.9.9'
$mavenZipPath = Join-Path $localMavenRoot "apache-maven-$mavenVersion-bin.zip"
$mavenHome = Join-Path $localMavenRoot "apache-maven-$mavenVersion"
$mavenBinPath = Join-Path $mavenHome 'bin'
$mavenCommand = Join-Path $mavenBinPath 'mvn.cmd'
$mavenDownloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$curlPath = (Get-Command curl.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue)

function Ensure-Maven {
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        return
    }

    if (Test-Path $mavenCommand) {
        if (-not ($env:Path -split ';' | Where-Object { $_ -eq $mavenBinPath })) {
            $env:Path += ";$mavenBinPath"
        }
        return
    }

    if (-not (Test-Path $localMavenRoot)) {
        New-Item -ItemType Directory -Path $localMavenRoot | Out-Null
    }

    $downloaded = $false
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            if (Test-Path $mavenZipPath) {
                Remove-Item $mavenZipPath -Force -ErrorAction SilentlyContinue
            }

            Write-Host "Maven chưa có trong PATH, đang tải xuống vào $localMavenRoot (lần $attempt/3)..."
            if ($curlPath) {
                & $curlPath -L --fail --retry 5 --retry-delay 2 -o $mavenZipPath $mavenDownloadUrl
            }
            else {
                Invoke-WebRequest -Uri $mavenDownloadUrl -OutFile $mavenZipPath -UseBasicParsing
            }
            Expand-Archive -Force $mavenZipPath $localMavenRoot
            if (Test-Path $mavenCommand) {
                $downloaded = $true
                break
            }
        }
        catch {
            if (Test-Path $mavenZipPath) {
                Remove-Item $mavenZipPath -Force -ErrorAction SilentlyContinue
            }

            if ($attempt -eq 3) {
                throw
            }

            Write-Host "Tải Maven thất bại, thử lại sau 2 giây..."
            Start-Sleep -Seconds 2
        }
    }

    if (-not $downloaded -or -not (Test-Path $mavenCommand)) {
        throw "Không thể cài đặt Maven tại $mavenHome."
    }

    if (-not ($env:Path -split ';' | Where-Object { $_ -eq $mavenBinPath })) {
        $env:Path += ";$mavenBinPath"
    }

    if (-not (Get-Command mvn -ErrorAction SilentlyContinue) -and -not (Test-Path $mavenCommand)) {
        throw "Không thể kích hoạt Maven từ $mavenBinPath."
    }
}

function Ensure-FrontendDependencies {
    if (-not (Test-Path (Join-Path $frontendPath 'node_modules'))) {
        Write-Host "Thiếu node_modules, đang chạy npm install..."
        Push-Location $frontendPath
        try {
            npm install
        }
        finally {
            Pop-Location
        }
    }
}

function Get-BackendCommand {
    if (Test-Path $mavenCommand) {
        return "Set-Location '$backendPath'; & '$mavenCommand' spring-boot:run"
    }

    if (Get-Command mvn -ErrorAction SilentlyContinwue) {
        return "Set-Location '$backendPath'; mvn spring-boot:run"
    }

    if (Get-Command docker -ErrorAction SilentlyContinue) {
        return "docker compose -f '$composePath' up --build"
    }

    throw "Không tìm thấy Maven hoặc Docker để chạy backend."
}

if ($DryRun) {
    Write-Host "[DryRun] Sẽ chạy backend:"
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        Write-Host "docker compose -f '$composePath' up --build"
    }
    else {
        Write-Host "Set-Location '$backendPath'; mvn spring-boot:run"
        Write-Host "(script sẽ tự tải Maven nếu chưa có trong PATH)"
    }
    Write-Host "[DryRun] Sẽ chạy frontend:"
    Write-Host "Set-Location '$frontendPath'; npm run dev"
    exit 0
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "Thiếu lệnh 'npm'. Hãy cài Node.js trước khi chạy script này."
    exit 1
}

Ensure-Maven
Ensure-FrontendDependencies

$backendCommand = Get-BackendCommand
$frontendCommand = "Set-Location '$frontendPath'; npm run dev"

Write-Host "Đang khởi động backend..."
Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $backendCommand

Write-Host "Đang khởi động frontend..."
Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $frontendCommand

Write-Host "Hoàn tất."
Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
Write-Host "Lưu ý: đảm bảo PostgreSQL đang chạy hoặc Docker compose đã được lên trước khi backend khởi động."
