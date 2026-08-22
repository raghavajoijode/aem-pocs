# PCDF — Spec Kit requirements

**PCDF** = Programmatic Content Delivery Framework.

Use this document as the feature description for `/speckit-specify`. Paste the **Feature description** section (from the heading below through **Assumptions**) into the command. Keep **How to use with Spec Kit** and **Implementation hints** out of the paste so specify stays focused on WHAT and WHY.

---

## Feature description

### Feature name

PCDF (Programmatic Content Delivery Framework)

### Identity and placement

Everything for this POC lives under the shared POCs umbrella, not under the archetype site (`/apps/aem-poc`). Nodes, code, and the shareable package are isolated so a teammate can install **only** PCDF.

- **Umbrella (repository)**: `/apps/aem-pocs`
- **This POC (apps)**: `/apps/aem-pocs/pcdf`
- **Java / OSGi / Maven package**: `com.aem.poc.pcdf`
- **Shareable package artifact**: `com.aem.poc.pcdf` (content nodes and bundle kept as separate parts inside that package)
- **DAM / promotions**: `/content/dam/aem-pocs/pcdf/{locale}/…`
- **Conf / models**: `/conf/aem-pocs/pcdf`
- **Delivery API**: under the same POC identity (for example `/services/aem-pocs/pcdf`)

Future POCs follow the same pattern: `/apps/aem-pocs/{poc-id}` and `com.aem.poc.{poc-id}`.

### One-liner

Authors create locale-scoped promotions once (content, schedule, targeting, priority). Consumer applications request the single winning promotion for a locale and context, or receive no content when nothing matches—without page-level campaign wiring or developer work per campaign.

### Problem / goal

Marketing teams need to author promotional content once and deliver it across many experiences (thousands of pages / consumer apps) with scheduling, targeting, and preview—without attaching campaigns to individual pages or waiting on development for each campaign.

This POC proves that hypothesis on AEM: Author for authoring, Publish for a delivery API, sample content for a visible demo.

### Actors

- **Content author** — creates and maintains promotions (copy, image, CTA, schedule, targeting, status, priority, tags).
- **Marketing / ops reviewer** — previews which promotion would win for a locale, context, and date without changing live eligibility.
- **Consumer application** — requests the winning promotion for a given locale and optional context.
- **Anonymous publish reader** — may call the publish delivery API without authentication.
- **Authenticated author user** — may use authoring and preview only when signed in on Author.

### In scope

- One promotion = one content item (no content-fragment variations).
- Locale is represented by repository folders (for example `en-us`, `fr`, `en-gb` under `/content/dam/aem-pocs/pcdf`).
- Authoring of content fields, scheduling, targeting, and administration fields without development support per campaign.
- Eligibility: only ACTIVE promotions whose start/end window includes “now” (or preview date).
- Targeting dimensions for this POC: country, market, brand, property, page type, URL parameter (`promo`), and tag.
- Request: locale is mandatory; country, brand, market, property, page type, promo, and tag are optional.
- Resolution: all eligible, matching promotions → highest priority wins; no match → no content (no silent fallback).
- Preview using the same rules as live, with an optional preview date.
- Publish: anonymous read of the delivery API. Author: authenticated access only. Delivery API is for Publish (not as a public Author campaign engine).
- Sample content and documented demo path so a reviewer can see authoring and a winning vs no-match response.
- Clear separation of PCDF repository nodes and the PCDF code bundle, assembled into one shareable package (`com.aem.poc.pcdf`) that can be handed off without the rest of the aem-poc site.
- Extensibility constraint (not implementation of new dimensions): future targeting fields should be addable via the promotion model and rule configuration without redesigning the delivery contract.

### Out of scope (this POC)

- Membership tier, loyalty status, campaign audience, customer segment, device type, geographic region beyond country, and other personalisation signals.
- Content Fragment variations.
- Page-level or component-level campaign configuration.
- Implementing or configuring Akamai (or any CDN) cache keys; edge caching is an external recommendation only.
- Production hardening, observability stacks, and test-suite-as-acceptance.
- Redesigning the delivery contract for future targeting dimensions.

### User stories

#### User Story 1 — Author a promotion without developers (Priority: P1)

A content author creates a promotion for a locale folder with headline, body, image, CTA text, CTA link, start and end dates, targeting lists, promotion id, status, priority, and tags. They publish it. No developer is required to launch that campaign.

**Why this priority**: This is the core “author once / no dev per campaign” value.

**Independent test**: Create one promotion in a locale folder, publish, and confirm it is available for delivery when eligible.

**Acceptance scenarios**:

1. **Given** the author is on Author, **When** they create a promotion under a locale folder with required content and metadata, **Then** the promotion is stored as a single item for that locale and can be published.
2. **Given** a published ACTIVE promotion with a current date window, **When** a consumer requests that locale with matching context, **Then** the promotion’s content is returned as the winner (if it has the highest priority among matches).

#### User Story 2 — Consumer gets one winner or no content (Priority: P1)

A consumer application sends locale plus optional context (country, brand, market, property, page type, promo, tag). The system returns exactly one winning promotion’s content or an explicit no-match result. Unrelated promotions are never returned.

**Why this priority**: Delivery correctness is the other half of the hypothesis.

**Independent test**: Hit the delivery API with sample promotions that match, conflict on priority, and miss—without authoring new items if sample content already covers the cases.

**Acceptance scenarios**:

1. **Given** one eligible matching promotion, **When** the consumer requests with that locale and context, **Then** the response indicates content was found and includes promotion id, headline, body, image, CTA text, and CTA link.
2. **Given** two eligible matching promotions with different priorities, **When** the consumer requests, **Then** only the higher-priority promotion is returned.
3. **Given** no eligible matching promotion, **When** the consumer requests, **Then** the response indicates content was not found and does not include another campaign as a fallback.

#### User Story 3 — Preview by date and context (Priority: P2)

A reviewer previews which promotion would win for a locale, context, and a chosen date (including dates in the future or past relative to “now”). Preview uses the same eligibility and targeting rules as live delivery, substituting the preview date for the current date.

**Why this priority**: Campaigns need scheduled activation/expiration confidence before go-live.

**Independent test**: Same promotion set as live; call delivery with `previewDate` and confirm match/no-match matches the schedule for that date.

**Acceptance scenarios**:

1. **Given** a promotion that is ACTIVE but whose window starts in the future, **When** live delivery is requested “now”, **Then** it is not returned.
2. **Given** the same promotion, **When** preview is requested with a preview date inside the window and matching context, **Then** it can win according to the same targeting and priority rules.
3. **Given** preview, **When** targeting or locale would not match, **Then** the result is no content, same as live.

#### User Story 4 — Reuse across experiences, not pages (Priority: P3)

Promotions live in locale folders and are not attached to individual pages. Many pages or consumer apps reuse the same promotion by calling delivery with locale and context.

**Why this priority**: Proves “publish everywhere” without per-page campaign setup.

**Independent test**: Two different consumer contexts (or demo clients) request the same locale and matching targeting and receive the same winner; no page has a campaign property pointing at the fragment.

**Acceptance scenarios**:

1. **Given** a promotion authored only under a locale folder, **When** two different experiences request that locale with matching context, **Then** both can receive the same winning promotion.
2. **Given** authors, **When** they launch a campaign, **Then** they do not configure the campaign on individual pages.

### Edge cases

- Request missing locale: reject as invalid (do not invent a default locale).
- Optional context fields omitted: those dimensions do not constrain matching.
- Empty targeting list on a promotion for a dimension: that dimension matches all request values.
- Promotion status not ACTIVE: excluded even if dates and targeting match.
- Current or preview date before start or after end: excluded.
- Expired content is never returned on live requests.
- Multiple matches with the same priority: winner is the one with the lexicographically lower `promotionId`.
- Tag filter: if the request includes `tag`, the promotion must include that tag; if `tag` is omitted, tags do not constrain matching.
- URL parameter `promo`: if present on the request, the promotion’s URL-parameter targeting must include that value (or the promotion’s list for that dimension must be empty).
- No implicit fallback to another locale or an “unrelated” default promotion.

### Functional requirements

- **FR-001**: Authors MUST be able to create and edit a promotion under a locale folder with content fields: headline, body, image, CTA text, CTA link.
- **FR-002**: Authors MUST be able to set scheduling fields: start date and end date.
- **FR-003**: Authors MUST be able to set targeting: countries, markets, brands, properties, page types, URL parameters.
- **FR-004**: Authors MUST be able to set administration fields: promotion id, status, priority, tags.
- **FR-005**: The system MUST treat one content item as one promotion; it MUST NOT require content-fragment variations for this POC.
- **FR-006**: Locale MUST be expressed by folder topology, not by variation sets.
- **FR-007**: Promotions MUST NOT be attached to individual pages as the way campaigns are configured.
- **FR-008**: Creating a new campaign MUST NOT require development support.
- **FR-009**: The delivery request MUST require `locale`.
- **FR-010**: The delivery request MAY include `country`, `brand`, `market`, `property`, `pageType`, `promo`, and `tag`.
- **FR-011**: A promotion is eligible only when status is ACTIVE AND start date ≤ evaluation date AND end date ≥ evaluation date.
- **FR-012**: Live delivery MUST use the current date as the evaluation date.
- **FR-013**: Preview MUST accept an optional preview date and use it as the evaluation date; all other rules MUST match live delivery.
- **FR-014**: For each targeting dimension that has a non-empty list on the promotion, the request value for that dimension MUST be present in the list for the promotion to match. Empty list means match-all for that dimension. Omitted request parameters MUST NOT constrain that dimension.
- **FR-015**: Among eligible matching promotions, the system MUST return the one with the highest priority.
- **FR-016**: If two or more eligible matches share the highest priority, the system MUST return the one with the lexicographically lower promotion id.
- **FR-017**: If no promotion is eligible and matching, the system MUST return an explicit no-match result (`contentFound` false) and MUST NOT return a substitute promotion.
- **FR-018**: A successful match response MUST include: content found flag, promotion id, headline, body, image, CTA text, CTA link.
- **FR-019**: Publish delivery MUST allow anonymous read access.
- **FR-020**: Author MUST require authenticated access for authoring (and Author-side use of the capability).
- **FR-021**: The public delivery API MUST be exposed on Publish, not as the primary public surface on Author.
- **FR-022**: Sample promotions MUST exist under locale folders sufficient to demo match, priority winner, and no-match.
- **FR-023**: Documentation MUST include hypothesis, non-goals, install/demo steps, paths to look at, and expected output (visible outcome).
- **FR-024**: Future targeting dimensions (membership, audience, segment, device, and similar) are out of scope for this POC, but the delivery contract MUST remain usable if those fields are added later via the promotion model and rule configuration without changing the existing request/response fields defined here.
- **FR-025**: All PCDF application nodes MUST live under `/apps/aem-pocs/pcdf`. Sample promotions MUST live under `/content/dam/aem-pocs/pcdf`. Models/config MUST live under `/conf/aem-pocs/pcdf`. Code and the shareable package MUST use `com.aem.poc.pcdf`. PCDF MUST NOT be mixed into `/apps/aem-poc`.

### Key entities

- **Promotion**: A single locale-scoped campaign item. Content: headline, body, image, CTA text, CTA link. Administration: promotion id, status, priority, tags. Schedule: start date, end date. Targeting: countries, markets, brands, properties, page types, URL parameters.
- **Locale folder**: A repository location under `/content/dam/aem-pocs/pcdf` that groups promotions for one locale (for example `en-us`). Delivery resolves candidates from the folder that matches the requested locale.
- **Delivery request**: Locale plus optional country, brand, market, property, page type, promo, tag, and optional preview date.
- **Delivery result**: Either one winning promotion’s content fields plus promotion id, or an explicit no-match.

### Success criteria

- **SC-001**: A reviewer can create (or use sample) a new campaign on Author with no code change and see it delivered when eligible.
- **SC-002**: For the sample promotion set, a consumer request with a matching locale and context returns the expected single winner; a non-matching request returns no content—demonstrable in one demo pass.
- **SC-003**: Preview with a date inside a future campaign window returns that campaign when targeting matches; the same request without preview date does not return it if the window has not started.
- **SC-004**: Two experiences can reuse the same promotion without any page-level campaign configuration.
- **SC-005**: For the POC sample set, selecting the winner (eligibility + targeting + priority) completes in under 100ms on a local AEM instance used for the demo.
- **SC-006**: A teammate can reproduce the demo from written steps (prerequisites, deploy, Author path, Publish request examples, expected JSON-shaped outcomes) without tribal knowledge.

### Assumptions

- Empty targeting list for a dimension means match-all for that dimension.
- Missing optional request parameters mean those dimensions are unconstrained.
- Priority ties are broken by lexicographically lower `promotionId`.
- Tag matching is exact inclusion of the requested tag when `tag` is provided.
- `promo` on the request maps to the promotion’s URL-parameter targeting list.
- Evaluation dates use the same calendar-day semantics as the authored start/end fields (timezone: AEM instance default unless documented otherwise in the demo notes).
- Akamai/CDN is out of scope for implementation; cache-key guidance (`locale|country|brand|promo`) may be documented as an external recommendation only.
- Default local AEM Author `localhost:4502` and Publish `localhost:4503`; credentials follow archetype convention for local demo only.
- Tests are optional; the POC is accepted via visible demo and documentation, not coverage gates.

---

## How to use with Spec Kit

1. Copy everything under **Feature description** (from “Feature name” through the last **Assumptions** bullet).
2. Run `/speckit-specify` in this repo and paste that text as the feature description.
3. Do not paste **Implementation hints** into specify; use them later with `/speckit-plan`.

Suggested short name if asked: `pcdf`.

---

## Implementation hints (for plan phase only — do not paste into specify)

These notes come from the original architecture brief. They are HOW, not acceptance criteria for specify.

- Apps / clientlibs / components: `/apps/aem-pocs/pcdf` (do not use `/apps/aem-poc` for this POC).
- Java and Maven package: `com.aem.poc.pcdf`. Shareable FileVault package group/artifact: `com.aem.poc.pcdf`. Keep nodes and the OSGi bundle as separate embeddeds inside that package.
- DAM root: `/content/dam/aem-pocs/pcdf/{locale}/{promotionName}`.
- Conf / Content Fragment model: `/conf/aem-pocs/pcdf`. Suggested model name: `ProgrammaticPromotion`.
- Suggested delivery path: `GET /services/aem-pocs/pcdf`.
- Preview query example: `locale`, `country`, `promo`, `previewDate=2026-10-15`.
- Logical layers: request validation → locale path → query fragments → ACTIVE + date filter → targeting rules → highest priority → JSON.
- Named services in the brief (servlet, content service, query service, rule evaluation) are a planning sketch, not a mandate to copy class names.
- Recommended external cache key: `locale|country|brand|promo` (Akamai not built in this POC).
- Target rule-evaluation time under 100ms is a demo NFR for the sample set, not a production SLA.
