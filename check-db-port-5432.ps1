param(
    [string]$HostName = 'localhost',
    [int]$Port = 5432
)

Write-Host "Kiểm tra kết nối đến ${HostName}:${Port}..."

try {
    $result = Test-NetConnection -ComputerName $HostName -Port $Port -WarningAction SilentlyContinue
}
catch {
    Write-Error "Lỗi khi thực hiện kiểm tra mạng: $_"
    exit 1
}

if ($result.TcpTestSucceeded) {
    Write-Host "OK: cổng $Port đang mở và lắng nghe trên $Host."
    exit 0
}

Write-Error "LỖI: cổng $Port không mở hoặc không thể kết nối. Dừng khởi chạy backend."
exit 1
