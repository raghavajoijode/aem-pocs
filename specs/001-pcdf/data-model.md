# Data model: PCDF

## Promotion (Content Fragment)

**Model name**: `ProgrammaticPromotion`  
**Model path**: `/conf/aem-pocs/pcdf/settings/dam/cfm/models/programmatic-promotion` (implementation may use an equivalent CFM path under `/conf/aem-pocs/pcdf`).  
**Instance path**: `/content/dam/aem-pocs/pcdf/{locale}/{promotionName}`  
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

**Path**: `/content/dam/aem-pocs/pcdf/{locale}`  
**Example**: `en_US`, `fr_CA`  
**Rule**: Delivery loads candidates only from the folder whose name equals request `locale`. Unknown locale → no candidates → `{ "contentFound": false }` (not a substitute locale). Missing `locale` parameter → 400.

## Delivery request (not stored)

See [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). Evaluation date:

- Publish: today’s calendar date on the instance; `previewDate` forbidden.
- Author (authenticated): `previewDate` if present, else today.

## Delivery result (not stored)

Match: `contentFound=true` plus `promotionId`, `headline`, `body`, `image`, `ctaText`, `ctaLink`.  
No-match: `contentFound=false` and no substitute promotion fields.

## Sample set (required for demo)

Place under `/content/dam/aem-pocs/pcdf/en_US/` (names may vary; IDs must match the quickstart):

| promotionId | status | priority | startDate | endDate | Targeting (non-empty) | Purpose |
| --- | --- | --- | --- | --- | --- | --- |
| `pcdf-match-low` | ACTIVE | 10 | 2020-01-01 | 2030-12-31 | `countries=US` | Loses priority contest |
| `pcdf-match-high` | ACTIVE | 20 | 2020-01-01 | 2030-12-31 | `countries=US` | Winner when `locale=en_US&country=US` |
| `pcdf-future` | ACTIVE | 50 | 2026-10-01 | 2026-10-31 | (empty) | Preview-only until October 2026 |
| `pcdf-inactive` | INACTIVE | 99 | 2020-01-01 | 2030-12-31 | (empty) | Never returned |

No-match demo: `locale=en_US&country=CA` (US-targeted fragments do not match; future/inactive excluded).

## Validation summary

- Empty targeting list → dimension does not constrain.
- Omitted request parameter → dimension does not constrain.
- Request `promo` matches `urlParameters`.
- Request `tag` requires exact inclusion in `tags`.
- Ranking: highest integer `priority` (larger wins), then lowest `promotionId` within that locale.
