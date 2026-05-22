#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  echo "Verwendung: $0 <major|minor|patch>"
  echo ""
  echo "  major  — erhöht die erste Stelle   (z.B. 4.8.6 → 5.0.0)"
  echo "  minor  — erhöht die zweite Stelle  (z.B. 4.8.6 → 4.9.0)"
  echo "  patch  — erhöht die dritte Stelle  (z.B. 4.8.6 → 4.8.7)"
  exit 1
}

[[ $# -ne 1 ]] && usage
BUMP="$1"
[[ "$BUMP" != "major" && "$BUMP" != "minor" && "$BUMP" != "patch" ]] && usage

if ! git -C "$SCRIPT_DIR" diff --quiet || ! git -C "$SCRIPT_DIR" diff --cached --quiet; then
  echo "Fehler: Es gibt uncommittete Änderungen. Bitte zuerst committen oder stashen." >&2
  exit 1
fi

# Aktuelle Version aus root pom.xml lesen
CURRENT=$(grep -m1 '<version>' "$SCRIPT_DIR/pom.xml" | sed 's/.*<version>\(.*\)<\/version>.*/\1/')

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"

case "$BUMP" in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
esac

NEW="${MAJOR}.${MINOR}.${PATCH}"

echo "Bump: $CURRENT → $NEW ($BUMP)"
echo ""

FILES=(
  "$SCRIPT_DIR/pom.xml"
  "$SCRIPT_DIR/backend/pom.xml"
  "$SCRIPT_DIR/frontend/package.json"
  "$SCRIPT_DIR/frontend/project.toml"
  "$SCRIPT_DIR/backend/project.toml"
  "$SCRIPT_DIR/backend/src/main/resources/application.properties"
  "$SCRIPT_DIR/README.md"
)

for FILE in "${FILES[@]}"; do
  if grep -qF "$CURRENT" "$FILE"; then
    sed -i '' "s/${CURRENT}/${NEW}/g" "$FILE"
    echo "  ✓ $FILE"
  else
    echo "  ✗ $FILE (Version $CURRENT nicht gefunden — übersprungen)"
  fi
done

git -C "$SCRIPT_DIR" add "${FILES[@]}"
git -C "$SCRIPT_DIR" commit -m "feat: bump version to ${NEW}"

echo ""
echo "Fertig. Neue Version: $NEW"
