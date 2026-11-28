<img src="./frontend/src/assets/Logo_Resulter_Circle.png" alt="Resulter Logo" width="512"/>

# **Resulter**

[Deutsch](README.de.md) · **English**

[![version](https://img.shields.io/badge/version-4.12.4-blue)]()
[![license](https://img.shields.io/badge/license-CC%20BY--NC--ND%204.0-blue)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![code style](https://antfu.me/badge-code-style.svg)](https://github.com/antfu/eslint-config)

## Overview

**Resulter** is an innovative web application for orienteering enthusiasts and organizers of orienteering competitions.

This application allows for the input of competition results files and subsequently displays detailed result lists.
As the first major feature there is the possibility to design certificate templates and activate certificate download
for an event for every single participant.
Other functions include:
- Calculation and display of cup and ranking points for the German Nebel-Cup, Kristall-Cup and the North-East-Ranking
- Split time analysis with various evaluations (ranking, mental resilience, anomaly detection, hanging detection)

Possible future extensions: generalization of cups, calculation of recommended course lengths, etc.

## Architecture

The frontend communicates with the backend via a REST API, which is implemented in the pattern of Hexagonal
Architecture. Authentication follows the Backend-for-Frontend (BFF) pattern, where the backend handles OAuth2/OIDC
communication with Keycloak, and the frontend uses secure session cookies.

The full architecture documentation (arc42) is in [docs/architecture.md](docs/architecture.md).

## Technologies

- **Backend**: Java, SpringBoot, Spring Data JDBC
- **Frontend**: Typescript, Vue.js 3, Vite, Pinia, Primevue, Tailwind, Tanstack Query
- **Communication**: REST API
- **Authentication/Authorisation**: BFF Pattern with Keycloak (OAuth2/OIDC), Session-based
- **Database**: PostgreSQL, H2

## Prerequisites

- Java (recommended: latest version)
- Node.js and npm/pnpm
- An IDE of your choice (e.g., IntelliJ IDEA for backend, VSCode for frontend)

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/HenryJobst/resulter.git
   ```

2. Create and edit environment file

    ```bash
    cp .env.example .env
    ```
   
3. Switch to the backend directory and start the SpringBoot server:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. Switch to the frontend directory and start the Vue.js frontend:

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```
5. Deployment

   Images are built with Paketo Buildpacks (Cloud Native Buildpacks):
   - Build scripts: `./backend/build-paketo.sh` and `./frontend/build-paketo.sh`
   - Requires pack CLI: `brew install buildpacks/tap/pack` (macOS) or see [buildpacks.io](https://buildpacks.io/docs/tools/pack/)
   - Benefits: 12-factor apps, runtime configuration, automatic SBOM generation, non-root user
   - CI/CD: `.github/workflows/build-paketo.yml`

   **Running the application:**
   - There is a compose file in `deploy/resulter/` to start the complete application with database in Docker containers

## Usage

After starting the application, you can upload result files from orienteering competitions. The application processes
these and provides detailed result lists. Future features such as certificate printing and points calculations will be
added in upcoming versions.

Documentation:
- User manual: [`docs/manual`](docs/manual/README.md) (German: [`README.de.md`](docs/manual/README.de.md))
- Architecture decision records: [`docs/adr`](docs/adr/README.md) (German: [`README.de.md`](docs/adr/README.de.md))
- Guide for integrating new fonts into document PDF export (iText):
  [`doc/itext-font-integration-guide-generic.md`](doc/itext-font-integration-guide-generic.md)

## Contributions

We welcome contributions to the development of Resulter. Please consult with us in advance to avoid duplicate
developments.

## License

Resulter © 2023-2026 by Henry Jobst is licensed
under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) - see [LICENSE.txt](LICENSE.txt) for
details.
