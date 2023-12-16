<img src="Logo_Resulter.png" alt="Resulter Logo" width="512"/>

# **Resulter**

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

- **Backend**: SpringBoot
- **Frontend**: Vue.js
- **Kommunikation**: REST-API

## Voraussetzungen

- Java (empfohlen: neueste Version)
- Node.js und npm
- Eine IDE Ihrer Wahl (z.B. IntelliJ IDEA für Backend, VSCode für Frontend)

## Installation

1. Klonen Sie das Repository:

```bash
   git clone [URL des Repositories]
````

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

Wir begrüßen Beiträge zur Weiterentwicklung von Resulter. Bitte halten sie vorab Rücksprache um Doppelentwicklungen zu
vermeiden.

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die LICENSE Datei für Details.

© 2023-