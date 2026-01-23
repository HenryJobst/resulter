#!/bin/bash

# Paketo Buildpack build script for Resulter Frontend
# Alternative to Dockerfile-based build

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# No cleanup needed for this approach

# Image name and tag
IMAGE_NAME="${IMAGE_NAME:-resulter-frontend}"
IMAGE_TAG="${IMAGE_TAG:-paketo-latest}"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

# Vite build-time environment variables (compiled into JS bundle)
VITE_API_ENDPOINT="${VITE_API_ENDPOINT:-https://resulter.olberlin.de/api}"
VITE_IMPRESS_TEXT_DE="${VITE_IMPRESS_TEXT_DE:-Impressumstext}"
VITE_IMPRESS_TEXT_EN="${VITE_IMPRESS_TEXT_EN:-Impress text}"

echo "========================================"
echo "Building Resulter Frontend with Paketo"
echo "========================================"
echo "Image: ${FULL_IMAGE}"
echo "Builder: paketobuildpacks/builder-jammy-base"
echo ""
echo "Build-time configuration (compiled into JS):"
echo "  VITE_API_ENDPOINT: ${VITE_API_ENDPOINT}"
echo "  VITE_IMPRESS_TEXT_DE: ${VITE_IMPRESS_TEXT_DE}"
echo "  VITE_IMPRESS_TEXT_EN: ${VITE_IMPRESS_TEXT_EN}"
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

# Step 1: Build frontend locally with pnpm
echo "Step 1: Building frontend with pnpm..."
if ! command -v pnpm &> /dev/null; then
    echo "ERROR: pnpm not found! Install with: npm install -g pnpm"
    exit 1
fi

# Set environment variables for the build
export VITE_API_ENDPOINT="${VITE_API_ENDPOINT}"
export VITE_IMPRESS_TEXT_DE="${VITE_IMPRESS_TEXT_DE}"
export VITE_IMPRESS_TEXT_EN="${VITE_IMPRESS_TEXT_EN}"

# Build with pnpm
pnpm install
pnpm build-only

echo "Frontend build complete. dist/ directory created."
echo ""

# Step 2: Package with Paketo (nginx only)
echo "Step 2: Packaging with Paketo (nginx)..."
echo ""

pack build "${FULL_IMAGE}" \
    --builder paketobuildpacks/builder-jammy-base \
    --buildpack paketo-buildpacks/nginx \
    --env BP_WEB_SERVER=nginx \
    --env BP_WEB_SERVER_ROOT=dist \
    --env BP_NGINX_CONF_FILE=default.conf \
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
echo "To run locally:"
echo "  docker run -p 8080:8080 ${FULL_IMAGE}"
echo ""
echo "To override nginx port:"
echo "  docker run -p 3000:8080 -e PORT=8080 ${FULL_IMAGE}"
