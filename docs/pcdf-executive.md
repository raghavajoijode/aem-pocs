# PCDF — executive brief

**What it is:** A proof of concept so marketers author a promotion once, in the language folder that matches the experience, and every consumer app asks AEM Publish for **the single winner** (or an explicit “nothing matches”). No developer ticket per campaign. No campaign widget on each page.

**What success looks like:** A teammate installs one zip (`com.aem.poc.pcdf`) on Author and Publish. An author publishes a Content Fragment. A curl (or app) to Publish returns JSON. Isolation is proven if the default site (`/apps/aem-poc`) and default `core` bundle are not required.

Slide deck: [pcdf-brief.html](pcdf-brief.html). Operator install and curl table: [pcdf.md](pcdf.md).

## Authoring experience

Authors work only in **Assets / Content Fragments**, not in page properties.

- Locale is the **third DAM folder** (`/content/dam/aem-poc/pcdf/{region}/{country}/{locale}`, for example `americas/us/en-us`). Region, country, and locale query values are those folder names (lowercase).
- One fragment = one promotion. They do **not** create CF variations for language; they put the fragment in the right folder.
- They set copy (headline, body, image, CTA), targeting **start and end dates** (no time of day), targeting lists (empty means “any” for that dimension), integer **priority** (higher number wins), and **tags** for status (`pcdf:status/ACTIVE` or `INACTIVE`) and optional brand (`pcdf:brand/TH`).
- They **publish**. Unpublished work is invisible to Publish and to Author preview.
- They do **not** attach the promotion to a page. Home, PDP, and a mobile app can all request the same locale and targeting and receive the same winner.

## How delivery decides

1. Require `region`, `country`, and `locale`. Missing any → invalid request (HTTP 400).
2. **QueryBuilder** in that folder (optional CF `name` / `promo` as node name; tags `ACTIVE` and optional brand/topic). Unknown folder → no-match, **no** fallback.
3. Keep hits whose targeting date window includes today (Author may pass `previewDate` instead; Publish rejects preview).
4. Apply remaining targeting lists (market, property, page type, urlParameters when `name` is also sent).
5. Pick the highest priority; if tied, the lexicographically lower `promotionId`.

`promotionId` is unique **per folder**. Hindi and US English may both use `pcdf-match-high`.

## Demo / validation scenarios

Run after [PCDF-only install](pcdf.md#build-and-deploy-pcdf-alone). Publish base: `http://localhost:4503/services/aem-poc/pcdf`.

| # | Story | Call | Pass if |
| --- | --- | --- | --- |
| 1 | US priority | `region=americas&country=us&locale=en-us` | `pcdf-match-high` (not the priority-10 sibling) |
| 2 | Explicit no-match | `region=americas&country=ca&locale=en-us` | `contentFound: false` |
| 3 | Reuse, no page campaign | same as #1 and again with `&pageType=home` | Same `promotionId` both times |
| 4 | UK brand | `region=emea&country=gb&locale=en-gb&brand=TH` | `pcdf-gb-high` |
| 5 | Brand miss | `region=emea&country=gb&locale=en-gb&brand=XX` | `contentFound: false` |
| 6 | France match-all | `region=emea&country=fr&locale=fr` and with `&pageType=home` | `pcdf-fr-welcome` both |
| 7 | Italy tag | `region=emea&country=it&locale=it&tag=estate` | `pcdf-it-sale` |
| 8 | Italy tag miss | `region=emea&country=it&locale=it&tag=inverno` | `contentFound: false` |
| 9 | Germany by CF name | `region=emea&country=de&locale=de&promo=pcdf-de-summer` | `pcdf-de-summer` |
| 10 | Germany other CF | `region=emea&country=de&locale=de&name=pcdf-de-always` | `pcdf-de-always` |
| 11 | Hindi, reused id | `region=apac&country=in&locale=hi` | `pcdf-match-high`, Hindi headline |
| 12 | Unknown locale | `region=apac&country=jp&locale=ja` | `contentFound: false` |
| 13 | Bad request | no `locale` | HTTP 400 `locale_required` |
| 14 | Preview blocked on live | Publish + `previewDate=2026-10-15` (with topology params) | HTTP 400 `preview_not_allowed` |
| 15 | Author preview | Author + auth + `region=americas&country=us&locale=en-us&previewDate=2026-10-15` | `pcdf-future` |
| 16 | Author locked down | Author **without** credentials | HTTP 401 |
| 17 | Isolation | Install **only** the PCDF zip | `/apps/aem-poc/pcdf` present; default site not required |

## Out of scope (say this up front)

CDN/Akamai, time-of-day schedules, and a test-suite gate are not this POC. Cache key recommendation for a later CDN: `region|country|locale|brand|promo`.
