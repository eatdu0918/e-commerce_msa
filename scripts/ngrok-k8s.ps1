#Requires -Version 5.1
<#
  쿠버네티스(sparta-msa)에 떠 있는 client 서비스를 로컬 8080으로 포워딩한 뒤
  ngrok으로 외부 공개 URL을 발급받습니다.

  - svc/client(80) → localhost:8080  (kubectl port-forward, 백그라운드)
  - ngrok http 8080                  (포그라운드, Ctrl+C로 종료)

  client(nginx)가 / → SPA, /api → gateway-service:8000 으로 프록시하므로
  이 한 개의 터널만으로 SPA + 모든 API가 외부에 노출됩니다.

  종료 시 백그라운드 port-forward도 함께 정리합니다.

  사용법:
    .\scripts\ngrok-k8s.ps1                # client 서비스를 8080으로 노출
    .\scripts\ngrok-k8s.ps1 -Target gateway # gateway-service(8000)만 단독 노출
#>
param(
  [ValidateSet('client', 'gateway')]
  [string]$Target = 'client',
  [int]$LocalPort = 8080,
  [string]$Namespace = 'sparta-msa'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$config = Join-Path $root 'ngrok.yml'

switch ($Target) {
  'client'  { $svc = 'svc/client'; $remotePort = 80 }
  'gateway' { $svc = 'svc/gateway-service'; $remotePort = 8000 }
}

Write-Host "[1/2] kubectl port-forward $svc ${LocalPort}:${remotePort} (ns=$Namespace)" -ForegroundColor Cyan
$pf = Start-Process -FilePath 'kubectl' `
  -ArgumentList @('port-forward', '-n', $Namespace, $svc, "${LocalPort}:${remotePort}") `
  -PassThru -WindowStyle Hidden

# port-forward가 listen 시작할 때까지 잠깐 대기
$ready = $false
for ($i = 0; $i -lt 20; $i++) {
  Start-Sleep -Milliseconds 250
  try {
    $tcp = Test-NetConnection -ComputerName '127.0.0.1' -Port $LocalPort -WarningAction SilentlyContinue
    if ($tcp.TcpTestSucceeded) { $ready = $true; break }
  } catch {}
}
if (-not $ready) {
  Write-Host "경고: localhost:$LocalPort 가 아직 응답하지 않습니다. ngrok은 그대로 진행합니다." -ForegroundColor Yellow
} else {
  Write-Host "    → localhost:$LocalPort 준비 완료" -ForegroundColor Green
}

try {
  Write-Host "[2/2] ngrok http $LocalPort 시작 (Ctrl+C 로 종료)" -ForegroundColor Cyan
  if (Test-Path $config) {
    $tunnelName = if ($Target -eq 'client') { 'k8s-client' } else { 'gateway' }
    Write-Host "    설정 파일 사용: $config (tunnel: $tunnelName)" -ForegroundColor DarkCyan
    ngrok start $tunnelName --config $config
  } else {
    ngrok http $LocalPort
  }
}
finally {
  if ($pf -and -not $pf.HasExited) {
    Write-Host "포그라운드 종료 → kubectl port-forward(pid=$($pf.Id)) 정리" -ForegroundColor DarkYellow
    try { Stop-Process -Id $pf.Id -Force -ErrorAction SilentlyContinue } catch {}
  }
}
