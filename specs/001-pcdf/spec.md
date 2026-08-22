# Feature Specification: PCDF (Programmatic Content Delivery Framework)

**Feature Branch**: `001-pcdf`

**Created**: 2026-08-22

**Status**: Draft

**Input**: User description: "PCDF (Programmatic Content Delivery Framework). Authors create locale-scoped promotions once (content, schedule, targeting, priority). Consumer applications request the single winning promotion for a locale and context, or receive no content when nothing matches—without page-level campaign wiring or developer work per campaign. Isolated under the shared POCs umbrella so a teammate can install only PCDF."

## Problem / goal

Marketing teams need to author promotional content once and deliver it across many experiences (thousands of pages or consumer applications) with scheduling, targeting, and preview—without attaching campaigns to individual pages or waiting on development for each campaign.

This proof of concept proves that hypothesis: Author for authoring, Publish for delivery, sample content for a visible demo.

## Clarifications

### Session 2026-08-22

- Q: Should a Publish caller be allowed to pass a preview date on an unauthenticated delivery request, or is preview limited to signed-in Author users? → A: Preview is limited to a signed-in user on Author only. Publish live delivery always evaluates “now” and must not honor a preview date.
- Q: Must schedule fields carry timezone (global application)? → A: Desired for a global product, but **out of scope for this POC**.
- Q: When previewing on Author, is the preview value a date-time with timezone or a calendar date? How are start and end stored? → A: For this POC, `startDate` and `endDate` are **dates only (no time)**. Preview uses a **calendar date** (no time).
- Q: Should this POC’s Java live in a separate Maven module and bundle (`core.pcdf`) or inside the existing `core` module? → A: Separate `core.pcdf` module and bundle (`com.aem.poc.pcdf`); default `core` stays free of PCDF.
- Q: Runtime and workstream scope? → A: **Java 21** (parent POM). Local **AEM 6.5 LTS SP2** (Author `localhost:4502`, Publish `localhost:4503`). Delivery: Publish `GET /services/aem-poc/pcdf` per `docs/programmatic-content-delivery-requirements.md`. PCDF feature modules are a **backlog** (`tasks.md`); `/speckit-implement` is opt-in.
- Q: Unknown locale folder, unpublished preview, OSGi config module, `promotionId` uniqueness? → A: Missing DAM folder for `locale` → no-match (`contentFound` false). Author preview uses **published** fragments only. OSGi runmode config in **`ui.config.pcdf`**. Same `promotionId` **may** be reused across locales; uniqueness is **per locale folder**.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Author a promotion without developers (Priority: P1)

A content author creates a promotion as one Content Fragment in a locale folder with headline, body, image, call-to-action text, call-to-action link, `startDate` and `endDate` (dates only, no time), targeting lists, promotion id, status, priority, and tags. They publish it. No developer is required to launch that campaign.

**Why this priority**: This is the core “author once / no development per campaign” value.

**Independent Test**: Create one promotion in a locale folder, publish it, and confirm it is available for delivery when it is eligible.

**Acceptance Scenarios**:

1. **Given** the author is signed in on Author, **When** they create a promotion under a locale folder with required content and metadata, **Then** the promotion is stored as a single item for that locale and can be published.
2. **Given** a published ACTIVE promotion whose `startDate`–`endDate` window includes today’s calendar date, **When** a consumer requests that locale with matching context on Publish, **Then** the promotion’s content is returned as the winner if it has the highest priority among matches.

---

### User Story 2 - Consumer gets one winner or no content (Priority: P1)

A consumer application sends locale plus optional context (country, brand, market, property, page type, promo, tag). The system returns exactly one winning promotion’s content or an explicit no-match result. Unrelated promotions are never returned.

**Why this priority**: Delivery correctness is the other half of the hypothesis.

**Independent Test**: Request delivery against sample promotions that match, conflict on priority, and miss—without authoring new items if sample content already covers those cases.

**Acceptance Scenarios**:

1. **Given** one eligible matching promotion, **When** the consumer requests with that locale and context, **Then** the response indicates content was found and includes promotion id, headline, body, image, call-to-action text, and call-to-action link.
2. **Given** two eligible matching promotions with different priorities, **When** the consumer requests, **Then** only the higher-priority promotion is returned.
3. **Given** no eligible matching promotion, **When** the consumer requests, **Then** the response indicates content was not found and does not include another campaign as a fallback.

---

### User Story 3 - Hand off only the PCDF package (Priority: P1)

A teammate receives a single named PCDF package (`com.aem.poc.pcdf`) and installs it. They get PCDF application nodes, sample promotions, models and configuration, and a **dedicated PCDF code bundle** from the `core.pcdf` module—without the default site (`/apps/aem-poc`), without the default `core` bundle, and without other proofs of concept. Repository nodes and the PCDF bundle remain separately identifiable parts inside that package.

**Why this priority**: Independent install is a stated delivery constraint; without it, the proof of concept cannot be shared as an explicit artifact.

**Independent Test**: Build or obtain the PCDF package, install it on a clean local Author (and Publish as documented), and confirm PCDF paths and delivery work while the default site package is not required.

**Acceptance Scenarios**:

1. **Given** a reviewer who should not install the whole site, **When** they are given the PCDF package, **Then** they can install only that package and reach authoring and delivery for this proof of concept.
2. **Given** the installed package, **When** a reviewer inspects identity, **Then** application nodes live under `/apps/aem-poc/pcdf`, promotions under `/content/dam/aem-poc/pcdf`, models and configuration under `/conf/aem-poc/pcdf`, and code uses `com.aem.poc.pcdf`.
3. **Given** the shareable package, **When** a reviewer inspects its parts, **Then** repository nodes and the dedicated PCDF code bundle (`core.pcdf` / `com.aem.poc.pcdf`) are distinct, and the default site `core` bundle is not required.

---

### User Story 4 - Preview by date and context (Priority: P2)

A marketing or operations reviewer who is signed in on Author previews which promotion would win for a locale, context, and a chosen calendar date (including dates in the future or past relative to today). Preview uses the same eligibility and targeting rules as live delivery, substituting the preview date for today’s date. Preview does not change live eligibility on Publish. Only **published** fragments may win on Author preview.

**Why this priority**: Campaigns need scheduled activation and expiration confidence before go-live, without exposing future windows on the public Publish surface.

**Independent Test**: Use the same promotion set as live; while signed in on Author, request delivery with a preview date and confirm match or no-match matches the `startDate`/`endDate` window for that date. On Publish, the same promotion set is evaluated using today’s date only.

**Acceptance Scenarios**:

1. **Given** a promotion that is ACTIVE but whose `startDate` is in the future, **When** live delivery is requested on Publish today, **Then** it is not returned.
2. **Given** the same promotion, **When** a signed-in Author user requests preview with a calendar date inside the window and matching context, **Then** it can win according to the same targeting and priority rules.
3. **Given** preview on Author, **When** targeting or locale would not match, **Then** the result is no content, same as live.
4. **Given** an unauthenticated Publish request, **When** a preview value is supplied, **Then** the request is rejected as invalid (Publish does not preview).

---

### User Story 5 - Reuse across experiences, not pages (Priority: P3)

Promotions live in locale folders and are not attached to individual pages. Many pages or consumer applications reuse the same promotion by requesting delivery with locale and context.

**Why this priority**: Proves “publish everywhere” without per-page campaign setup.

**Independent Test**: Two different consumer contexts request the same locale and matching targeting and receive the same winner; no page has a campaign property pointing at the promotion.

**Acceptance Scenarios**:

1. **Given** a promotion authored only under a locale folder, **When** two different experiences request that locale with matching context, **Then** both can receive the same winning promotion.
2. **Given** authors, **When** they launch a campaign, **Then** they do not configure the campaign on individual pages.

---

### Edge Cases

- Request missing locale: reject as invalid (do not invent a default locale).
- Optional context fields omitted: those dimensions do not constrain matching.
- Empty targeting list on a promotion for a dimension: that dimension matches all request values.
- Promotion status not `ACTIVE` (including `INACTIVE`): excluded even if dates and targeting match.
- Request `locale` whose DAM folder does not exist: explicit no-match (`contentFound` false); do not substitute another locale.
- Author preview: only **published** Content Fragments (unpublished drafts never win).
- Current or preview calendar date before `startDate` or after `endDate` (inclusive date comparison, no time of day): excluded.
- Expired content is never returned on live Publish requests.
- Preview supplied on Publish (anonymous or otherwise): reject as invalid; do not evaluate as a preview.
- Preview without a signed-in Author session: reject as invalid.
- Multiple matches with the same priority: winner is the one with the lexicographically lower promotion id.
- Tag filter: if the request includes a tag, the promotion must include that tag; if tag is omitted, tags do not constrain matching.
- URL parameter promo: if present on the request, the promotion’s URL-parameter targeting must include that value (or the promotion’s list for that dimension must be empty).
- No implicit fallback to another locale or an unrelated default promotion.
- Installing the PCDF package must not depend on installing the default site; mixing PCDF into default `core` or `/apps/aem-poc/components` is a failure.

## Requirements *(mandatory)*

### Identity and placement

This proof of concept lives under `/apps/aem-poc/pcdf` (same parent as the site, separate child and modules). A teammate MUST be able to receive **one explicit PCDF package** and install only that.

| Concern | Identity |
| --- | --- |
| Umbrella (apps) | `/apps/aem-poc` |
| This POC (apps / nodes) | `/apps/aem-poc/pcdf` |
| Shareable package and code identity | `com.aem.poc.pcdf` |
| PCDF Java / OSGi bundle | Dedicated `core.pcdf` module (not the default `core` module) |
| PCDF OSGi runmode config | Dedicated `ui.config.pcdf` module (not default `ui.config`) |
| Promotions | `/content/dam/aem-poc/pcdf/{locale}/…` |
| Models / configuration | `/conf/aem-poc/pcdf` |
| Delivery surface | Same POC identity on **Publish** (example: `/services/aem-poc/pcdf`). Source: `docs/programmatic-content-delivery-requirements.md` feature description. Not a public Author campaign engine. |

Inside the shareable package, **repository nodes and the code bundle MUST remain separate parts** so the package can be handed off as a whole without folding PCDF into the rest of the shared site.

Future proofs of concept follow the same pattern: `/apps/aem-poc/{poc-id}` and `com.aem.poc.{poc-id}`.

### Functional Requirements

- **FR-001**: Authors MUST be able to create and edit a promotion under a locale folder with content fields: headline, body, image, call-to-action text, call-to-action link.
- **FR-002**: Authors MUST be able to set scheduling fields `startDate` and `endDate` as **calendar dates only** (no time of day) for this POC.
- **FR-003**: Authors MUST be able to set targeting: countries, markets, brands, properties, page types, URL parameters.
- **FR-004**: Authors MUST be able to set administration fields: promotion id, status (`ACTIVE` or `INACTIVE`), priority (integer, **larger number wins**), tags. `promotionId` MUST be unique within a locale folder; the same `promotionId` MAY appear in other locale folders.
- **FR-005**: The system MUST treat one Content Fragment as one promotion; it MUST NOT require Content Fragment variations (or other variant sets) of the same item for this proof of concept.
- **FR-006**: Locale MUST be expressed by folder topology, not by variant sets.
- **FR-007**: Promotions MUST NOT be attached to individual pages as the way campaigns are configured.
- **FR-008**: Creating a new campaign MUST NOT require development support.
- **FR-009**: The delivery request MUST require locale.
- **FR-010**: The delivery request MAY include country, brand, market, property, page type, promo, and tag.
- **FR-011**: A promotion is eligible only when status is ACTIVE AND `startDate` is on or before the evaluation date AND `endDate` is on or after the evaluation date (inclusive calendar dates, no time).
- **FR-012**: Live delivery on Publish MUST use today’s calendar date as the evaluation date and MUST NOT accept a preview value.
- **FR-013**: Preview MUST be available only to a signed-in user on Author. Preview MUST accept an optional preview **date** (no time) and use it as the evaluation date; all other rules MUST match live delivery, and only **published** fragments MAY win. Preview MUST NOT change which promotions are eligible on Publish. Unpublished drafts MUST NOT win on Author preview.
- **FR-014**: For each targeting dimension that has a non-empty list on the promotion, the request value for that dimension MUST be present in the list for the promotion to match. Empty list means match-all for that dimension. Omitted request parameters MUST NOT constrain that dimension.
- **FR-015**: Among eligible matching promotions, the system MUST return the one with the **highest integer priority** (larger number wins).
- **FR-016**: If two or more eligible matches share the highest priority, the system MUST return the one with the lexicographically lower `promotionId` (compared within that locale’s candidates).
- **FR-017**: If no promotion is eligible and matching, **or** the requested `locale` folder does not exist, the system MUST return an explicit no-match result and MUST NOT return a substitute promotion or locale.
- **FR-018**: A successful match result MUST include: an indication that content was found, promotion id, headline, body, image, call-to-action text, and call-to-action link. Optional content fields that are empty MUST still be present as empty strings.
- **FR-019**: Publish delivery MUST allow anonymous read access.
- **FR-020**: Author MUST require authenticated access for authoring and Author-side use of the capability.
- **FR-021**: The public delivery capability MUST be exposed on Publish, not as the primary public surface on Author.
- **FR-022**: Sample promotions MUST exist under locale folders sufficient to demo match, priority winner, and no-match.
- **FR-023**: Documentation MUST include hypothesis, non-goals, install and demo steps, paths to look at, how to obtain and install the PCDF-only package, and expected output (visible outcome).
- **FR-024**: Future targeting dimensions (membership, audience, segment, device, and similar) are out of scope for this proof of concept, but the delivery contract MUST remain usable if those fields are added later via the promotion model and rule configuration without changing the existing request and result fields defined here.
- **FR-025**: All PCDF application nodes MUST live under `/apps/aem-poc/pcdf`. Sample promotions MUST live under `/content/dam/aem-poc/pcdf`. Models MUST live under `/conf/aem-poc/pcdf`. OSGi runmode config MUST live in a dedicated **`ui.config.pcdf`** module (PCDF-only; not default `ui.config`). PCDF Java MUST live in a dedicated `core.pcdf` module whose bundle identity is `com.aem.poc.pcdf`. PCDF MUST NOT be mixed into `/apps/aem-poc/components` or into the default `core` module.
- **FR-026**: The proof of concept MUST produce one shareable package identified as `com.aem.poc.pcdf` that a teammate can install without the default site package and without the default `core` bundle. That package MUST keep repository nodes and the `core.pcdf` bundle as separate parts (not fused with other proofs of concept or the default site).
- **FR-027**: A delivery request on Publish that includes a preview value MUST be rejected as invalid.

### Key Entities

- **Promotion**: A single locale-scoped campaign item authored as one Content Fragment (no variations). Content: headline, body, image, call-to-action text, call-to-action link. Administration: promotion id, status, priority, tags. Schedule: `startDate` and `endDate` as calendar dates (no time). Targeting: countries, markets, brands, properties, page types, URL parameters.
- **Locale folder**: A repository location under `/content/dam/aem-poc/pcdf` that groups promotions for one locale (lowercase hyphenated codes such as `en-us`, `en-gb`, `fr`, `it`, `de`, `hi`). Delivery resolves candidates from the folder that matches the requested locale.
- **Delivery request**: Locale plus optional country, brand, market, property, page type, promo, and tag. On Author only, an optional preview date (no time) may be supplied by a signed-in user.
- **Delivery result**: Either one winning promotion’s content fields plus promotion id, or an explicit no-match.
- **PCDF package**: The explicit, shareable install artifact for this proof of concept only. Contains separately identifiable repository-node content and the dedicated `core.pcdf` bundle under identity `com.aem.poc.pcdf`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A reviewer can create (or use sample) a new campaign on Author with no code change and see it delivered when eligible.
- **SC-002**: For the sample promotion set, a consumer request with a matching locale and context returns the expected single winner; a non-matching request returns no content—demonstrable in one demo pass.
- **SC-003**: On Author, preview with a calendar date inside a future campaign window returns that campaign when targeting matches; the same request on Publish (no preview) does not return it if `startDate` is still in the future.
- **SC-004**: Two experiences can reuse the same promotion without any page-level campaign configuration.
- **SC-005**: For the sample set used in the demo, selecting the winner (eligibility, targeting, and priority) completes in under 100 milliseconds on a local instance used for the demo.
- **SC-006**: A teammate can reproduce the demo from written steps (prerequisites, deploy of the PCDF package, Author path, Publish request examples, expected outcomes) without tribal knowledge.
- **SC-007**: A teammate who installs only the PCDF package (not the default site and not the default `core` bundle) can complete the documented demo; PCDF nodes, sample promotions, and delivery are present, and nothing from this proof of concept lives under the default site apps tree or in the default `core` module.
- **SC-008**: A reviewer can see `startDate` and `endDate` on Author as dates without a time of day, and a preview date uses that same date-only comparison.

## Out of Scope

- Membership tier, loyalty status, campaign audience, customer segment, device type, geographic region beyond country, and other personalisation signals.
- Variant sets of a single promotion, including Content Fragment variations.
- Page-level or component-level campaign configuration.
- Implementing or configuring a content delivery network or edge cache keys; edge caching is an external recommendation only.
- Production hardening, observability stacks, and treating a test suite as acceptance.
- Redesigning the delivery contract for future targeting dimensions.
- Time-of-day scheduling and timezone-aware instants (a global product may need them later; this POC uses calendar dates only).
- Building PCDF modules/servlet/sample content remains **opt-in** (`/speckit-implement`); this workstream maintains spec, plan, tasks, and repo Java 21 settings.

## Assumptions

- Empty targeting list for a dimension means match-all for that dimension.
- Missing optional request parameters mean those dimensions are unconstrained.
- Priority ties are broken by lexicographically lower promotion id within the locale. The same `promotionId` MAY be used in different locale folders.
- Tag matching is exact inclusion of the requested tag when tag is provided.
- Promo on the request maps to the promotion’s URL-parameter targeting list.
- Eligibility compares calendar dates only: `startDate` ≤ evaluation date ≤ `endDate`. Live evaluation date is today’s date on the instance used for the demo (Author preview uses the supplied date). Time of day and per-field timezone are out of scope for this POC.
- One promotion equals one Content Fragment; Content Fragment variations are out of scope.
- Edge caching is out of scope for implementation; cache-key guidance of locale, country, brand, and promo may be documented as an external recommendation only.
- Default local Author is `localhost:4502` and Publish is `localhost:4503` on **AEM 6.5 LTS SP2**. Credentials follow the project’s local demo convention only. Live “today” is that instance’s calendar date.
- Target language is **Java 21** at the parent POM (`release` 21).
- Automated tests are optional; the proof of concept is accepted via visible demo and documentation, not coverage gates.
- Targeting dimensions in this proof of concept are country, market, brand, property, page type, URL parameter (promo), and tag.
- The shareable package is the hand-off unit; teammates are not expected to assemble PCDF from the full multi-module site install unless they choose to.
- Placement paths and package identity are isolation constraints for independent install, not a mandate for a particular matching implementation inside those locations.
- PCDF Java is a dedicated `core.pcdf` module/bundle. OSGi runmode config is `ui.config.pcdf`. The existing default `core` and `ui.config` modules are not used for this POC’s PCDF-specific services or configs.
