# Acceptance checklist

Run on a disposable MySQL database. Automated tests are authoritative only when the corresponding workflow succeeded; see PROGRESS.md for actual evidence.

## Automated gates

- `MoneyRulesTest`: decimal precision, oldest-first partial allocation, overpayment rejection and historical reversal dates.
- `AiAssistantTest`: missing credentials, Arabic valid JSON, malformed/extra fields, unknown category, provider failure and timeout. Mockito mocks the provider; no live model call is made.
- `PropertyWorkflowIT`: real migrations/JPA/MySQL, OMR 300 → 200 after a 100 payment, unchanged pending/bounced cheque balances, exact-once replacement clearance, concurrent clearance, concurrent competing allocations, deposits excluded from rent, reversals and immutable dues, overlap/renewal/concurrent leases, owner/manager/tenant/admin isolation, CSRF denial, vendor/guard restrictions, files, utilities and preventive workflow.
- `tests/browser/smoke.mjs`: real dev database/server and all owner module screens, an actual 10 OMR payment, a tenant urgent maintenance submission, all six role journeys, including assigned and unassigned managers, English/Arabic layout direction and 360px overflow checks. Screenshots are saved as workflow artifacts.

## Human review

- [ ] On desktop and a 360px phone viewport, keyboard focus is visible; modal controls are labeled; Escape closes the dialog; tables can scroll without moving the whole page.
- [ ] Arabic/English navigation, form labels, statuses, error states and empty states are understandable. User-entered names/descriptions remain in their original language.
- [ ] Refresh after saving; sign out/in; confirm records come from MySQL. Language preference is the only browser-stored application preference.
- [ ] A manager with `canWrite=false` can view assigned records and receives a server rejection on changes. Revoking the assignment removes access.
- [ ] Owner2 and tenant2 cannot fetch another person's lease, receipt, report or protected file by changing an ID in the URL.
- [ ] Record partial payments and an overpayment; verify oldest dues, errors and stable idempotency on retries.
- [ ] Keep cheque scheduled/deposited/bounced and verify no rental collection. Clear its replacement twice and verify a single linked settled payment.
- [ ] Receive, deduct and refund a deposit; reject negative/excess balances; reconcile rent collections without deposit money.
- [ ] Reverse a settlement with a reason. View the retained payment/allocations and historical effective dates. Confirm a cleared reversed cheque cannot settle again.
- [ ] Create a lease renewal with the same unit/tenant; reject overlap. Terminate a lease only after reversing allocations to future cancelled dues.
- [ ] Select explicit exempt versus zero-rated policies; verify distinct labels. Confirm policy coverage and tax registration; later policy changes must not rewrite due amounts.
- [ ] Progress maintenance through all five states. Vendor may resolve its assigned job, while only authorized staff close it. Urgency remains a human field.
- [ ] Without an AI key, save and process a request manually. With an approved key, verify the real model returns only a proposed summary/category, then edit/approve it.
- [ ] Upload only synthetic PDF/PNG/JPEG files. Confirm 8 MiB/type restrictions, tenant linking, authorized download and visible unscanned development status.
- [ ] Configure a production scanner in a staging environment and verify clean, malicious, unavailable and timeout outcomes before real document use.
- [ ] Record meter readings in increasing dates; reject decreases; review threshold observations and attach move-in/out photos to the correct reading.
- [ ] Record preventive completion; ensure its next date advances once. Review safety/document expiry and seasonal AC priorities without claiming certification.
- [ ] Exercise enquiry/viewing/follow-up, announcements/manual sharing, visitor exit, guard attendance and parking allocation.
- [ ] Reconcile each report metric using its supporting records; verify period reversals and future dues; no investment basis means no yield metric.
- [ ] Restore a coordinated database/file backup into a separate environment and reconcile before reopening access.

Human signoff, real provider tests, load testing, penetration testing and jurisdiction-specific contract/tax review have not been represented as completed automated checks.
