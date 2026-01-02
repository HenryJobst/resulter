#!/bin/bash

# Secure Docker build using BuildKit secrets
# This script prevents secrets from being baked into Docker image layers
#
# Prerequisites:
# 1. Docker BuildKit must be enabled (DOCKER_BUILDKIT=1)
# 2. A .env.production file must exist with all required environment variables
#
# The .env.production file is mounted as a secret during build but not persisted in the image

set -e

# Check if .env.production file exists
if [ ! -f .env.production ]; then
    echo "ERROR: .env.production file not found!"
    echo "Please create a .env.production file with required VITE_* variables"
    exit 1
fi

# Enable Docker BuildKit
export DOCKER_BUILDKIT=1

echo "Building resulter-frontend with secure BuildKit secrets..."
echo "Secrets will NOT be persisted in image layers."

# Build with secret mounted from .env.production file
docker build \
    --secret id=dotenv,src=.env.production \
    -t resulter-frontend:latest \
    .

echo "Build complete! Image tagged as resulter-frontend:latest"
