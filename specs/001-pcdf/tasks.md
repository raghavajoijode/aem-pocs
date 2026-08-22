---
description: "Task list for PCDF implementation"
---

# Tasks: PCDF (Programmatic Content Delivery Framework)

**Input**: Design documents from `/specs/001-pcdf/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/delivery-api.yaml](./contracts/delivery-api.yaml)

**Tests**: Not requested. Constitution and spec treat tests as optional; this list has **no** automated-test tasks.

**Runtime**: Java 21 (parent POM), local **AEM 6.5 LTS SP2** (Author `4502`, Publish `4503`). Delivery: Publish `GET /services/aem-pocs/pcdf`.

**Workstream note**: Implemented in `core.pcdf`, `ui.apps.pcdf`, `ui.content.pcdf`, `ui.config.pcdf`, and `pcdf`.

**Organization**: Tasks are grouped by user story so each story can be implemented, demoed, and handed off independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1–US5)
- Every task includes an exact file path

## Path Conventions

PCDF modules from plan.md: `core.pcdf/`, `ui.apps.pcdf/`, `ui.content.pcdf/`, `ui.config.pcdf/`, `pcdf/`. Default `core/`, `ui.config/`, and `/apps/aem-poc` stay free of PCDF.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add PCDF Maven modules without putting code in default `core`

- [X] T001 Register modules `core.pcdf`, `ui.apps.pcdf`, `ui.content.pcdf`, `ui.config.pcdf`, and `pcdf` in `/workspace/pom.xml`
- [X] T002 Create OSGi bundle module `/workspace/core.pcdf/pom.xml` (inherits parent Java 21, Bundle-SymbolicName `com.aem.poc.pcdf`, artifact `aem-poc.core.pcdf`) modeled on `/workspace/core/pom.xml` with no PCDF classes under `/workspace/core/`
- [X] T003 [P] Create apps package `/workspace/ui.apps.pcdf/pom.xml` and `/workspace/ui.apps.pcdf/src/main/content/META-INF/vault/filter.xml` rooted at `/apps/aem-pocs/pcdf`
- [X] T004 [P] Create content package `/workspace/ui.content.pcdf/pom.xml` and `/workspace/ui.content.pcdf/src/main/content/META-INF/vault/filter.xml` for `/conf/aem-pocs/pcdf` and `/content/dam/aem-pocs/pcdf`
- [X] T039 [P] Create config package `/workspace/ui.config.pcdf/pom.xml` and `/workspace/ui.config.pcdf/src/main/content/META-INF/vault/filter.xml` rooted at `/apps/aem-pocs/pcdf/osgiconfig` (not `/apps/aem-poc/osgiconfig`)
- [X] T005 Create container package `/workspace/pcdf/pom.xml` (FileVault group/name `com.aem.poc.pcdf`) embedding `aem-poc.core.pcdf`, `aem-poc.ui.apps.pcdf`, `aem-poc.ui.content.pcdf`, and `aem-poc.ui.config.pcdf` only (not `aem-poc.core`, `aem-poc.ui.apps`, or `aem-poc.ui.config`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Skeleton delivery path, CF model location, and Publish anonymous GET — required before story work

**⚠️ CRITICAL**: No user story work until this phase is complete

- [X] T006 Add Content Fragment model skeleton `ProgrammaticPromotion` at `/workspace/ui.content.pcdf/src/main/content/jcr_root/conf/aem-pocs/pcdf/settings/dam/cfm/models/programmatic-promotion/.content.xml`
- [X] T007 [P] Add Publish runmode anonymous access for `/services/aem-pocs/pcdf` in `/workspace/ui.config.pcdf/src/main/content/jcr_root/apps/aem-pocs/pcdf/osgiconfig/config.publish/` (sling.auth.requirements or equivalent; do not add PCDF PIDs under `/workspace/ui.config/`)
- [X] T008 [P] Add path servlet `GET /services/aem-pocs/pcdf` in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java`
- [X] T009 Return JSON errors (`locale_required`) from `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java` per `/workspace/specs/001-pcdf/contracts/delivery-api.yaml`
- [X] T010 Add calendar-date evaluation helper (no time) in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/eligibility/DateWindow.java`
- [X] T011 Add CF-to-promotion mapping in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/model/Promotion.java`

**Checkpoint**: Foundation ready — user stories can proceed

---

## Phase 3: User Story 1 - Author a promotion without developers (Priority: P1) 🎯 MVP

**Goal**: Authors create one `ProgrammaticPromotion` CF under a locale folder (date-only `startDate`/`endDate`, no variations) and an eligible published fragment can be delivered

**Independent Test**: On Author, create or open a CF under `/content/dam/aem-pocs/pcdf/en-us`, publish it, then request Publish `GET /services/aem-pocs/pcdf?locale=en-us` and see that fragment when ACTIVE and in date window

### Implementation for User Story 1

- [X] T012 [US1] Complete CFM fields (`promotionId`, `status`, `priority`, `tags`, `headline`, `body`, `image`, `ctaText`, `ctaLink`, `startDate`, `endDate`, targeting lists) in `/workspace/ui.content.pcdf/src/main/content/jcr_root/conf/aem-pocs/pcdf/settings/dam/cfm/models/programmatic-promotion/.content.xml`
- [X] T013 [P] [US1] Add locale folder `/workspace/ui.content.pcdf/src/main/content/jcr_root/content/dam/aem-pocs/pcdf/en-us/.content.xml`
- [X] T014 [US1] Add sample fragment `pcdf-match-high` under `/workspace/ui.content.pcdf/src/main/content/jcr_root/content/dam/aem-pocs/pcdf/en-us/` per `/workspace/specs/001-pcdf/data-model.md`
- [X] T015 [US1] Query **published** fragments in the locale folder in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/service/PromotionQueryService.java` (missing folder → empty list / no-match)
- [X] T016 [US1] Apply ACTIVE + inclusive `startDate`/`endDate` in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/eligibility/EligibilityService.java`
- [X] T017 [US1] Emit match JSON (`contentFound`, `promotionId`, `headline`, `body`, `image`, `ctaText`, `ctaLink`) from `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java`
- [X] T018 [US1] Document Author CF path and date-only fields in `/workspace/docs/pcdf.md`

**Checkpoint**: US1 demoable independently (author + one eligible delivery)

---

## Phase 4: User Story 2 - Consumer gets one winner or no content (Priority: P1)

**Goal**: Request locale + optional context; return exactly one winner or `{ "contentFound": false }` with no fallback

**Independent Test**: Sample set — `locale=en-us&country=US` → `pcdf-match-high`; `country=CA` → `contentFound: false`; two ACTIVE US-targeted fragments differ only by priority

### Implementation for User Story 2

- [X] T019 [US2] Implement targeting (empty list = match-all; omitted request param unconstrained; `promo` → `urlParameters`; `tag` exact inclusion) in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/eligibility/TargetingRules.java` without adding new request/response fields (FR-024: later dimensions via model/rules only)
- [X] T020 [US2] Rank by highest integer `priority` then lexicographically lower `promotionId` (per locale) in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/eligibility/Ranking.java`
- [X] T021 [US2] Return no-match body with `contentFound: false` and no substitute fields in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java` (including unknown locale folder)
- [X] T022 [P] [US2] Add samples `pcdf-match-low` and `pcdf-inactive` under `/workspace/ui.content.pcdf/src/main/content/jcr_root/content/dam/aem-pocs/pcdf/en-us/` per `/workspace/specs/001-pcdf/data-model.md`
- [X] T023 [US2] Reject missing `locale` with HTTP 400 `locale_required` in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java`

**Checkpoint**: US1 and US2 independently demoable (winner vs no-match)

---

## Phase 5: User Story 3 - Hand off only the PCDF package (Priority: P1)

**Goal**: One zip `com.aem.poc.pcdf` installs apps, conf, DAM samples, and `core.pcdf` without default site or default `core`

**Independent Test**: Install only `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip` on a clean AEM LTS Author (and Publish); `/apps/aem-pocs/pcdf` present; `/apps/aem-poc` and `aem-poc.core` not required

### Implementation for User Story 3

- [X] T024 [US3] Embed only PCDF artifacts in `/workspace/pcdf/pom.xml` and `/workspace/pcdf/src/main/content/META-INF/vault/filter.xml` (include `ui.config.pcdf`)
- [X] T025 [US3] Confirm `/workspace/ui.apps.pcdf/src/main/content/META-INF/vault/filter.xml` and `/workspace/ui.content.pcdf/src/main/content/META-INF/vault/filter.xml` do not include `/apps/aem-poc`
- [X] T026 [US3] Document `mvn clean install -pl pcdf -am` and zip coordinates in `/workspace/docs/pcdf.md`
- [X] T027 [US3] Keep `/workspace/all/pom.xml` from making PCDF isolation depend on the site `all` package (optional embed is convenience only)

**Checkpoint**: Teammate can install PCDF-only zip

---

## Phase 6: User Story 4 - Preview by date and context (Priority: P2)

**Goal**: Signed-in Author may pass `previewDate` (YYYY-MM-DD); Publish always uses today and rejects preview

**Independent Test**: Author `previewDate=2026-10-15` returns `pcdf-future`; Publish without preview does not; Publish with `previewDate` is HTTP 400 `preview_not_allowed`

### Implementation for User Story 4

- [X] T028 [US4] Use `previewDate` as evaluation date only for authenticated Author, considering **published** fragments only, in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java`
- [X] T029 [US4] Reject `previewDate` on Publish with HTTP 400 `preview_not_allowed` in `/workspace/core.pcdf/src/main/java/com/aem/poc/pcdf/internal/servlet/PromotionDeliveryServlet.java`
- [X] T030 [P] [US4] Add sample `pcdf-future` under `/workspace/ui.content.pcdf/src/main/content/jcr_root/content/dam/aem-pocs/pcdf/en-us/` per `/workspace/specs/001-pcdf/data-model.md`
- [X] T031 [US4] Keep Author delivery authenticated (401 without session) via `/workspace/ui.config.pcdf/src/main/content/jcr_root/apps/aem-pocs/pcdf/osgiconfig/config.author/` (no anonymous sling.auth exemption)

**Checkpoint**: Preview Author-only; Publish live unchanged

---

## Phase 7: User Story 5 - Reuse across experiences, not pages (Priority: P3)

**Goal**: Promotions live only under locale DAM folders; two request contexts can share the same winner; no page campaign wiring

**Independent Test**: Two Publish requests with the same locale and matching targeting return the same `promotionId`; no page under this POC has a campaign property pointing at a fragment

### Implementation for User Story 5

- [X] T032 [US5] Ensure `/workspace/ui.content.pcdf/src/main/content/jcr_root/` contains no page/component campaign bindings to promotions
- [X] T033 [US5] Add a second matching request example in `/workspace/specs/001-pcdf/quickstart.md`
- [X] T034 [US5] State in `/workspace/docs/pcdf.md` that authors do not configure campaigns on individual pages

**Checkpoint**: All five stories independently demoable

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Docs and demo alignment across stories (no production hardening)

- [X] T035 [P] Add PCDF hypothesis, modules, install, demo, expected JSON, and cleanup to `/workspace/README.md`
- [X] T036 Align `/workspace/specs/001-pcdf/quickstart.md` with built zip path, JDK 21, and AEM 6.5 LTS SP2
- [X] T037 [P] Document CDN cache-key `locale|country|brand|promo` as external recommendation only in `/workspace/docs/pcdf.md`
- [X] T038 Walk `/workspace/specs/001-pcdf/quickstart.md` on local AEM 6.5 LTS SP2 Author and Publish and record expected output in `/workspace/docs/pcdf.md`
- [X] T040 Time sample-set winner selection on the local pair and record that it is under 100ms in `/workspace/docs/pcdf.md` (SC-005; not a test-suite gate)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User stories (Phases 3–7)**: Depend on Foundational; then P1 stories (US1, US2, US3) before P2/P3
- **Polish (Phase 8)**: After the stories you intend to demo

### User Story Dependencies

- **US1 (P1)**: After Phase 2 — MVP (author + one eligible delivery)
- **US2 (P1)**: After Phase 2; uses US1 servlet/query; independently testable with sample set
- **US3 (P1)**: After Phase 1 modules exist; can proceed in parallel with US1/US2 once artifacts exist to embed
- **US4 (P2)**: After US2 ranking/eligibility (preview uses the same rules)
- **US5 (P3)**: After US2 (reuse is two requests, not new ranking)

### Within Each User Story

- Model/content before query
- Eligibility before JSON response
- Samples before demo documentation
- No automated tests unless later requested

### Parallel Opportunities

- T003, T004, and T039 after T001
- T007 and T008 after Phase 1
- T013 with other content work after T012
- T022 and T030 sample fragments in parallel
- T035 and T037 in polish
- US3 packaging vs US1/US2 Java after modules exist

---

## Parallel Example: User Story 1

```bash
# After T012 (model fields), content folder and mapping can proceed:
Task: "Add locale folder ui.content.pcdf/.../content/dam/aem-pocs/pcdf/en-us/.content.xml"
Task: "Add CF-to-promotion mapping already in Phase 2 Promotion.java — extend fields if needed"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 Setup  
2. Phase 2 Foundational  
3. Phase 3 US1  
4. **STOP**: Author one CF, publish, hit Publish GET with `locale=en-us`  
5. Then US2 (winner vs no-match) before calling the POC “delivery complete”

### Incremental Delivery

1. Setup + Foundational  
2. US1 → author + one delivery  
3. US2 → ranking / no-match  
4. US3 → PCDF-only zip  
5. US4 → Author preview  
6. US5 → reuse docs / no page wiring  
7. Polish / quickstart on AEM LTS  

### Suggested MVP scope

**US1 + US2 + US3** (all P1): author without developers, correct winner/no-match, install-only-PCDF package. US4/US5 after that.

---

## Notes

- [P] = different files, no incomplete-task dependency
- Do not add PCDF to `/workspace/core/` or `/apps/aem-poc`
- Do not add unit/integration test suites as acceptance
- Do not implement Akamai/CDN
- Commit after each task or logical group when implementing
- `/speckit-implement` will treat unchecked `checklists/*.md` as a gate unless you proceed anyway
