# Bayt · بيت

Arabic-first building and property management for Oman. A connected Java Spring Boot / MySQL application with a vanilla JavaScript frontend, session security, real rent ledgers and role-restricted workflows.

**Work branch:** `feat/oman-property-management`. This is an initial reviewable implementation; see [requirements and verification status](docs/PROGRESS.md) and [the feature matrix](docs/REQUIREMENTS.md). Provider integrations are explicitly disabled until configured and implemented against a selected provider.

## Start the development application

Install Docker Desktop with Compose. In PowerShell:

```powershell
git clone https://github.com/LGN2/demo-example.git
cd demo-example
git checkout feat/oman-property-management
Copy-Item .env.example .env
# Edit .env and choose your own local database passwords.
docker compose up --build
```

Linux/macOS:

```sh
git clone https://github.com/LGN2/demo-example.git
cd demo-example
git checkout feat/oman-property-management
cp .env.example .env
# Edit .env and choose your own local database passwords.
docker compose up --build
```

Open **http://localhost:8080**. Arabic/RTL is the default; use the English button to switch. Initial startup downloads build dependencies and creates the MySQL schema. `/api/health` returns 200 after startup and development seeding have finished. Data survives restarts in Compose volumes.

## Development accounts

These synthetic accounts are created **only with `SPRING_PROFILES_ACTIVE=dev` on an empty database**. The shared password is your `DEMO_PASSWORD` value; the example is `BaytDemo!2026`.

| Account | Journey |
|---|---|
| `owner@demo.test` | Main portfolio, accounts, leases, finance and operations |
| `owner2@demo.test` | Independent portfolio for isolation checks |
| `manager@demo.test` | Assigned building with write permission |
| `unassigned@demo.test` | No building access |
| `tenant@demo.test` | Own lease, payments, documents and maintenance |
| `vendor@demo.test` | Assigned maintenance jobs and job photos |
| `guard@demo.test` | Assigned-building visitors, check-ins and parking |
| `admin@demo.test` | Owner/platform account management, without portfolio data access |

The main demo has one current OMR 300.000 monthly lease, a settled OMR 100.000 payment, a pending OMR 200.000 cheque, and an OMR 300.000 deposit. Pending cheques and deposits do not reduce outstanding rent.

## Run without Docker

Install **JDK 21 (or 17)** and **MySQL 8.4**. Set `JAVA_HOME` to the JDK directory and confirm `java -version`. Maven is downloaded by the included wrapper; a separate Maven installation is optional.

As a MySQL administrator, run the following once, replacing the password before execution:

```sql
CREATE DATABASE bayt CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'bayt'@'localhost' IDENTIFIED BY 'choose-a-unique-local-password';
GRANT ALL PRIVILEGES ON bayt.* TO 'bayt'@'localhost';
```

PowerShell:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/bayt?connectionTimeZone=UTC'
$env:DB_USER = 'bayt'
$env:DB_PASSWORD = 'your-local-database-password'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DEMO_PASSWORD = 'BaytDemo!2026'
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```sh
export DB_URL='jdbc:mysql://localhost:3306/bayt?connectionTimeZone=UTC'
export DB_USER='bayt'
export DB_PASSWORD='your-local-database-password'
export SPRING_PROFILES_ACTIVE=dev
export DEMO_PASSWORD='BaytDemo!2026'
sh ./mvnw spring-boot:run
```

The non-Docker process does not automatically load `.env`; set environment variables as shown. The dev profile enables HTTP localhost cookies. Flyway migrates the database automatically; Hibernate validates the resulting schema. Do not change applied migrations on an existing database.

## Tests

Unit tests (no database required):

```sh
sh ./mvnw test
```

PowerShell equivalent: `.\mvnw.cmd test`.

For integration tests, create a **separate empty database**, grant the test user access, set `DB_URL`, `DB_USER`, `DB_PASSWORD` to that database, then run:

```sh
sh ./mvnw verify
```

`PropertyWorkflowIT` requires real MySQL and inserts isolated synthetic fixtures. It does not use H2 or silently skip when MySQL is missing. Never point the test environment at production. GitHub Actions provisions MySQL 8.4.6 for these tests, then launches a separate dev database and the packaged app for browser acceptance tests. Test results and screenshots are workflow artifacts.

Browser tests are optional development tooling; Node is **not** used to serve the application. With a fresh demo running, Node 22+ can run:

```sh
npm ci --prefix tests/browser
npx --prefix tests/browser playwright install chromium
npm test --prefix tests/browser
```

Browser tests record a synthetic OMR 10.000 payment. Recreate a fresh demo before rerunning the fixed-balance acceptance scenario.

## What is included

- Owners, assigned managers, tenants, vendors, guards and separate platform administration.
- Buildings, floor groups, units, tenant/company profiles, lease renewal/termination and municipal recordkeeping.
- Fixed monthly dues, partial allocations, cheque lifecycle, idempotent settlements, separate deposits, reversals and explicit tax-policy snapshots.
- Protected files, bilingual printable draft leases, invoices, receipts and tenant statements.
- Maintenance workflow, comments, assignment, optional Spring AI summaries with manager approval and manual fallback.
- Vendors, recurring preventive tasks, seasonal AC indicators, safety records, meters/readings, consumption observations, announcements, enquiries/viewings, visitor records, guard check-ins and parking.
- Date/building/unit reporting, balances, aging, occupancy, expenses, conditional investment yield, CSV and in-app reminders.

## Integrations and deployment limits

`AI_API_KEY` enables the server-side Spring AI adapter; `AI_MODEL` selects the model. Requests are saved before AI is called. Missing credentials or failures are labeled and do not block manual maintenance. Tests use no real provider calls.

Payments, automated WhatsApp, property portals, locks/intercoms, CCTV status, chillers, lifts, tank/pump sensors and energy monitoring require selected providers, official documentation and sandbox/hardware access. Their status screen says disabled; no invented adapters or live side effects are included. [Activation requirements](docs/INTEGRATIONS.md) list the remaining work.

Before a real deployment, configure HTTPS, production accounts/secrets, file scanning, backups and confirmed tax/contract rules. Use [security and operations guidance](docs/permissions.md). Generated records do not certify legal compliance or municipal registration. This implementation is intended for a development team to review and validate for its operating context.

## Project documentation

- [Architecture, database diagrams and scope decisions](docs/database.md)
- [API reference](docs/api.md) and [Postman collection](docs/postman/property-management.postman_collection.json)
- [Financial metric definitions](docs/METRICS.md)
- [Acceptance checklist](docs/acceptance-checklist.md) and [eight-minute demonstration](docs/demo-script.md)
- [ClickUp-compatible backlog](docs/clickup-backlog.csv)
- [Requirements matrix](docs/REQUIREMENTS.md), [test evidence and progress](docs/PROGRESS.md)
- [Original attached build specification](docs/BUILD_SPECIFICATION.txt)

## IntelliJ IDEA and Java 21

Open the root `pom.xml` as a Maven project. Set **Project SDK** and **Maven Runner JRE** to JDK 21, then reload all Maven projects. The POM explicitly compiles with release 17, which JDK 21 supports. If IntelliJ still reports JVM target 5, remove that stale override in Settings → Build, Execution, Deployment → Compiler → Java Compiler and reload Maven; use target 17. Do not change it to 5.

Run `com.codevictims.propertymanagement.PropertyManagementApplication` with the database environment variables and `dev` profile described above. The packaged artifact is `target/property-management-1.0.0-SNAPSHOT.jar`.

To update an existing checkout:

```sh
git switch feat/oman-property-management
git pull --ff-only origin feat/oman-property-management
```

The project follows feature packages under `com/codevictims/propertymanagement`: `account`, `security`, `property`, `tenancy`, `billing`, `maintenance`, `dashboard`, and `common`. See [the structure and database guide](docs/database.md).

Additional guides: [maintenance AI](docs/maintenance-ai.md), [UI](docs/ui-guide.md), [responsive checks](docs/responsive-checklist.md), [test results](docs/test-results.md), and [Postman environment](docs/postman/local.postman_environment.json).

Suggested ownership follows the supplied team layout: Almajd—Maven, API and demo; Mohammed—database; Reem—Docker, CI, permissions and acceptance; Nawaf—maintenance AI; SHATHA—UI and responsive checks. These are handover responsibilities, not claims of authorship.
