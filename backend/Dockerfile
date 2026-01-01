# syntax=docker/dockerfile:experimental
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# inject all environment vars we'll need
ARG API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI
ARG API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI
ARG BFF_OAUTH2_CLIENT_ID
ARG BFF_OAUTH2_CLIENT_SECRET
ARG BFF_FRONTEND_URL
ARG API_CORS_ALLOWED_ORIGINS
ARG COOKIE_DOMAIN
ARG RESULTER_DATABASE_HOST
ARG RESULTER_DATABASE_PORT
ARG POSTGRES_DB
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ARG RESULTER_LOG_LEVEL
ARG PROMETHEUS_API_TOKEN

# expose the variable to the finished container
ENV API_OAUTH2_RESOURCE_SERVER_JWT_ISSUER_URI=$API_OAUTH2_RESOURCE_SERVER_JWT_ISSER_URI
ENV API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI=$API_OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI
ENV BFF_OAUTH2_CLIENT_ID=$BFF_OAUTH2_CLIENT_ID
ENV BFF_OAUTH2_CLIENT_SECRET=$BFF_OAUTH2_CLIENT_SECRET
ENV BFF_FRONTEND_URL=$BFF_FRONTEND_URL
ENV API_CORS_ALLOWED_ORIGINS=$API_CORS_ALLOWED_ORIGINS
ENV COOKIE_DOMAIN=$COOKIE_DOMAIN
ENV RESULTER_DATABASE_HOST=$RESULTER_DATABASE_HOST
ENV RESULTER_DATABASE_PORT=$RESULTER_DATABASE_PORT
ENV POSTGRES_DB=$POSTGRES_DB
ENV POSTGRES_USER=$POSTGRES_USER
ENV POSTGRES_PASSWORD=$POSTGRES_PASSWORD
ENV RESULTER_LOG_LEVEL=$RESULTER_LOG_LEVEL
ENV PROMETHEUS_API_TOKEN=$PROMETHEUS_API_TOKEN
ENV SPRING_PROFILES_ACTIVE=prod,debug

LABEL org.opencontainers.image.title=resulter-backend
LABEL org.opencontainers.image.description="Backend for the resulter app"
LABEL org.opencontainers.image.source=https://github.com/HenryJobst/resulter
LABEL org.opencontainers.image.licenses='CC BY-NC-ND 4.0'

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
RUN mkdir -p media && mkdir -p media/thumbnails

FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Dspring.profiles.active=prod,debug","-cp","app:app/lib/*","de.jobst.resulter.ResulterApplication"]
