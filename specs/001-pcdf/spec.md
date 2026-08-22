# Feature Specification: PCDF (Programmatic Content Delivery Framework)

**Feature Branch**: `001-pcdf`

**Created**: 2026-08-22

**Status**: Draft

**Input**: User description: "PCDF (Programmatic Content Delivery Framework). Authors create locale-scoped promotions once (content, schedule, targeting, priority). Consumer applications request the single winning promotion for a locale and context, or receive no content when nothing matches—without page-level campaign wiring or developer work per campaign. Isolated under the shared POCs umbrella so a teammate can install only PCDF."

## Problem / goal

Marketing teams need to author promotional content once and deliver it across many experiences (thousands of pages or consumer applications) with scheduling, targeting, and preview—without attaching campaigns to individual pages or waiting on development for each campaign.

This proof of concept proves that hypothesis: Author for authoring, Publish for delivery, sample content for a visible demo.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Author a promotion without developers (Priority: P1)

A content author creates a promotion in a locale folder with headline, body, image, call-to-action text, call-to-action link, start and end dates, targeting lists, promotion id, status, priority, and tags. They publish it. No developer is required to launch that campaign.

**Why this priority**: This is the core “author once / no development per campaign” value.

**Independent Test**: Create one promotion in a locale folder, publish it, and confirm it is available for delivery when it is eligible.

**Acceptance Scenarios**:

1. **Given** the author is signed in on Author, **When** they create a promotion under a locale folder with required content and metadata, **Then** the promotion is stored as a single item for that locale and can be published.
2. **Given** a published ACTIVE promotion whose date window includes today, **When** a consumer requests that locale with matching context, **Then** the promotion’s content is returned as the winner if it has the highest priority among matches.

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

A teammate receives a single named PCDF package (`com.aem.poc.pcdf`) and installs it. They get PCDF application nodes, sample promotions, models and configuration, and the PCDF code bundle—without the default site (`/apps/aem-poc`) or other proofs of concept. Repository nodes and the code bundle remain separately identifiable parts inside that package so either part can be inspected or rebuilt without mixing them into the rest of the project.

**Why this priority**: Independent install is a stated delivery constraint; without it, the proof of concept cannot be shared as an explicit artifact.

**Independent Test**: Build or obtain the PCDF package, install it on a clean local Author (and Publish as documented), and confirm PCDF paths and delivery work while the default site package is not required.

**Acceptance Scenarios**:

1. **Given** a reviewer who should not install the whole site, **When** they are given the PCDF package, **Then** they can install only that package and reach authoring and delivery for this proof of concept.
2. **Given** the installed package, **When** a reviewer inspects identity, **Then** application nodes live under `/apps/aem-pocs/pcdf`, promotions under `/content/dam/aem-pocs/pcdf`, models and configuration under `/conf/aem-pocs/pcdf`, and code uses `com.aem.poc.pcdf`.
3. **Given** the shareable package, **When** a reviewer inspects its parts, **Then** repository nodes and the code bundle are distinct (not a single mixed blob with the default site).

---

### User Story 4 - Preview by date and context (Priority: P2)

A marketing or operations reviewer previews which promotion would win for a locale, context, and a chosen date (including dates in the future or past relative to today). Preview uses the same eligibility and targeting rules as live delivery, substituting the preview date for the current date. Preview does not change live eligibility.

**Why this priority**: Campaigns need scheduled activation and expiration confidence before go-live.

**Independent Test**: Use the same promotion set as live; request delivery with a preview date and confirm match or no-match matches the schedule for that date.

**Acceptance Scenarios**:

1. **Given** a promotion that is ACTIVE but whose window starts in the future, **When** live delivery is requested for today, **Then** it is not returned.
2. **Given** the same promotion, **When** preview is requested with a preview date inside the window and matching context, **Then** it can win according to the same targeting and priority rules.
3. **Given** preview, **When** targeting or locale would not match, **Then** the result is no content, same as live.

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
- Promotion status not ACTIVE: excluded even if dates and targeting match.
- Current or preview date before start or after end: excluded.
- Expired content is never returned on live requests.
- Multiple matches with the same priority: winner is the one with the lexicographically lower promotion id.
- Tag filter: if the request includes a tag, the promotion must include that tag; if tag is omitted, tags do not constrain matching.
- URL parameter promo: if present on the request, the promotion’s URL-parameter targeting must include that value (or the promotion’s list for that dimension must be empty).
- No implicit fallback to another locale or an unrelated default promotion.
- Installing the PCDF package must not depend on installing the default site; mixing PCDF into `/apps/aem-poc` is a failure.

## Requirements *(mandatory)*

### Identity and placement

This proof of concept lives under the shared POCs umbrella, not under the default site. A teammate MUST be able to receive **one explicit PCDF package** and install only that.

| Concern | Identity |
| --- | --- |
| Umbrella (apps) | `/apps/aem-pocs` |
| This POC (apps / nodes) | `/apps/aem-pocs/pcdf` |
| Shareable package and code identity | `com.aem.poc.pcdf` |
| Promotions | `/content/dam/aem-pocs/pcdf/{locale}/…` |
| Models / configuration | `/conf/aem-pocs/pcdf` |
| Delivery surface | Same POC identity (under the PCDF umbrella, not the default site) |

Inside the shareable package, **repository nodes and the code bundle MUST remain separate parts** so the package can be handed off as a whole without folding PCDF into the rest of the shared site.

Future proofs of concept follow the same pattern: `/apps/aem-pocs/{poc-id}` and `com.aem.poc.{poc-id}`.

### Functional Requirements

- **FR-001**: Authors MUST be able to create and edit a promotion under a locale folder with content fields: headline, body, image, call-to-action text, call-to-action link.
- **FR-002**: Authors MUST be able to set scheduling fields: start date and end date.
- **FR-003**: Authors MUST be able to set targeting: countries, markets, brands, properties, page types, URL parameters.
- **FR-004**: Authors MUST be able to set administration fields: promotion id, status, priority, tags.
- **FR-005**: The system MUST treat one content item as one promotion; it MUST NOT require variant sets of the same item for this proof of concept.
- **FR-006**: Locale MUST be expressed by folder topology, not by variant sets.
- **FR-007**: Promotions MUST NOT be attached to individual pages as the way campaigns are configured.
- **FR-008**: Creating a new campaign MUST NOT require development support.
- **FR-009**: The delivery request MUST require locale.
- **FR-010**: The delivery request MAY include country, brand, market, property, page type, promo, and tag.
- **FR-011**: A promotion is eligible only when status is ACTIVE AND start date is on or before the evaluation date AND end date is on or after the evaluation date.
- **FR-012**: Live delivery MUST use the current date as the evaluation date.
- **FR-013**: Preview MUST accept an optional preview date and use it as the evaluation date; all other rules MUST match live delivery.
- **FR-014**: For each targeting dimension that has a non-empty list on the promotion, the request value for that dimension MUST be present in the list for the promotion to match. Empty list means match-all for that dimension. Omitted request parameters MUST NOT constrain that dimension.
- **FR-015**: Among eligible matching promotions, the system MUST return the one with the highest priority.
- **FR-016**: If two or more eligible matches share the highest priority, the system MUST return the one with the lexicographically lower promotion id.
- **FR-017**: If no promotion is eligible and matching, the system MUST return an explicit no-match result and MUST NOT return a substitute promotion.
- **FR-018**: A successful match result MUST include: an indication that content was found, promotion id, headline, body, image, call-to-action text, and call-to-action link.
- **FR-019**: Publish delivery MUST allow anonymous read access.
- **FR-020**: Author MUST require authenticated access for authoring and Author-side use of the capability.
- **FR-021**: The public delivery capability MUST be exposed on Publish, not as the primary public surface on Author.
- **FR-022**: Sample promotions MUST exist under locale folders sufficient to demo match, priority winner, and no-match.
- **FR-023**: Documentation MUST include hypothesis, non-goals, install and demo steps, paths to look at, how to obtain and install the PCDF-only package, and expected output (visible outcome).
- **FR-024**: Future targeting dimensions (membership, audience, segment, device, and similar) are out of scope for this proof of concept, but the delivery contract MUST remain usable if those fields are added later via the promotion model and rule configuration without changing the existing request and result fields defined here.
- **FR-025**: All PCDF application nodes MUST live under `/apps/aem-pocs/pcdf`. Sample promotions MUST live under `/content/dam/aem-pocs/pcdf`. Models and configuration MUST live under `/conf/aem-pocs/pcdf`. Code MUST use `com.aem.poc.pcdf`. PCDF MUST NOT be mixed into `/apps/aem-poc`.
- **FR-026**: The proof of concept MUST produce one shareable package identified as `com.aem.poc.pcdf` that a teammate can install without the default site package. That package MUST keep repository nodes and the code bundle as separate parts (not fused with other proofs of concept or the default site).

### Key Entities

- **Promotion**: A single locale-scoped campaign item. Content: headline, body, image, call-to-action text, call-to-action link. Administration: promotion id, status, priority, tags. Schedule: start date, end date. Targeting: countries, markets, brands, properties, page types, URL parameters.
- **Locale folder**: A repository location under `/content/dam/aem-pocs/pcdf` that groups promotions for one locale (for example `en_US`). Delivery resolves candidates from the folder that matches the requested locale.
- **Delivery request**: Locale plus optional country, brand, market, property, page type, promo, tag, and optional preview date.
- **Delivery result**: Either one winning promotion’s content fields plus promotion id, or an explicit no-match.
- **PCDF package**: The explicit, shareable install artifact for this proof of concept only. Contains separately identifiable repository-node content and the PCDF code bundle under identity `com.aem.poc.pcdf`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A reviewer can create (or use sample) a new campaign on Author with no code change and see it delivered when eligible.
- **SC-002**: For the sample promotion set, a consumer request with a matching locale and context returns the expected single winner; a non-matching request returns no content—demonstrable in one demo pass.
- **SC-003**: Preview with a date inside a future campaign window returns that campaign when targeting matches; the same request without preview date does not return it if the window has not started.
- **SC-004**: Two experiences can reuse the same promotion without any page-level campaign configuration.
- **SC-005**: For the sample set used in the demo, selecting the winner (eligibility, targeting, and priority) completes in under 100 milliseconds on a local instance used for the demo.
- **SC-006**: A teammate can reproduce the demo from written steps (prerequisites, deploy of the PCDF package, Author path, Publish request examples, expected outcomes) without tribal knowledge.
- **SC-007**: A teammate who installs only the PCDF package (not the default site) can complete the documented demo; PCDF nodes, sample promotions, and delivery are present, and nothing from this proof of concept lives under the default site apps tree.

## Out of Scope

- Membership tier, loyalty status, campaign audience, customer segment, device type, geographic region beyond country, and other personalisation signals.
- Variant sets of a single promotion.
- Page-level or component-level campaign configuration.
- Implementing or configuring a content delivery network or edge cache keys; edge caching is an external recommendation only.
- Production hardening, observability stacks, and treating a test suite as acceptance.
- Redesigning the delivery contract for future targeting dimensions.

## Assumptions

- Empty targeting list for a dimension means match-all for that dimension.
- Missing optional request parameters mean those dimensions are unconstrained.
- Priority ties are broken by lexicographically lower promotion id.
- Tag matching is exact inclusion of the requested tag when tag is provided.
- Promo on the request maps to the promotion’s URL-parameter targeting list.
- Evaluation dates use the same calendar-day semantics as the authored start and end fields (timezone: instance default unless documented otherwise in the demo notes).
- Edge caching is out of scope for implementation; cache-key guidance of locale, country, brand, and promo may be documented as an external recommendation only.
- Default local Author is `localhost:4502` and Publish is `localhost:4503`; credentials follow the project’s local demo convention only.
- Automated tests are optional; the proof of concept is accepted via visible demo and documentation, not coverage gates.
- One promotion equals one content item; variant sets of a single promotion are out of scope.
- Targeting dimensions in this proof of concept are country, market, brand, property, page type, URL parameter (promo), and tag.
- The shareable package is the hand-off unit; teammates are not expected to assemble PCDF from the full multi-module site install unless they choose to.
- Placement paths and package identity are isolation constraints for independent install, not a mandate for a particular matching implementation inside those locations.
