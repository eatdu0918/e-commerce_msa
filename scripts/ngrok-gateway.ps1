#Requires -Version 5.1
<#
  공개 URL -> 로컬 게이트웨이(8000) 포워딩.
  모든 /api/** 요청은 이 주소 하나로 통과합니다 (MSA 라우팅).

  전제: scripts/start-local-msa.ps1 로 백엔드가 떠 있고, 8000이 응답해야 합니다.
  (8080만 ngrok에 쓰면 user-service만 열리고 나머지 API는 실패합니다.)

  외부에서 SPA를 쓸 때: client에 .env.development.local 로 VITE_API_URL=<ngrok HTTPS URL> 설정
#>
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$config = Join-Path $root "ngrok.yml"

try {
  $r = Invoke-WebRequest -Uri "http://127.0.0.1:8000/actuator/health" -UseBasicParsing -TimeoutSec 2
  if ($r.StatusCode -ne 200) { throw "unexpected status" }
} catch {
  Write-Host "경고: localhost:8000 에 연결되지 않습니다. 먼저 .\scripts\start-local-msa.ps1 로 백엔드를 기동하세요." -ForegroundColor Yellow
  Write-Host ""
}

if (Test-Path $config) {
  Write-Host "ngrok 설정 사용: $config (tunnel: gateway)" -ForegroundColor Cyan
  ngrok start gateway --config $config
} else {
  Write-Host "ngrok.yml 이 없어 기본 모드: ngrok http 8000" -ForegroundColor Cyan
  ngrok http 8000
}
