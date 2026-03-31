# Minikube에 sparta-msa 배포 (MySQL·Kafka·Redis 준비 후 앱을 순차 기동해 메모리 스파이크 완화)
# 사전: minikube addons enable ingress
# 브라우저: http://$(minikube ip):30080  |  Ingress: hosts에 minikube IP + sparta-msa.local

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "=== 1/3 kustomize 적용 ==="
kubectl apply -k k8s/

Write-Host "=== 1b 레거시 per-service MySQL 제거(매니페스트에서 제외된 리소스 정리) ==="
$legacyMysql = @(
    "user-mysql", "product-mysql", "order-mysql", "discount-mysql",
    "payment-mysql", "cancel-mysql", "refund-mysql"
)
foreach ($n in $legacyMysql) {
    kubectl delete deployment $n -n sparta-msa --ignore-not-found=true
    kubectl delete service $n -n sparta-msa --ignore-not-found=true
}

Write-Host "=== 1c 앱 Deployment 레플리카 0 (kustomize apply 후 Java/Kafka/MySQL 동시 기동 방지) ==="
$scaleZero = @(
    "user-service", "product-service", "discount-service", "payment-service",
    "order-service", "cancel-service", "refund-service", "gateway-service"
)
foreach ($d in $scaleZero) {
    kubectl scale "deployment/$d" -n sparta-msa --replicas=0
}

Write-Host "=== 2/3 인프라 순차 재시작 + Ready ==="
$infra = @("mysql", "redis", "kafka")
foreach ($d in $infra) {
    Write-Host "-- infra: $d --"
    kubectl rollout restart "deployment/$d" -n sparta-msa
    kubectl rollout status "deployment/$d" -n sparta-msa --timeout=2400s
    Start-Sleep -Seconds 25
}

Write-Host "=== 3/3 앱 순차 재시작 (한 서비스씩 Ready까지 대기) ==="
$apps = @(
    "user-service",
    "product-service",
    "discount-service",
    "payment-service",
    "order-service",
    "cancel-service",
    "refund-service",
    "gateway-service",
    "client"
)
foreach ($d in $apps) {
    Write-Host "-- scale 1 + Ready: $d --"
    kubectl scale "deployment/$d" -n sparta-msa --replicas=1
    kubectl rollout status "deployment/$d" -n sparta-msa --timeout=2400s
    # 메모리 피크 분산 (16Gi 노드에서 Kafka·MySQL·Java 동시 상승 시 OOM 완화)
    Start-Sleep -Seconds 45
}

kubectl get pods -n sparta-msa

$ip = minikube ip
Write-Host ""
Write-Host "접속: http://${ip}:30080"
Write-Host "Ingress: http://sparta-msa.local"
