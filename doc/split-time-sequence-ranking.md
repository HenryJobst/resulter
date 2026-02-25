# Abschnittsfolgen in der Zwischenzeiten-Rangliste

## Ziel
Die Zwischenzeiten-Rangliste wurde um **zusammenhängende Abschnittsfolgen** erweitert. Neben der bisherigen Einzelabschnitts-Auswertung (z. B. `31 → 32`) werden nun auch Folgen über mehrere Kontrollen ausgewertet (z. B. `S → 31 → 32`).

## Fachliche Semantik
- Es werden nur **zusammenhängende** Folgen gebildet (Sliding Window).
- Die maximale Folgenlänge ist intern auf **`n-1` Posten** begrenzt (bezogen auf den verfügbaren Kontrollverlauf des Läufers).
- Die minimale Folgenlänge ist über `sequenceMinControls` steuerbar.
- Bestehende Filterregeln gelten konsistent:
  - `filterPersonIds`: nur ausgewählte Läufer
  - `filterIntersection=true`: nur Folgen, in denen alle gefilterten Läufer vorkommen
- Ohne Personenfilter werden Folgen mit nur einem Läufer ausgeblendet.

## API-Erweiterung
Endpoint: `GET /split_time_analysis/result_list/{id}/ranking`

Neue optionale Query-Parameter:
- `includeSequences` (boolean, default `false`)
- `sequenceMinControls` (int, default `3`)

Rückwärtskompatibilität:
- Ohne neue Parameter bleibt Verhalten identisch zur bisherigen Einzelabschnitts-Ausgabe.

## Guardrails
Zur Begrenzung von Payload und Laufzeit:
- maximale Läufer pro Folge: `100`
- maximale Anzahl Folgen in der Antwort: `500`
- deterministische Auswahl/Sortierung vor dem Cut

## Manuelle Abnahmeszenarien
1. Basisfall mit Folgen aktiv
- Request: `...?includeSequences=true&sequenceMinControls=3`
- Erwartung: `sequenceSegments` ist vorhanden und enthält Folgenlabels mit mindestens 3 Kontrollen.

2. Rückwärtskompatibilität
- Request ohne neue Parameter
- Erwartung: `sequenceSegments` leer, Einzelabschnittslogik unverändert.

3. Intersection-Filter
- Request mit `filterPersonIds` (>=2) und `filterIntersection=true`
- Erwartung: nur Folgen, die alle gefilterten Läufer enthalten.

4. Klassenfilter im UI
- In der Zwischenzeiten-Seite Klassenfilter setzen
- Erwartung: Einzelabschnitte und Abschnittsfolgen werden konsistent gefiltert.

5. Große Ergebnisliste
- Mit vielen Läufern/Kontrollen testen
- Erwartung: Antwort bleibt performant, Sequenzen werden bei Bedarf auf Guardrail-Limits begrenzt.
