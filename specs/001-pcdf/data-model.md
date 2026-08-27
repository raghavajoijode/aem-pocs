# Data model: PCDF

## Promotion (Content Fragment)

**Model name**: `ProgrammaticPromotion`  
**Model path**: `/conf/aem-poc-pcdf/settings/dam/cfm/models/programmatic-promotion` (implementation may use an equivalent CFM path under `/conf/aem-poc-pcdf`).  
**Instance path**: `/content/dam/aem-poc/pcdf/{region}/{country}/{locale}/{promotionName}`  
**Cardinality**: one fragment = one promotion; no variations.

### Fields

| Field | Role | Type | Rules |
| --- | --- | --- | --- |
| `promotionId` | Administration | String | Required. Tie-break key (lexicographically lower wins). Unique **within a region/country/locale folder**; the same id MAY be used in other folders. |
| `priority` | Administration | Integer | Required. Higher number wins. |
| `headline` | Content | String | Required for a useful demo response. |
| `body` | Content | Text / String | Optional in storage; included in the match response when present (empty string if blank). |
| `image` | Content | Content reference (DAM image) | Optional; match response uses the asset path (or empty). |
| `ctaText` | Content | String | Optional. |
| `ctaLink` | Content | String (URL or path) | Optional. |
| `startDate` | Targeting | Date (date only) | Required. Inclusive. No time of day. |
| `endDate` | Targeting | Date (date only) | Required. Inclusive. Must be on or after `startDate` for sample content. |
| `markets` | Targeting | String[] | Empty = match-all for `market`. |
| `properties` | Targeting | String[] | Empty = match-all for `property`. |
| `pageTypes` | Targeting | String[] | Empty = match-all for `pageType`. |
| `urlParameters` | Targeting | String[] | Empty = match-all for request `promo`. |

### Tags (on the Content Fragment, not model fields)

Namespace: `/content/cq:tags/pcdf`. Stored as `cq:tags`.

| Tag | Role | Rules |
| --- | --- | --- |
| `pcdf:status/ACTIVE` or `pcdf:status/INACTIVE` | Status | Required for eligibility. Only ACTIVE is eligible. |
| `pcdf:brand/{code}` | Brand | Optional. Empty brand tags = match-all for request `brand`. |
| `pcdf:topic/{name}` | Topic | Optional. If the request includes `tag`, the fragment must contain that topic (or the same leaf name). |

### Lifecycle

```text
created (status tag typically INACTIVE or ACTIVE)
  → ACTIVE  : eligible if evaluation date is in [startDate, endDate]
  → INACTIVE: never eligible, even if dates and targeting match
```

Unpublished fragments are not visible on Publish. Author preview uses the same rule: **only published** fragments may win (plus `previewDate` for the calendar window).

## Topology folder

**Path**: `/content/dam/aem-poc/pcdf/{region}/{country}/{locale}`  
**Examples**: `americas/us/en-us`, `emea/gb/en-gb`, `emea/fr/fr`, `emea/it/it`, `emea/de/de`, `apac/in/hi`  
**Rule**: Delivery loads candidates only from the folder whose three segments equal request `region`, `country`, and `locale` (exact, lowercase). Unknown folder → no candidates → `{ "contentFound": false }` (not a substitute). Missing `region`, `country`, or `locale` parameter → 400. Underscore locale forms such as `en_US` are not the same as `en-us`. Country codes are lowercase folder names (`us`, not `US`).

## Delivery request (not stored)

See [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). Evaluation date:

- Publish: today’s calendar date on the instance; `previewDate` forbidden.
- Author (authenticated): `previewDate` if present, else today.

## Delivery result (not stored)

Match: `contentFound=true` plus `promotionId`, `headline`, `body`, `image`, `ctaText`, `ctaLink`.  
No-match: `contentFound=false` and no substitute promotion fields.

## Sample set (required for demo)

Place under `/content/dam/aem-poc/pcdf/{region}/{country}/{locale}/`.

### `americas/us/en-us` — priority, inactive, preview

| promotionId | status tag | priority | startDate | endDate | Other | Purpose |
| --- | --- | --- | --- | --- | --- | --- |
| `pcdf-match-low` | ACTIVE | 10 | 2020-01-01 | 2030-12-31 | | Loses priority contest |
| `pcdf-match-high` | ACTIVE | 20 | 2020-01-01 | 2030-12-31 | | Winner when `region=americas&country=us&locale=en-us` |
| `pcdf-future` | ACTIVE | 50 | 2026-10-01 | 2026-10-31 | | Preview-only until October 2026 |
| `pcdf-inactive` | INACTIVE | 99 | 2020-01-01 | 2030-12-31 | | Never returned |

No-match: `region=americas&country=ca&locale=en-us` (folder does not exist).

### Other folders

| Folder | promotionId | Targeting / notes | Demo |
| --- | --- | --- | --- |
| `emea/gb/en-gb` | `pcdf-gb-high` (20), `pcdf-gb-low` (10) | `pcdf:brand/TH` | `brand=TH` → high; `brand=XX` → no-match |
| `emea/fr/fr` | `pcdf-fr-welcome` | empty lists (match-all) | `locale=fr` and `&pageType=home` same winner |
| `emea/it/it` | `pcdf-it-sale` (20, `pcdf:topic/estate`), `pcdf-it-evergreen` (5) | topic inclusion | `tag=estate` → sale; `tag=inverno` → no-match |
| `emea/de/de` | `pcdf-de-summer` (20, `urlParameters=SUMMER`), `pcdf-de-always` (5) | CF name | `promo=pcdf-de-summer` or `name=pcdf-de-always` |
| `apac/in/hi` | `pcdf-match-high` | empty; **same id as en-us** | per-folder uniqueness |

Unknown folder (for example `apac/jp/ja`) → no-match.

## Validation summary

- Empty targeting list → dimension does not constrain.
- Empty brand tags → brand does not constrain.
- Omitted request parameter → dimension does not constrain.
- Request `promo` matches `urlParameters`.
- Request `tag` requires exact inclusion of a topic tag leaf.
- Request `brand` requires inclusion of a brand tag leaf when brand tags are present.
- Ranking: highest integer `priority` (larger wins), then lowest `promotionId` within that folder.
