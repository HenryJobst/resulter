# ADR 0018: Dependency-Updates mit Renovate und kontrolliertem Auto-Merge

> **Deutsch** · [English](0018-renovate-and-auto-merge.md)

- **Status:** akzeptiert
- **Datum:** 2026-05-21
- **Kontext:** Commit `7049651c` („Renovate Bot konfigurieren (ersetzt Dependabot)“); Auto-Merge-Workflow seit 2026-06-24 (`301d2fe5`)

## Kontext

Backend (Maven) und Frontend (pnpm, Nx) haben viele Abhängigkeiten; Sicherheitslücken in
transitiven Paketen kamen im `CHANGELOG.md` mehrfach vor (u. a. `qs`, `tmp`). Bis Mai 2026
setzte das Repository Dependabot ein; Renovate löste es am 2026-05-21 ab. Die
Commit-Nachricht nennt keine weitere Begründung.

## Entscheidung

1. **Renovate** (`renovate.json`, Basis `config:recommended`) erzeugt die Update-PRs.
   Zeitplan: am Wochenende, Zeitzone Europe/Berlin; `minimumReleaseAge` 1 Tag; Label
   `renovate`; Dependency-Dashboard aktiv.

2. **Sicherheits-Updates sofort.** `vulnerabilityAlerts` sind aktiviert, mit Label
   `security`, und ohne Mindestalter der Version.

3. **Major-Updates nur mit Freigabe.** Regel `matchUpdateTypes: major` mit
   `dependencyDashboardApproval`; die PRs tragen das Label `major-update`.

4. **Ausnahmen mit Begründung in der Regel.** Die Nx-Pakete sind abgeschaltet,
   solange `@nx/vue` `vue-router` 5 und `vue-tsc` 3 nicht unterstützt; `undici` bleibt unter
   Version 8 (jsdom 29 braucht eine Datei, die in 8 fehlt); der `ajv@^6`-Override in
   `pnpm-workspace.yaml` wird nicht angehoben (ESLint 10 nutzt die API von ajv 6).

5. **Auto-Merge per GitHub-Workflow.** `auto-merge-deps.yml` läuft alle vier Stunden
   (`0 2,6,10,14,18,22 * * *`) und mergt offene PRs mit den Labels `renovate` oder
   `dependencies`, wenn sie mergebar sind und alle Checks bestanden haben. PRs mit dem Label
   `major-update` werden nie automatisch gemergt. Zum Zusammenführen dient ein
   Merge-Commit; der Branch wird gelöscht. Als Check gilt hier der CI-Bau, nicht
   ein Testlauf ([ADR 0006](0006-test-strategy.de.md)).

6. **Benachrichtigungen.** Der Workflow meldet per E-Mail CI-Fehler bei Dependency-PRs
   und offene Security-PRs, außerdem eine Tagesübersicht ausstehender Freigaben um
   16:00 Uhr Berlin.

7. **Täglicher Patch-Release.** Gab es an dem Tag Merges, löst der Workflow genau einmal
   ein Patch-Release aus ([ADR 0017](0017-release-branches-and-version-script.de.md)).

## Konsequenzen

**Positiv**

- Abhängigkeiten bleiben aktuell; Sicherheits-PRs warten kein Mindestalter ab und werden
  per Mail gemeldet; Major-Sprünge brauchen eine bewusste Entscheidung.
- Die Ausnahmen sind mit Begründung in `renovate.json` festgehalten.

**Negativ / Kosten**

- Da die CI nur kompiliert, entscheidet beim Auto-Merge im Wesentlichen der erfolgreiche
  Bau; ein fachlicher Fehler durch ein Update fällt erst später auf.
- Jede Dependency-Änderung kann ein Release erzeugen und damit einen Rollout.
- Die Pinnung von Versionen (z. B. `openvue` exakt) und Overrides in
  `pnpm-workspace.yaml` müssen gepflegt werden.
- Der Workflow setzt Secrets für die E-Mail voraus (nicht im Repository enthalten).

## Alternativen (nicht dokumentiert, naheliegend)

- **Dependabot** (bis 2026-05-21 im Einsatz; der Workflow behandelt weiterhin das Label
  `dependencies`).
- **Nur manuelle Updates.**

## Verwandte Dokumente

- [`renovate.json`](../../renovate.json)
- [`auto-merge-deps.yml`](../../.github/workflows/auto-merge-deps.yml)
- [`CHANGELOG.md`](../../CHANGELOG.de.md)
- [`pnpm-workspace.yaml`](../../frontend/pnpm-workspace.yaml)
- [`bump-version.sh`](../../bump-version.sh)
