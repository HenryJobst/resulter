# ADR 0017: Releases via release branches and version script

> [Deutsch](0017-release-branches-and-version-script.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-05-22
- **Context:** Commit `4d4d85cd` („add version bump and release branch scripts“); pre-push hook with Trivy since 2026-06-07

## Context

Container images are built from release branches and tags
([ADR 0016](0016-container-images-with-paketo-buildpacks.md)). The version appears
in several places in the repository, and a rollout should only happen after a security scan.
Delivery is outside this repository.

## Decision

1. **Semantic versioning.** `CHANGELOG.md` follows „Keep a Changelog“ and
   „Semantic Versioning“. The authoritative version is in the root `pom.xml`.

2. **Version changes only via the script.** `bump-version.sh major|minor|patch` reads the
   version from the root `pom.xml`, replaces it in `pom.xml`, `backend/pom.xml`,
   `frontend/package.json`, both `project.toml` files, `application.properties`
   (`openapi.config.version`) and the README badge, and creates the commit
   `feat: bump version to X.Y.Z`. The script aborts on uncommitted changes.

3. **Release branch instead of tag as trigger.** `create-release-branch.sh` creates the branch
   `releases/vX.Y.Z` from `main` and pushes it; the workflow `build-paketo.yml` builds
   the images from it with the tag `paketo-X.Y.Z`.

4. **Pre-push hook with Trivy.** `simple-git-hooks` (configured in
   `frontend/package.json`) runs `pre-push-trivy.sh` before every push. It scans only on
   pushes to `refs/heads/releases/*`: it builds the images locally and checks them with
   `trivy-scan-local.sh`, which mirrors the CI steps (about one minute with existing images,
   otherwise around ten minutes according to the script).

5. **Daily automatic patch release.** After dependency merges, the workflow `auto-merge-deps.yml`
   raises the patch version once per day (from 15:00 Berlin time)
   with `bump-version.sh patch`, pushes `main` and creates the release branch with
   `create-release-branch.sh` ([ADR 0018](0018-renovate-and-auto-merge.md)).

6. **Delivery is outside this repository.** The process here ends with the
   published images `paketo-X.Y.Z`. How and where they are rolled out is not
   part of this repository and is deliberately not described here.

## Consequences

**Positive**

- One source for the version, one script for all places; no manual editing.
- Images from release branches were scanned locally before the push and again in CI.

**Negative / costs**

- After the release, a further step outside this repository is needed for the
  new version to go live.
- The pre-push hook takes several minutes and needs Docker, `pack` and `trivy`.
- Delivery is not described in this repository.
- The hook configuration is in `frontend/package.json`, the script in the root directory.

## Alternatives (not documented, obvious candidates)

- **Release via Git tag** (the workflow also reacts to `v*` tags).

## Related documents

- [`bump-version.sh`](../../bump-version.sh)
- [`create-release-branch.sh`](../../create-release-branch.sh)
- [`pre-push-trivy.sh`](../../pre-push-trivy.sh)
- [`trivy-scan-local.sh`](../../trivy-scan-local.sh)
- [`build-paketo.yml`](../../.github/workflows/build-paketo.yml)
- [`auto-merge-deps.yml`](../../.github/workflows/auto-merge-deps.yml)
- [`CHANGELOG.md`](../../CHANGELOG.md)
- [`package.json` of the frontend](../../frontend/package.json)
