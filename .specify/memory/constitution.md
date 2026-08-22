<!--
Sync Impact Report
- Version change: 1.0.0 → 1.0.1
- Modified principles: none renamed; V. Simplicity and AEM Fit clarified
  with preferred-patterns guidance
- Added sections: none (AEM POC Constraints extended with one practice rule)
- Removed sections: none
- Follow-up TODOs: none
-->

# AEM POCs Constitution

## Core Principles

### I. POC-Scoped Delivery
Every change in this repository MUST serve a named proof of concept, not a
product-complete platform. A POC MUST have a stated hypothesis, a bounded
scope, and an explicit non-goal list. Work MUST stay inside that scope unless
the constitution is amended or the POC is explicitly superseded.

Rationale: This repo (`aem-poc`, groupId `com.aem.poc`) exists to try AEM
ideas quickly. Unbounded "production hardening" hides the experiment.

### II. Visible Outcomes
A POC is complete only when a reviewer can see the result without reading
implementation internals first. Each POC MUST produce at least one of:

- A page, component, dialog, or content path that can be opened on Author
  (and Publish when the experiment requires it)
- A package, log snippet, screenshot, or exported artifact that proves the
  behavior
- A before/after comparison when the POC changes existing behavior

Hidden-only work (code merged with no observable surface) is NOT a finished
POC. Tests MAY exist, but they MUST NOT be the primary evidence of success.

### III. Documentation First
Tests are optional for POCs and MUST NOT block delivery when the outcome is
visible and documented. Automated unit, integration, and UI tests SHOULD be
omitted unless they are themselves the experiment or they prevent a
high-cost regression in a shared module.

Every POC MUST ship with documentation that a teammate can follow without
tribal knowledge. That documentation MUST include:

- What was proven (and what was not)
- Where to look in the repo (modules, packages, content paths)
- Any AEM version, Cloud vs AMS, or runmode assumptions

Module READMEs and the root README MUST stay accurate for the current POC
set. Stale install or usage steps are a constitution violation.

### IV. Install, Demo, and Usage Clarity
A POC MUST be reproducible from written steps. Documentation MUST include:

1. Prerequisites (JDK/Maven, AEM instance, ports, credentials assumptions)
2. Build and deploy commands (for example `mvn clean install` and
   `-PautoInstallSinglePackage` against Author on `localhost:4502` unless
   documented otherwise)
3. Demo path: which page, component, or console to open, in what order
4. Expected output: what the reviewer MUST see if the POC succeeded
5. Cleanup or teardown notes when the POC leaves sample content or configs

If a step requires a local AEM instance, the docs MUST say so. If the POC
cannot run without Cloud-only features, that MUST be stated up front.

### V. Simplicity and AEM Fit
Prefer the smallest change that proves the hypothesis. MUST follow existing
AEM project layout (`core`, `ui.apps`, `ui.content`, `ui.config`, frontend
modules, dispatcher modules) rather than inventing parallel trees. MUST NOT
add frameworks, Cloud Services, or extra Maven modules unless they are
required to demonstrate the POC.

YAGNI applies: no production observability stacks, no mandatory TDD, no
enterprise versioning ceremony for throwaway sample content. Shared OSGi
contracts and clientlibs MUST stay understandable; clever abstractions that
only serve future imaginary products are forbidden.

## AEM POC Constraints

This is an Adobe Experience Manager multi-module Maven project. POCs MUST
respect:

- Author default `localhost:4502` and Publish `localhost:4503` unless a POC
  document overrides them
- Content and code split: authorable UI in `ui.apps` / `ui.content`; Java
  services and servlets in `core`; runmode config in `ui.config`
- Prefer established AEM patterns already used in this archetype (HTL,
  Sling Models, OSGi configs, clientlibs). Deviate only when the POC
  hypothesis requires it, and document the deviation in the POC notes
- Full Adobe best-practice catalogs MUST NOT be treated as acceptance
  criteria for POCs; this constitution is not a substitute for AEM product
  documentation
- AEMaaCS analyser is advisory for POCs when present; it MUST NOT be a
  release gate unless a POC is specifically about Cloud deployability
- Secrets MUST NOT be committed. Sample docs MAY mention default local
  `admin` credentials as AEM archetype convention, but real environments
  MUST use privately supplied credentials
- Sample content is allowed and expected; it MUST be documented so demos
  are not guesswork

## Delivery and Review

A POC is ready to share when all of the following are true:

1. Hypothesis and non-goals are written
2. The outcome is visible (Principle II)
3. Install/demo/usage steps are complete and have been followed at least
   once by the author (Principle IV)
4. Scope stayed inside the stated experiment (Principle I)

Code review SHOULD check documentation and demo steps before code style.
Reviewers MUST be able to reproduce the demo from the README or POC notes
alone. Missing expected-output description is sufficient reason to reject.

Tests MAY be added as supporting evidence. Absence of tests MUST NOT be used
as a reject reason unless the POC's hypothesis is about test strategy.

## Governance

This constitution supersedes informal "just ship it" habits and also
supersedes archetype defaults that privilege test suites over demos. When
this document conflicts with a template README, this document wins until
the README is updated.

Amendments MUST:

- Be written into this file
- Bump **Version** using semver: MAJOR for removed or redefined principles,
  MINOR for new principles or material new constraints, PATCH for wording
  and clarifications
- Set **Last Amended** to the amendment date (ISO `YYYY-MM-DD`)
- Record impact in the Sync Impact Report comment at the top of this file

Compliance: specs, plans, and implementation tasks for this repo MUST
encode visible outcomes and install/demo/usage documentation. They MUST NOT
require TDD, coverage targets, or integration-test matrices as default
acceptance criteria.

Runtime development guidance lives in module READMEs, `AGENTS.md`, and
feature specs under Spec Kit. This constitution does not replace AEM product
docs; it governs how we use this repository.

**Version**: 1.0.1 | **Ratified**: 2026-08-22 | **Last Amended**: 2026-08-22
