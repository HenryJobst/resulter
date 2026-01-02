# build stage
FROM node:lts-alpine AS build-stage

WORKDIR /resulter

# Install bash for environment variable parsing
RUN apk add --no-cache bash

LABEL org.opencontainers.image.title=resulter-frontend
LABEL org.opencontainers.image.description="Frontend for the resulter app"
LABEL org.opencontainers.image.source=https://github.com/HenryJobst/resulter
LABEL org.opencontainers.image.licenses='CC BY-NC-ND 4.0'

COPY package.json ./

RUN npm install -g corepack@latest && corepack enable pnpm && pnpm --version

RUN pnpm install --no-frozen-lockfile

COPY . .

# Build with secrets mounted as files (not persisted in image layers)
RUN --mount=type=secret,id=dotenv \
    if [ -f /run/secrets/dotenv ]; then \
      while IFS='=' read -r key value || [ -n "$key" ]; do \
        if [ -n "$key" ] && [ "${key#\#}" = "$key" ]; then \
          export "$key=$value"; \
        fi; \
      done < /run/secrets/dotenv && \
      pnpm run build; \
    else \
      echo "WARNING: No secrets file found, building without environment variables"; \
      pnpm run build; \
    fi

# production stage
FROM nginx AS production-stage

COPY --from=build-stage /resulter/dist /usr/share/nginx/html

# Erstellen Sie Ihre eigene Nginx-Konfigurationsdatei `default.conf`
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
