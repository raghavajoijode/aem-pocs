# PCDF (Programmatic Content Delivery Framework)

Hypothesis: authors can manage locale-scoped promotions as **one Content Fragment each** (no variations, no page campaign wiring). Consumer apps call Publish and receive **one winner** or `{ "contentFound": false }`.

## Non-goals

- CDN / Akamai implementation (cache-key recommendation only: `locale|country|brand|promo`)
- Time-of-day or timezone-aware instants (`startDate` / `endDate` are **calendar dates**)
- PCDF code in default `core` or nodes under `/apps/aem-poc`
- Test-suite coverage as an acceptance gate

## Where it lives

| Piece | Location |
| --- | --- |
| Java (servlet, eligibility, query) | `core.pcdf` — Bundle-SymbolicName `com.aem.poc.pcdf` |
| Apps root | `/apps/aem-pocs/pcdf` (`ui.apps.pcdf`) |
| OSGi runmode config | `/apps/aem-pocs/pcdf/osgiconfig` (`ui.config.pcdf`) |
| CF model | `/conf/aem-pocs/pcdf/settings/dam/cfm/models/programmatic-promotion` |
| Sample fragments | `/content/dam/aem-pocs/pcdf/en_US/` |
| Shareable zip | `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip` — FileVault group/name `com.aem.poc.pcdf` |

The zip embeds **only** `aem-poc.core.pcdf`, `aem-poc.ui.apps.pcdf`, `aem-poc.ui.content.pcdf`, and `aem-poc.ui.config.pcdf`. It does **not** embed `aem-poc.core`, `aem-poc.ui.apps`, or `aem-poc.ui.config`. Site `all` is optional convenience and is **not** required for isolation.

## Authoring

1. Sign in on Author (`http://localhost:4502`).
2. Open `/content/dam/aem-pocs/pcdf/en_US`.
3. Create or edit a **Programmatic Promotion** fragment. Set `promotionId` (unique in that locale folder), `status` (`ACTIVE` / `INACTIVE`), integer `priority` (larger wins), date-only `startDate` / `endDate` (inclusive), and optional targeting lists (empty = match-all).
4. Do **not** put campaign properties on pages. Delivery is request-driven.
5. Publish the fragment. Author preview and Publish delivery both consider **published** fragments only.

## Install

Requires **JDK 21** and local **AEM 6.5 LTS SP2** (Author `4502`, Publish `4503`).

```bash
mvn clean install -pl pcdf -am
```

Upload `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip` in Package Manager on Author and again on Publish (or `mvn clean install -pl pcdf -am -PautoInstallPackage` / `-PautoInstallPackagePublish` from the `pcdf` module).

## Demo / expected JSON

Publish (anonymous):

```bash
curl -s "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&country=US"
# {"contentFound":true,"promotionId":"pcdf-match-high","headline":"Fall welcome",...}

curl -s "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&country=CA"
# {"contentFound":false,"promotionId":"","headline":"","body":"","image":"","ctaText":"","ctaLink":""}

curl -s "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&country=US&pageType=home"
# Same winner as the first call (omitted/unconstrained dimensions). No page wiring.

curl -s -o /dev/stderr -w "%{http_code}" \
  "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&previewDate=2026-10-15"
# HTTP 400 {"error":"preview_not_allowed"}
```

Missing `locale` → HTTP 400 `{"error":"locale_required"}`. Unknown locale folder → `contentFound: false` (no fallback locale).

Author preview (authenticated; published fragments only):

```bash
curl -s -u admin:admin \
  "http://localhost:4502/services/aem-pocs/pcdf?locale=en_US&previewDate=2026-10-15"
# promotionId pcdf-future (window 2026-10-01..2026-10-31)
```

Without credentials on Author the path stays authenticated (HTTP 401).

### Sample-set timing (SC-005)

Winner selection for these four fragments is an in-memory filter/rank after listing the locale folder. On a local Author/Publish pair that is expected **under 100ms**. This cloud workspace does not run AEM 6.5 LTS, so live curl timing was not recorded here; re-check with `curl -s -o /dev/null -w "%{time_total}"` against your `localhost:4503` sample-set request.

## Cleanup

Uninstall `com.aem.poc.pcdf` from Package Manager on Author and Publish, or delete `/apps/aem-pocs/pcdf`, `/apps/aem-pocs-pcdf-packages`, `/conf/aem-pocs/pcdf`, and `/content/dam/aem-pocs/pcdf`.
