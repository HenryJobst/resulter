# 1 Einführung

> **Deutsch** · [English](01-introduction.md)

## Was Resulter ist

Resulter liest Ergebnisdateien von Orientierungsläufen ein und zeigt daraus Ergebnislisten. Dazu
kommen Cup- und Ranglistenpunkte (Nebel-Cup, Kristall-Cup, Nord-Ost-Rangliste), eine
Splitzeiten-Analyse und Urkunden, die für jeden Teilnehmer erzeugt werden können. Eine kurze
Übersicht steht in der [README](../../README.de.md) des Projekts.

## Aufbau der Oberfläche

![Liste der Urkunden mit Menü und Fußzeile](images/de/urkunden-liste.webp)

Die Oberfläche hat vier Bereiche:

- **Kopfzeile:** das Menü **Start**, **Cups**, **Wettkämpfe**, **Analyse** und **Über**,
  rechts **Logout** (bei angemeldeten Benutzern) und die Sprachwahl Deutsch/Englisch.
- **Seitenleiste** (Abschnitt **Einstellungen**): **Organisationen**, **Personen**, **Medien** und
  **Urkunden**. Die Einträge sehen Benutzer mit entsprechender Berechtigung.
- **Arbeitsbereich:** die jeweilige Seite, hier die Liste der Urkunden.
- **Fußzeile:** die Version von Frontend und Backend, der angemeldete Benutzer sowie die Links
  **Datenschutz** und **Impressum**.

Der Menüknopf links in der Kopfzeile blendet die Seitenleiste ein und aus.

## Anmelden und Rollen

Die Anmeldung läuft über einen zentralen Anmeldedienst (Keycloak). Dorthin wird man beim Anmelden
weitergeleitet und danach zur gewünschten Seite zurückgebracht. Die technische Beschreibung steht in
[ADR 0004](../adr/0004-bff-with-keycloak-and-session-cookie.de.md).

Was ein Benutzer darf, hängt von seiner Rolle ab. Die Oberfläche kennt diese Berechtigungen:

| Berechtigung | Gilt für |
|---|---|
| Wettkämpfe verwalten | Wettkämpfe anlegen und ändern |
| Ergebnisse hochladen | Ergebnisdateien importieren |
| Cups verwalten | Cups und Wertungen pflegen |
| Berichte ansehen | Auswertungen ansehen (ohne weitere Angabe erlaubt) |
| Benutzer verwalten | Benutzerverwaltung |
| Administration | Einstellungen und Stammdaten |

Der Administrator (Rolle `admin`) hat standardmäßig alle diese Berechtigungen. Welche Rollen
außer `admin` es im Betrieb gibt, ist im Quelltext nicht festgelegt. Sie werden im
Anmeldedienst gepflegt.

## Die Urkunden-Seiten

Alle Urkunden-Funktionen sind in der Seitenleiste unter **Urkunden** erreichbar. Sie sind im
[Kapitel 6](06-certificates.de.md) beschrieben, der Editor in den Kapiteln 7 bis 9.

## Die übrigen Seiten

- Cups und Ranglisten: [Kapitel 2](02-cups-and-rankings.de.md)
- Wettkämpfe, Import und Ergebnislisten: [Kapitel 3](03-events-and-results.de.md)
- Splitzeiten-Analyse und die weiteren Auswertungen: [Kapitel 4](04-analysis.de.md)
- Organisationen, Personen und Medien: [Kapitel 5](05-master-data.de.md)

Jedes dieser Kapitel endet mit einer Tabelle, wer was darf.
