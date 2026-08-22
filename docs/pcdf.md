# PCDF (Programmatic Content Delivery Framework)

Hypothesis: authors can manage locale-scoped promotions as **one Content Fragment each** (no variations, no page campaign wiring). Consumer apps call Publish and receive **one winner** or `{ "contentFound": false }`.

Locale folders use lowercase hyphenated codes (`en-us`, `en-gb`, `fr`, `it`, `de`, `hi`). Request `locale` must match the folder name exactly (`en_US` is not `en-us`).

Stakeholder brief and validation scenarios: [`pcdf-executive.md`](pcdf-executive.md). Slide deck (open in a browser): [`pcdf-brief.html`](pcdf-brief.html).

## Non-goals

- CDN / Akamai implementation (cache-key recommendation only: `locale|country|brand|promo`)
- Time-of-day or timezone-aware instants (`startDate` / `endDate` are **calendar dates**)
- PCDF code in default `core` or site nodes under `/apps/aem-poc/components` (PCDF stays in `/apps/aem-poc/pcdf`)
- Test-suite coverage as an acceptance gate

## Where it lives

| Piece | Location |
| --- | --- |
| Java (servlet, eligibility, query) | `core.pcdf` — Bundle-SymbolicName `com.aem.poc.pcdf` |
| Apps root | `/apps/aem-poc/pcdf` (`ui.apps.pcdf`) |
| OSGi runmode config | `/apps/aem-poc/pcdf/config.publish` (`ui.config.pcdf`) |
| CF model | `/conf/aem-poc/pcdf/settings/dam/cfm/models/programmatic-promotion` |
| Sample fragments | `/content/dam/aem-poc/pcdf/{en-us,en-gb,fr,it,de,hi}/` |
| Shareable zip | `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip` — FileVault group/name `com.aem.poc.pcdf` |

The zip embeds **only** `aem-poc.core.pcdf`, `aem-poc.ui.apps.pcdf`, `aem-poc.ui.content.pcdf`, and `aem-poc.ui.config.pcdf` under `/apps/aem-poc-packages/pcdf`. It does **not** embed `aem-poc.core`, `aem-poc.ui.apps`, or `aem-poc.ui.config`. The site `all` package embeds those **same four artifacts** at the same path (not the PCDF container zip), so `mvn clean install -PautoInstallSinglePackage` deploys the default site **and** PCDF once. Use `-pl pcdf` when you need PCDF without the rest of the site.

## Authoring experience

Authors stay in the stock AEM Content Fragment editor. They do not open pages, campaigns, or code.

1. Sign in on Author (`http://localhost:4502`).
2. Open DAM: `/content/dam/aem-poc/pcdf/` and pick a locale folder (`en-us`, `en-gb`, `fr`, `it`, `de`, `hi`, or a new folder with the same lowercase hyphenated name you will send as `locale`).
3. Create a **Programmatic Promotion** fragment (model `ProgrammaticPromotion`). One fragment = one promotion; **do not** use CF variations for locale.
4. Fill administration (`promotionId` unique **in this folder**, `status` `ACTIVE` / `INACTIVE`, integer `priority` — larger wins), date-only `startDate` / `endDate` (inclusive), content (headline, body, image, CTA), and optional targeting lists (empty = match-all for that dimension except `tag`, which must include the request value when `tag` is sent).
5. Publish the fragment. Live Publish and Author preview both consider **published** fragments only. Unpublished drafts never win.
6. Do **not** put campaign properties on pages. Apps reuse a promotion by calling delivery with locale and context.

## Build and deploy PCDF alone

Requires **JDK 21** and local **AEM 6.5 LTS SP2** (Author `4502`, Publish `4503`). These commands **do not** install the site `all` package.

```bash
# Build only the PCDF reactor (core.pcdf + apps + content + config + container zip)
mvn clean install -pl pcdf -am

# Build and install that zip on local Author
mvn clean install -pl pcdf -am -PautoInstallPcdf

# Same zip on local Publish
mvn clean install -pl pcdf -am -PautoInstallPcdfPublish
```

Manual alternative: upload `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip` in Package Manager on Author and again on Publish.

Full-repo `mvn clean install -PautoInstallSinglePackage` installs site `all`, which embeds the PCDF artifacts under `/apps/aem-poc-packages/pcdf`. Isolation reviews should still use the `-pl pcdf` commands above.

## Demo / expected JSON (Publish, anonymous)

Base: `http://localhost:4503/services/aem-poc/pcdf`

| Flow | Request | Expected |
| --- | --- | --- |
| Priority winner | `?locale=en-us&country=US` | `pcdf-match-high` |
| Country miss | `?locale=en-us&country=CA` | `contentFound: false` |
| Reuse (no page wiring) | `?locale=en-us&country=US&pageType=home` | same `pcdf-match-high` |
| Brand winner | `?locale=en-gb&brand=TH` | `pcdf-gb-high` |
| Brand miss | `?locale=en-gb&brand=XX` | `contentFound: false` |
| Match-all locale | `?locale=fr` | `pcdf-fr-welcome` |
| Second experience | `?locale=fr&pageType=home` | same `pcdf-fr-welcome` |
| Tag hit | `?locale=it&tag=estate` | `pcdf-it-sale` |
| Tag miss | `?locale=it&tag=inverno` | `contentFound: false` |
| Promo hit | `?locale=de&promo=SUMMER` | `pcdf-de-summer` |
| Promo miss → match-all | `?locale=de&promo=WINTER` | `pcdf-de-always` |
| Same id, other locale | `?locale=hi` | `pcdf-match-high` (Hindi copy) |
| Unknown locale | `?locale=ja` | `contentFound: false` (no fallback) |
| Missing locale | *(no query)* | HTTP 400 `locale_required` |
| Preview on Publish | `?locale=en-us&previewDate=2026-10-15` | HTTP 400 `preview_not_allowed` |

Author preview (authenticated; published fragments only):

```bash
curl -s -u admin:admin \
  "http://localhost:4502/services/aem-poc/pcdf?locale=en-us&previewDate=2026-10-15"
# promotionId pcdf-future (window 2026-10-01..2026-10-31)
```

Without credentials on Author the path stays authenticated (HTTP 401).

### Sample-set timing (SC-005)

Winner selection is an in-memory filter/rank after listing the locale folder. On a local Author/Publish pair that is expected **under 100ms**. Re-check with `curl -s -o /dev/null -w "%{time_total}"` against your `localhost:4503` sample-set request.

## Why `PublishAnonymousAccess` is a separate component

Sling decides authentication **before** the servlet runs. Anonymous Publish callers never reach `doGet` if the path is protected.

`PublishAnonymousAccess` is an OSGi component that registers `sling.auth.requirements=-/services/aem-poc/pcdf`. It uses `configurationPolicy = REQUIRE` and is **only** given config under `config.publish`. Author has no such config, so Author stays signed-in.

Putting the same `sling.auth.requirements` property on `PromotionDeliveryServlet` would apply wherever the bundle starts (Author and Publish). The servlet cannot “skip auth” itself: a 401 happens upstream. Keep the exemption as this Publish-only component (or equivalent Publish runmode config). The servlet still rejects `previewDate` on Publish as defense in depth.

## Cleanup

Uninstall `com.aem.poc.pcdf` from Package Manager on Author and Publish, or delete `/apps/aem-poc/pcdf`, `/apps/aem-poc-packages/pcdf`, `/conf/aem-poc/pcdf`, and `/content/dam/aem-poc/pcdf`.
