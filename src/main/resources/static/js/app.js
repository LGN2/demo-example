import { api, signIn, renewCsrf } from "./api.js";
import { t, lang, setLanguage, money, date, today } from "./i18n.js";
import {
  esc,
  icon,
  btn,
  link,
  badge,
  table,
  heading,
  empty,
  loading,
  toast,
  errorBox,
  modal,
  confirmAction,
} from "./ui.js";
import { modules, f, select, relation, cat } from "./modules.js";

let me,
  buildings = [],
  lookups = {},
  rows = [],
  currentRecord;
let buildingId = "",
  page = 0,
  search = "",
  status = "",
  activeTab = "dues",
  reportTab = "dues",
  report;
let period = { from: today().slice(0, 8) + "01", to: today(), asOf: today() },
  requestNumber = 0;
const root = document.querySelector("#app");
const staff = () => ["OWNER", "MANAGER"].includes(me?.role);
const canCreate = (key) =>
  me?.role === "OWNER" ||
  (me?.role === "MANAGER" && key !== "buildings") ||
  (me?.role === "TENANT" && ["maintenance", "documents"].includes(key)) ||
  (me?.role === "GUARD" && ["visits", "checkins"].includes(key));
const route = () => location.hash.slice(1) || "dashboard";
const query = (extra) => {
  const params = new URLSearchParams({ ...extra });
  if (buildingId) params.set("buildingId", buildingId);
  return "?" + params;
};
const localeButton = () =>
  `<button class="language" data-action="language">${lang() === "ar" ? "English" : "العربية"}</button>`;
const brand = () =>
  `<div class="brand"><div class="brand-mark">${icon("home")}</div><div><div class="brand-name">${t("brand")}</div><small>${t("tagline")}</small></div></div>`;
function navigation() {
  if (me.role === "PLATFORM_ADMIN") return [["settings", "gear"]];
  if (me.role === "VENDOR")
    return [
      ["maintenance", "tool"],
      ["documents", "document"],
    ];
  if (me.role === "GUARD") return [["operations", "shield"]];
  if (me.role === "TENANT")
    return [
      ["dashboard", "home"],
      ["leases", "document"],
      ["maintenance", "tool"],
      ["notices", "bell"],
      ["documents", "document"],
      ["meters", "chart"],
      ["parking", "units"],
    ];
  return [
    ["dashboard", "home"],
    ["buildings", "building"],
    ["units", "units"],
    ["tenants", "people"],
    ["leases", "document"],
    ["maintenance", "tool"],
    ["expenses", "money"],
    ["operations", "gear"],
    ["documents", "document"],
    ["notices", "bell"],
    ["reports", "chart"],
    ...(me.role === "OWNER" ? [["settings", "shield"]] : []),
    ["integrations", "link"],
  ];
}
function shell() {
  const key = route().split("/")[0];
  root.innerHTML = `<aside class="sidebar" aria-label="${t("management")}">${brand()}<div class="nav-section">${t("portfolio")}</div><nav>${navigation()
    .map(
      ([name, ico]) =>
        `<a href="#${name}" class="nav-item ${key === name ? "active" : ""}">${icon(ico)}<span>${t(name)}</span></a>`,
    )
    .join(
      "",
    )}</nav><div class="sidebar-foot"><div class="profile"><span class="avatar">${esc(me.name.charAt(0))}</span><div><strong>${esc(me.name)}</strong><small>${t(me.role)}</small></div></div><button class="nav-item" data-action="logout">${icon("logout")}${t("logout")}</button></div></aside><div class="workspace"><header class="topbar"><div><button class="menu-toggle" aria-label="${t("management")}" data-action="menu">${icon("menu")}</button><span class="breadcrumb">${t("home")} &nbsp; / &nbsp; <strong>${t(modules[key]?.title || key)}</strong></span></div><div class="top-actions"><span class="today">${date(today())}</span>${localeButton()}<button class="language" data-action="password" aria-label="${t("changePassword")}">${icon("shield")}</button></div></header><main id="main" class="content" tabindex="-1">${loading()}</main></div>`;
}
function showLogin(error) {
  root.innerHTML = `<main id="main" class="login"><section class="login-art">${brand()}<h1>${esc(t("loginHeadline")).replace("\n", "<br>")}</h1><p>${t("loginDescription")}</p><div class="hero-art" aria-hidden="true"><span></span><span></span><span></span></div></section><section class="login-form">${localeButton()}<h2>${t("loginTitle")}</h2><p class="muted">${t("loginText")}</p><form id="login-form"><label class="field">${t("username")}<input name="username" required autocomplete="username" dir="ltr"></label><label class="field">${t("password")}<input type="password" name="password" required autocomplete="current-password"></label><div id="login-error">${error ? errorBox(error) : ""}</div><button class="btn" type="submit">${t("login")}</button></form><p class="login-foot">${t("loginFoot")}</p></section></main>`;
  root.querySelector("form").onsubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    form.querySelector("button").disabled = true;
    try {
      me = await signIn(form.username.value, form.password.value);
      await start();
    } catch (err) {
      root.querySelector("#login-error").innerHTML = errorBox(err);
    } finally {
      form.querySelector("button").disabled = false;
    }
  };
}
async function start() {
  buildings = me.role === "PLATFORM_ADMIN" ? [] : await api("/buildings");
  if (
    !navigation().some(([key]) => route().startsWith(key)) &&
    !["lease", "job", "meter"].includes(route().split("/")[0])
  ) {
    location.hash = navigation()[0][0];
    return;
  }
  await render();
}
async function options(source) {
  if (source === "buildings")
    return buildings.map((b) => ({ value: b.id, label: b.name }));
  let data;
  if (["tenantUsers", "vendorUsers", "users"].includes(source)) {
    if (me.role === "OWNER" || me.role === "PLATFORM_ADMIN")
      data = await api("/users");
    else return [];
    if (source === "tenantUsers")
      data = data.filter((u) => u.role === "TENANT");
    if (source === "vendorUsers")
      data = data.filter((u) => u.role === "VENDOR");
  } else {
    const path = source === "tax-policies"
      ? "/tax-policies"
      : source === "meters" ? "/operations/meters" : "/" + source;
    data = [];
    for (let page = 0; ; page++) {
      const result = await api(path + query({ size: 100, page }));
      if (Array.isArray(result)) {
        data = result;
        break;
      }
      data.push(...result.items);
      if (!result.items.length || data.length >= result.total) break;
    }
  }
  lookups[source] = data;
  return data.map((r) => ({
    value: r.id,
    label:
      r.name ||
      r.displayName ||
      r.code ||
      r.accountNumber ||
      (source === "maintenance"
        ? `#${r.id} · ${r.description.slice(0, 45)}`
        : source === "tax-policies"
          ? `${r.supplyClassification} · ${t(r.treatment)}`
          : `#${r.id}`),
  }));
}
function refLabel(key, value) {
  if (value == null) return "—";
  if (key === "buildingId")
    return buildings.find((b) => b.id === value)?.name || `#${value}`;
  const source = { unitId: "units", tenantId: "tenants", userId: "users" }[key];
  const r = lookups[source]?.find((r) => r.id === value);
  return r?.code || r?.name || r?.displayName || `#${value}`;
}
const dateKeys = [
  "startDate",
  "endDate",
  "dueDate",
  "effectiveDate",
  "chequeDate",
  "expiryDate",
  "nextDue",
  "lastCompleted",
  "createdAt",
  "expenseDate",
  "effectiveFrom",
  "effectiveTo",
  "followUpDate",
  "checkIn",
  "checkOut",
];
const moneyKeys = [
  "rent",
  "deposit",
  "marketRent",
  "amount",
  "outstanding",
  "paid",
  "rentAmount",
  "taxAmount",
  "hourlyRate",
];
function column(key) {
  if (key === "action" || key === "observation") return {key,render:r=>esc(t(r[key]))};
  if (key.endsWith("Id"))
    return { key, render: (r) => esc(refLabel(key, r[key])) };
  if (["urgent", "seasonalPriority", "occupied", "canWrite"].includes(key))
    return { key, render: (r) => (r[key] ? esc(t("yes")) : esc(t("no"))) };
  if (dateKeys.includes(key)) return { key, type: "date" };
  if (moneyKeys.includes(key)) return { key, type: "money" };
  if (
    [
      "kind",
      "status",
      "availability",
      "category",
      "treatment",
      "scanStatus",
      "responsibility",
      "role",
    ].includes(key)
  )
    return { key, type: "status" };
  if (key === "description")
    return {
      key,
      render: (r) =>
        esc(
          r.description.length > 65
            ? r.description.slice(0, 65) + "…"
            : r.description,
        ),
    };
  return { key };
}
function filters(withSearch = true) {
  return `<div class="toolbar"><div class="toolbar-group">${buildings.length ? `<label><span class="muted">${t("buildingId")}</span><select id="building-filter"><option value="">${t("allBuildings")}</option>${buildings.map((b) => `<option value="${b.id}" ${String(b.id) === buildingId ? "selected" : ""}>${esc(b.name)}</option>`).join("")}</select></label>` : ""}</div>${withSearch ? `<form id="search-form"><label><span class="muted">${t("search")}</span><input class="search" type="search" name="q" value="${esc(search)}" placeholder="${t("search")}"></label></form>` : ""}</div>`;
}
function attachFilters() {
  const b = document.querySelector("#building-filter");
  if (b)
    b.onchange = () => {
      buildingId = b.value;
      period.unitId = "";
      page = 0;
      render();
    };
  const f = document.querySelector("#search-form");
  if (f)
    f.onsubmit = (e) => {
      e.preventDefault();
      search = new FormData(f).get("q");
      page = 0;
      render();
    };
}
async function render() {
  if (!me) return;
  const generation = ++requestNumber;
  shell();
  const main = document.querySelector("#main");
  const [key, id] = route().split("/");
  try {
    let html;
    if (key === "dashboard" || key === "reports") html = await dashboard(key);
    else if (key === "lease" && id) html = await leasePage(id);
    else if (key === "job" && id) html = await jobPage(id);
    else if (key === "meter" && id) html = await meterPage(id);
    else if (key === "operations") html = operationsPage();
    else if (key === "settings") html = await settingsPage();
    else if (key === "integrations") html = await integrationsPage();
    else if (modules[key]) html = await listPage(key);
    else html = empty();
    if (generation !== requestNumber) return;
    main.innerHTML = html;
    attachFilters();
    const pf = document.querySelector("#period-form");
    if (pf)
      pf.onsubmit = (e) => {
        e.preventDefault();
        period = Object.fromEntries(new FormData(pf));
        render();
      };
  } catch (e) {
    if (generation !== requestNumber) return;
    if (e.message === "UNAUTHENTICATED") {
      me = null;
      showLogin(e);
    } else
      main.innerHTML =
        heading("errorTitle") + errorBox(e) + btn("retry", "reload");
  }
}
async function listPage(key) {
  const config = modules[key];
  let data = await api(
    config.endpoint + query({ page, size: 20, q: search, status }),
  );
  if (Array.isArray(data)) {
    data = {
      items: data.filter(
        (r) =>
          !search ||
          JSON.stringify(r).toLowerCase().includes(search.toLowerCase()),
      ),
      total: data.length,
      page: 0,
      size: 100,
    };
  }
  rows = data.items;
  let performance = "";
  if (key === "vendors") {
    const stats = await api("/vendor-performance" + query({}));
    performance = `<div class="card filters"><h2>${t("performance")}</h2>${table([{ key: "name", render: (r) => esc(r.vendor.name) }, ...["assignedJobs", "completedJobs", "meanResolutionHours"].map(column)], stats)}</div>`;
  }
  if (key === "units" || key === "leases")
    await options("tenants").catch(() => {});
  if (
    ["leases", "maintenance", "parking", "visits", "leads", "meters"].includes(
      key,
    )
  )
    await options("units");
  const action = (r) => {
    if (key === "leases")
      return link("view", `#lease/${r.id}`, "small secondary");
    if (key === "maintenance")
      return link("view", `#job/${r.id}`, "small secondary");
    if (key === "meters")
      return (
        link("readings", `#meter/${r.id}`, "small secondary") +
        (staff()
          ? btn("edit", "edit", `data-id="${r.id}"`, "small secondary")
          : "")
      );
    if (key === "documents")
      return link(
        "download",
        `/api/documents/${r.id}/download`,
        "small secondary",
      );
    if (key === "expenses")
      return !r.reversedOn
        ? btn("reverse", "expense-reverse", `data-id="${r.id}"`, "small danger")
        : badge("REVERSAL");
    if (key === "visits")
      return !r.checkOut
        ? btn("checkout", "checkout", `data-id="${r.id}"`, "small secondary")
        : "";
    if (key === "preventive")
      return (
        btn(
          "complete",
          "preventive-complete",
          `data-id="${r.id}"`,
          "small secondary",
        ) + btn("edit", "edit", `data-id="${r.id}"`, "small secondary")
      );
    if (key === "notices")
      return (
        btn("view", "notice", `data-id="${r.id}"`, "small secondary") +
        (staff()
          ? btn("edit", "edit", `data-id="${r.id}"`, "small secondary")
          : "")
      );
    return config.update && canCreate(key)
      ? btn("edit", "edit", `data-id="${r.id}"`, "small secondary")
      : "";
  };
  return (
    heading(
      config.title || key,
      "",
      canCreate(key) ? btn("add", "create", "", "") : "",
    ) +
    filters() +
    (config.note ? `<div class="notice-box">${t(config.note)}</div>` : "") +
    table(config.columns.map(column), rows, action) +
    `<div class="pagination"><span>${data.total} ${t("records")}</span><div>${btn("previous", "previous", page === 0 ? "disabled" : "", "small secondary")} ${btn("next", "next", (page + 1) * 20 >= data.total ? "disabled" : "", "small secondary")}</div></div>` +
    performance
  );
}
async function openEditor(key, id) {
  const config = modules[key],
    record = id ? rows.find((r) => String(r.id) === String(id)) : {};
  let fields = [];
  for (const item of config.fields) {
    const field = { ...item };
    if (field.source) {
      field.options = await options(field.source);
      if (!field.options.length && field.optional) continue;
    }
    fields.push(field);
  }
  const defaults = { buildingId: buildingId || buildings[0]?.id, ...record };
  for (const field of fields)
    if (field.type === "date" && !defaults[field.key] && !field.optional)
      defaults[field.key] = today();
  if (key === "documents" && me.role === "TENANT") {
    fields = fields.filter((x) => !["tenantId", "readingId"].includes(x.key));
  }
  modal(
    id ? "edit" : key === "documents" ? "upload" : "add",
    fields,
    async (data, form) => {
      for (const field of fields)
        if (field.type === "datetime-local" && data[field.key])
          data[field.key] = new Date(
            data[field.key] + ":00+04:00",
          ).toISOString();
      if (key === "documents") {
        const fd = new FormData(form);
        for (const [k, v] of [...fd])
          if (!(v instanceof File) && v === "") fd.delete(k);
        await api("/documents", { method: "POST", body: fd, form: true });
      } else
        await api(config.endpoint + (id ? "/" + id : ""), {
          method: id ? "PUT" : "POST",
          body: data,
        });
      if (key === "buildings") buildings = await api("/buildings");
      await render();
    },
    { values: defaults, note: config.note },
  );
}
const printLink = (type, id, label) =>
  link(label, `/api/print/${type}/${id}?language=${lang()}`, "small secondary");
async function leasePage(id) {
  currentRecord = await api("/leases/" + id);
  const l = currentRecord;
  await Promise.all([options("units"), options("tenants")]);
  let data = await api(
    activeTab === "history"
      ? `/history/LEASE/${id}`
      : `/leases/${id}/${activeTab}`,
  );
  const entries = data.entries || data;
  rows = entries;
  const fields = {
    dues: [
      "dueDate",
      "rentAmount",
      "taxAmount",
      "amount",
      "paid",
      "outstanding",
      "daysLate",
    ],
    payments: ["id", "effectiveDate", "amount", "method", "reference"],
    cheques: ["chequeNumber", "bank", "chequeDate", "amount", "status"],
    deposits: ["id", "effectiveDate", "kind", "amount", "reason"],
    history: ["createdAt", "action", "note"],
    "follow-ups": ["createdAt", "note", "nextDate"],
  };
  const action = (r) =>
    activeTab === "dues"
      ? printLink("invoice", r.id, "invoice")
      : activeTab === "payments"
        ? printLink("receipt", r.id, "receipt") +
          (staff() && !r.reversedOn
            ? btn(
                "reverse",
                "payment-reverse",
                `data-id="${r.id}"`,
                "small danger",
              )
            : r.reversedOn
              ? badge("REVERSAL")
              : "")
        : activeTab === "cheques" &&
            staff() &&
            ["SCHEDULED", "DEPOSITED"].includes(r.status)
          ? btn(
              "transition",
              "cheque-status",
              `data-id="${r.id}"`,
              "small secondary",
            )
          : activeTab === "deposits"
            ? printLink("deposit", r.id, "receipt")
            : "";
  return (
    heading("leases", "", link("back", "#leases")) +
    `<div class="card"><div class="detail-grid">${["unitId", "tenantId", "startDate", "endDate", "rent", "deposit", "status", "municipalityStatus"].map((k) => `<div class="detail-item"><small>${t(k)}</small><strong>${k.endsWith("Id") ? esc(refLabel(k, l[k])) : moneyKeys.includes(k) ? money(l[k]) : dateKeys.includes(k) ? date(l[k]) : esc(t(l[k]))}</strong></div>`).join("")}</div><div class="toolbar-group filters">${printLink("lease", l.id, "draftLease")}${printLink("statement", l.id, "statement")}${staff() ? btn("recordPayment", "payment") + btn("recordCheque", "cheque") + btn("recordDeposit", "deposit") + btn("municipality", "municipality") + btn("renew", "renew") + (l.status === "ACTIVE" ? btn("terminate", "terminate", "", "danger") : "") : ""}</div></div><div class="tabs">${["dues", "payments", "cheques", "deposits", "history", ...(staff() ? ["follow-ups"] : [])].map((tab) => `<button data-action="lease-tab" data-tab="${tab}" class="${tab === activeTab ? "active" : ""}">${t(tab === "follow-ups" ? "followUps" : tab)}</button>`).join("")}</div>${activeTab === "cheques" ? `<div class="info-box">${t("chequeWarning")}</div>` : ""}${activeTab === "deposits" ? `<div class="info-box">${t("depositWarning")} · ${t("held")}: <strong>${money(data.held)} ${t("currency")}</strong></div>` : ""}${activeTab === "follow-ups" ? btn("add", "follow-up") : ""}${table((fields[activeTab] || []).map(column), entries, action)}`
  );
}
async function jobPage(id) {
  currentRecord = await api("/maintenance/" + id);
  const m = currentRecord;
  const events = await api("/history/MAINTENANCE/" + id);
  await options("units");
  return (
    heading("maintenance", "", link("back", "#maintenance")) +
    `<div class="card"><div class="card-head"><h2>#${m.id} · ${esc(refLabel("unitId", m.unitId))}</h2>${badge(m.status)}</div><h3>${t("originalDescription")}</h3><p class="description">${esc(m.description)}</p><div class="toolbar-group">${badge(m.category)}${m.urgent ? `<span class="badge danger">${t("urgent")}</span>` : ""}</div><div class="toolbar-group filters">${staff() || me.role === "VENDOR" ? btn("transition", "maintenance-status") : ""}${btn("addComment", "comment")}${staff() ? btn("aiSuggest", "ai") + btn("approveSummary", "approve-summary") : ""}</div>${staff() ? `<div class="notice-box">${t("aiPrivacy")}</div><small>${t("aiStatus")}: ${t(m.aiStatus)}</small>` : ""}${m.proposedSummary ? `<div class="info-box"><strong>${t("proposedSummary")}</strong><p>${esc(m.proposedSummary)} · ${t(m.proposedCategory)}</p></div>` : ""}${m.approvedSummary ? `<h3>${t("summary")}</h3><p>${esc(m.approvedSummary)}</p>` : ""}<p class="muted">${t("manualFallback")}</p></div><div class="card"><h2>${t("history")}</h2>${table(["createdAt", "action", "note"].map(column), events)}</div>`
  );
}
async function dashboard(key) {
  report = await api("/dashboard" + query(period));
  const reminders = await api("/reminders" + query({}));
  const r = report;
  const metric = (label, value, ico, note, unit = "currency") =>
    `<button class="metric ${label === "overdue" ? "alert" : ""}" data-action="metric" data-metric="${label}"><span class="metric-icon">${icon(ico)}</span><span class="metric-label">${t(label)}</span><strong class="metric-value">${unit === "currency" ? money(value) : esc(value)}</strong><span class="metric-unit">${unit === "currency" ? t("currency") : unit}</span><span class="metric-note">${t(note)}</span></button>`;
  const selectableUnits = await options("units");
  const filterHtml =
    filters(false) +
    `<form class="filters" id="period-form"><label>${t("unitId")}<select name="unitId"><option value="">${t("allUnits")}</option>${selectableUnits.map((u) => `<option value="${u.value}" ${String(period.unitId) === String(u.value) ? "selected" : ""}>${esc(u.label)}</option>`).join("")}</select></label>${["from", "to", "asOf"].map((k) => `<label>${t(k)}<input type="date" name="${k}" value="${period[k]}" required></label>`).join("")}<button class="btn secondary">${t("apply")}</button>${staff() ? link("export", "/api/reports/finance.csv" + query(period)) : ""}</form>`;
  const top =
    heading(
      key,
      key === "dashboard" ? "overviewSubtitle" : "reportingNote",
      staff() ? link("recordPayment", "#leases", "") : "",
    ) +
    filterHtml +
    (key === "dashboard"
      ? `<section class="hero"><div><div class="eyebrow">${t("heroEyebrow")}</div><h2>${t("heroTitle")}</h2><p>${t("heroText")}</p></div><div class="hero-art" aria-hidden="true"><span></span><span></span><span></span></div></section>`
      : "") +
    `<section class="metrics">${metric("expectedRent", r.expectedRent, "money", "monthlyRent")}${metric("settledCollections", r.settledCollections, "check", "grossCollections")}${metric("overdue", r.overdue, "calendar", "asOf")}${metric("occupancyPercent", r.occupancyPercent, "building", "liveRecords", "%")}</section>`;
  const detail =
    key === "reports"
      ? `<div class="card"><div class="detail-grid">${[
          "outstanding",
          "futureUnpaid",
          "recordedExpenses",
          "netOperatingIncome",
          "costPerUnit",
          "periodNetYieldPercent",
        ]
          .filter((k) => r[k] !== undefined)
          .map(
            (k) =>
              `<div class="detail-item"><small>${t(k)}</small><strong>${money(r[k])}</strong></div>`,
          )
          .join("")}</div></div>`
      : "";
  const overview =
    key === "dashboard"
      ? `<div class="dashboard-grid"><section class="card"><div class="card-head"><h2>${t("portfolioOverview")}</h2><small>${r.unitCount} ${t("units")}</small></div><div class="occupancy-bar"><span>${t("occupied")}: ${r.occupied}</span><span>${t("vacant")}: ${r.vacant}</span></div><progress value="${r.occupied}" max="${Math.max(r.unitCount, 1)}" aria-label="${t("occupancyPercent")}"></progress>${buildings
          .filter((b) => !buildingId || String(b.id) === buildingId)
          .map(
            (b) =>
              `<div class="building-row"><div class="building-name"><span class="building-glyph">${icon("building")}</span><div><strong>${esc(b.name)}</strong><small>${esc(b.wilayat)} · ${esc(b.address)}</small></div></div>${link("view", "#units", "small ghost")}</div>`,
          )
          .join(
            "",
          )}</section><section class="card"><div class="card-head"><h2>${t("recentMaintenance")}</h2>${link("seeAll", "#maintenance", "small ghost")}</div>${
          r.maintenance
            .slice(0, 4)
            .map(
              (m) =>
                `<a class="section-link list-row" href="#job/${m.id}"><div><strong>${esc(m.description.slice(0, 45))}</strong><small>${t(m.category)} · ${date(m.createdAt)}</small></div>${badge(m.status)}</a>`,
            )
            .join("") || empty()
        }<div class="key-value"><span>${t("pendingCheques")}</span><strong>${r.pendingCheques}</strong></div><div class="key-value"><span>${t("bouncedCheques")}</span><strong>${r.bouncedCheques}</strong></div></section></div>`
      : "";
  let support;
  if (reportTab === "vacancies")
    support =
      table(
        [
          { key: "unitId", render: (v) => esc(v.unit.code) },
          { key: "days" },
          { key: "estimatedForegoneRent", type: "money" },
        ],
        r.vacancies,
      ) + `<p class="notice-box">${t("vacancyAssumption")}</p>`;
  else if (reportTab === "leaseExpiries")
    support = table(
      ["id", "unitId", "endDate", "rent"].map(column),
      r.leaseExpiries,
      (l) => link("view", "#lease/" + l.id, "small secondary"),
    );
  else if (reportTab === "payments")
    support = table(
      ["id", "leaseId", "effectiveDate", "amount", "kind"].map(column),
      r.collectionEvents,
      (p) => printLink("receipt", p.id, "receipt"),
    );
  else if (reportTab === "occupancy")
    support = table(
      ["code", "buildingId", "occupied", "availability"].map(column),
      r.occupancy,
    );
  else if (reportTab === "expectedRent")
    support = table(
      ["leaseId", "dueDate", "rentAmount", "taxAmount", "amount"].map(column),
      r.dues.filter(
        (d) =>
          !d.cancelled && d.dueDate >= period.from && d.dueDate <= period.to,
      ),
      (d) => link("view", "#lease/" + d.leaseId, "small secondary"),
    );
  else
    support = table(
      ["leaseId", "dueDate", "amount", "paid", "outstanding", "daysLate"].map(
        column,
      ),
      r.dues.filter((d) => d.daysLate > 0),
      (d) => link("view", "#lease/" + d.leaseId, "small secondary"),
    );
  return (
    top +
    detail +
    overview +
    `<section class="card"><h2>${t("reminders")}</h2><div class="alert-list">${
      reminders
        .slice(0, 8)
        .map(
          (r) =>
            `<a class="alert-item section-link" href="${esc(r.link)}">${t(r.type)} · ${esc(t(r.title))} · ${date(r.dueDate)}</a>`,
        )
        .join("") || empty()
    }</div></section>` +
    `<section class="card"><div class="tabs">${["dues", "expectedRent", "payments", "occupancy", "leaseExpiries", "vacancies"].map((tab) => `<button data-action="report-tab" data-tab="${tab}" class="${reportTab === tab ? "active" : ""}">${t(tab === "dues" ? "arrears" : tab)}</button>`).join("")}</div>${support}</section><div class="footer-note"><span>${t("reportingNote")}</span><span>Bayt · ${t("oman")}</span></div>`
  );
}
function operationsPage() {
  const names =
    me.role === "GUARD"
      ? ["visits", "checkins", "parking"]
      : [
          "safety",
          "preventive",
          "vendors",
          "meters",
          "leads",
          "visits",
          "checkins",
          "parking",
          "tax-policies",
        ];
  return (
    heading("operations", "operationsText") +
    `<div class="module-grid">${names.map((name) => `<a class="card module-card" href="#${name}">${icon(modules[name].icon)}<h2>${t(modules[name].title || name)}</h2><span class="muted">${t("view")} ${icon("arrow")}</span></a>`).join("")}</div>`
  );
}
async function settingsPage() {
  rows = await api("/users");
  let grants = [];
  if (me.role === "OWNER" && buildingId)
    grants = await api("/access?buildingId=" + buildingId);
  return (
    heading("settings", "", btn("add", "user", "", "")) +
    filters() +
    table(["displayName", "username", "role"].map(column), rows) +
    (me.role === "OWNER"
      ? `<div class="card filters"><div class="toolbar-group">${btn("grant", "grant")}${btn("taxRegistered", "tax-status")}${link("taxPolicies", "#tax-policies")}</div>${table(["buildingId", "userId", "canWrite"].map(column), grants, (r) => btn("cancel", "revoke", `data-id="${r.id}"`, "small danger"))}</div>`
      : "")
  );
}
async function integrationsPage() {
  const data = await api("/integrations");
  return (
    heading("integrations", "integrationText") +
    `<div class="integration-grid">${data.map((i) => `<article class="integration-card">${icon(i.key === "ai" ? "tool" : "link")}<h2>${t(i.key)}</h2><p>${t(i.key === "ai" ? "manualFallback" : "providerNeeded")}</p>${badge(i.available ? "AVAILABLE" : "UNAVAILABLE")}</article>`).join("")}</div>`
  );
}
async function meterPage(id) {
  currentRecord = await api("/operations/meters/" + id);
  rows = await api("/operations/meters/" + id + "/readings");
  return (
    heading("readings", "", link("back", "#meters")) +
    `<div class="card"><h2>${esc(currentRecord.accountNumber)} · ${t(currentRecord.kind)}</h2>${staff() ? btn("add", "reading") : ""}<p class="notice-box">${t("UNUSUAL_CONSUMPTION")}</p></div>` +
    table(["readingDate", "value", "kind", "observation"].map(column), rows)
  );
}
async function simpleForm(
  title,
  fields,
  path,
  values = {},
  note = "",
  method = "POST",
) {
  let prepared = [];
  for (const field of fields) {
    prepared.push(
      field.source ? { ...field, options: await options(field.source) } : field,
    );
  }
  modal(
    title,
    prepared,
    async (data) => {
      await api(path, { method, body: data });
      await render();
    },
    { values, note },
  );
}
async function handleAction(action, el) {
  const [key, id] = route().split("/");
  const target = el.dataset.id;
  const lease = currentRecord;
  let data;
  switch (action) {
    case "language":
      setLanguage(lang() === "ar" ? "en" : "ar");
      me ? render() : showLogin();
      break;
    case "menu":
      document.querySelector(".sidebar").classList.toggle("open");
      break;
    case "logout":
      await api("/auth/logout", { method: "POST" });
      me = null;
      lookups = {};
      showLogin();
      break;
    case "reload":
      await render();
      break;
    case "previous":
      page = Math.max(0, page - 1);
      await render();
      break;
    case "next":
      page++;
      await render();
      break;
    case "create":
      await openEditor(key);
      break;
    case "edit":
      await openEditor(key, target);
      break;
    case "lease-tab":
      activeTab = el.dataset.tab;
      await render();
      break;
    case "report-tab":
      reportTab = el.dataset.tab;
      await render();
      break;
    case "metric":
      reportTab =
        el.dataset.metric === "settledCollections"
          ? "payments"
          : el.dataset.metric === "occupancyPercent"
            ? "occupancy"
            : el.dataset.metric === "expectedRent"
              ? "expectedRent"
              : "dues";
      await render();
      document.querySelector(".tabs")?.scrollIntoView({ behavior: "smooth" });
      break;
    case "payment":
      await simpleForm(
        "recordPayment",
        [
          f("amount", "money"),
          select("method", ["BANK_TRANSFER", "CASH", "OTHER"]),
          f("effectiveDate", "date"),
          f("reference", "text", { optional: true }),
        ],
        `/leases/${id}/payments`,
        { effectiveDate: today() },
      );
      break;
    case "cheque":
      await simpleForm(
        "recordCheque",
        [
          f("chequeNumber"),
          f("bank"),
          f("chequeDate", "date"),
          f("amount", "money"),
        ],
        `/leases/${id}/cheques`,
        { chequeDate: today() },
        "chequeWarning",
      );
      break;
    case "deposit":
      await simpleForm(
        "recordDeposit",
        [
          select("kind", ["RECEIPT", "DEDUCTION", "REFUND", "REVERSAL"]),
          f("amount", "money"),
          f("effectiveDate", "date"),
          f("reason"),
          f("reversesId", "number", { optional: true }),
        ],
        `/leases/${id}/deposits`,
        { effectiveDate: today() },
        "depositWarning",
      );
      break;
    case "payment-reverse":
      await simpleForm("reverse", [f("reason")], `/payments/${target}/reverse`);
      break;
    case "expense-reverse":
      await simpleForm("reverse", [f("reason")], `/expenses/${target}/reverse`);
      break;
    case "cheque-status":
      data = rows.find((r) => String(r.id) === target);
      await simpleForm(
        "transition",
        [
          select(
            "status",
            data.status === "SCHEDULED"
              ? ["DEPOSITED", "CANCELLED"]
              : ["CLEARED", "BOUNCED", "CANCELLED"],
          ),
          f("effectiveDate", "date"),
        ],
        `/cheques/${target}/status`,
        { effectiveDate: today() },
        "chequeWarning",
      );
      break;
    case "municipality":
      await simpleForm(
        "municipality",
        [
          select("municipalityStatus", [
            "UNREGISTERED",
            "SUBMITTED",
            "REGISTERED",
          ]),
          f("municipalityAuthority", "text", { optional: true }),
          f("municipalityReference", "text", { optional: true }),
          f("municipalityFee", "money"),
        ],
        `/leases/${id}/municipality`,
        lease,
        "",
        "PUT",
      );
      break;
    case "renew":
      await simpleForm(
        "renew",
        modules.leases.fields,
        `/leases/${id}/renew`,
        {
          ...lease,
          startDate: new Date(
            new Date(lease.endDate + "T12:00Z").getTime() + 86400000,
          )
            .toISOString()
            .slice(0, 10),
          months: 12,
        },
        "leaseWarning",
      );
      break;
    case "terminate":
      await simpleForm(
        "terminate",
        [f("terminatedOn", "date"), f("reason")],
        `/leases/${id}/terminate`,
        { terminatedOn: today() },
        "confirmText",
      );
      break;
    case "follow-up":
      await simpleForm(
        "add",
        [f("note"), f("nextDate", "date", { optional: true })],
        `/leases/${id}/follow-ups`,
      );
      break;
    case "comment":
      await simpleForm(
        "addComment",
        [f("note", "textarea", { wide: true })],
        `/maintenance/${id}/comments`,
      );
      break;
    case "maintenance-status": {
      const next = {
        OPEN: "ASSIGNED",
        ASSIGNED: "IN_PROGRESS",
        IN_PROGRESS: "RESOLVED",
        RESOLVED: "CLOSED",
      }[currentRecord.status];
      if (!next) throw new Error("INVALID_TRANSITION");
      const fields = [
        select("status", [next]),
        f("note", "text", { optional: true }),
      ];
      if (next === "ASSIGNED") {
        const choices = await api(
          "/assignees?buildingId=" + currentRecord.buildingId,
        );
        fields.push(
          select(
            "assignedTo",
            choices.map((u) => ({ value: u.id, label: u.displayName })),
          ),
        );
      }
      await simpleForm("transition", fields, `/maintenance/${id}/status`, {
        status: next,
      });
      break;
    }
    case "ai":
      confirmAction(async () => {
        await api(`/maintenance/${id}/ai-suggestion`, {
          method: "POST",
          body: { language: lang() },
        });
        await render();
      });
      break;
    case "approve-summary":
      await simpleForm(
        "approveSummary",
        [
          f("summary", "textarea", { max: 255, wide: true }),
          select("category", cat),
        ],
        `/maintenance/${id}/approve-summary`,
        {
          summary:
            currentRecord.proposedSummary || currentRecord.approvedSummary,
          category: currentRecord.proposedCategory || currentRecord.category,
        },
      );
      break;
    case "password":
      await simpleForm(
        "changePassword",
        [f("currentPassword", "password"), f("newPassword", "password")],
        "/auth/password",
      );
      break;
    case "user":
      await simpleForm(
        "add",
        [
          f("username"),
          f("displayName"),
          f("password", "password"),
          select(
            "role",
            me.role === "PLATFORM_ADMIN"
              ? ["OWNER", "PLATFORM_ADMIN"]
              : ["MANAGER", "TENANT", "VENDOR", "GUARD"],
          ),
        ],
        "/users",
      );
      break;
    case "grant":
      await simpleForm(
        "grant",
        [
          relation("buildingId", "buildings"),
          relation("userId", "users"),
          f("canWrite", "checkbox"),
        ],
        "/access",
        { buildingId },
      );
      break;
    case "revoke":
      confirmAction(async () => {
        await api("/access/" + target, { method: "DELETE" });
        await render();
      });
      break;
    case "tax-status":
      await simpleForm(
        "taxRegistered",
        [f("taxRegistered", "checkbox")],
        "/owner/tax-status",
        {},
        "taxNotice",
        "PUT",
      );
      break;
    case "checkout":
      confirmAction(async () => {
        await api(`/operations/visits/${target}/checkout`, {
          method: "POST",
          body: {},
        });
        await render();
      });
      break;
    case "preventive-complete":
      confirmAction(async () => {
        await api(`/operations/preventive/${target}/complete`, {
          method: "POST",
          body: {},
        });
        await render();
      });
      break;
    case "reading":
      await simpleForm(
        "add",
        [
          f("readingDate", "date"),
          f("value", "money"),
          select("kind", ["ROUTINE", "MOVE_IN", "MOVE_OUT"]),
        ],
        `/operations/meters/${id}/readings`,
        { readingDate: today() },
      );
      break;
    case "notice": {
      const n = rows.find((r) => String(r.id) === target);
      modal(
        lang() === "ar" ? n.titleAr : n.titleEn,
        [f("description", "textarea", { wide: true })],
        async () => {
          await navigator.clipboard.writeText(
            lang() === "ar"
              ? n.titleAr + "\n" + n.bodyAr
              : n.titleEn + "\n" + n.bodyEn,
          );
        },
        {
          values: { description: lang() === "ar" ? n.bodyAr : n.bodyEn },
          submit: "manualShare",
        },
      );
      break;
    }
  }
}
document.addEventListener("click", async (event) => {
  const el = event.target.closest("[data-action]");
  if (!el) return;
  try {
    await handleAction(el.dataset.action, el);
  } catch (e) {
    toast(errorTextSafe(e));
  }
});
function errorTextSafe(e) {
  const holder = document.createElement("div");
  holder.innerHTML = errorBox(e);
  return holder.textContent;
}
window.addEventListener("hashchange", () => {
  page = 0;
  search = "";
  status = "";
  activeTab = "dues";
  render();
});
try {
  me = await api("/auth/me");
  await start();
} catch (e) {
  showLogin(e.message === "UNAUTHENTICATED" ? null : e);
}
