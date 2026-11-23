# Auswertungen Nord-Ost-Rangliste

Skripte für den Jahresrückblick. Sie lesen nur, es wird nichts in der Datenbank geändert.

## Ablauf

```bash
# 1. Export (Produktions-DB, lesend). Host, Schlüssel und Container an die eigene Umgebung anpassen.
ssh -i ~/.ssh/<key> root@192.0.2.1 \
  "docker exec -i resulter-postgres-1 sh -c 'psql -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\"'" \
  < export_starts.sql > starts.csv

# 2. Auswertung
python3 nor_auswertung.py starts.csv
```

`export_starts.sql` enthält die Cup-IDs der NOR (2021 = 15, 2022 = 1, 2023 = 2, 2024 = 3, 2025 = 9, 2026 = 17).
Für ein neues Jahr die ID in `export_starts.sql` und in `CUPS` in `nor_auswertung.py` ergänzen.

## Definitionen

- **Starts:** alle Starts außer „nicht gestartet“, ohne Personen, die in Resulter auf „ignoriert“ stehen.
- **Sammel- und Teameinträge** (Name mit `+ & / ,`, „und“ oder „Team …“) und **Begleitung** werden nicht gezählt.
- **Alter:** Wettkampfjahr minus Geburtsjahr. Starts ohne Geburtsjahr fehlen in beiden Auswertungen (am Ende wird aufgelistet, wer fehlt).
- **Kinder und Jugendliche:** Alter bis 18 Jahre, unabhängig von der gestarteten Klasse, alle Bahnen einschließlich Anfängerbahnen.
- **Schwierigere Klasse (streng):** Klasse schwerer als die Altersklasse der Person.
  - Klassennamen werden vereinheitlicht (`D-10`, `D10`, `D21L`, `D19-L` …).
  - Offene kurze Bahn (K) gilt nie als schwieriger, die lange offene Bahn (L) schon.
  - D19/H19/D20 ohne K/L-Angabe zählen als Start, aber nie als schwieriger (die Bahnlänge ist unbekannt).
  - Nicht bewertet: Anfängerbahnen (B, D/H10B, D/H12B), BK, BL, Begleitung, Hasen, Off*, KL.

## Bekannte Grenzen

- Dubletten (gleiche Person mit mehreren IDs) verfälschen die Personenzahlen. Sie vorher in der Liste der Personen bereinigen.
- Fehlende Geburtsjahre machen die Jugendzahlen zu einer Untergrenze.
