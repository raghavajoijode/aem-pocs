# PCDF quickstart

This file is the **validation guide for a future implementation**. This workstream does **not** implement PCDF (no `core.pcdf` module yet).

Visible proof **when built**: author a Content Fragment (or use samples), then see one JSON winner on **Publish** — or explicit `contentFound: false`. Preview only on signed-in Author.

**Where delivery lives** (from `docs/programmatic-content-delivery-requirements.md`): Publish delivery API under the PCDF identity, **`GET /services/aem-pocs/pcdf`**. Not a public Author campaign engine.

Contract: [contracts/delivery-api.yaml](./contracts/delivery-api.yaml). Sample IDs: [data-model.md](./data-model.md).

## Prerequisites

- **JDK 21** and Maven 3.3.9+
- Local **AEM 6.5 LTS SP2** **Author** `http://localhost:4502` and **Publish** `http://localhost:4503`
- Adobe Maven repo in `~/.m2/settings.xml`
- Local credentials: POM default `admin` / `admin` (do not commit real secrets)

## Build the PCDF-only package

From the repository root:

```bash
mvn clean install -pl pcdf -am
```

Shareable zip (names follow Maven coordinates):

`pcdf/target/aem-poc.pcdf-1.0.0-SNAPSHOT.zip`  
FileVault group/name: `com.aem.poc.pcdf`.

The zip must embed **separately**:

- OSGi bundle from `core.pcdf` (symbolic name `com.aem.poc.pcdf`)
- Apps package (`/apps/aem-pocs/pcdf`)
- Content package (`/conf/aem-pocs/pcdf`, `/content/dam/aem-pocs/pcdf`)

It must **not** embed `aem-poc.core` or `/apps/aem-poc`.

## Install

1. Package Manager on Author: upload and install the PCDF zip. Repeat on Publish (or install then replicate / use `mvn` auto-install Publish as documented in the PCDF README).
2. Confirm `/apps/aem-pocs/pcdf`, `/conf/aem-pocs/pcdf`, `/content/dam/aem-pocs/pcdf` exist.
3. Confirm default site `/apps/aem-poc` is **not** required for this demo.

Optional convenience (whole repo, not the hand-off): `mvn clean install -PautoInstallSinglePackage` still installs the site `all` package; reviewers proving isolation should use the PCDF zip only.

## Demo path

### Authoring

1. Sign in on Author.
2. Open DAM: `/content/dam/aem-pocs/pcdf/en_US`.
3. Open sample fragment `pcdf-match-high` (or create a new `ProgrammaticPromotion` CF). Confirm `startDate` / `endDate` are dates without time.
4. Publish the fragment if you changed it.

### Publish (live, anonymous)

```bash
# Winner (priority 20 beats 10)
curl -s "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&country=US"

# No-match (CA does not match US targeting; no fallback)
curl -s "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&country=CA"

# Preview must fail on Publish
curl -s -o /dev/stderr -w "%{http_code}" \
  "http://localhost:4503/services/aem-pocs/pcdf?locale=en_US&previewDate=2026-10-15"
```

**Expected**: first call `contentFound: true`, `promotionId: pcdf-match-high`. Second `contentFound: false`. Third HTTP 400 and `preview_not_allowed`. Winner selection for the sample set should feel immediate (under 100ms on this local pair).

### Author preview (signed in)

```bash
curl -s -u admin:admin \
  "http://localhost:4502/services/aem-pocs/pcdf?locale=en_US&previewDate=2026-10-15"
```

**Expected**: `contentFound: true` and `promotionId: pcdf-future` (window 2026-10-01..2026-10-31). Same URL **without** `previewDate` on Publish does not return `pcdf-future` before 2026-10-01.

Two “experiences”: repeat the winning Publish curl with a second query string that still matches (for example add `pageType` omitted). Same winner; no page has a campaign property.

## Cleanup

Uninstall the PCDF package from Package Manager on Author and Publish, or delete `/apps/aem-pocs/pcdf`, `/conf/aem-pocs/pcdf`, `/content/dam/aem-pocs/pcdf` if leftover. Stop using `/services/aem-pocs/pcdf`.
