# ADR 0018: Dependency updates with Renovate and controlled auto-merge

> [Deutsch](0018-renovate-and-auto-merge.de.md) · **English**

- **Status:** accepted
- **Date:** 2026-05-21
- **Context:** Commit `7049651c` („Renovate Bot konfigurieren (ersetzt Dependabot)“); auto-merge workflow since 2026-06-24 (`301d2fe5`)

## Context

Backend (Maven) and frontend (pnpm, Nx) have many dependencies; vulnerabilities in
transitive packages appeared several times in `CHANGELOG.md` (among others `qs`, `tmp`). Until May 2026
the repository used Dependabot; Renovate replaced it on 2026-05-21. The
commit message gives no further rationale.

## Decision

1. **Renovate** (`renovate.json`, base `config:recommended`) creates the update PRs.
   Schedule: on weekends, time zone Europe/Berlin; `minimumReleaseAge` 1 day; label
   `renovate`; dependency dashboard enabled.

2. **Security updates immediately.** `vulnerabilityAlerts` are enabled, with label
   `security`, and without a minimum age for the version.

3. **Major updates only with approval.** Rule `matchUpdateTypes: major` with
   `dependencyDashboardApproval`; the PRs carry the label `major-update`.

4. **Exceptions with rationale in the rule.** The Nx packages are disabled
   as long as `@nx/vue` does not support `vue-router` 5 and `vue-tsc` 3; `undici` stays below
   version 8 (jsdom 29 needs a file that is missing in 8); the `ajv@^6` override in
   `pnpm-workspace.yaml` is not raised (ESLint 10 uses the ajv 6 API).

5. **Auto-merge via GitHub workflow.** `auto-merge-deps.yml` runs every four hours
   (`0 2,6,10,14,18,22 * * *`) and merges open PRs with the labels `renovate` or
   `dependencies` if they are mergeable and all checks have passed. PRs with the label
   `major-update` are never merged automatically. A
   merge commit is used for merging; the branch is deleted. The check here is the CI build, not
   a test run ([ADR 0006](0006-test-strategy.md)).

6. **Notifications.** The workflow reports by email CI failures on dependency PRs
   and open security PRs, plus a daily summary of pending approvals at
   16:00 Berlin time.

7. **Daily patch release.** If there were merges that day, the workflow triggers exactly one
   patch release ([ADR 0017](0017-release-branches-and-version-script.md)).

## Consequences

**Positive**

- Dependencies stay current; security PRs do not wait for a minimum age and are
  reported by email; major jumps require a deliberate decision.
- The exceptions are recorded with their rationale in `renovate.json`.

**Negative / costs**

- Since CI only compiles, auto-merge essentially depends on a successful
  build; a functional error caused by an update is noticed only later.
- Every dependency change can produce a release and thus a rollout.
- The pinning of versions (e.g. `openvue` exact) and overrides in
  `pnpm-workspace.yaml` have to be maintained.
- The workflow requires secrets for the email (not contained in the repository).

## Alternatives (not documented, obvious candidates)

- **Dependabot** (in use until 2026-05-21; the workflow still handles the label
  `dependencies`).
- **Manual updates only.**

## Related documents

- [`renovate.json`](../../renovate.json)
- [`auto-merge-deps.yml`](../../.github/workflows/auto-merge-deps.yml)
- [`CHANGELOG.md`](../../CHANGELOG.md)
- [`pnpm-workspace.yaml`](../../frontend/pnpm-workspace.yaml)
- [`bump-version.sh`](../../bump-version.sh)
