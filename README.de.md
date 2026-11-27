<img src="./frontend/src/assets/Logo_Resulter_Circle.png" alt="Resulter Logo" width="512"/>

# **Resulter**

**Deutsch** · [English](README.md)

[![version](https://img.shields.io/badge/version-4.12.3-blue)]()
[![license](https://img.shields.io/badge/license-CC%20BY--NC--ND%204.0-blue)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![code style](https://antfu.me/badge-code-style.svg)](https://github.com/antfu/eslint-config)

## Überblick

**Resulter** ist eine innovative Web-Anwendung für Orientierungslauf-Enthusiasten und Organisatoren von
Orientierungslauf-Wettkämpfen.

Mit dieser Anwendung können Ergebnisdateien von Wettkämpfen eingelesen und anschließend detaillierte Ergebnislisten
angezeigt werden.
Als erste große Funktion besteht die Möglichkeit, Urkundenvorlagen zu gestalten und für eine Veranstaltung den
Urkunden-Download für jeden einzelnen Teilnehmer zu aktivieren.
Weitere Funktionen umfassen:
- Berechnung und Anzeige von Cup- und Ranglistenpunkten für den deutschen Nebel-Cup, Kristall-Cup und die Nord-Ost-Rangliste
- Splittzeiten-Analyse mit verschiedenen Auswertungen (Ranking, mentale Belastbarkeit, Anomalieerkennung, Hängererkennung)

Zukünftige mögliche Erweiterungen: Generalisierung von Cups, Berechnung von empfohlenen Bahnlängen etc.

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
    pnpm install
    pnpm dev
    ```
5. Deployment

   Die Images werden mit Paketo Buildpacks (Cloud Native Buildpacks) gebaut:
   - Build-Skripte: `./backend/build-paketo.sh` und `./frontend/build-paketo.sh`
   - Benötigt pack CLI: `brew install buildpacks/tap/pack` (macOS) oder siehe [buildpacks.io](https://buildpacks.io/docs/tools/pack/)
   - Vorteile: 12-Faktor-Apps, Runtime-Konfiguration, automatische SBOM-Generierung, Non-Root-User
   - CI/CD: `.github/workflows/build-paketo.yml`

   **Anwendung ausführen:**
   - Im Verzeichnis `deploy/resulter/` gibt es ein Docker-Compose-File, mit dem die komplette Anwendung mit Datenbank gestartet werden kann

## Nutzung

Nach dem Starten der Anwendung können Sie Ergebnisdateien von Orientierungslauf-Wettkämpfen hochladen. Die Anwendung
verarbeitet diese und stellt detaillierte Ergebnislisten zur Verfügung. Zukünftige Funktionen wie Urkundendruck und
Punkteberechnungen werden in kommenden Versionen ergänzt.

Dokumentation:
- Benutzerhandbuch: [`docs/manual`](docs/manual/README.de.md) (Englisch: [`README.md`](docs/manual/README.md))
- Architekturentscheidungen (ADR): [`docs/adr`](docs/adr/README.de.md) (Englisch: [`README.md`](docs/adr/README.md))
- Anleitung zum Einbinden neuer Schriftarten in den Dokument-PDF-Export (iText):
  [`doc/itext-font-integration-guide-generic.md`](doc/itext-font-integration-guide-generic.de.md)

## Beiträge

Wir begrüßen Beiträge zur Weiterentwicklung von Resulter. Bitte halten Sie vorab Rücksprache, um Doppelentwicklungen zu
vermeiden.

## Lizenz

Das Projekt Resulter © 2023-2026 von Henry Jobst ist unter der
Lizenz [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) lizenziert -
siehe [LICENSE.txt](LICENSE.txt) für Details.

