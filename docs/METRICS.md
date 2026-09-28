# Report calculations

All rows are restricted to the authenticated portfolio, assigned buildings, or the tenant's own leases. Building and unit filters apply before aggregation. Date ranges are inclusive; monetary amounts are OMR.

| Metric | Definition / supporting records |
|---|---|
| Unit count | Accessible units created on or before the selected as-of date |
| Occupied | Distinct units with a lease covering the as-of date, through any effective termination date |
| Vacant | Unit count minus occupied; maintenance availability is a separate attribute |
| Occupancy % | Occupied / unit count × 100; zero for an empty portfolio |
| Expected rent | Sum of uncancelled due rent amounts with due dates within the selected period; excludes tax and deposits |
| Expected tax | Stored tax on those dues |
| Settled collections | Payments effective within the period, minus reversals effective within the period; includes tax; no pending cheques/deposits |
| Rental collections | Each settlement allocation × stored due rent / stored due total, rounded to three decimals; reversals negate the same amounts |
| Outstanding to date | Unpaid balance on dues due on/before the as-of date; settlements/reversals evaluated at that date |
| Overdue | Outstanding dues strictly before the as-of date; today's due is outstanding but not one day late |
| Future unpaid | Unpaid scheduled dues strictly after the as-of date; not arrears |
| Aging | Overdue amounts grouped into 1–30, 31–60, 61–90 and over 90 days |
| Pending cheques | Current scheduled or deposited cheque count |
| Bounced cheques | Current bounced cheque count; no effect on collections |
| Lease expiries | Un-terminated contracts ending within each building's configured reminder period (default 90 days) |
| Open maintenance | Current requests not closed, including resolved requests awaiting closure |
| Recorded expenses | Recorded expenses in the period minus reversals in the period |
| Net operating income | Cash-basis rental collections excluding tax minus recorded expenses; deposits and capital valuation excluded |
| Cost per unit | Period expenses / selected unit count; a unit filter includes expenses explicitly linked to that unit |
| Period net yield | NOI / explicit investment basis × 100, only if every selected building has a positive basis and no unit filter is used; not implicitly annualized |
| Vacancy duration | Days since the most recent ended tenancy or unit creation, bounded at zero |
| Estimated foregone rent | Asking rent × vacant days / 30; an estimate using the current asking rent, not a booked accounting loss |
| Vendor mean resolution hours | Mean hours from request creation to recorded resolution for resolved assigned jobs; not a contractual SLA score |
| Unusual consumption | Difference between consecutive meter readings exceeds the configured absolute threshold; an observation, not a diagnosis |

Dashboard drill-downs expose expected dues, period payment/reversal events, occupancy by unit, arrears, lease expiries and vacancies. Cheque and maintenance counts describe current workflow state; date filters do not reconstruct their historical status. Balances and financial reversals retain effective-date semantics. Building values, availability, asking rents and expenses are operator-entered; the system does not invent missing valuations or forecasts.

CSV exports contain metrics and supporting dues. Individual receipts, invoices, deposits, statements and contract drafts are downloaded from the lease screen. A legal tax invoice may require additional jurisdiction-specific registration fields and approval; the generated invoice is an operational rent record until the operator confirms those requirements.
