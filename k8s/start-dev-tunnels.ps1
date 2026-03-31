# Minikube(sparta-msa) 기동 후 로컬에서 쓰기 위한 터널을 별도 PowerShell 창으로 띄웁니다.
# - client:  http://127.0.0.1:<ClientPort>  (기본 8080)
# - mysql:   jdbc/mysql 클라이언트용 127.0.0.1:<MysqlPort> (기본 3307)
# 사용:  .\k8s\start-dev-tunnels.ps1
#       .\k8s\start-dev-tunnels.ps1 -Ngrok -Dashboard
# 중지:  .\k8s\stop-dev-tunnels.ps1

param(
    [string]$Namespace = "sparta-msa",
    [int]$ClientPort = 8080,
    [int]$MysqlPort = 3307,
    [int]$GatewayPort = 0,
    [switch]$SkipClient,
    [switch]$SkipMysql,
    [switch]$Ngrok,
    [switch]$Dashboard,
    [switch]$EnsureMinikube,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$pidFile = Join-Path $env:TEMP "sparta-msa-dev-tunnel-pids.json"

function Test-PidAlive {
    param([int]$ProcessId)
    return [bool](Get-Process -Id $ProcessId -ErrorAction SilentlyContinue)
}

if (Test-Path $pidFile) {
    try {
        $old = Get-Content -Raw -Path $pidFile | ConvertFrom-Json
        $anyAlive = $false
        foreach ($id in @($old.pids)) {
            if (Test-PidAlive -ProcessId $id) { $anyAlive = $true; break }
        }
        if ($anyAlive -and -not $Force) {
            Write-Warning "이전에 띄운 터널 프로세스가 남아 있을 수 있습니다. 종료 후 다시 실행하려면: .\k8s\stop-dev-tunnels.ps1 또는 -Force"
            exit 1
        }
    } catch { }
}

if ($Force -and (Test-Path $pidFile)) {
    & "$PSScriptRoot\stop-dev-tunnels.ps1" -ErrorAction SilentlyContinue
}

if ($EnsureMinikube) {
    minikube status 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "=== minikube start ==="
        minikube start
    }
}

Write-Host "=== kubectl 연결 확인 ==="
kubectl cluster-info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Error "kubectl이 클러스터에 연결되지 않습니다. minikube start 후 다시 실행하세요."
    exit 1
}

kubectl get ns $Namespace *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Error "네임스페이스 '$Namespace' 가 없습니다. 먼저 배포하세요: .\k8s\deploy-minikube.ps1"
    exit 1
}

$pids = [System.Collections.Generic.List[int]]::new()

function Start-TunnelWindow {
    param(
        [string]$Title,
        [string]$Command
    )
    $psCmd = "Write-Host '[$Title] (창을 닫으면 터널이 종료됩니다)'; $Command"
    $p = Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $psCmd
    ) -PassThru
    if ($p) { [void]$pids.Add($p.Id) }
}

if (-not $SkipClient) {
    Write-Host "client port-forward: 127.0.0.1:$ClientPort -> svc/client:80"
    Start-TunnelWindow -Title "sparta-msa client" -Command "kubectl port-forward -n $Namespace svc/client ${ClientPort}:80"
}

if (-not $SkipMysql) {
    Write-Host "mysql port-forward: 127.0.0.1:$MysqlPort -> svc/mysql:3306"
    Start-TunnelWindow -Title "sparta-msa mysql" -Command "kubectl port-forward -n $Namespace svc/mysql ${MysqlPort}:3306"
}

if ($GatewayPort -gt 0) {
    Write-Host "gateway port-forward: 127.0.0.1:$GatewayPort -> svc/gateway-service:8000"
    Start-TunnelWindow -Title "sparta-msa gateway" -Command "kubectl port-forward -n $Namespace svc/gateway-service ${GatewayPort}:8000"
}

Start-Sleep -Seconds 2

if ($Ngrok) {
    $ng = Get-Command ngrok -ErrorAction SilentlyContinue
    if (-not $ng) {
        Write-Warning "ngrok이 PATH에 없습니다. -Ngrok 옵션은 건너뜁니다."
    } else {
        Write-Host "ngrok: https:// -> http://127.0.0.1:$ClientPort"
        Start-TunnelWindow -Title "ngrok client" -Command "ngrok http $ClientPort"
    }
}

if ($Dashboard) {
    Write-Host "Kubernetes Dashboard 창 실행"
    $p = Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command",
        "Write-Host '[minikube dashboard]'; minikube dashboard"
    ) -PassThru
    if ($p) { [void]$pids.Add($p.Id) }
}

$payload = @{
    pids       = @($pids)
    startedAt  = (Get-Date).ToString("o")
    clientPort = $ClientPort
    mysqlPort  = $MysqlPort
} | ConvertTo-Json
Set-Content -Path $pidFile -Value $payload -Encoding UTF8

Write-Host ""
Write-Host "=== 완료 ==="
if (-not $SkipClient) { Write-Host "  프론트: http://127.0.0.1:$ClientPort" }
if (-not $SkipMysql) {
    Write-Host "  MySQL:  jdbc:mysql://127.0.0.1:${MysqlPort}/user_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul"
}
if ($GatewayPort -gt 0) { Write-Host "  게이트웨이: http://127.0.0.1:$GatewayPort" }
Write-Host "  터널 종료: .\k8s\stop-dev-tunnels.ps1"
