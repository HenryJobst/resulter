# ADR 0020: License CC BY-NC-ND 4.0

> [Deutsch](0020-license-cc-by-nc-nd-4-0.de.md) · **English**

- **Status:** accepted
- **Date:** 2023-12-16
- **Context:** Commit `433c42ba` („LICENSE.txt hinzugefügt“)

## Context

The source code of Resulter is on GitHub (`HenryJobst/resulter`, as stated in the
README; visibility not checked). Two weeks
after the first commit, a license file was added. The commit message gives no
reason; the rationale comes from the project owner.

## Decision

1. **Resulter is licensed under Creative Commons Attribution-NonCommercial-NoDerivatives
   4.0 International (CC BY-NC-ND 4.0).** The full license text is in
   `LICENSE.txt`; the README shows a license badge and states
   „Resulter © 2023-2026 by Henry Jobst“.

2. **Contributions by arrangement.** The README asks that contributions be agreed beforehand, to
   avoid duplicate development.

3. **The license applies to the project's own code, not to bundled third-party components.**
   Fonts under the SIL Open Font License and the Apache License 2.0 keep their
   licenses ([ADR 0011](0011-certificate-fonts.md)); dependencies have their own
   licenses.

Rationale for choosing this license (project owner): it is meant to prevent a commercial
clone.

## Consequences

**Positive**

- Clear terms of use: redistribution with attribution, non-commercial, without
  modifications.

**Negative / costs**

- CC BY-NC-ND is not an open source license recognized by the Open Source Initiative;
  forks with changes are therefore generally not permitted.
- Creative Commons does not recommend its licenses for software; they contain no
  provisions on patents or liability of the kind software licenses usually
  contain. (General information, not documented in the repository.)
- Contributions from third parties require a separate clarification of rights.

## Alternatives (not documented, obvious candidates)

- **An open source license** (MIT, Apache 2.0, AGPL).
- **A software license with a non-commercial clause** (e.g. PolyForm Noncommercial).

## Related documents

- [`LICENSE.txt`](../../LICENSE.txt)
- [README, License section](../../README.md)
- [ADR 0011: Bundled certificate fonts](0011-certificate-fonts.md)
