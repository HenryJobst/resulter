#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

VERSION=$(grep -m1 '<version>' "$SCRIPT_DIR/pom.xml" | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
BRANCH="releases/v${VERSION}"

if git show-ref --verify --quiet "refs/heads/${BRANCH}"; then
  echo "Branch '${BRANCH}' existiert bereits lokal." >&2
  exit 1
fi

if git show-ref --verify --quiet "refs/remotes/origin/${BRANCH}"; then
  echo "Branch '${BRANCH}' existiert bereits auf origin." >&2
  exit 1
fi

git branch "$BRANCH" main
git push origin "$BRANCH"
echo "Branch '${BRANCH}' von main erstellt und gepusht."
