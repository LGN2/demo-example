# Responsive verification

Run `npm test --prefix tests/browser` against a freshly seeded development database after installing Playwright Chromium.

- Arabic RTL and English LTR dashboard at desktop width and 360px mobile width.
- No horizontal page overflow in either language.
- Owner navigation covers 21 screens; payment and tenant-maintenance forms submit real requests.
- Role journeys cover assigned/unassigned managers, tenants, vendors, guards and platform administration.
- No JavaScript errors; screenshots capture completed layout transitions.

Retain `browser-evidence` from CI and inspect all four dashboard screenshots. Automated checks do not replace manual keyboard, screen-reader or physical-device testing. Current executed evidence is recorded in [test results](test-results.md).
