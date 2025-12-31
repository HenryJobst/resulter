<img src="./frontend/src/assets/Logo_Resulter_Circle.png" alt="Resulter Logo" width="512"/>

# **Resulter**

[![version](https://img.shields.io/badge/version-4.2.3-blue)]()
[![license](https://img.shields.io/badge/license-CC%20BY--NC--ND%204.0-blue)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![code style](https://antfu.me/badge-code-style.svg)](https://github.com/antfu/eslint-config)

## Overview

**Resulter** is an innovative web application for orienteering enthusiasts and organizers of orienteering competitions.

This application allows for the input of competition results files and subsequently displays detailed result lists.
As the first major feature there is the possibility to design certificate templates and activate certificate download
for an event for every single participant.
Other functions include the calculation and display of cup and ranking points for the German Nebel-Cup, Kristall-Cup and the North-East-Ranking.

Possible future extensions: generalization of cups, evaluation of split times, calculation of recommended course lengths, etc.

## Architecture

The frontend communicates with the backend via a REST API, which is implemented in the pattern of Hexagonal
Architecture. Authentication follows the Backend-for-Frontend (BFF) pattern, where the backend handles OAuth2/OIDC
communication with Keycloak, and the frontend uses secure session cookies.

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
   npm install
   npm run serve
   ```
5. Deployment

- there are separate deployment scripts (build.sh) to build docker images for frontend and backend
- there is a compose file to start the whole application with database in docker containers

## Usage

After starting the application, you can upload result files from orienteering competitions. The application processes
these and provides detailed result lists. Future features such as certificate printing and points calculations will be
added in upcoming versions.

## Contributions

We welcome contributions to the development of Resulter. Please consult with us in advance to avoid duplicate
developments.

## License

Resulter © 2023-2025 by Henry Jobst is licensed
under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) - see [LICENSE.txt](LICENSE.txt) for
details.

-----

## Überblick

**Resulter** ist eine innovative Web-Anwendung für Orientierungslauf-Enthusiasten und Organisatoren von
Orientierungslauf-Wettkämpfen.

Mit dieser Anwendung können Ergebnisdateien von Wettkämpfen eingelesen und anschließend detaillierte Ergebnislisten
angezeigt werden.
Als erste große Funktion besteht die Möglichkeit, Urkundenvorlagen zu gestalten und für eine Veranstaltung den
Urkunden-Download für jeden einzelnen Teilnehmer zu aktivieren.
Weitere Funktionen sind die Berechnung und Anzeige von Cup- und Ranglistenpunkten für den deutschen Nebel-Cup, Kristall-Cup und die Nord-Ost-Rangliste.

Zukünftige mögliche Erweiterungen: Generalisierung von Cups, Auswertung von Splittzeiten, Berechnung von empfohlenen Bahnlängen etc.

## Architektur

Das Frontend kommuniziert über eine REST-API mit dem Backend, welches im Muster der Hexagonalen Architektur
implementiert ist. Die Authentifizierung folgt dem Backend-for-Frontend (BFF) Pattern, wobei das Backend die OAuth2/OIDC
Kommunikation mit Keycloak übernimmt und das Frontend sichere Session-Cookies verwendet.

## Technologien

- **Backend**: Java, SpringBoot, Spring Data JDBC
- **Frontend**: Typescript, Vue.js 3, Vite, Pinia, Primevue, Tailwind, Tanstack Query
- **Kommunikation**: REST API
- **Authentifizierung/Berechtigung**: BFF Pattern mit Keycloak (OAuth2/OIDC), Session-basiert
- **Datenbank**: PostgreSQL, H2

## Voraussetzungen

- Java (empfohlen: neueste Version)
- Node.js und npm/pnpm
- Eine IDE Ihrer Wahl (z.B. IntelliJ IDEA für Backend, VSCode für Frontend)

## Installation

1. Klonen Sie das Repository:

    ```bash
    git clone https://github.com/HenryJobst/resulter.git
    ```

2. Environment-Datei anlegen und anpassen

    ```bash
    cp .env.example .env
    ```
   
3. Wechseln Sie in das Backend-Verzeichnis und starten Sie den SpringBoot-Server:

    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

4. Wechseln Sie in das Frontend-Verzeichnis und starten Sie das Vue.js-Frontend:

    ```bash
    cd frontend
    npm install
    npm run serve
    ```
5. Deployment

- es gibt je ein Script (build.sh) um Docker-Images für Frontend und Backend zu bauen
- im Verzeichnis deploy/resulter gibt es ein Docker-Compose-File mit dem die komplette Anwendung mit Datenbank gestartet
  werden kann

## Nutzung

Nach dem Starten der Anwendung können Sie Ergebnisdateien von Orientierungslauf-Wettkämpfen hochladen. Die Anwendung
verarbeitet diese und stellt detaillierte Ergebnislisten zur Verfügung. Zukünftige Funktionen wie Urkundendruck und
Punkteberechnungen werden in kommenden Versionen ergänzt.

## Beiträge

Wir begrüßen Beiträge zur Weiterentwicklung von Resulter. Bitte halten Sie vorab Rücksprache, um Doppelentwicklungen zu
vermeiden.

## Lizenz

Das Projekt Resulter © 2023-2025 von Henry Jobst ist unter der
Lizenz [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) lizenziert -
siehe [LICENSE.txt](LICENSE.txt) für Details.
