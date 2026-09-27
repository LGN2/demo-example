# API reference

Base URL: `http://localhost:8080/api`. Same-origin browser sessions authenticate requests. JSON request/response encoding is UTF-8. Successful creates return the saved record with generated `id`. Decimal values are OMR with at most three fractional digits. ISO dates use `YYYY-MM-DD`; timestamp inputs use ISO instants (`2026-09-27T10:00:00Z`). The UI displays Asia/Muscat.

## Authentication

1. `GET /auth/csrf` → `{ "headerName": "X-CSRF-TOKEN", "token": "..." }`. Retain the session cookie.
2. `POST /auth/login`, form encoded `username` and `password`, with the CSRF header.
3. Fetch `/auth/csrf` again after successful login because authentication rotates the token/session.
4. Include the resulting header on every POST/PUT/DELETE and retain the cookie jar.
5. `POST /auth/logout` ends the current session. `POST /auth/password` accepts `currentPassword` and `newPassword`.

`GET /auth/me` returns the current account and accessible building IDs. `GET /health` is a minimal public readiness response and returns 200 after startup/seeding; it reveals no records.

The Postman collection includes a pre-request script to retrieve a fresh CSRF token for writes using Postman's cookie jar. No Bearer token is used.

## List conventions and errors

Paginated lists accept `page=0`, `size=20` (maximum 100), `q`, and optional `buildingId`; maintenance also accepts `status`. They return `{items,total,page,size}`. Buildings, tax policies, users, lease subresources, meter readings, histories and reminders return arrays (deposit subresources return `{entries,held}`). No client-supplied owner ID is accepted.

Domain errors return `{ "code": "..." }`. Important responses: 400 invalid input/overpayment, 401 unauthenticated, 403 forbidden, 404 absent or inaccessible record, 409 overlap/invalid transition/idempotency conflict, 413 oversized upload, 503 unavailable scanner. Unexpected failures return a generic 500 code without database details.

## Portfolios and leases

| Method | Path | Input / behavior |
|---|---|---|
| GET / POST | `/buildings` | Create: `name,wilayat,address`, optional `investmentValue,reminderDays,seasonalStart,seasonalEnd`; owner only |
| PUT | `/buildings/{id}` | Same editable fields; ownership cannot change |
| GET / POST | `/units` | Create: `buildingId,code,floorName,size,kind,availability,marketRent`, optional `listing` |
| GET / PUT | `/units/{id}` | Read authorized unit or edit its fields; cannot move between buildings |
| GET / POST | `/tenants` | `buildingId,name,kind,phone`, optional `email,emergencyContact,accountId` |
| PUT | `/tenants/{id}` | Update profile; reassigning its login account requires the owner |
| GET / POST | `/leases` | Create: `unitId,tenantId,startDate,months,rent,deposit,taxPolicyId` |
| GET | `/leases/{id}` | Authorized lease details |
| POST | `/leases/{id}/renew` | Same lease-create fields; same unit/tenant, future non-overlapping term |
| POST | `/leases/{id}/terminate` | `terminatedOn,reason`; cancels future dues after allocated payments are reversed |
| PUT | `/leases/{id}/municipality` | `municipalityStatus,municipalityAuthority,municipalityReference,municipalityFee` |
| GET / POST | `/tax-policies` | `buildingId,treatment,supplyClassification,rate,effectiveFrom`, optional `effectiveTo`; owner creates |
| PUT | `/owner/tax-status` | `taxRegistered` boolean; owner only |
| GET | `/history/{type}/{id}` | `type` = `LEASE`, `MAINTENANCE`, `BUILDING`; authorized audit history |

Unit kinds: `RESIDENTIAL,COMMERCIAL`. Availability: `AVAILABLE,MAINTENANCE`. Tenant kinds: `PERSON,COMPANY`. Municipal states: `UNREGISTERED,SUBMITTED,REGISTERED`. Tax treatments: `STANDARD,ZERO_RATED,EXEMPT,OUT_OF_SCOPE`; rates are fractional (e.g. `0.05`), never derived from the unit kind. Standard treatment requires confirmed owner registration and a positive configured rate; other treatments have zero rates. Policies cannot overlap for the same supply classification.

Example lease request:

```json
{"unitId":1,"tenantId":1,"startDate":"2026-10-01","months":12,"rent":"300.000","deposit":"300.000","taxPolicyId":1}
```

## Finance

| Method | Path | Input / behavior |
|---|---|---|
| GET | `/leases/{id}/dues?asOf=YYYY-MM-DD` | Stored due/tax amounts, paid, outstanding and days late |
| GET / POST | `/leases/{id}/payments` | `amount,method,effectiveDate,idempotencyKey`, optional `reference`; methods `BANK_TRANSFER,CASH,OTHER` |
| POST | `/payments/{id}/reverse` | `reason`; preserves allocations and records today's reversal |
| GET / POST | `/leases/{id}/cheques` | `chequeNumber,bank,chequeDate,amount`; initially scheduled |
| POST | `/cheques/{id}/status` | `status`, and `effectiveDate` for clearance |
| GET / POST | `/leases/{id}/deposits` | `kind,amount,effectiveDate,reason,idempotencyKey`, plus `reversesId` for a reversal |
| GET / POST | `/leases/{id}/follow-ups` | Staff-only; `note`, optional `nextDate` |
| GET / POST | `/expenses` | `buildingId,amount,expenseDate,category,description`, optional `unitId` |
| POST | `/expenses/{id}/reverse` | `reason` |

Cheques transition `SCHEDULED → DEPOSITED → CLEARED` or `DEPOSITED → BOUNCED`; scheduled/deposited cheques can be cancelled. Bounced cheques require a replacement record. Clearing twice returns the same settled payment. Deposited/uncleared cheques are not money received. Overpayments are rejected; partial payments allocate oldest dues first, including future scheduled dues if earlier dues are paid. Repeated payment keys must carry the same payload.

Deposit kinds: `RECEIPT,DEDUCTION,REFUND,REVERSAL`. Reversals reference a same-lease non-reversal entry for its full original amount, exactly once. Held balances cannot become negative or exceed the agreed security deposit. Financial dates cannot be future dates or predate the latest event in that ledger.

## Maintenance and AI

| Method | Path | Input / behavior |
|---|---|---|
| GET / POST | `/maintenance` | `unitId,description,category,urgent`; optional staff-supplied `tenantId` |
| GET | `/maintenance/{id}` | Original description, current approved category, proposed AI fields and state |
| POST | `/maintenance/{id}/status` | `status`, optional `note`; `assignedTo` required for assignment |
| POST | `/maintenance/{id}/comments` | `note` |
| POST | `/maintenance/{id}/ai-suggestion` | `language`: `ar` or `en`; staff only, after request exists |
| POST | `/maintenance/{id}/approve-summary` | Manager-selected `summary,category`; original description unchanged |
| GET | `/assignees?buildingId={id}` | Current owner / assigned writable managers / vendors |

Categories: `AC,PLUMBING,ELECTRICAL,LIFT,OTHER`. Required workflow: `OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED`. Vendors may advance their assigned jobs to in-progress/resolved only; owners/managers close them. Tenants may create and comment on their own requests. The AI cannot set urgency or perform other actions. AI status may be `NOT_REQUESTED,PROPOSED,UNAVAILABLE,TIMEOUT,ERROR`; a provider failure never deletes a request.

## Standalone operations

`GET /operations/{type}` lists authorized records. `POST /operations/{type}` creates; `GET /operations/{type}/{id}` reads; supported editable types accept `PUT` with the same fields.

| Type | Fields | Edit? |
|---|---|---|
| `safety` | `buildingId,kind,reference,expiryDate`, optional `notes` | Yes |
| `preventive` | `buildingId,title,category,intervalDays,nextDue,enabled`, optional `unitId` | Yes |
| `vendors` | `buildingId,userId,name,categories,hourlyRate` | Yes |
| `meters` | `buildingId,accountNumber,kind,responsibility,commonArea,alertThreshold`, optional `unitId` | Yes; original unit/kind retained |
| `notices` | `buildingId,titleAr,titleEn,bodyAr,bodyEn` | Yes |
| `leads` | `buildingId,unitId,name,phone,status`, optional `viewingAt,followUpDate,notes` | Yes |
| `visits` | `buildingId,visitorName,purpose`, optional `unitId`; server records entry time | No |
| `checkins` | `buildingId`, optional `note`; current account/time recorded | No |
| `parking` | `buildingId,unitId,space,vehicle` | Yes |

Additional endpoints:

- `POST /operations/preventive/{id}/complete`: records completion and advances the next due date by the configured interval until it is future. Repeated completion before due is rejected.
- `POST /operations/visits/{id}/checkout`: records exit time once.
- `GET|POST /operations/meters/{id}/readings`: create `readingDate,value,kind` (`ROUTINE,MOVE_IN,MOVE_OUT`). Dates advance and readings cannot decrease. Consumption alerts are threshold observations.
- `GET /vendor-performance?buildingId=`: assigned/completed counts and average resolution duration.
- `GET /reminders?buildingId=`: lease, cheque, arrears, certificate/file expiry, preventive and enquiry follow-up reminders.

## Documents, reporting and administration

- `GET /documents`: authorized metadata. `POST /documents` is multipart with `file,buildingId,kind` and optional `unitId,tenantId,maintenanceId,readingId,expiryDate`. Maintenance/reading photos must match their linked record. Identity/CR files require a tenant. Supported content signatures: PDF/JPEG/PNG; maximum 8 MiB.
- `GET /documents/{id}/download`: authorized attachment; storage keys are not serialized.
- `GET /print/{type}/{id}?language=ar|en`: standalone printable HTML for `lease,statement,invoice,receipt,deposit`. Lease and statement use a lease ID; other types use a due/payment/deposit-entry ID.
- `GET /dashboard?buildingId=&unitId=&from=&to=&asOf=`: current counts, dated balances and supporting records. See metric definitions.
- `GET /reports/finance.csv` accepts the same filters; owners/managers only.
- `GET /integrations`: truthful adapter availability and activation requirements.
- `GET|POST /users`: owners see/create their portfolio's restricted users; platform admins see/create owner/platform accounts. Create requires `username,displayName,password,role`.
- `GET /access?buildingId=`; `POST /access` with `buildingId,userId,canWrite`; `DELETE /access/{id}`. Owner-only building grants for manager/guard accounts.

There are no settled-payment, allocation or ledger-delete endpoints. Corrections use reversals. No public payment callback or device-control endpoint exists until a real provider adapter is implemented and verified.
