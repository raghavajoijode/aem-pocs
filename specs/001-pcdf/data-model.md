# Data model: PCDF

## Promotion (Content Fragment)

**Model name**: `ProgrammaticPromotion`  
**Model path**: `/conf/aem-poc-pcdf/settings/dam/cfm/models/programmatic-promotion` (implementation may use an equivalent CFM path under `/conf/aem-poc-pcdf`).  
**Instance path**: `/content/dam/aem-poc/pcdf/{locale}/{promotionName}`  
**Cardinality**: one fragment = one promotion; no variations.

### Fields

| Field | Role | Type | Rules |
| --- | --- | --- | --- |
| `promotionId` | Administration | String | Required. Tie-break key (lexicographically lower wins). Unique **within a locale folder**; the same id MAY be used in other locales. |
| `status` | Administration | Enumeration | Required. At least `ACTIVE` and `INACTIVE`. Only `ACTIVE` is eligible. |
| `priority` | Administration | Integer | Required. Higher number wins. |
| `tags` | Administration | String[] | Optional. If the request includes `tag`, the fragment must contain that exact value. |
| `headline` | Content | String | Required for a useful demo response. |
| `body` | Content | Text / String | Optional in storage; included in the match response when present (empty string if blank). |
| `image` | Content | Content reference (DAM image) | Optional; match response uses the asset path (or empty). |
| `ctaText` | Content | String | Optional. |
| `ctaLink` | Content | String (URL or path) | Optional. |
| `startDate` | Schedule | Date (date only) | Required. Inclusive. No time of day. |
| `endDate` | Schedule | Date (date only) | Required. Inclusive. Must be on or after `startDate` for sample content. |
| `countries` | Targeting | String[] | Empty = match-all for `country`. |
| `markets` | Targeting | String[] | Empty = match-all for `market`. |
| `brands` | Targeting | String[] | Empty = match-all for `brand`. |
| `properties` | Targeting | String[] | Empty = match-all for `property`. |
| `pageTypes` | Targeting | String[] | Empty = match-all for `pageType`. |
| `urlParameters` | Targeting | String[] | Empty = match-all for request `promo`. |

### Lifecycle

```text
created (status typically INACTIVE or ACTIVE)
  → ACTIVE  : eligible if evaluation date is in [startDate, endDate]
  → INACTIVE: never eligible, even if dates and targeting match
```

Unpublished fragments are not visible on Publish. Author preview uses the same rule: **only published** fragments may win (plus `previewDate` for the calendar window).

## Locale folder

**Path**: `/content/dam/aem-poc/pcdf/{locale}`  
**Example**: `en-us`, `en-gb`, `fr`, `it`, `de`, `hi`  
**Rule**: Delivery loads candidates only from the folder whose name equals request `locale` (exact, case-sensitive). Unknown locale → no candidates → `{ "contentFound": false }` (not a substitute locale). Missing `locale` parameter → 400. Underscore forms such as `en_US` are not the same as `en-us`.

## Delivery request (not stored)

See [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). Evaluation date:

- Publish: today’s calendar date on the instance; `previewDate` forbidden.
- Author (authenticated): `previewDate` if present, else today.

## Delivery result (not stored)

Match: `contentFound=true` plus `promotionId`, `headline`, `body`, `image`, `ctaText`, `ctaLink`.  
No-match: `contentFound=false` and no substitute promotion fields.

## Sample set (required for demo)

Place under `/content/dam/aem-poc/pcdf/` (folder name = request `locale`):

### `en-us` — priority, country, inactive, preview

| promotionId | status | priority | startDate | endDate | Targeting (non-empty) | Purpose |
| --- | --- | --- | --- | --- | --- | --- |
| `pcdf-match-low` | ACTIVE | 10 | 2020-01-01 | 2030-12-31 | `countries=US` | Loses priority contest |
| `pcdf-match-high` | ACTIVE | 20 | 2020-01-01 | 2030-12-31 | `countries=US` | Winner when `locale=en-us&country=US` |
| `pcdf-future` | ACTIVE | 50 | 2026-10-01 | 2026-10-31 | (empty) | Preview-only until October 2026 |
| `pcdf-inactive` | INACTIVE | 99 | 2020-01-01 | 2030-12-31 | (empty) | Never returned |

No-match: `locale=en-us&country=CA`.

### Other locales

| Folder | promotionId | Targeting / notes | Demo |
| --- | --- | --- | --- |
| `en-gb` | `pcdf-gb-high` (20), `pcdf-gb-low` (10) | `brands=TH` | `brand=TH` → high; `brand=XX` → no-match |
| `fr` | `pcdf-fr-welcome` | empty (match-all) | `locale=fr` and `locale=fr&pageType=home` same winner |
| `it` | `pcdf-it-sale` (20, `tags=estate`), `pcdf-it-evergreen` (5) | tag inclusion | `tag=estate` → sale; `tag=inverno` → no-match |
| `de` | `pcdf-de-summer` (20, `urlParameters=SUMMER`), `pcdf-de-always` (5) | promo | `promo=SUMMER` → summer; `promo=WINTER` → always |
| `hi` | `pcdf-match-high` | empty; **same id as en-us** | per-locale uniqueness |

Unknown folder (for example `ja`) → no-match.

## Validation summary

- Empty targeting list → dimension does not constrain.
- Omitted request parameter → dimension does not constrain.
- Request `promo` matches `urlParameters`.
- Request `tag` requires exact inclusion in `tags`.
- Ranking: highest integer `priority` (larger wins), then lowest `promotionId` within that locale.
