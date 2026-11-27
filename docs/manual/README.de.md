# Benutzerhandbuch Resulter

> **Deutsch** · [English](README.md)

Dieses Handbuch beschreibt die Bedienung von Resulter, der Web-Anwendung für
Orientierungslauf-Ergebnisse. Alle Kapitel sind geschrieben. Die Kapitel folgen dem Menü der
Anwendung: zuerst die Einträge der oberen Leiste (Cups, Events, Analyse), dann die Einstellungen
in der Seitenleiste (Stammdaten, Urkunden).

**Stand:** 27.11.2026. Beschrieben ist der Urkunden-Editor so, wie er nach den Korrekturen aus
dem Test im Browser arbeitet (Quelltext nach Version 4.12.3, siehe
[Stand und Grenzen](#stand-und-grenzen)).

## Inhalt

| Kapitel | Inhalt | Stand |
|---|---|---|
| [1 Einführung](01-introduction.de.md) | Aufbau der Oberfläche, Anmelden, Rollen, Begriffe | vollständig |
| [2 Cups und Ranglisten](02-cups-and-rankings.de.md) | Nebel-Cup, Kristall-Cup, Nord-Ost-Rangliste | aus dem Quelltext |
| [3 Wettkämpfe und Ergebnisse](03-events-and-results.de.md) | Wettkämpfe anlegen, Ergebnisdateien importieren, Ergebnislisten | aus dem Quelltext |
| [4 Analyse](04-analysis.de.md) | Splitzeiten-Analyse, mentale Belastbarkeit, Anomalien, Hängererkennung | aus dem Quelltext |
| [5 Stammdaten](05-master-data.de.md) | Organisationen, Personen, Medien | aus dem Quelltext |
| [6 Urkunden](06-certificates.de.md) | Urkunden verwalten, Hintergrund, Vorschau, Platzhalter, Layoutmodell | vollständig |
| [7 Editor: Grafisch](07-certificate-editor-graphic.de.md) | Layout auf der Seitenansicht bearbeiten | vollständig |
| [8 Editor: Formular](08-certificate-editor-form.de.md) | Layout über Eingabefelder bearbeiten | vollständig |
| [9 Editor: JSON](09-certificate-editor-json.de.md) | Layout als JSON-Text bearbeiten, Schema und Fehlermeldungen | vollständig |
| [10 Referenz](10-certificate-reference.de.md) | Platzhalter, Schriften, Maße, Tastenkürzel, Grenzen | vollständig |

Die Kapitel 2 bis 5 beschreiben, was der Quelltext zeigt. Was sich dort nicht belegen ließ, steht
als „Im Quelltext nicht belegt.“ im Text. Die Funktionen sind auch in der
[README](../../README.de.md) des Projekts aufgelistet.

## So ist das Handbuch aufgebaut

- Jedes Kapitel steht in einer eigenen Datei, die Nummer bestimmt die Reihenfolge.
- Bilder liegen im Ordner [`images`](images/de/) im Format WebP.
- Die Bilder zeigen Beispieldaten (zum Beispiel Max Mustermann). Sie wurden mit der Anwendung im
  Browser aufgenommen.
- Bedienelemente sind **fett** gesetzt, so wie sie in der Anwendung beschriftet sind
  (zum Beispiel **Text hinzufügen**).
- „Punkt“ (pt) ist die Maßeinheit der Seite. Eine A4-Seite ist 595 × 842 pt groß.

## Stand und Grenzen

- Die Bilder stammen aus einem Test im Browser mit einem Entwicklungsserver, dessen
  Schnittstelle durch feste Beispieldaten ersetzt war. Die PDF-Vorschau in den Bildern wurde
  dagegen von der echten Anwendung erzeugt.
- Das Handbuch beschreibt, was die Anwendung tut. Wo eine Funktion in der Praxis noch nicht
  ausprobiert wurde, steht das im jeweiligen Kapitel.
- Die Kapitel 2 bis 5 entstanden aus dem Quelltext. Ihre Bilder zeigen die echte Oberfläche, das
  beschriebene Verhalten wurde aber nicht in einem Lauf ausprobiert. Wo Oberfläche und Server sich
  unterscheiden (zum Beispiel bei Berechtigungen), sagt das Kapitel es.

## Weitere Dokumentation

- Architekturentscheidungen: [`docs/adr`](../adr/README.de.md)
- Plan des Urkunden-Editors: [`docs/plans/certificate-layout-editor.de.md`](../plans/certificate-layout-editor.de.md)
- Anleitung zum Einbinden neuer Schriften: [`doc/itext-font-integration-guide-generic.md`](../../doc/itext-font-integration-guide-generic.de.md)
