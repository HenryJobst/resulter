# ADR 0016: Container-Images mit Paketo Buildpacks

> **Deutsch** · [English](0016-container-images-with-paketo-buildpacks.md)

- **Status:** akzeptiert
- **Datum:** 2026-01-23
- **Kontext:** Commit `222c5878` („add Paketo Buildpacks support with fixes for permissions“)

## Kontext

Backend und Frontend werden als Container-Images ausgeliefert. Der Commit vom
2026-01-23 führte Cloud Native Buildpacks (Paketo) „as alternative build method“ ein. Am 2026-11-22
entfernte der Commit `3ae26395` („remove legacy Dockerfile image build, keep Paketo
only“) die Dockerfiles von Backend und Frontend; seitdem ist Paketo der einzige Weg. Das README nennt als Vorteile: 12-Factor-Apps,
Konfiguration zur Laufzeit, automatische SBOM-Erzeugung, Nutzer ohne Root-Rechte. Der Projektinhaber nennt außerdem allgemein, dass Paketo diverse
Vorteile gegenüber eigenen Dockerfiles hat.

## Entscheidung

1. **Images entstehen mit `pack` und dem Builder `paketobuildpacks/builder-jammy-base`.**
   - Backend: Buildpack `paketo-buildpacks/java`, Java 21 (`BP_JVM_VERSION`), das
     JAR wird vorher mit Maven gebaut (`./mvnw clean install -Dmaven.test.skip=true`), um
     Rechteprobleme beim Bau aus den Quellen zu vermeiden.
   - Frontend: Es wird lokal mit pnpm gebaut; das Buildpack
     `paketo-buildpacks/nginx` (Version 1.3.8 festgelegt) verpackt `dist/` mit der
     Konfiguration `default.conf`.
   Lokal übernehmen `backend/build-paketo.sh` und `frontend/build-paketo.sh` das.

2. **Konfiguration kommt zur Laufzeit.** Zugangsdaten und Hosts (OAuth2, Datenbank)
   stehen nicht im Image, sondern werden beim Start als Umgebungsvariablen übergeben.

3. **CI baut und veröffentlicht die Images.** `.github/workflows/build-paketo.yml` läuft
   bei Tags `v*` und bei Branches `releases/**` (und manuell). Es gibt ein
   Qualitätstor für Backend und Frontend (Kompilieren bzw. `pnpm build`), dann den Bau
   der beiden Images. Veröffentlicht wird in die Registry `ghcr.io` unter
   `<Repository>/backend` und `<Repository>/frontend`.

4. **Sicherheitsprüfung vor dem Veröffentlichen.** Trivy scannt das Image
   (`--severity HIGH,CRITICAL --exit-code 1 --ignore-unfixed`) mit je einer
   `.trivyignore` für Backend und Frontend; Bekanntes steht dort mit Begründung und
   Verweis auf den Upstream-Fix. Trivy erzeugt außerdem eine SBOM im CycloneDX-Format.

5. **Tag-Schema.** Bei einem Release-Branch `releases/vX.Y.Z` bekommt das Image den Tag
   `paketo-X.Y.Z` (sowie `paketo-X` und `paketo-X.Y` bei vorhandener Versionsangabe);
   zusätzlich `paketo-latest` und `paketo-<Commit-SHA>`.

## Konsequenzen

**Positiv**

- Kein Dockerfile zu pflegen; das Basis-Image wird vom Buildpack-Anbieter
  aktualisiert.
- Jedes veröffentlichte Image wurde auf HIGH/CRITICAL-Lücken geprüft und hat eine SBOM.

**Negativ / Kosten**

- Der Bau dauert lange (der Pre-Push-Hook nennt „~10 Min“ ohne vorhandene lokale
  Images, siehe [ADR 0017](0017-release-branches-and-version-script.de.md)).
- Frontend und Backend werden vor dem Buildpack gebaut; die Buildpack-Gruppe ist
  durch feste Versionsangaben (nginx 1.3.8) und `--trust-builder` zu pflegen.
- Der `.trivyignore` kann Funde bis zum Upstream-Fix überdecken und muss gepflegt werden.
- Für lokale Builds ist die `pack`-CLI nötig.

## Alternativen (nicht dokumentiert, naheliegend)

- **Dockerfile mit Multi-Stage-Build** (bis 2026-11-22 neben Paketo vorhanden, dann
  entfernt).
- **Jib** oder Spring-Boot-`build-image`.

## Verwandte Dokumente

- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`backend/build-paketo.sh`](../../backend/build-paketo.sh)
- [`frontend/build-paketo.sh`](../../frontend/build-paketo.sh)
- [`backend/project.toml`](../../backend/project.toml)
- [`frontend/project.toml`](../../frontend/project.toml)
- [`default.conf`](../../frontend/default.conf)
- [`trivy-scan-local.sh`](../../trivy-scan-local.sh)
- [README, Abschnitt Deployment](../../README.md)
