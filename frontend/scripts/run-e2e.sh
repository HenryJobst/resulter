#!/bin/bash

# Helper script to run E2E tests with proper setup
# This script checks if servers are running and provides guidance

set -e

echo "🧪 Resulter E2E Test Runner"
echo "=========================="
echo ""

# Check if frontend is running
if ! curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo "❌ Frontend server is not running on http://localhost:5173"
    echo ""
    echo "Please start the frontend server in a separate terminal:"
    echo "  cd $(pwd)"
    echo "  pnpm dev"
    echo ""
    exit 1
fi

echo "✅ Frontend server is running on http://localhost:5173"

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ Backend server is not running on http://localhost:8080"
    echo ""
    echo "Please start the backend server in a separate terminal:"
    echo "  cd $(pwd)/../backend"
    echo "  ./mvnw spring-boot:run"
    echo ""
    exit 1
fi

echo "✅ Backend server is running on http://localhost:8080"
echo ""

# Check if .env.local exists
if [ ! -f "e2e/.env.local" ]; then
    echo "❌ e2e/.env.local not found"
    echo ""
    echo "Please create e2e/.env.local with:"
    echo "  HOSTNAME=localhost"
    echo "  FRONTEND_PROTOCOL=http"
    echo "  PORT=5173"
    echo "  BACKEND_PROTOCOL=http"
    echo "  BACKEND_PORT=8080"
    echo "  BACKEND_PROFILES=dev"
    echo "  VITE_MODE=development"
    echo "  USERNAME=<your-username>"
    echo "  PASSWORD=<your-password>"
    echo ""
    exit 1
fi

echo "✅ Environment configuration found"
echo ""

# Run E2E tests
echo "🚀 Running E2E tests..."
echo ""

pnpm test:e2e "$@"
