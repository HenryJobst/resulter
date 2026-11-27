# ADR 0020: Lizenz CC BY-NC-ND 4.0

> **Deutsch** · [English](0020-license-cc-by-nc-nd-4-0.md)

- **Status:** akzeptiert
- **Datum:** 2023-12-16
- **Kontext:** Commit `433c42ba` („LICENSE.txt hinzugefügt“)

## Kontext

Der Quelltext von Resulter liegt auf GitHub (`HenryJobst/resulter`, so im README
angegeben; Sichtbarkeit nicht geprüft). Zwei Wochen
nach dem ersten Commit wurde eine Lizenzdatei ergänzt. Die Commit-Nachricht nennt keinen
Grund; die Begründung stammt vom Projektinhaber.

## Entscheidung

1. **Resulter steht unter Creative Commons Namensnennung – Nicht kommerziell – Keine
   Bearbeitungen 4.0 International (CC BY-NC-ND 4.0).** Der volle Lizenztext liegt in
   `LICENSE.txt`; das README zeigt ein Lizenz-Badge und nennt
   „Resulter © 2023-2026 by Henry Jobst“.

2. **Beiträge nach Absprache.** Das README bittet darum, Beiträge vorher abzusprechen, um
   doppelte Entwicklungen zu vermeiden.

3. **Die Lizenz gilt für den eigenen Code, nicht für eingebundene Fremdkomponenten.**
   Schriften unter der SIL Open Font License und der Apache License 2.0 behalten ihre
   Lizenzen ([ADR 0011](0011-certificate-fonts.de.md)); Abhängigkeiten haben ihre eigenen
   Lizenzen.

Begründung für die Wahl dieser Lizenz (Projektinhaber): Sie soll einen kommerziellen Nachbau
verhindern.

## Konsequenzen

**Positiv**

- Eindeutige Nutzungsbedingungen: Weitergabe mit Namensnennung, nicht kommerziell, ohne
  Bearbeitungen.

**Negativ / Kosten**

- CC BY-NC-ND ist keine von der Open Source Initiative anerkannte Open-Source-Lizenz;
  Forks mit Änderungen sind damit grundsätzlich nicht erlaubt.
- Creative Commons empfiehlt die Lizenzen nicht für Software; sie enthalten keine
  Regelungen zu Patenten oder zur Haftung, wie sie Softwarelizenzen üblicherweise
  enthalten. (Allgemeine Auskunft, im Repository nicht belegt.)
- Beiträge Dritter erfordern eine gesonderte Klärung der Rechte.

## Alternativen (nicht dokumentiert, naheliegend)

- **Eine Open-Source-Lizenz** (MIT, Apache 2.0, AGPL).
- **Eine Software-Lizenz mit Nicht-kommerziell-Klausel** (z. B. PolyForm Noncommercial).

## Verwandte Dokumente

- [`LICENSE.txt`](../../LICENSE.txt)
- [README, Abschnitt License](../../README.md)
- [ADR 0011: Eingebundene Urkunden-Schriften](0011-certificate-fonts.de.md)
