#!/bin/bash

# Paketo Buildpack build script for Resulter Backend
# Alternative to Dockerfile-based build

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Image name and tag
IMAGE_NAME="${IMAGE_NAME:-resulter-api-java}"
IMAGE_TAG="${IMAGE_TAG:-paketo-latest}"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

echo "========================================"
echo "Building Resulter Backend with Paketo"
echo "========================================"
echo "Image: ${FULL_IMAGE}"
echo "Builder: paketobuildpacks/builder-jammy-base"
echo ""

# Check if pack CLI is installed
if ! command -v pack &> /dev/null; then
    echo "ERROR: pack CLI not found!"
    echo "Install from: https://buildpacks.io/docs/tools/pack/"
    echo ""
    echo "Quick install:"
    echo "  macOS:   brew install buildpacks/tap/pack"
    echo "  Linux:   curl -sSL https://github.com/buildpacks/pack/releases/download/v0.35.1/pack-v0.35.1-linux.tgz | tar -xz -C /usr/local/bin"
    exit 1
fi

echo "Pack CLI version:"
pack version
echo ""

# Pre-build JAR with Maven to avoid permission issues
echo "Step 1: Building JAR with Maven..."
cd ..
./mvnw clean install -Dmaven.test.skip=true -pl backend
cd backend
echo "JAR built successfully"
echo ""

# Build image using Paketo buildpacks
echo "Step 2: Building Docker image with Paketo buildpacks..."
echo "Note: Environment variables (OAuth2, database, etc.) are provided at RUNTIME, not build time."
echo ""

pack build "${FULL_IMAGE}" \
    --builder paketobuildpacks/builder-jammy-base \
    --buildpack paketo-buildpacks/java \
    --env BP_JVM_VERSION=21 \
    --path . \
    --tag "${FULL_IMAGE}" \
    --clear-cache \
    --trust-builder \
    --verbose

echo ""
echo "========================================"
echo "Build Complete!"
echo "========================================"
echo "Image: ${FULL_IMAGE}"
echo ""
echo "To run locally (example):"
echo "  docker run -p 8080:8080 \\"
echo "    -e SPRING_PROFILES_ACTIVE=prod \\"
echo "    -e POSTGRES_DB=resulter \\"
echo "    -e POSTGRES_USER=resulter \\"
echo "    -e POSTGRES_PASSWORD=secret \\"
echo "    -e RESULTER_DATABASE_HOST=localhost \\"
echo "    -e RESULTER_DATABASE_PORT=5432 \\"
echo "    -e API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=https://keycloak/realms/resulter \\"
echo "    ${FULL_IMAGE}"
echo ""
echo "For production, use docker-compose with .env file"
