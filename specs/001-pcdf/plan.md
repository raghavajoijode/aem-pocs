# Implementation Plan: PCDF (Programmatic Content Delivery Framework)

**Branch**: `cursor/pcdf-specify-9319` (`001-pcdf`) | **Date**: 2026-08-22 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-pcdf/spec.md`

## Summary

**Design only** — this plan is not a commitment to implement in this workstream (`/speckit-tasks` / `/speckit-implement` are out of scope until explicitly requested).

Authors would manage locale-scoped promotions as **one Content Fragment each** (no variations). Consumer apps call **Publish** at the PCDF delivery surface from `docs/programmatic-content-delivery-requirements.md`: **`GET /services/aem-pocs/pcdf`**. Response is **one winner** or `{ "contentFound": false }`. Preview with `previewDate` is **signed-in Author only**. Java would live in **`core.pcdf`** (Java 21) on **local AEM LTS**; teammates would install **`com.aem.poc.pcdf`** without default `core` or `/apps/aem-poc`.

## Technical Context

**Language/Version**: **Java 21**; Maven 3.3.9+ (parent POM may still list older `source`/`target` until implementation is in scope)

**Primary Dependencies**: Local **AEM LTS**; OSGi DS, Sling servlets, Content Fragments, FileVault. Do not treat the archetype `uber-jar` `6.6.2` / Java 8 compiler settings as the runtime target.

**Storage**: JCR — CF model under `/conf/aem-pocs/pcdf`; fragments under `/content/dam/aem-pocs/pcdf/{locale}/`

**Testing**: Optional; not an acceptance gate. Demo via [quickstart.md](./quickstart.md) **when/if implemented**

**Target Platform**: Local **AEM LTS** Author `localhost:4502` and Publish `localhost:4503` (not Cloud-only)

**Project Type**: AEM multi-module Maven POC (OSGi bundle + FileVault packages + JSON delivery) — **specified, not built in this workstream**

**Performance Goals**: Winner selection for the documented sample set under 100ms on the local demo pair (SC-005), when implemented

**Constraints**: No PCDF in default `core` or `/apps/aem-poc`; no CF variations; date-only schedule; no CDN/Akamai implementation; no test-suite gate; secrets not committed; **no feature implementation in this workstream**

**Scale/Scope**: Sample promotions for match / priority / no-match / future preview; not a production personalisation platform

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
| --- | --- | --- |
| I. POC-scoped, hypothesis + non-goals | Pass | Spec problem/goal and Out of Scope |
| II. Visible outcome | Pass (deferred to a later implementation) | Intended: CF in DAM + JSON on Publish. This workstream delivers spec/plan only |
| III. Documentation first; tests optional | Pass | Spec, plan, quickstart (validation guide for a future build); no coverage gate |
| IV. Install / demo / expected output | Pass | [quickstart.md](./quickstart.md) |
| V. Simplicity and AEM fit | Pass with justified extra modules | See Complexity Tracking — isolation **is** the POC |
| Content/code split | Pass | Java in `core.pcdf`; apps in `ui.apps.pcdf`; sample/conf in `ui.content.pcdf` |
| Prefer HTL / Sling Models / OSGi / clientlibs | Pass | Authoring uses stock CF editor; delivery is a Sling servlet. No new SPA. Deviation: extra modules + `/apps/aem-pocs` umbrella, documented |
| AEMaaCS analyser not a release gate | Pass | Unused as gate |
| No committed secrets | Pass | `admin`/`admin` as local convention in docs only |

**Post-Phase 1 re-check**: Same gates pass. Contracts do not add CDN, test matrices, or frameworks. Extra modules remain the minimum to ship a PCDF-only zip.

## Project Structure

### Documentation (this feature)

```text
specs/001-pcdf/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── delivery-api.yaml
└── tasks.md             # /speckit-tasks — not created; implementation not in this workstream
```

### Source Code (repository root)

```text
pom.xml                          # add modules: core.pcdf, ui.apps.pcdf, ui.content.pcdf, pcdf
core/                            # UNCHANGED — no PCDF classes
core.pcdf/
  src/main/java/com/aem/poc/pcdf/
    # path servlet, query, eligibility/ranking (names not mandated)
  src/main/resources/
ui.apps.pcdf/
  src/main/content/jcr_root/apps/aem-pocs/pcdf/
    config.author/               # auth required (default) if any
    config.publish/              # anonymous GET for /services/aem-pocs/pcdf
ui.content.pcdf/
  src/main/content/jcr_root/conf/aem-pocs/pcdf/
  src/main/content/jcr_root/content/dam/aem-pocs/pcdf/{locale}/
pcdf/                            # container package com.aem.poc.pcdf
  embed: core.pcdf bundle + ui.apps.pcdf zip + ui.content.pcdf zip
all/                             # optional: may also embed pcdf for full-repo install; must not be the isolation proof
ui.frontend/                     # not used for this POC
ui.frontend.react/               # not used for this POC
```

**Structure Decision**: Keep the archetype tree **when implementing later**. Intended add: four PCDF-specific Maven modules so the shareable container does not contain `aem-poc.core` or `/apps/aem-poc`. Logical processing: validate request → locale DAM path → load `ProgrammaticPromotion` fragments → ACTIVE + date → targeting → highest priority / lowest `promotionId` → JSON. Servlet class names are not a spec mandate. **Do not create these modules in this workstream.**

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Extra Maven modules (`core.pcdf`, `ui.apps.pcdf`, `ui.content.pcdf`, `pcdf`) | Spec requires a PCDF-only package and a dedicated bundle | Putting code in `core` or nodes in `ui.apps` (`/apps/aem-poc`) makes independent install impossible |
| `/apps/aem-pocs` umbrella instead of `/apps/aem-poc` | Isolation identity for this and future POCs | Mixing into `/apps/aem-poc` is an explicit spec failure |
