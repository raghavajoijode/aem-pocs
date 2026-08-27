# PCDF quickstart

Visible proof: author a Content Fragment (or use samples), then see one JSON winner on **Publish** — or explicit `contentFound: false`. Preview only on signed-in Author.

**Where delivery lives**: Publish `GET /services/aem-poc/pcdf`. Authenticated on Author.

Topology folders: `/content/dam/aem-poc/pcdf/{region}/{country}/{locale}` — samples `americas/us/en-us`, `emea/gb/en-gb`, `emea/fr/fr`, `emea/it/it`, `emea/de/de`, `apac/in/hi`. Status and brand are tags (`pcdf:status/*`, `pcdf:brand/*`). Start and end dates are targeting fields.

Contract: [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). Samples: [data-model.md](./data-model.md). Install: [`docs/pcdf.md`](../../docs/pcdf.md). Executive / validation: [`docs/pcdf-executive.md`](../../docs/pcdf-executive.md). Slides: [`docs/pcdf-brief.html`](../../docs/pcdf-brief.html).

## Prerequisites

- **JDK 21** and Maven 3.3.9+
- Local **AEM 6.5 LTS SP2** **Author** `http://localhost:4502` and **Publish** `http://localhost:4503`
- Adobe Maven repo in `~/.m2/settings.xml`
- Local credentials: POM default `admin` / `admin` (do not commit real secrets)

## Build and deploy PCDF alone

From the repository root (does **not** install the site `all` package):

```bash
mvn clean install -pl pcdf -am
mvn clean install -pl pcdf -am -PautoInstallPcdf
mvn clean install -pl pcdf -am -PautoInstallPcdfPublish
```

Shareable zip: `pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip`  
FileVault group/name: `com.aem.poc.pcdf`.

The zip embeds `core.pcdf`, `ui.apps.pcdf`, `ui.config.pcdf`, and `ui.content.pcdf` only — not `aem-poc.core` or site components.

Confirm `/apps/aem-poc/pcdf`, `/conf/aem-poc-pcdf`, `/content/cq:tags/pcdf`, `/content/dam/aem-poc/pcdf` exist. Whole-repo `mvn clean install -PautoInstallSinglePackage` installs site `all` with the same PCDF artifacts under `/apps/aem-poc-packages/pcdf`. Isolation reviews should still use `-pl pcdf` only.

## Demo path

### Authoring

1. Sign in on Author.
2. Open DAM: `/content/dam/aem-poc/pcdf/americas/us/en-us` (also `emea/gb/en-gb`, `emea/fr/fr`, `emea/it/it`, `emea/de/de`, `apac/in/hi`).
3. Open sample fragment `pcdf-match-high` (or create a new `ProgrammaticPromotion` CF). Confirm `startDate` / `endDate` are targeting dates without time. Confirm status tag `pcdf:status/ACTIVE`.
4. Publish the fragment if you changed it.
5. Authors do not configure campaigns on individual pages.

### Publish (live, anonymous)

```bash
# First exact match (by CF name)
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=americas&country=us&locale=en-us&name=pcdf-match-high"

# Same fragment from a second experience
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=americas&country=us&locale=en-us&name=pcdf-match-high&pageType=home"

# No-match (unknown country folder)
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=americas&country=ca&locale=en-us"

curl -s "http://localhost:4503/services/aem-poc/pcdf?region=emea&country=gb&locale=en-gb&brand=TH&name=pcdf-gb-high"
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=emea&country=fr&locale=fr"
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=emea&country=it&locale=it&tag=estate"
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=emea&country=de&locale=de&promo=pcdf-de-summer"
curl -s "http://localhost:4503/services/aem-poc/pcdf?region=apac&country=in&locale=hi"

# Preview must fail on Publish
curl -s -o /dev/stderr -w "%{http_code}" \
  "http://localhost:4503/services/aem-poc/pcdf?region=americas&country=us&locale=en-us&previewDate=2026-10-15"
```

**Expected**: `name=pcdf-match-high` → `pcdf-match-high`; `country=ca` → `contentFound: false`; `emea/gb/en-gb&brand=TH&name=pcdf-gb-high` → `pcdf-gb-high`; `fr` → `pcdf-fr-welcome`; `it&tag=estate` → `pcdf-it-sale`; `de&promo=pcdf-de-summer` → `pcdf-de-summer`; `hi` → `pcdf-match-high` (Hindi). Preview on Publish → HTTP 400 `preview_not_allowed`. Full table: [`docs/pcdf-executive.md`](../../docs/pcdf-executive.md).

### Author preview (signed in)

```bash
curl -s -u admin:admin \
  "http://localhost:4502/services/aem-poc/pcdf?region=americas&country=us&locale=en-us&previewDate=2026-10-15"
```

**Expected**: `pcdf-future`. Publish without preview does not return that fragment before 2026-10-01.

## Cleanup

Uninstall the PCDF package from Package Manager on Author and Publish, or delete `/apps/aem-poc/pcdf`, `/apps/aem-poc-packages/pcdf`, `/conf/aem-poc-pcdf`, `/content/cq:tags/pcdf`, `/content/dam/aem-poc/pcdf`.
