import assert from "node:assert/strict";
import { mkdir } from "node:fs/promises";
import { chromium } from "playwright";

const base = process.env.BAYT_BASE_URL || "http://127.0.0.1:8080";
const password = process.env.DEMO_PASSWORD || "BaytDemo!2026";
const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
const errors = [];
page.on("pageerror", (error) => errors.push(error.message));
await mkdir("artifacts", { recursive: true });
async function login(username) {
  await page.goto(base);
  await page.locator("#login-form").waitFor();
  await page.locator("[name=username]").fill(username);
  await page.locator("[name=password]").fill(password);
  await page.locator("#login-form button[type=submit]").click();
  await page.locator(".sidebar").waitFor();
  await page.locator("#main h1").waitFor();
}
async function navigate(route) {
  await page.goto(base + "/#" + route);
  await page.locator("#main h1").waitFor();
  assert.equal(
    await page.locator("#main .error").count(),
    0,
    `Error in ${route}`,
  );
}
async function json(path) {
  const response = await page.request.get(base + "/api" + path);
  assert.equal(response.status(), 200, path);
  return response.json();
}
async function logout() {
  await page.locator("[data-action=logout]").click();
  await page.locator("#login-form").waitFor();
}
try {
  await login("owner@demo.test");
  assert.equal(await page.locator("html").getAttribute("dir"), "rtl");
  await page.locator(".metric").first().waitFor();
  assert.equal(await page.locator(".metric").count(), 4);
  await page.screenshot({
    path: "artifacts/dashboard-ar-desktop.png",
    fullPage: true,
    animations: "disabled",
  });
  for (const route of [
    "buildings",
    "units",
    "tenants",
    "leases",
    "maintenance",
    "expenses",
    "operations",
    "documents",
    "notices",
    "reports",
    "settings",
    "integrations",
    "safety",
    "preventive",
    "vendors",
    "meters",
    "leads",
    "visits",
    "checkins",
    "parking",
    "tax-policies",
  ])
    await navigate(route);
  await navigate("leases");
  await page.locator('a[href^="#lease/"]').first().click();
  await page.locator("[data-action=payment]").waitFor();
  const leaseId = page.url().split("/").at(-1);
  await page.locator("[data-action=payment]").click();
  await page.locator("#f-amount").fill("10.000");
  await page.locator("#f-method").selectOption("BANK_TRANSFER");
  await page.locator("#f-reference").fill("Browser acceptance");
  await page.locator("#modal-form [type=submit]").click();
  await page.locator("#dialog").waitFor({ state: "hidden" });
  const dues = await json("/leases/" + leaseId + "/dues");
  assert.equal(Number(dues[0].outstanding), 190);
  for (const tab of [
    "payments",
    "cheques",
    "deposits",
    "history",
    "follow-ups",
  ]) {
    await page.locator(`[data-action=lease-tab][data-tab="${tab}"]`).click();
    await page.locator("#main h1").waitFor();
    assert.equal(await page.locator("#main .error").count(), 0, tab);
  }
  await navigate("dashboard");
  await page.locator("#toast").waitFor({ state: "hidden" });
  await page.locator("[data-action=language]").click();
  await page.locator("html[dir=ltr]").waitFor();
  await page.locator(".metric").first().waitFor();
  await page.screenshot({
    path: "artifacts/dashboard-en-desktop.png",
    fullPage: true,
    animations: "disabled",
  });
  await page.setViewportSize({ width: 360, height: 800 });
  await page.screenshot({
    path: "artifacts/dashboard-en-mobile.png",
    fullPage: true,
    animations: "disabled",
  });
  assert.ok(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth + 1,
    ),
    "Mobile horizontal overflow",
  );
  await page.locator("[data-action=language]").click();
  await page.locator("html[dir=rtl]").waitFor();
  await page.locator(".metric").first().waitFor();
  await page.screenshot({
    path: "artifacts/dashboard-ar-mobile.png",
    fullPage: true,
    animations: "disabled",
  });
  assert.ok(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth + 1,
    ),
    "RTL mobile horizontal overflow",
  );
  await page.setViewportSize({ width: 1440, height: 1000 });
  await logout();
  await login("manager@demo.test");
  await navigate("leases");
  assert.equal((await page.request.get(base + "/api/leases/" + leaseId)).status(), 200);
  await logout();
  await login("unassigned@demo.test");
  assert.equal((await page.request.get(base + "/api/leases/" + leaseId)).status(), 404);
  await logout();
  await login("tenant@demo.test");
  await navigate("maintenance");
  await page.locator("[data-action=create]").click();
  const firstUnit = await page
    .locator("#f-unitId option")
    .nth(1)
    .getAttribute("value");
  await page.locator("#f-unitId").selectOption(firstUnit);
  await page.locator("#f-description").fill("تسرب مياه في المطبخ — طلب تجريبي");
  await page.locator("#f-category").selectOption("PLUMBING");
  await page.locator("#f-urgent").check();
  await page.locator("#modal-form [type=submit]").click();
  await page.locator("#dialog").waitFor({ state: "hidden" });
  assert.ok(
    (await json("/maintenance")).items.some(
      (m) => m.description.includes("طلب تجريبي") && m.urgent,
    ),
  );
  await logout();
  await login("owner2@demo.test");
  assert.equal(
    (await page.request.get(base + "/api/leases/" + leaseId)).status(),
    404,
  );
  await logout();
  await login("guard@demo.test");
  await navigate("visits");
  assert.equal(
    (
      await page.request.get(base + "/api/leases/" + leaseId + "/payments")
    ).status(),
    403,
  );
  await logout();
  await login("vendor@demo.test");
  await navigate("maintenance");
  await logout();
  await login("admin@demo.test");
  await navigate("settings");
  assert.equal(
    (await page.request.get(base + "/api/leases/" + leaseId)).status(),
    403,
  );
  assert.deepEqual(errors, [], "Browser JavaScript errors");
  console.log(
    "PASS: 21 owner screens, real payment, tenant request, role isolation, Arabic/English desktop and 360px layouts.",
  );
} catch (error) {
  await page.screenshot({ path: "artifacts/failure.png", fullPage: true });
  throw error;
} finally {
  await browser.close();
}
