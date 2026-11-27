# ADR 0008: OpenVue statt PrimeVue als UI-Bibliothek

> **Deutsch** · [English](0008-openvue-instead-of-primevue.md)

- **Status:** akzeptiert
- **Datum:** 2026-11-24
- **Kontext:** Commit `49576fc7` („migrate from PrimeVue to OpenVue 1.1.1“)

## Kontext

Das Frontend nutzte seit dem Start (2023-12-17) PrimeVue als Komponentenbibliothek mit
dem Theme-Paket `@primeuix/themes`. Am 2026-11-24 wurde in einem Commit auf OpenVue 1.1.1
umgestellt. Die Commit-Nachricht nennt nur das Vorgehen und die Prüfung. Der Grund stammt vom
Projektinhaber: PrimeVue ist ab Version 5 nicht mehr frei nutzbar.

## Entscheidung

1. **OpenVue ersetzt PrimeVue.** Importe wechselten von `primevue/*` auf `openvue/*`,
   das Theme von `@primeuix/themes` auf `@openvue/themes`. Die Umstellung lief mit dem
   Werkzeug `@openvue/migrate` 1.1.1 (55 Dateien im Commit). Komponenten und ihre
   Verwendung (Button, Dialog, Select, ColorPicker, …) blieben im Aufbau gleich.

2. **Versionen exakt gepinnt.** `openvue` und `@openvue/themes` stehen in
   `package.json` ohne Versionsbereich auf `1.1.1`.

3. **Die Bezeichner `primevue` bleiben an einigen Stellen.** `usePrimeVue` wird aus
   `openvue/config` importiert; Dateien und Texte wie `PrimevueMessages.ts` und
   `locales/primevue` heißen unverändert.

Begründung des Wechsels (Projektinhaber): PrimeVue ist ab Version 5 nicht mehr frei nutzbar.

## Konsequenzen

**Positiv**

- Eine mechanische Migration ohne Umbau der Seiten; Typprüfung, Build, Lint und
  Unit-Tests liefen laut Commit-Nachricht danach durch.

**Negativ / Kosten**

- README und `.cursorrules` nennen noch PrimeVue.
- Die exakte Versionspinnung verhindert automatische Patch-Updates der Bibliothek;
  Renovate muss sie anheben ([ADR 0018](0018-renovate-and-auto-merge.de.md)).
- Die Kommentar in der nginx-Konfiguration zur CSP spricht weiterhin von PrimeVue.

## Alternativen (nicht dokumentiert, naheliegend)

- **PrimeVue bei Version 4 behalten:** kein Umbau, aber ohne Updates auf Version 5, die nicht
  mehr frei nutzbar ist.
- **Andere Bibliothek** (Vuetify, Element Plus, Nuxt UI): nicht erwogen dokumentiert.

## Verwandte Dokumente

- [`package.json` des Frontends](../../frontend/package.json)
- [`i18n.ts`](../../frontend/src/i18n.ts)
- [`PrimevueMessages.ts`](../../frontend/src/PrimevueMessages.ts)
- [`default.conf`](../../frontend/default.conf)
- [ADR 0007: Frontend-Stack und Struktur](0007-frontend-stack-and-structure.de.md)
