# Sparta MSA Minikube 도입 가이드

본 가이드는 현재 Docker Compose 기반의 프로젝트를 로컬 Kubernetes(Minikube) 환경에서 구동하는 방법을 안내합니다.

## 1. 사전 준비 (Prerequisites)

- **Minikube 설치**: [Minikube 설치 가이드](https://minikube.sigs.k8s.io/docs/start/)
- **Kubectl 설치**: Kubernetes 제어 도구
- **시스템 요구 사항**: 최소 16GB RAM (모든 서비스 구동 시)

## 2. Minikube 시작 및 환경 설정

Minikube를 실행하고 필요한 자원을 할당합니다.

```powershell
# Minikube 시작 (리소스 넉넉히 할당)
minikube start --memory=16384 --cpus=4

# 전용 네임스페이스 생성
kubectl create namespace sparta-msa
```

## 3. 로컬 이미지 빌드

Minikube 내부의 Docker 데몬을 사용하여 이미지를 로컬에서 빌드합니다. (별도의 Registry 없이 사용 가능)

```powershell
# Minikube의 docker env 사용 설정 (해당 터미널 세션에만 적용)
minikube docker-env | Invoke-Expression

# 각 서비스 빌드 (예: Gateway Service)
cd gateway-service
docker build -t gateway-service:latest .
cd ../user-service
docker build -t user-service:latest .
```

## 4. 매니페스트 적용 (Deployment)

작성된 Kubernetes 설정 파일을 순서대로 적용합니다.

```powershell
# 1. 공통 설정 적용
kubectl apply -f k8s/common/

# 2. 인프라(DB, Redis) 적용
kubectl apply -f k8s/infra/mysql/
kubectl apply -f k8s/infra/

# 3. 마이크로서비스 배포
kubectl apply -f k8s/services/
```

## 5. 서비스 확인 및 접속

```powershell
# 배포 상태 확인
kubectl get all -n sparta-msa

# Gateway Service 접속 (외부 터널링)
minikube service gateway-service -n sparta-msa
```

> [!TIP]
> **Dashboard 확인**: `minikube dashboard` 명령어를 통해 GUI 환경에서 클러스터 상태를 모니터링할 수 있습니다.
