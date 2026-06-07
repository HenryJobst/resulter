#!/bin/bash
# Pre-push Hook: Trivy-Scan nur für releases/**-Branches.
# Wird von simple-git-hooks aufgerufen; stdin enthält die zu pushenden Refs.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
NEEDS_SCAN=false

while IFS=' ' read -r _local_ref _local_sha remote_ref _remote_sha; do
  if [[ "$remote_ref" == refs/heads/releases/* ]]; then
    NEEDS_SCAN=true
    break
  fi
done

$NEEDS_SCAN || exit 0

echo "[trivy] Release-Branch erkannt — Sicherheitsscan wird gestartet..."

BACKEND_IMAGE="temp-backend:local"
FRONTEND_IMAGE="temp-frontend:local"

BACKEND_EXISTS=false
FRONTEND_EXISTS=false
docker image inspect "$BACKEND_IMAGE" &>/dev/null && BACKEND_EXISTS=true
docker image inspect "$FRONTEND_IMAGE" &>/dev/null && FRONTEND_EXISTS=true

if $BACKEND_EXISTS && $FRONTEND_EXISTS; then
  echo "[trivy] Lokale Images gefunden — überspringe Build (~1 Min)."
  "$REPO_ROOT/trivy-scan-local.sh" --skip-build
else
  echo "[trivy] Kein lokales Image vorhanden — vollständiger Build nötig (~10 Min)."
  echo "[trivy] Abbrechen mit Ctrl-C und danach manuell: ./trivy-scan-local.sh"
  "$REPO_ROOT/trivy-scan-local.sh"
fi
