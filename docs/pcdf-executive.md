# PCDF — executive brief

**What it is:** A proof of concept so marketers author a promotion once, in the language folder that matches the experience, and every consumer app asks AEM Publish for **the single winner** (or an explicit “nothing matches”). No developer ticket per campaign. No campaign widget on each page.

**What success looks like:** A teammate installs one zip (`com.aem.poc.pcdf`) on Author and Publish. An author publishes a Content Fragment. A curl (or app) to Publish returns JSON. Isolation is proven if the default site (`/apps/aem-poc`) and default `core` bundle are not required.

Slide deck: [pcdf-brief.html](pcdf-brief.html). Operator install and curl table: [pcdf.md](pcdf.md).

## Authoring experience

Authors work only in **Assets / Content Fragments**, not in page properties.

- Locale is a **folder** (`/content/dam/aem-poc/pcdf/en-us`, `en-gb`, `fr`, `it`, `de`, `hi`, …). The name is the `locale` query value (lowercase, hyphenated).
- One fragment = one promotion. They do **not** create CF variations for language; they put the fragment in the right folder.
- They set copy (headline, body, image, CTA), a calendar **start and end date** (no time of day), targeting lists (empty means “any” for that dimension), status (`ACTIVE` / `INACTIVE`), and an integer **priority** (higher number wins).
- They **publish**. Unpublished work is invisible to Publish and to Author preview.
- They do **not** attach the promotion to a page. Home, PDP, and a mobile app can all request the same locale and targeting and receive the same winner.

## How delivery decides

1. Require `locale`. Missing → invalid request (HTTP 400).
2. Load **published** fragments in that folder only. Unknown folder (for example `ja`) → no-match, **no** fallback language.
3. Keep `ACTIVE` rows whose date window includes today (Author may pass `previewDate` instead; Publish rejects preview).
4. Apply targeting (country, brand, market, property, page type, promo, tag).
5. Pick the highest priority; if tied, the lexicographically lower `promotionId`.

`promotionId` is unique **per folder**. Hindi and US English may both use `pcdf-match-high`.

## Demo / validation scenarios

Run after [PCDF-only install](pcdf.md#build-and-deploy-pcdf-alone). Publish base: `http://localhost:4503/services/aem-poc/pcdf`.

| # | Story | Call | Pass if |
| --- | --- | --- | --- |
| 1 | US priority | `locale=en-us&country=US` | `pcdf-match-high` (not the priority-10 sibling) |
| 2 | Explicit no-match | `locale=en-us&country=CA` | `contentFound: false` |
| 3 | Reuse, no page campaign | `locale=en-us&country=US` and again with `&pageType=home` | Same `promotionId` both times |
| 4 | UK brand | `locale=en-gb&brand=TH` | `pcdf-gb-high` |
| 5 | Brand miss | `locale=en-gb&brand=XX` | `contentFound: false` |
| 6 | France match-all | `locale=fr` and `locale=fr&pageType=home` | `pcdf-fr-welcome` both |
| 7 | Italy tag | `locale=it&tag=estate` | `pcdf-it-sale` |
| 8 | Italy tag miss | `locale=it&tag=inverno` | `contentFound: false` |
| 9 | Germany promo | `locale=de&promo=SUMMER` | `pcdf-de-summer` |
| 10 | Germany promo fallback list | `locale=de&promo=WINTER` | `pcdf-de-always` |
| 11 | Hindi, reused id | `locale=hi` | `pcdf-match-high`, Hindi headline |
| 12 | Unknown locale | `locale=ja` | `contentFound: false` |
| 13 | Bad request | no `locale` | HTTP 400 `locale_required` |
| 14 | Preview blocked on live | Publish + `previewDate=2026-10-15` | HTTP 400 `preview_not_allowed` |
| 15 | Author preview | Author + auth + `locale=en-us&previewDate=2026-10-15` | `pcdf-future` |
| 16 | Author locked down | Author **without** credentials | HTTP 401 |
| 17 | Isolation | Install **only** the PCDF zip | `/apps/aem-poc/pcdf` present; default site not required |

## Out of scope (say this up front)

CDN/Akamai, time-of-day schedules, and a test-suite gate are not this POC. Cache key recommendation for a later CDN: `locale|country|brand|promo`.
