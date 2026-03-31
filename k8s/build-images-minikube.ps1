# Minikube가 사용하는 Docker 데몬으로 빌드합니다 (이미지가 노드에 올라감).
# 1) 터미널에서 먼저 연결:
#      minikube -p minikube docker-env --shell powershell | Invoke-Expression
# 2) 저장소 루트에서 실행:
#      .\k8s\build-images-minikube.ps1
# Windows에서는 `minikube image build`의 컨텍스트 마운트가 실패할 수 있어 docker-env 방식을 사용합니다.
#
# 빠른 재빌드(clean 생략):  .\k8s\build-images-minikube.ps1 -NoClean

param(
    [switch]$NoClean
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if ($NoClean) {
    & .\gradlew.bat build -x test
} else {
    & .\gradlew.bat clean build -x test
}

$services = @(
    "gateway-service",
    "user-service",
    "product-service",
    "order-service",
    "discount-service",
    "payment-service",
    "cancel-service",
    "refund-service",
    "client"
)

foreach ($name in $services) {
    Write-Host "docker build -t ${name}:latest ./$name"
    & docker build -t "${name}:latest" "./$name"
}

Write-Host "Done. 다음:"
Write-Host "  (권장) Minikube: minikube start --cpus 8 --memory 24576  |  브로커는 apache/kafka KRaft(JVM 힙 512Mi)"
Write-Host "  배포(권장, DB 선기동): .\k8s\deploy-minikube.ps1"
Write-Host "  또는: kubectl apply -k k8s/"
Write-Host "  브라우저: http://`$(minikube ip):30080 (NodePort) 또는 Ingress: sparta-msa.local"
Write-Host "  kubectl rollout restart deployment -n sparta-msa gateway-service user-service ..."
Write-Host "  (전체 재시작):"
Write-Host "  kubectl rollout restart deployment -n sparta-msa gateway-service user-service product-service order-service discount-service payment-service cancel-service refund-service client"
