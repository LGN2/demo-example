# External integration activation register

The application completes its independent workflows without these providers. `ExternalIntegration` is the availability/activation contract; `IntegrationRegistry` exposes disabled states. It is not a functional payment, messaging or hardware adapter.

| Integration | Current state | Required activation work |
|---|---|---|
| Spring AI maintenance summary/category | Real server-side adapter implemented; live provider call not tested without credentials | Supply `AI_API_KEY`, confirm `AI_MODEL` access and data-handling terms; verify Arabic/English summaries against chosen model; keep manual fallback |
| Local payment gateway | Blocked; no provider chosen | Select an Oman-supported provider, obtain merchant/sandbox credentials and official callback spec; implement adapter and test signature verification, currency OMR, exact amount, owner/lease context, provider-event uniqueness and transactional settlement before enabling |
| Automated WhatsApp reminders/intake | Blocked | Approved account/number/templates, official API/webhook documentation, credentials, consent rules and verified event routing; manual copy/share remains available |
| Property portals | Blocked | Selected portal's supported listing API, commercial permissions, schema, credentials and synchronization/error rules |
| Smart locks/intercoms | Blocked | Hardware inventory, vendor API, trusted network, explicit access-command authorization and audit policy; no physical actions in tests |
| CCTV equipment health | Blocked | Vendor read-only status API/network access; this feature is equipment status, not video analytics or surveillance identity inference |
| Central AC/chillers | Blocked | BMS/vendor protocol, sensor inventory, read/write scope, network access and a separate hardware test environment |
| Lift status | Blocked | Vendor integration agreement, telemetry API and event semantics; no operational control is currently exposed |
| Tank levels/pump alerts | Blocked | Device identifiers, telemetry protocol, thresholds, signed event/source validation and hardware sandbox |
| Energy monitoring | Blocked | Supported meter/feed API, owner-authorized accounts, units/timezone/schema and data quality validation |

There are no development endpoints that fabricate live payment success, AI answers or hardware state. Manual rent records, cheque processing, notices, maintenance and meter readings are working application features and are explicitly separate from these integrations.

AI receives only the saved issue description and chosen language. Its system instruction treats the description as untrusted, requests strict JSON, uses a bounded worker pool and timeouts, validates category/summary, and exposes suggestions for manager approval. No identity documents, ledger records or database/action tools are provided. Do not put identity or financial details into maintenance descriptions.

A supplied payment API key alone is not enough to enable payments: the provider-specific adapter and callback acceptance tests are still required. No real messages, charges or physical commands are performed by the included tests.
