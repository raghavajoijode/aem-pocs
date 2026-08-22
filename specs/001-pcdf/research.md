# Research: PCDF

## Extra Maven modules vs constitution layout

**Decision**: Add `core.pcdf`, `ui.apps.pcdf`, `ui.content.pcdf`, and a container package module `pcdf` (artifact `aem-poc.pcdf` / FileVault group `com.aem.poc.pcdf`). Do not put PCDF Java in default `core` or nodes under `/apps/aem-poc`.

**Rationale**: Clarify session chose a dedicated bundle. Spec FR-025/026 require a hand-off that does not install the default site or `aem-poc.core`. Principle V allows extra modules when they **are** the experiment. Isolation is the experiment.

**Alternatives considered**:

- Java in default `core` — rejected in clarify (Option B).
- PCDF nodes in existing `ui.apps` / `ui.content` with the same filters as the site — the site package would always include `/apps/aem-poc`, so a teammate could not install “only PCDF.”
- One mixed `ui.pcdf` package for apps + DAM + conf — possible, but splitting apps vs content matches this repo and makes filter roots obvious.

## Delivery surface

**Decision**: `GET /services/aem-pocs/pcdf` (Sling path servlet on the `core.pcdf` bundle). Query parameters as in [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). JSON body; HTTP 200 for match and no-match; HTTP 400 for invalid requests (missing locale, preview on Publish, bad date).

**Rationale**: Path sits under the PCDF identity, not `/apps/aem-poc`. GET avoids CSRF complexity for the demo. Distinguishing 400 vs `{ "contentFound": false }` keeps “bad request” separate from “nothing eligible.”

**Alternatives considered**:

- Resource servlet under a DAM path — couples delivery to a content resource and is harder to call from consumer apps.
- GraphQL / Content Fragments REST — more surface than the POC needs; the contract is one winner, not a fragment dump.

## Author vs Publish access

**Decision**: Publish runmode OSGi config removes authentication requirement for `/services/aem-pocs/pcdf` (anonymous GET). Author keeps default authentication. Servlet rejects `previewDate` on Publish (400) even if someone later misconfigures auth.

**Rationale**: FR-019, FR-020, FR-021, FR-027. Defense in depth: runmode config for anonymous Publish plus servlet rule for preview.

**Alternatives considered**:

- Same anonymous access on Author — rejected in clarify.
- Ignore `previewDate` on Publish silently — harder to demo; spec says reject as invalid.

## Content model

**Decision**: One Content Fragment model `ProgrammaticPromotion` under `/conf/aem-pocs/pcdf`. One fragment per promotion; **no variations**. Locale is the DAM folder `/content/dam/aem-pocs/pcdf/{locale}/`. Date fields are Date (date-only in the authoring UI). Targeting fields are multi-value strings.

**Rationale**: Spec FR-005/006 and authoring without developers (standard CF editor). Empty multi-value = match-all.

**Alternatives considered**:

- Experience Fragments or page components — implies per-page wiring (out of scope).
- CF variations for locale — spec forbids variant sets; locale is folder topology.

## Query and ranking

**Decision**: Resolve candidates with a query limited to the locale folder and the `ProgrammaticPromotion` model, then apply ACTIVE + date + targeting in Java, then pick max priority and lexicographically lowest `promotionId` on ties. Target “rule evaluation under 100ms” for the **sample set**, not a production query SLA.

**Rationale**: Sample set is small; in-memory filter keeps matching rules obvious and testable in a demo. Query-only targeting in JCR is brittle for empty-list match-all.

**Alternatives considered**:

- Pure QueryBuilder predicates for every dimension — empty-list match-all is awkward in JCR.
- Caching / CDN keys in product — out of scope; document `locale|country|brand|promo` as an external recommendation only.

## Documentation and tests

**Decision**: Primary evidence **when implemented** is [quickstart.md](./quickstart.md) plus a short PCDF section in the root README. This workstream stops at spec/plan/checklist; it does not add modules or a servlet.

**Rationale**: Visible Author CF + Publish JSON is the eventual POC outcome. Constitution III still applies if/when the feature is built.

**Alternatives considered**: Contract-test suite as acceptance — constitution forbids test-suite-as-gate.

## AEM stack

**Decision**: **Java 21** at the parent POM (`release` 21). Local **AEM 6.5 LTS SP2** Author/Publish (`localhost:4502` / `localhost:4503`). Delivery is **Publish** `GET /services/aem-pocs/pcdf`. OSGi runmode config in **`ui.config.pcdf`**. Unknown locale folder → no-match. Author preview → published fragments only. `promotionId` unique per locale; reusable across locales.

**Rationale**: Project-stated runtime. Constitution config split preserved without putting PCDF PIDs in default `ui.config`.

**Alternatives considered**: Java 21 only on `core.pcdf` — rejected (parent bump chosen). OSGi config under `ui.apps.pcdf` — rejected. Unpublished drafts on Author preview — rejected.
