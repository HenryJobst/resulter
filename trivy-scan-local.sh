#!/bin/bash
# Lokaler Trivy-Scan: spiegelt die CI-Schritte aus build-paketo.yml exakt.
# Voraussetzungen: pack, docker, trivy (brew install trivy)
#
# Optionen:
#   --backend-only   nur Backend scannen
#   --frontend-only  nur Frontend scannen
#   --skip-build     vorhandenes Image wiederverwenden (kein pack build)
#
# Dauer beim ersten Aufruf: ~5-15 Min (pack lädt Builder + Buildpacks herunter).
# Folgeaufrufe ohne --skip-build sind deutlich schneller (Caches warm).

set -euo pipefail

BACKEND_ONLY=false
FRONTEND_ONLY=false
SKIP_BUILD=false

for arg in "$@"; do
  case $arg in
    --backend-only)  BACKEND_ONLY=true ;;
    --frontend-only) FRONTEND_ONLY=true ;;
    --skip-build)    SKIP_BUILD=true ;;
  esac
done

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_IMAGE="temp-backend:local"
FRONTEND_IMAGE="temp-frontend:local"

check_deps() {
  for cmd in pack docker trivy; do
    if ! command -v "$cmd" &>/dev/null; then
      echo "FEHLER: '$cmd' nicht gefunden."
      [[ "$cmd" == "trivy" ]] && echo "  → brew install trivy"
      exit 1
    fi
  done
}

build_backend() {
  echo "==> Backend bauen mit Paketo..."
  local compose_bak=""
  if [[ -f "$REPO_ROOT/backend/compose.yml" ]]; then
    compose_bak=$(mktemp)
    cp "$REPO_ROOT/backend/compose.yml" "$compose_bak"
  fi
  rm -f "$REPO_ROOT/backend/compose.yml" "$REPO_ROOT/backend/.env" 2>/dev/null || true

  pack build "$BACKEND_IMAGE" \
    --builder paketobuildpacks/builder-jammy-base \
    --buildpack paketo-buildpacks/java \
    --env BP_JVM_VERSION=21 \
    --env BP_JVM_TYPE=JDK \
    --env BP_MAVEN_VERSION=3.9.x \
    --env BP_MAVEN_BUILD_ARGUMENTS="-Dmaven.test.skip=true clean install" \
    --env BP_MAVEN_SETTINGS_PATH=settings.xml \
    --path "$REPO_ROOT/backend"

  [[ -n "$compose_bak" ]] && mv "$compose_bak" "$REPO_ROOT/backend/compose.yml"
}

build_frontend() {
  echo "==> Frontend bauen mit Paketo..."
  (cd "$REPO_ROOT/frontend" && pnpm install --frozen-lockfile --trust-lockfile && pnpm build-only)
  pack build "$FRONTEND_IMAGE" \
    --builder paketobuildpacks/builder-jammy-base \
    --buildpack paketo-buildpacks/nginx \
    --env BP_WEB_SERVER=nginx \
    --env BP_WEB_SERVER_ROOT=dist \
    --env BP_NGINX_CONF_FILE=default.conf \
    --path "$REPO_ROOT/frontend"
}

scan_backend() {
  echo "==> Backend scannen..."
  trivy image \
    --severity HIGH,CRITICAL \
    --exit-code 1 \
    --ignore-unfixed \
    --ignorefile "$REPO_ROOT/backend/.trivyignore" \
    "$BACKEND_IMAGE"
}

scan_frontend() {
  echo "==> Frontend scannen..."
  trivy image \
    --severity HIGH,CRITICAL \
    --exit-code 1 \
    --ignore-unfixed \
    --ignorefile "$REPO_ROOT/frontend/.trivyignore" \
    "$FRONTEND_IMAGE"
}

check_deps

BACKEND_EXIT=0
FRONTEND_EXIT=0

if ! $FRONTEND_ONLY; then
  $SKIP_BUILD || build_backend
  scan_backend || BACKEND_EXIT=$?
fi

if ! $BACKEND_ONLY; then
  $SKIP_BUILD || build_frontend
  scan_frontend || FRONTEND_EXIT=$?
fi

if [[ $BACKEND_EXIT -ne 0 || $FRONTEND_EXIT -ne 0 ]]; then
  echo ""
  echo "SCAN FEHLGESCHLAGEN — neue CVEs gefunden."
  echo "Betroffene CVE-IDs in backend/.trivyignore bzw. frontend/.trivyignore eintragen,"
  echo "falls es sich um Buildpack-interne Go-Binaries handelt (kein eigener Code)."
  exit 1
fi

echo ""
echo "Scan erfolgreich — keine ungefixten HIGH/CRITICAL-Schwachstellen."
