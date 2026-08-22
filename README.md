# AEM POCs

Multi-module Adobe Experience Manager project for **proofs of concept**, not a
production product template. Goal: try an idea, show a visible result, and leave
clear install / demo / usage steps for the next person.

Artifact: `com.aem.poc:aem-poc` (`1.0.0-SNAPSHOT`). App id / title: **aem-poc** /
**AEM POCs**. Generated from the AEM project archetype with example content
enabled and a general (Webpack) frontend module.

## Prerequisites

- **JDK 21** and **Maven 3.3.9+**
- Local **AEM 6.5 LTS SP2** **Author** at `http://localhost:4502` (default); Publish at
  `http://localhost:4503` when the POC needs it
- Adobe Maven repositories configured in `~/.m2/settings.xml` — see
  [Set up the Adobe Maven repository](https://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html)
- Default local credentials in the POM are archetype `admin` / `admin`; do not
  commit real secrets

## Modules

| Module | Role |
| --- | --- |
| `core` | OSGi services, servlets, filters, Sling Models, and other Java |
| `ui.apps` | `/apps` components, templates, clientlibs |
| `ui.content` | Sample content for demos |
| `ui.config` | Runmode-specific OSGi configs |
| `ui.apps.structure` | Repository structure package |
| `ui.frontend` | General Webpack frontend → AEM clientlibs |
| `ui.frontend.react` | Optional React SPA frontend (use only when the POC needs it) |
| `all` | Single package embedding bundles and content packages |
| `dispatcher.ams` | Dispatcher config for AMS-style setups |
| `dispatcher.cloud` | Dispatcher config for AEMaaCS-style setups |

There is **no** `it.tests`, `analyse`, or UI-test module in this checkout.
Tests are optional for POCs; prefer a visible Author/Publish outcome and written
demo steps.

## Build and install

From the project root:

```bash
# Build everything
mvn clean install

# Build and deploy the `all` package to local Author (4502)
mvn clean install -PautoInstallSinglePackage

# Deploy to Publish (4503)
mvn clean install -PautoInstallSinglePackagePublish

# Same idea with an explicit port
mvn clean install -PautoInstallSinglePackage -Daem.port=4503

# Deploy only the `core` bundle to Author
mvn clean install -PautoInstallBundle

# Deploy a single content package from its module (e.g. ui.apps)
cd ui.apps && mvn clean install -PautoInstallPackage
```

Frontend clientlibs are produced during the Maven build when the frontend
modules run. See `ui.frontend/README.md` (and `ui.frontend.react/README.md` if
you use that module).

## How we run POCs

For each POC, document in the root README (or a short note under the feature):

1. **Hypothesis** and non-goals  
2. **Where the code lives** (modules / content paths)  
3. **Install steps** (commands above, plus any extras)  
4. **Demo path** (page, component, or console to open)  
5. **Expected output** (what success looks like)  
6. **Cleanup** if sample content or configs should be removed later  

Prefer patterns already in this repo (HTL, Sling Models, OSGi configs,
clientlibs). Deviate only when the experiment requires it, and say so in the
POC notes.

## Spec Kit (optional)

This repo is set up with [GitHub Spec Kit](https://github.com/github/spec-kit)
for structured specify → plan → tasks → implement flows. Cursor skills live
under `.cursor/skills/`; Copilot skills under `.github/skills/`. Shared Spec Kit
files are under `.specify/`.

Agent orientation for any IDE: see [`AGENTS.md`](AGENTS.md).

## Related docs

- [`README-CIF.md`](README-CIF.md) — Commerce Integration Framework notes (CIF
  not enabled in `archetype.properties`)
- [`README-precompiled-scripts.md`](README-precompiled-scripts.md) — precompiled
  scripts notes
- Frontend: [`ui.frontend/README.md`](ui.frontend/README.md),
  [`ui.frontend.react/README.md`](ui.frontend.react/README.md)
- Dispatcher: [`dispatcher.ams/README.md`](dispatcher.ams/README.md),
  [`dispatcher.cloud/README.md`](dispatcher.cloud/README.md)
