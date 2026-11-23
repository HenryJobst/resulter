#!/usr/bin/env python3
"""Auswertungen der Nord-Ost-Rangliste aus dem Export von export_starts.sql.

Aufruf: python3 nor_auswertung.py starts.csv
Definitionen stehen in README.md.
"""
import csv
import re
import sys
from collections import defaultdict

CUPS = {15: 2021, 1: 2022, 2: 2023, 3: 2024, 9: 2025, 17: 2026}
GROUP = re.compile(r'[+&/,]| und |^Team ', re.IGNORECASE)
# Klassen, die weder zur Altersklassen-Schwierigkeit noch zur Altersauswertung ausgeschlossen sind,
# aber nicht als "schwieriger" gelten: Anfänger, Begleitung, Hasen, offene Bahnen, Kinderklasse
NOT_RANKED = re.compile(r'^(BK|BL|Begleitung|Hasen|Off.*|KL|D/H.*)$|^[DH]-?\d+[AB]?B$|^[DH]-?(10|12)[ -]?B.*$')
CLS = re.compile(r'^([DH])-?(\d+)-?(A?)([KL]?)$')


def parse_class(name):
    """-> (Altersklassen-Zahl, 'K'|'L'|'') oder None, wenn nicht in der Schwierigkeitsbewertung."""
    if NOT_RANKED.match(name):
        return None
    m = CLS.match(name)
    if not m:
        return None
    num, kind = int(m.group(2)), m.group(4)
    if num in (20, 21):
        num = 19
    return num, kind


def age_class(age):
    if age <= 18:
        return 10 if age <= 10 else (age + 1) // 2 * 2
    if age < 35:
        return 19
    return age // 5 * 5


def difficulty(num, kind):
    """Höher = schwerer. Offen zählt nur mit Angabe L als schwieriger (K und ohne Angabe nie)."""
    if num == 19:
        return 6 if kind == 'L' else 0
    if num <= 18:
        return (num - 8) // 2
    return 6 - (num - 30) // 5


def load(path):
    with open(path, encoding='utf-8') as f:
        return list(csv.DictReader(f))


def is_person(r):
    return not GROUP.search(r['nm']) and r['cls'] != 'Begleitung'


def youth(rows):
    print('\nKinder und Jugendliche bis 18 Jahre (Alter = Wettkampfjahr - Geburtsjahr)')
    print('Jahr  Läufe  Personen  Starts  Starts/Lauf  Anteil')
    for cup, year in CUPS.items():
        rs = [r for r in rows if int(r['cup']) == cup and is_person(r)]
        ev = {r['ev'] for r in rs}
        ys = [r for r in rs if r['yob'] and int(r['yr']) - int(r['yob']) <= 18]
        print(f'{year}  {len(ev):5}  {len({r["pid"] for r in ys}):8}  {len(ys):6}  '
              f'{len(ys) / len(ev):11.0f}  {100 * len(ys) / len(rs):5.1f} %')


def harder(rows):
    print('\nStarts in schwierigerer Klasse als die Altersklasse (streng)')
    print('Jahr  Starts  davon schwieriger  Anteil  Personen  davon schwieriger')
    for cup, year in CUPS.items():
        total = hard = 0
        pers, pers_hard = set(), set()
        for r in rows:
            if int(r['cup']) != cup or not r['yob'] or not is_person(r):
                continue
            c = parse_class(r['cls'])
            if c is None:
                continue
            total += 1
            pers.add(r['pid'])
            own = age_class(int(r['yr']) - int(r['yob']))
            own_d = 6 if own == 19 else difficulty(own, '')
            if difficulty(*c) > own_d:
                hard += 1
                pers_hard.add(r['pid'])
        print(f'{year}  {total:6}  {hard:17}  {100 * hard / total:5.1f} %  {len(pers):8}  {len(pers_hard):17}')


def missing_birth_year(rows):
    print('\nNOR 2026: Personen ohne Geburtsjahr (nicht ignoriert)')
    miss = defaultdict(set)
    starts = 0
    for r in rows:
        if int(r['cup']) == 17 and not r['yob']:
            miss[(r['pid'], r['nm'])].add(r['cls'])
            starts += is_person(r)
    print(f'  {starts} Starts von Einzelpersonen (ohne Sammel- und Begleitungseinträge, ohne Geburtsjahr)')
    for (pid, nm), cls in sorted(miss.items(), key=lambda x: x[0][1]):
        print(f'  {pid:>5}  {nm}  ({", ".join(sorted(cls))})')


if __name__ == '__main__':
    data = load(sys.argv[1])
    youth(data)
    harder(data)
    missing_birth_year(data)
