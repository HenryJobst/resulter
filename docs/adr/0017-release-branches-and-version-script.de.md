# ADR 0017: Release über Release-Branches und Versionsskript

> **Deutsch** · [English](0017-release-branches-and-version-script.md)

- **Status:** akzeptiert
- **Datum:** 2026-05-22
- **Kontext:** Commit `4d4d85cd` („add version bump and release branch scripts“); Pre-Push-Hook mit Trivy seit 2026-06-07

## Kontext

Container-Images entstehen aus Release-Branches und Tags
([ADR 0016](0016-container-images-with-paketo-buildpacks.de.md)). Die Version steht an
mehreren Stellen des Repositorys, und ein Rollout soll nur nach einem Sicherheitsscan
passieren. Die Auslieferung liegt außerhalb dieses Repositorys.

## Entscheidung

1. **Semantische Versionierung.** `CHANGELOG.md` folgt „Keep a Changelog“ und
   „Semantic Versioning“. Die maßgebliche Version steht in der Wurzel-`pom.xml`.

2. **Versionsänderung nur über das Skript.** `bump-version.sh major|minor|patch` liest die
   Version aus der Wurzel-`pom.xml`, ersetzt sie in `pom.xml`, `backend/pom.xml`,
   `frontend/package.json`, beiden `project.toml`, `application.properties`
   (`openapi.config.version`) und im README-Badge und erzeugt den Commit
   `feat: bump version to X.Y.Z`. Das Skript bricht bei uncommitteten Änderungen ab.

3. **Release-Branch statt Tag als Auslöser.** `create-release-branch.sh` legt den Branch
   `releases/vX.Y.Z` von `main` an und pusht ihn; der Workflow `build-paketo.yml` baut daraus
   die Images mit dem Tag `paketo-X.Y.Z`.

4. **Pre-Push-Hook mit Trivy.** `simple-git-hooks` (konfiguriert in
   `frontend/package.json`) startet vor jedem Push `pre-push-trivy.sh`. Er scannt nur bei
   Pushes nach `refs/heads/releases/*`: Er baut die Images lokal und prüft sie mit
   `trivy-scan-local.sh`, das die CI-Schritte spiegelt (mit vorhandenen Images etwa eine
   Minute, sonst laut Skript rund zehn Minuten).

5. **Tägliches automatisches Patch-Release.** Der Workflow `auto-merge-deps.yml`
   erhöht nach Dependency-Merges einmal pro Tag (ab 15 Uhr Berlin) die Patch-Version
   mit `bump-version.sh patch`, pusht `main` und erstellt den Release-Branch mit
   `create-release-branch.sh` ([ADR 0018](0018-renovate-and-auto-merge.de.md)).

6. **Die Auslieferung liegt außerhalb dieses Repositorys.** Der Prozess hier endet mit den
   veröffentlichten Images `paketo-X.Y.Z`. Wie und wohin sie ausgerollt werden, ist nicht
   Teil dieses Repositorys und wird hier bewusst nicht beschrieben.

## Konsequenzen

**Positiv**

- Eine Quelle für die Version, ein Skript für alle Stellen; kein manuelles Eintragen.
- Images aus Release-Branches wurden vor dem Push lokal gescannt und in der CI erneut.

**Negativ / Kosten**

- Nach dem Release ist ein weiterer Schritt außerhalb dieses Repositorys nötig, damit die
  neue Version in Betrieb geht.
- Der Pre-Push-Hook dauert mehrere Minuten und braucht Docker, `pack` und `trivy`.
- Die Auslieferung ist in diesem Repository nicht beschrieben.
- Die Hook-Konfiguration steht in `frontend/package.json`, das Skript im Wurzelverzeichnis.

## Alternativen (nicht dokumentiert, naheliegend)

- **Release per Git-Tag** (der Workflow reagiert auch auf `v*`-Tags).

## Verwandte Dokumente

- [`bump-version.sh`](../../bump-version.sh)
- [`create-release-branch.sh`](../../create-release-branch.sh)
- [`pre-push-trivy.sh`](../../pre-push-trivy.sh)
- [`trivy-scan-local.sh`](../../trivy-scan-local.sh)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`auto-merge-deps.yml`](../../.github/workflows/auto-merge-deps.yml)
- [`CHANGELOG.md`](../../CHANGELOG.md)
- [`package.json` des Frontends](../../frontend/package.json)
