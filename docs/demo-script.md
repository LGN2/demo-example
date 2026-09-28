# Eight-minute demo

Use a fresh development database. Open two browser profiles to demonstrate isolation. The selected display date is today in Asia/Muscat. All people and documents used here are synthetic.

| Time | Demonstration |
|---|---|
| 0:00–0:45 | Sign in as `owner@demo.test`. Show the Arabic RTL dashboard, switch to English/LTR, explain three-decimal OMR and the reporting dates. |
| 0:45–1:30 | Open Buildings and Units. Show floor groups, asking rent and maintenance availability separate from occupancy. Filter to the main building. |
| 1:30–2:15 | Open the seeded lease. Show tenant, fixed monthly dues, deposit, municipality status and printable Arabic/English draft. Explain that the draft is neither a signature nor a municipal submission. |
| 2:15–3:20 | Show the current OMR 300.000 due, OMR 100.000 settled payment and OMR 200.000 balance. Explain why the scheduled cheque does not affect it. For clearance, create a replacement OMR 200.000 cheque dated today, mark it deposited, then cleared. Refresh to show one payment and the settled current due. |
| 3:20–4:00 | Show the separate OMR 300.000 deposit ledger. Download the payment receipt and tenant statement. Record a reasoned reversal only if demonstrating correction; show that original records remain in history. |
| 4:00–4:50 | In a second profile sign in as `tenant@demo.test`; submit an urgent maintenance issue in Arabic. Return to owner/manager, assign it, advance to in-progress/resolved/closed. Show original description, comments and audit timestamps. |
| 4:50–5:20 | Request an AI suggestion with no key: show the unavailable status and working manual summary/category approval. If a real key is configured, demonstrate a reviewed suggestion; do not claim an unavailable response is live AI. |
| 5:20–6:10 | Show preventive schedules, seasonal AC indicator, a safety expiry record and manual meter readings. Explain that alerts are observations and recordkeeping does not guarantee compliance. |
| 6:10–6:50 | Open reports, choose building/unit and dates, drill into expected dues and actual collection events, and export CSV. Explain that deposits are excluded and yield requires an explicit investment basis. |
| 6:50–7:30 | Sign in as owner2/unassigned manager/guard/vendor to demonstrate restricted views. The platform admin manages accounts and cannot inspect the first owner's lease. |
| 7:30–8:00 | Show the integration status page and requirements matrix. State which providers/hardware are still needed. Point to the passing automated checks and setup instructions. |

If browser tests have already modified the demo, either explain the additional OMR 10.000 acceptance payment or reset a disposable demo using the guarded reset script. Never reset a database containing real work.
