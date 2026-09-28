# Refactor verification — 28 September 2026

[CI run 36410118164](https://github.com/LGN2/demo-example/actions/runs/36410118164) passed for application commit `f1b5f5f65906fe7d971302941888ccf15cbfee10`.

| Environment | Executed result |
|---|---|
| Java 17 + MySQL 8.4.6 | 9 unit tests and 19 integration tests passed; zero failures, errors or skips |
| Java 21 + MySQL 8.4.6 | 9 unit tests and 19 integration tests passed; zero failures, errors or skips |
| Packaged application + fresh development database | Startup and readiness passed |
| Chromium acceptance | 21 owner screens, real payment, tenant request, role isolation, Arabic/English desktop and 360px layouts passed |

New HTTP regressions verify invalid DTO input returns 400, injected ownership cannot change the authenticated owner, password hashes are excluded, and multipart date fields bind correctly while storage keys remain private. Existing financial concurrency, lease overlap, tax history, deposit separation, authorization and protected-file regressions still pass.

The four dashboard screenshots in `browser-evidence` were visually inspected. Browser checks report no JavaScript errors or page overflow. Documentation links and Postman JSON parse checks passed locally. Postman execution, complete accessibility auditing and production load/security testing are not claimed.

Artifacts: `test-results-java-17`, `test-results-java-21`, `browser-evidence`. The subsequent documentation-only commit records these results without changing executable code.

---

# Test results before the structure refactor

Updated 28 September 2026. Branch: `feat/oman-property-management`. Review: [pull request #1](https://github.com/LGN2/demo-example/pull/1). The branch is published; no merge or production deployment has been performed.

## Delivered

Connected Java 17/Spring Boot/MySQL application, Arabic-first RTL and English vanilla-JS frontend, six scoped roles, tenancy/finance/maintenance, Spring AI adapter with manual fallback, standalone operations, protected file vault, reporting, migrations, Compose and training documentation. See [REQUIREMENTS.md](REQUIREMENTS.md) for per-feature scope and actual status.

## Executed verification

[Final application CI run 36343342902](https://github.com/LGN2/demo-example/actions/runs/36343342902) succeeded for application commit `bccf9521d6f4a27c9bc71c8ea8ba9f162c998859`:

- `./mvnw -B verify`: **9 unit tests + 17 real MySQL integration tests**, zero failures, errors or skipped tests.
- Financial evidence includes OMR 300 → 200 after payment, unchanged pending/bounced cheques, exact-once clearance, concurrent clearance/manual payments, separate deposits, retained reversals and reconciled dashboard balances.
- Authorization evidence includes cross-owner/tenant/admin restrictions, unassigned and read-only managers, vendor/guard limits, protected documents and staff-only collection notes.
- `npm test --prefix tests/browser`: **passed**, covering 21 owner screens, an actual OMR 10.000 payment, tenant urgent maintenance submission, assigned/unassigned manager access, other restricted role journeys, Arabic/English desktop and 360px overflow checks. No browser JavaScript errors.
- Packaged application started against a fresh development MySQL database and passed readiness before browser tests.
- Retained workflow artifacts: `test-results` and `browser-evidence`. The latter includes four dashboard screenshots and the application startup log. Desktop screenshots and refreshed Arabic/English mobile screenshots were visually inspected. Screenshot capture fast-forwards transitions and waits for transient payment notices.
- Local checks: JavaScript syntax, `git diff --check`, Postman JSON parsing, unique ClickUp task identifiers and local Markdown links. Earlier local Maven unit execution also passed 9 tests.

Subsequent delivery commits change documentation/API examples only; the executable application is the tested commit above. Postman import/execution itself has not been performed. Visual dashboard inspection does not represent a full accessibility or every-screen manual audit.

## Documentation

README includes PowerShell and Unix Docker/non-Docker setup and development accounts. The docs directory includes API reference/Postman collection, architecture/ER diagrams, financial definitions, security/backup guidance, manual acceptance checklist, eight-minute demo, ClickUp-compatible import and feature completion matrix.

## Remaining dependencies and limits

- Gateway, automated WhatsApp, portals and hardware integrations remain visibly disabled: see [INTEGRATIONS.md](INTEGRATIONS.md). Live AI requires a server key and provider verification.
- Production requires HTTPS, unique accounts/secrets, a provisioned upload scanner, coordinated backup/restore rehearsal and confirmed contract/tax rules. The demo image does not ship a scanner.
- List search/pagination runs after authorized query loading. Relationship selectors now load every authorized page; large portfolios need database pagination/autocomplete and load testing.
- Fixed monthly, one-unit/one-tenant leases only. Printable contracts are unsigned drafts. No proration, general ledger, automated legal registration or device control is claimed.
- Full accessibility, penetration/load tests and the remaining human acceptance checklist are deployment review work.

## Next steps for the team

Review pull request #1, run the development application using README, and complete [acceptance-checklist.md](acceptance-checklist.md) for the intended operating environment. Provider-specific adapters can proceed when their documented dependencies are available. No core-code completion step is delegated to the user.
