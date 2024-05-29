# build stage
FROM node:lts-alpine AS build-stage

WORKDIR /resulter

# inject all environment vars we'll need
ARG VITE_API_ENDPOINT
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_CLIENT_ID
ARG VITE_KEYCLOAK_REALM
# expose the variable to the finished container
ENV VITE_API_ENDPOINT=$VITE_API_ENDPOINT
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM

LABEL org.opencontainers.image.title=resulter-frontend
LABEL org.opencontainers.image.description="Frontend for the resulter app"
LABEL org.opencontainers.image.source=https://github.com/HenryJobst/resulter
LABEL org.opencontainers.image.licenses='CC BY-NC-ND 4.0'

COPY package.json ./

RUN corepack enable pnpm

RUN pnpm install

COPY . .

RUN pnpm run build

# production stage
FROM nginx AS production-stage

COPY --from=build-stage /resulter/dist /usr/share/nginx/html

# Erstellen Sie Ihre eigene Nginx-Konfigurationsdatei `default.conf`
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
