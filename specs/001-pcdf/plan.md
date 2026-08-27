# Implementation Plan: PCDF (Programmatic Content Delivery Framework)

**Branch**: `cursor/pcdf-specify-9319` (`001-pcdf`) | **Date**: 2026-08-22 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-pcdf/spec.md`

## Summary

Authors would manage locale-scoped promotions as **one Content Fragment each** (no variations). Consumer apps call **Publish** `GET /services/aem-poc/pcdf`. Response is **one winner** or `{ "contentFound": false }`. Preview with `previewDate` is **signed-in Author only** and considers **published** fragments only. Java is **21** at the parent POM; runtime is local **AEM 6.5 LTS SP2**. Code in **`core.pcdf`**; OSGi runmode config in **`ui.config.pcdf`**; teammates install **`com.aem.poc.pcdf`** without default `core`.

**Backlog:** [tasks.md](./tasks.md) is the implementation list. `/speckit-implement` stays opt-in (does not start until requested).

## Technical Context

**Language/Version**: **Java 21** (parent POM `maven-compiler-plugin` `release` 21; enforcer JDK 21)

**Primary Dependencies**: Local **AEM 6.5 LTS SP2**; OSGi DS, Sling servlets, Content Fragments, FileVault

**Storage**: JCR — CF model under `/conf/aem-poc-pcdf`; fragments under `/content/dam/aem-poc/pcdf/{region}/{country}/{locale}/`; tags under `/content/cq:tags/pcdf`

**Testing**: Optional; not an acceptance gate. Demo via [quickstart.md](./quickstart.md) when implemented

**Target Platform**: Local **AEM 6.5 LTS SP2** Author `localhost:4502` and Publish `localhost:4503` (not Cloud-only)

**Project Type**: AEM multi-module Maven POC (OSGi bundle + FileVault packages + JSON delivery)

**Performance Goals**: Winner selection for the documented sample set under 100ms on the local demo pair (SC-005)

**Constraints**: No PCDF in default `core` or `/apps/aem-poc`; OSGi config in `ui.config.pcdf` not default `ui.config`; no CF variations; date-only schedule; no CDN/Akamai implementation; no test-suite gate; secrets not committed

**Scale/Scope**: Sample promotions for match / no-match / future preview; not a production personalisation platform

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
| --- | --- | --- |
| I. POC-scoped, hypothesis + non-goals | Pass | Spec problem/goal and Out of Scope |
| II. Visible outcome | Pass (deferred to a later implementation) | Intended: CF in DAM + JSON on Publish. This workstream delivers spec/plan only |
| III. Documentation first; tests optional | Pass | Spec, plan, quickstart (validation guide for a future build); no coverage gate |
| IV. Install / demo / expected output | Pass | [quickstart.md](./quickstart.md) |
| V. Simplicity and AEM fit | Pass with justified extra modules | See Complexity Tracking — isolation **is** the POC |
| Content/code split | Pass | Java in `core.pcdf`; apps in `ui.apps.pcdf`; sample/conf in `ui.content.pcdf`; runmode OSGi in `ui.config.pcdf` |
| Prefer HTL / Sling Models / OSGi / clientlibs | Pass | Authoring uses stock CF editor; delivery is a Sling servlet. No new SPA. Deviation: extra modules under `/apps/aem-poc/pcdf`, documented |
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
└── tasks.md             # /speckit-tasks backlog; execute only when implementation is in scope
```

### Source Code (repository root)

```text
pom.xml                          # Java 21; add modules: core.pcdf, ui.apps.pcdf, ui.content.pcdf, ui.config.pcdf, pcdf
core/                            # UNCHANGED — no PCDF classes
core.pcdf/
  src/main/java/com/aem/poc/pcdf/
ui.apps.pcdf/
  src/main/content/jcr_root/apps/aem-poc/pcdf/
ui.config.pcdf/
  src/main/content/jcr_root/apps/aem-poc/pcdf/config/
  src/main/content/jcr_root/apps/aem-poc/pcdf/config.publish/  # anonymous GET for /services/aem-poc/pcdf
ui.content.pcdf/
  src/main/content/jcr_root/conf/aem-poc/pcdf/
  src/main/content/jcr_root/content/cq:tags/pcdf/
  src/main/content/jcr_root/content/dam/aem-poc/pcdf/{region}/{country}/{locale}/
pcdf/                            # container package com.aem.poc.pcdf
  embed: core.pcdf + ui.apps.pcdf + ui.content.pcdf + ui.config.pcdf
all/                             # optional embed of pcdf; not the isolation proof
ui.config/                       # default site configs — no PCDF PIDs
ui.frontend/                     # not used for this POC
ui.frontend.react/               # not used for this POC
```

**Structure Decision**: Keep the archetype tree. Add PCDF-specific modules so the shareable container does not contain `aem-poc.core` or `/apps/aem-poc/components`. Runmode config uses **`ui.config.pcdf`** (constitution `ui.config` split, PCDF-only zip). Logical processing: validate request → QueryBuilder path `{region}/{country}/{locale}` (+ optional CF `name`/`promo` nodename + ACTIVE/brand/topic tags) → missing folder = no-match → load **published** hits → targeting dates → remaining lists → **first** remaining result → JSON.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Extra Maven modules (`core.pcdf`, `ui.apps.pcdf`, `ui.content.pcdf`, `ui.config.pcdf`, `pcdf`) | PCDF-only zip + constitution config split | Putting PCDF in default `core` / `ui.apps` / `ui.config` prevents independent install |
| Extra package embed under `/apps/aem-poc-packages/pcdf` | Same install path for `all` and the PCDF zip | A second package root (`aem-pocs-pcdf-packages`) installed the bundle twice |
