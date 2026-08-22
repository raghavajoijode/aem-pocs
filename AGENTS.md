# Agent orientation — AEM POCs

Short map for coding agents (Cursor, Copilot, Claude, etc.). This is **not** a
dump of project governance; follow Spec Kit skills when present.

## What this repo is

- **AEM multi-module Maven** project for **POCs**, not a finished product
- Group/artifact: `com.aem.poc` / `aem-poc`
- Default Author `localhost:4502`, Publish `localhost:4503`
- Success = **visible outcome** + **install / demo / usage steps**, not test
  coverage

## Where to change what

| Goal | Put it in |
| --- | --- |
| Java (services, servlets, models) | `core` |
| Components, templates, clientlibs under `/apps` | `ui.apps` |
| Sample pages / demo content | `ui.content` |
| OSGi runmode config | `ui.config` |
| General Webpack UI build | `ui.frontend` |
| React SPA experiment | `ui.frontend.react` (only if needed) |
| Combined deploy package | `all` |
| Dispatcher | `dispatcher.ams` or `dispatcher.cloud` |

Do not invent parallel module trees. Prefer HTL, Sling Models, OSGi configs, and
clientlibs already used here unless the POC hypothesis requires something else
— then document the deviation.

## Build / deploy (local)

```bash
mvn clean install
mvn clean install -PautoInstallSinglePackage          # Author
mvn clean install -PautoInstallSinglePackagePublish   # Publish
mvn clean install -PautoInstallBundle                 # core only → Author
```

## Spec Kit

- Skills / commands: `.cursor/skills/` (Cursor), `.github/skills/` (Copilot)
- Shared state and templates: `.specify/`
- Typical flow: constitution → specify → plan → tasks → implement (and converge
  when catching up an existing codebase)
- Project rules for POC delivery live in `.specify/memory/constitution.md` —
  agents using Spec Kit load that via skills; do not copy it into feature code

## Do / don't

- **Do** keep README / POC notes accurate for install, demo path, and expected
  output
- **Do** leave sample content when it makes the demo obvious
- **Don't** treat TDD or UI-test suites as default acceptance criteria
- **Don't** commit secrets; local `admin`/`admin` is archetype convention only
- **Don't** expand scope into production hardening unless that *is* the POC
