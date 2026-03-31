# start-dev-tunnels.ps1 로 띄운 PowerShell(포트포워드/ngrok/dashboard) 창의 프로세스를 종료합니다.

param(
    [switch]$Quiet
)

$ErrorActionPreference = "SilentlyContinue"
$pidFile = Join-Path $env:TEMP "sparta-msa-dev-tunnel-pids.json"

if (-not (Test-Path $pidFile)) {
    if (-not $Quiet) { Write-Host "PID 파일이 없습니다. 이미 중지되었거나 start-dev-tunnels.ps1 을 실행하지 않았을 수 있습니다." }
    exit 0
}

try {
    $data = Get-Content -Raw -Path $pidFile | ConvertFrom-Json
    foreach ($id in @($data.pids)) {
        $proc = Get-Process -Id $id -ErrorAction SilentlyContinue
        if ($proc) {
            Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
            if (-not $Quiet) { Write-Host "종료: PID $id ($($proc.ProcessName))" }
        }
    }
} catch {
    if (-not $Quiet) { Write-Warning $_.Exception.Message }
}

Remove-Item -Path $pidFile -Force -ErrorAction SilentlyContinue
if (-not $Quiet) { Write-Host "완료." }
