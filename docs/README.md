# Woldeok Moneyverse Android Documentation

**DOC-2026.11.11.1500 — documentation reference refresh.** App source `ee97ddfc5a93d7198d2d1ee94bb5a13c3b62ebcb` verified 2026-11-11; [web/server product authority](https://github.com/wtrdd1-hash/Woldeok-Moneyverse-Migration/blob/main/docs/planning/PROJECT_PLAN.md) remains independent. No APK/API/Production verification implied.


**English canonical** | [한국어](README.ko.md)

## Authority

Product/system authority lives in the web/server repository:

1. `wtrdd1-hash/Woldeok-Moneyverse-Migration/docs/planning/PROJECT_PLAN.md`
2. `wtrdd1-hash/Woldeok-Moneyverse-Migration/docs/planning/INTEGRATED_PLANNING_MASTER.md`
3. Explicitly adopted detailed planning specifications in that repository.

This Android repository may document native implementation details, build behavior and API consumption, but it must not silently override the product, economy, safety, legal, API or release authority above.

## Current app documentation state

- Baseline app `main`: `ee97ddfc5a93d7198d2d1ee94bb5a13c3b62ebcb`
- Documentation organization version: `v1.0.20-docs`
- [Documentation policy](DOCUMENTATION_POLICY.md)
- [Project operating instructions](PROJECT_OPERATING_INSTRUCTIONS.md)
- [API snapshots](api/README.md)
- [Update history](updates/)

## Historical / execution references

- `APP_SPEC_AND_USER_GUIDE.ko.md` is a **historical specification snapshot** (v2026.09.22.343). It contains assumptions that may no longer match current web authority, including older casino/runtime/database descriptions.
- Repository-root `implementation_plan.md` is an execution/planning scratchpad and accumulated implementation history. It is not product authority by itself.
- API files under `docs/api/` are integration snapshots. When they conflict with current generated/runtime contracts or the web authoritative plan, follow the current web authority and exact runtime evidence.

## Language

New maintained app documentation uses English as canonical and Korean as the required second language. Historical/internal records are preserved without pretending that retroactive translation makes them current authority.
