DOCKER_BUILDKIT=1 docker build \
    --build-arg API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI \
    --build-arg API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI \
    --build-arg API_CORS_ALLOWED_ORIGINS \
    --build-arg RESULTER_DATABASE_HOST \
    --build-arg RESULTER_DATABASE_PORT \
    --build-arg POSTGRES_DB \
    --build-arg POSTGRES_USER \
    --build-arg POSTGRES_PASSWORD \
    --build-arg RESULTER_LOG_LEVEL \
    -t resulter-api-java:latest .
