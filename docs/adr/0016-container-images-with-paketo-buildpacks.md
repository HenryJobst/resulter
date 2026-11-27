# ADR 0016: Container images with Paketo Buildpacks

> [Deutsch](0016-container-images-with-paketo-buildpacks.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-01-23
- **Context:** Commit `222c5878` („add Paketo Buildpacks support with fixes for permissions“)

## Context

Backend and frontend are delivered as container images. The commit of
2026-01-23 introduced Cloud Native Buildpacks (Paketo) „as alternative build method“. On 2026-11-22
commit `3ae26395` („remove legacy Dockerfile image build, keep Paketo
only“) removed the Dockerfiles of backend and frontend; since then Paketo is the only way. The README lists these advantages: 12-factor apps,
configuration at runtime, automatic SBOM generation, non-root users. The project owner also states in general that Paketo has several
advantages over custom Dockerfiles.

## Decision

1. **Images are built with `pack` and the builder `paketobuildpacks/builder-jammy-base`.**
   - Backend: buildpack `paketo-buildpacks/java`, Java 21 (`BP_JVM_VERSION`); the
     JAR is built beforehand with Maven (`./mvnw clean install -Dmaven.test.skip=true`) to avoid
     permission problems when building from source.
   - Frontend: it is built locally with pnpm; the buildpack
     `paketo-buildpacks/nginx` (version 1.3.8 pinned) packages `dist/` with the
     configuration `default.conf`.
   Locally, `backend/build-paketo.sh` and `frontend/build-paketo.sh` take care of this.

2. **Configuration comes at runtime.** Credentials and hosts (OAuth2, database)
   are not part of the image but are passed as environment variables at startup.

3. **CI builds and publishes the images.** `.github/workflows/build-paketo.yml` runs
   on tags `v*` and on branches `releases/**` (and manually). There is a
   quality gate for backend and frontend (compile or `pnpm build`), then the build
   of the two images. They are published to the registry `ghcr.io` under
   `<repository>/backend` and `<repository>/frontend`.

4. **Security check before publishing.** Trivy scans the image
   (`--severity HIGH,CRITICAL --exit-code 1 --ignore-unfixed`) with one
   `.trivyignore` each for backend and frontend; known findings are listed there with a rationale and
   a reference to the upstream fix. Trivy also produces an SBOM in CycloneDX format.

5. **Tag scheme.** For a release branch `releases/vX.Y.Z` the image gets the tag
   `paketo-X.Y.Z` (plus `paketo-X` and `paketo-X.Y` if a version is given);
   additionally `paketo-latest` and `paketo-<commit SHA>`.

## Consequences

**Positive**

- No Dockerfile to maintain; the base image is updated by the buildpack
  provider.
- Every published image was checked for HIGH/CRITICAL vulnerabilities and has an SBOM.

**Negative / costs**

- The build takes long (the pre-push hook mentions „~10 Min“ without existing local
  images, see [ADR 0017](0017-release-branches-and-version-script.md)).
- Frontend and backend are built before the buildpack; the buildpack group has
  to be maintained because of pinned versions (nginx 1.3.8) and `--trust-builder`.
- The `.trivyignore` can hide findings until the upstream fix and has to be maintained.
- Local builds require the `pack` CLI.

## Alternatives (not documented, obvious candidates)

- **Dockerfile with multi-stage build** (present alongside Paketo until 2026-11-22, then
  removed).
- **Jib** or Spring Boot `build-image`.

## Related documents

- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`backend/build-paketo.sh`](../../backend/build-paketo.sh)
- [`frontend/build-paketo.sh`](../../frontend/build-paketo.sh)
- [`backend/project.toml`](../../backend/project.toml)
- [`frontend/project.toml`](../../frontend/project.toml)
- [`default.conf`](../../frontend/default.conf)
- [`trivy-scan-local.sh`](../../trivy-scan-local.sh)
- [README, Deployment section](../../README.md)
