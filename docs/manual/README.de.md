# Benutzerhandbuch Resulter

> **Deutsch** · [English](README.md)

Dieses Handbuch beschreibt die Bedienung von Resulter, der Web-Anwendung für
Orientierungslauf-Ergebnisse. Es entsteht schrittweise. Vollständig dokumentiert ist bisher
der **Urkunden-Editor** mit seinen drei Varianten. Die übrigen Kapitel sind als Gliederung
angelegt und werden nachgetragen.

**Stand:** 27.11.2026. Beschrieben ist der Urkunden-Editor so, wie er nach den Korrekturen aus
dem Test im Browser arbeitet (Quelltext nach Version 4.12.3, siehe
[Stand und Grenzen](#stand-und-grenzen)).

## Inhalt

| Kapitel | Inhalt | Stand |
|---|---|---|
| [1 Einführung](01-introduction.de.md) | Aufbau der Oberfläche, Anmelden, Rollen, Begriffe | Gerüst |
| [2 Urkunden](02-certificates.de.md) | Urkunden verwalten, Hintergrund, Vorschau, Platzhalter, Layoutmodell | vollständig |
| [3 Editor: Grafisch](03-certificate-editor-graphic.de.md) | Layout auf der Seitenansicht bearbeiten | vollständig |
| [4 Editor: Formular](04-certificate-editor-form.de.md) | Layout über Eingabefelder bearbeiten | vollständig |
| [5 Editor: JSON](05-certificate-editor-json.de.md) | Layout als JSON-Text bearbeiten, Schema und Fehlermeldungen | vollständig |
| [6 Referenz](06-certificate-reference.de.md) | Platzhalter, Schriften, Maße, Tastenkürzel, Grenzen | vollständig |
| 7 Wettkämpfe und Ergebnisse | Wettkämpfe anlegen, Ergebnisdateien importieren, Ergebnislisten | in Vorbereitung |
| 8 Cups und Ranglisten | Nebel-Cup, Kristall-Cup, Nord-Ost-Rangliste | in Vorbereitung |
| 9 Analyse | Splitzeiten-Analyse, mentale Belastbarkeit, Anomalien, Hängererkennung | in Vorbereitung |
| 10 Stammdaten | Organisationen, Personen, Medien | in Vorbereitung |

Die Kapitel 7 bis 10 sind noch nicht geschrieben. Welche Funktionen es dort gibt, steht in der
[README](../../README.de.md) des Projekts.

## So ist das Handbuch aufgebaut

- Jedes Kapitel steht in einer eigenen Datei, die Nummer bestimmt die Reihenfolge.
- Bilder liegen im Ordner [`images`](images/de/) im Format WebP.
- Die Bilder zeigen eine Beispiel-Urkunde mit Beispieldaten (Max Mustermann). Sie wurden mit der
  Anwendung im Browser aufgenommen.
- Bedienelemente sind **fett** gesetzt, so wie sie in der Anwendung beschriftet sind
  (zum Beispiel **Text hinzufügen**).
- „Punkt“ (pt) ist die Maßeinheit der Seite. Eine A4-Seite ist 595 × 842 pt groß.

## Stand und Grenzen

- Die Bilder des Editors stammen aus einem Test im Browser mit einem Entwicklungsserver, dessen
  Schnittstelle durch feste Beispieldaten ersetzt war. Die PDF-Vorschau in den Bildern wurde
  dagegen von der echten Anwendung erzeugt.
- Das Handbuch beschreibt, was die Anwendung tut. Wo eine Funktion in der Praxis noch nicht
  ausprobiert wurde, steht das im jeweiligen Kapitel.
- Zu klären ist, wie Urkunden den Teilnehmern zum Download angeboten werden (Kapitel 7). Das
  Handbuch beschreibt hier nur, was im Quelltext belegbar ist.

## Weitere Dokumentation

- Architekturentscheidungen: [`docs/adr`](../adr/README.de.md)
- Plan des Urkunden-Editors: [`docs/plans/certificate-layout-editor.de.md`](../plans/certificate-layout-editor.de.md)
- Anleitung zum Einbinden neuer Schriften: [`doc/itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.de.md)
