<img src="Logo_Resulter.png" alt="Resulter Logo" width="250"/>

# **Resulter**

## Overview

**Resulter** is an innovative web application for orienteering enthusiasts and organizers of orienteering competitions.

This application allows for the input of competition results files and subsequently displays detailed result lists.

Future expansions are planned to include the printing of certificates, calculations of cup and ranking list points, as
well as an analysis of split times.

## Architecture

The frontend communicates with the backend via a REST API, which is implemented in the pattern of Hexagonal
Architecture.

## Technologies

- **Backend**: Java, SpringBoot, Hibernate
- **Frontend**: Typescript, Vue.js 3, Vite, Pina, Primevue, Tailwind, Tanstack Query
- **Communication**: REST API
- **Authentication/Authorisation**: Keycloak
- **Database**: Postgresql, H2

## Prerequisites

- Java (recommended: latest version)
- Node.js and npm
- An IDE of your choice (e.g., IntelliJ IDEA for backend, VSCode for frontend)

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/HenryJobst/resulter.git
   ```

2. Switch to the backend directory and start the SpringBoot server:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. Switch to the frontend directory and start the Vue.js frontend:

   ```bash
   cd frontend
   npm install
   npm run serve
   ```

## Usage

After starting the application, you can upload result files from orienteering competitions. The application processes
these and provides detailed result lists. Future features such as certificate printing and points calculations will be
added in upcoming versions.

## Contributions

We welcome contributions to the development of Resulter. Please consult with us in advance to avoid duplicate
developments.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

© 2024

-----

## Überblick

**Resulter** ist eine innovative Web-Anwendung für Orientierungslauf-Enthusiasten und Organisatoren von
Orientierungslauf-Wettkämpfen.

Mit dieser Anwendung können Ergebnisdateien von Wettkämpfen eingelesen und anschließend detaillierte Ergebnislisten
angezeigt werden.

Zukünftige Erweiterungen sollen den Druck von Urkunden, Berechnungen von Cup- und Ranglistenpunkten sowie eine
Auswertung von Splittzeiten ermöglichen.

## Architektur

Das Frontend kommuniziert über eine REST-API mit dem Backend, welches im Muster der Hexagonalen Architektur
implementiert ist.

## Technologien

- **Backend**: Java, SpringBoot, Hibernate
- **Frontend**: Typescript, Vue.js 3, Vite, Pina, Primevue, Tailwind, Tanstack Query
- **Kommunikation**: REST API
- **Authentifizierung/Berechtigung**: Keycloak
- **Datenbank**: Postgresql, H2

## Voraussetzungen

- Java (empfohlen: neueste Version)
- Node.js und npm
- Eine IDE Ihrer Wahl (z.B. IntelliJ IDEA für Backend, VSCode für Frontend)

## Installation

1. Klonen Sie das Repository:

    ```bash
    git clone https://github.com/HenryJobst/resulter.git
    ```

Wechseln Sie in das Backend-Verzeichnis und starten Sie den SpringBoot-Server:

    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

Wechseln Sie in das Frontend-Verzeichnis und starten Sie das Vue.js-Frontend:

    ```bash
    cd frontend
    npm install
    npm run serve
    ```

## Nutzung

Nach dem Starten der Anwendung können Sie Ergebnisdateien von Orientierungslauf-Wettkämpfen hochladen. Die Anwendung
verarbeitet diese und stellt detaillierte Ergebnislisten zur Verfügung. Zukünftige Funktionen wie Urkundendruck und
Punkteberechnungen werden in kommenden Versionen ergänzt.

## Beiträge

Wir begrüßen Beiträge zur Weiterentwicklung von Resulter. Bitte halten Sie vorab Rücksprache, um Doppelentwicklungen zu
vermeiden.

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die [LICENSE.txt](LICENSE.txt) Datei für Details.

© 2024