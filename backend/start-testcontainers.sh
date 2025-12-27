#!/bin/bash

################################################################################
# Start Backend with Testcontainers Profile
#
# This script starts the Spring Boot backend with the 'testcontainers' profile,
# which is required for E2E tests with database isolation.
#
# Prerequisites:
# 1. Create a .env file in the parent directory (use .env.example as template)
# 2. Set all required environment variables (see REQUIRED_VARS below)
# 3. Ensure Docker is running (required for testcontainers)
#
# Usage:
#   ./start-testcontainers.sh
#
# The script will:
# - Load environment variables from ../.env
# - Validate that all required sensitive variables are set
# - Start the Spring Boot application with testcontainers profile
################################################################################

# Load environment variables from .env file
if [ -f ../.env ]; then
    export $(cat ../.env | grep -v '^#' | xargs)
else
    echo "ERROR: ../.env file not found!"
    echo "Please create a .env file in the parent directory using .env.example as template"
    exit 1
fi

# Set testcontainers specific variables
export SPRING_PROFILES_ACTIVE=testcontainers

# Non-sensitive defaults for development/testing
export CREATEDATABASE_API_TOKEN=${CREATEDATABASE_API_TOKEN:-test-database-token}
export RESULTER_SETTINGS_API_TOKEN_CLIENT_AUDIENCE=${RESULTER_SETTINGS_API_TOKEN_CLIENT_AUDIENCE:-resulter-api}
export PROMETHEUS_API_TOKEN=${PROMETHEUS_API_TOKEN:-test-prometheus-token}
export OPENAPI_CONFIG_CONTACT_NAME=${OPENAPI_CONFIG_CONTACT_NAME:-"Test User"}
export OPENAPI_CONFIG_CONTACT_EMAIL=${OPENAPI_CONFIG_CONTACT_EMAIL:-test@example.com}

# Check for required environment variables from .env
REQUIRED_VARS=(
    "API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI"
    "API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI"
    "BFF_OAUTH2_CLIENT_ID"
    "BFF_OAUTH2_CLIENT_SECRET"
    "KEYCLOAK_AUTHORIZATION_URI"
    "KEYCLOAK_TOKEN_URI"
    "KEYCLOAK_USER_INFO_URI"
    "KEYCLOAK_JWK_SET_URI"
    "RESULTER_MEDIA_FILE_PATH"
    "RESULTER_MEDIA_FILE_PATH_THUMBNAILS"
)

MISSING_VARS=()
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo "ERROR: The following required environment variables are not set:"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Please ensure these variables are set in ../.env file"
    exit 1
fi

./mvnw spring-boot:run
