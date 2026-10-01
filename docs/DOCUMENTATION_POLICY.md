# Android Documentation Policy

**English canonical** | [한국어](DOCUMENTATION_POLICY.ko.md)

> Version: v1.3.5-security
> Status: current app documentation governance

## 1. Authority order

1. Web/server authoritative `PROJECT_PLAN.md`.
2. Web/server `INTEGRATED_PLANNING_MASTER.md`.
3. Web/server detailed specs explicitly adopted by those documents.
4. Exact Android source and generated/runtime API evidence for app-specific implementation facts.
5. This repository's maintained app documentation.
6. Historical app guides, root execution plans and update records.

App documentation cannot authorize a product behavior that the web authoritative planning explicitly blocks or supersedes.

## 2. Change workflow

Meaningful app documentation changes use a dedicated branch. Before editing, re-read current web authority, app operating instructions and relevant app snapshots. Recheck both web and app `main` during work and before integration.

Documentation-only changes do not imply an APK build, Test deployment, backend verification or Production release.

## 3. Language

English is canonical. Korean is the required second language for newly maintained app governance/specification documentation. Historical public records may remain unpaired when they are clearly classified as history. Internal-only records must not be committed to a public repository and belong in approved private storage.

## 4. Status and evidence

Use `DRAFT`, `PLANNING`, `IMPLEMENTED`, `TEST_VERIFIED`, `PRODUCTION_VERIFIED`, `HISTORICAL`, `SUPERSEDED`, or `AUTHORITY_DRIFT`.

Implementation claims should identify an exact app commit and, when API/backend behavior matters, the exact compatible web/backend candidate. Compile success alone is not end-to-end runtime proof.

## 5. Legacy classification

- `docs/APP_SPEC_AND_USER_GUIDE.ko.md`: historical v2026.09.22.343 snapshot unless explicitly reconciled.
- `implementation_plan.md`: execution scratchpad/history, not canonical product authority.
- `docs/api/`: integration snapshots, subordinate to current generated/runtime contract and web planning authority.
- `docs/updates/`: public historical version/change evidence. Internal update records are prohibited in the public tracked tree.

Do not delete these merely for cleanup. Prefer status banners and current indexes so old links remain valid.
