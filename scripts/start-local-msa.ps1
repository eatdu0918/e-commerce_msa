#Requires -Version 5.1
<#
  로컬에서 MSA 백엔드 코어를 Docker로 기동합니다.
  - Gateway: http://localhost:8000  (클라이언트/Vite 프록시 기본 대상)
  - User Service 등: 8080~8086 (게이트웨이가 라우팅)

  사용 전: Docker Desktop 실행
  종료: docker compose stop gateway-service user-service ... (또는 전체 down)
#>
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$services = @(
  "gateway-service",
  "user-service",
  "product-service",
  "order-service",
  "discount-service",
  "payment-service",
  "cancel-service",
  "refund-service"
)

Write-Host "Docker Compose 기동 중... (의존 인프라·DB·Kafka 포함)" -ForegroundColor Cyan
# docker compose up -d <서비스들...>
$composeArgs = @('compose', 'up', '-d') + $services
docker @composeArgs

if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

Write-Host ""
Write-Host "기동 요청 완료. 상태 확인: docker compose ps" -ForegroundColor Green
Write-Host "  Gateway (API 진입점): http://localhost:8000" -ForegroundColor Green
Write-Host "  User Service 직접:    http://localhost:8080/actuator/health" -ForegroundColor Green
Write-Host ""
Write-Host "ngrok은 게이트웨이로만 붙이세요: .\scripts\ngrok-gateway.ps1 (또는 ngrok http 8000)" -ForegroundColor Yellow
