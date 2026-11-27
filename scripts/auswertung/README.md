# Analyses for the Nord-Ost-Rangliste

> [Deutsch](README.de.md) · **English**

Scripts for the annual review. They only read; nothing in the database is changed.

## Procedure

```bash
# 1. Export (production DB, read-only). Adjust host, key and container to your own environment.
ssh -i ~/.ssh/<key> root@<server> \
  "docker exec -i resulter-postgres-1 sh -c 'psql -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\"'" \
  < export_starts.sql > starts.csv

# 2. Analysis
python3 nor_auswertung.py starts.csv
```

`export_starts.sql` contains the cup IDs of the NOR (2021 = 15, 2022 = 1, 2023 = 2, 2024 = 3, 2025 = 9, 2026 = 17).
For a new year, add the ID in `export_starts.sql` and in `CUPS` in `nor_auswertung.py`.

## Definitions

- **Starts:** all starts except "did not start", excluding persons marked as "ignored" in Resulter.
- **Group and team entries** (name with `+ & / ,`, "und" or "Team …") and **accompanying persons** are not counted.
- **Age:** event year minus birth year. Starts without a birth year are missing from both analyses (the persons missing are listed at the end).
- **Children and youth:** age up to 18 years, regardless of the class started, all courses including beginner courses.
- **More difficult class (strict):** class harder than the person's age class.
  - Class names are normalized (`D-10`, `D10`, `D21L`, `D19-L` …).
  - The open short course (K) is never counted as more difficult, the long open course (L) is.
  - D19/H19/D20 without a K/L indication count as a start, but never as more difficult (the course length is unknown).
  - Not rated: beginner courses (B, D/H10B, D/H12B), BK, BL, accompanying persons, Hasen, Off*, KL.

## Known limitations

- Duplicates (the same person with several IDs) distort the person counts. Clean them up beforehand in the list of persons.
- Missing birth years make the youth figures a lower bound.
