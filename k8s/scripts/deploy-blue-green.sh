#!/usr/bin/env bash
# main 브랜치 CI에서 호출: 비활성 슬롯에 새 이미지 배포 후 gateway·client 트래픽 전환
# 필수 환경변수: GITHUB_REPOSITORY, GITHUB_SHA (또는 IMAGE_TAG)
# 선택: REGISTRY(기본 ghcr.io), K8S_NAMESPACE(기본 sparta-msa), ROLLOUT_TIMEOUT(기본 600s)
set -euo pipefail

NS="${K8S_NAMESPACE:-sparta-msa}"
REGISTRY="${REGISTRY:-ghcr.io}"
GITHUB_REPOSITORY_LOWER=$(echo "${GITHUB_REPOSITORY:?GITHUB_REPOSITORY 필수}" | tr '[:upper:]' '[:lower:]')
TAG="${IMAGE_TAG:-${GITHUB_SHA:?GITHUB_SHA 또는 IMAGE_TAG 필수}}"
TIMEOUT="${ROLLOUT_TIMEOUT:-600s}"

BACKENDS=(
  user-service
  product-service
  order-service
  discount-service
  payment-service
  cancel-service
  refund-service
)

image_uri() {
  local name=$1
  echo "${REGISTRY}/${GITHUB_REPOSITORY_LOWER}/${name}:${TAG}"
}

CURRENT=$(kubectl get svc gateway-service -n "$NS" -o jsonpath='{.spec.selector.slot}' 2>/dev/null || true)
if [ -z "$CURRENT" ]; then
  echo ">>> gateway-service selector.slot 없음 → blue 를 라이브로 간주"
  CURRENT=blue
fi
if [ "$CURRENT" = "blue" ]; then
  NEXT=green
else
  NEXT=blue
fi

echo ">>> Blue/Green: 라이브=${CURRENT} → 배포 대상(비활성)=${NEXT}, 태그=${TAG}"

for d in "${BACKENDS[@]}"; do
  echo ">>> 백엔드 롤링: ${d}"
  kubectl set image "deployment/${d}" "${d}=$(image_uri "${d}")" -n "$NS"
  kubectl patch "deployment/${d}" -n "$NS" --type=json \
    -p='[{"op":"replace","path":"/spec/template/spec/containers/0/imagePullPolicy","value":"Always"}]' || true
  kubectl rollout status "deployment/${d}" -n "$NS" --timeout="$TIMEOUT"
done

echo ">>> 게이트웨이·클라이언트 슬롯 ${NEXT}"
kubectl set image "deployment/gateway-service-${NEXT}" \
  "gateway-service=$(image_uri gateway-service)" -n "$NS"
kubectl set image "deployment/client-${NEXT}" \
  "client=$(image_uri client)" -n "$NS"
kubectl patch "deployment/gateway-service-${NEXT}" -n "$NS" --type=json \
  -p='[{"op":"replace","path":"/spec/template/spec/containers/0/imagePullPolicy","value":"Always"}]' || true
kubectl patch "deployment/client-${NEXT}" -n "$NS" --type=json \
  -p='[{"op":"replace","path":"/spec/template/spec/containers/0/imagePullPolicy","value":"Always"}]' || true

kubectl rollout status "deployment/gateway-service-${NEXT}" -n "$NS" --timeout="$TIMEOUT"
kubectl rollout status "deployment/client-${NEXT}" -n "$NS" --timeout="$TIMEOUT"

echo ">>> 트래픽 전환: gateway-service, client → slot=${NEXT}"
kubectl patch svc gateway-service -n "$NS" --type=json \
  -p="[{\"op\":\"replace\",\"path\":\"/spec/selector/slot\",\"value\":\"${NEXT}\"}]"
kubectl patch svc client -n "$NS" --type=json \
  -p="[{\"op\":\"replace\",\"path\":\"/spec/selector/slot\",\"value\":\"${NEXT}\"}]"

echo ">>> 모니터링 요약 (gateway / client)"
kubectl get deploy -n "$NS" | grep -E 'NAME|gateway-service|client-' || true
kubectl get pods -n "$NS" -o wide | grep -E 'gateway-service|client-' || true
echo ">>> 활성 슬롯: ${NEXT} | 미리보기: kubectl port-forward -n ${NS} svc/client-preview-${CURRENT} 8090:80"
