#!/bin/bash
# export environment variables of the .env.production file
set -o allexport
source .env.production
set +o allexport

# build
docker build --no-cache --build-arg="VITE_API_ENDPOINT=${VITE_API_ENDPOINT}" --build-arg="VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}" --build-arg="VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}" --build-arg="VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}" --build-arg="VITE_IMPRESS_TEXT_DE=${VITE_IMPRESS_TEXT_DE}" --build-arg="VITE_IMPRESS_TEXT_EN=${VITE_IMPRESS_TEXT_EN}" -t resulter-frontend:latest .
