import { t, money, date, errorText } from "./i18n.js";
export const esc = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const paths = {
  home: "M3 10 12 3l9 7v11h-6v-7H9v7H3Z",
  building: "M4 21V3h11v18M15 10h5v11M8 7h3M8 11h3M8 15h3M8 19h3",
  units: "M3 3h7v7H3ZM14 3h7v7h-7ZM3 14h7v7H3ZM14 14h7v7h-7Z",
  people:
    "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M15 3a4 4 0 0 1 0 8M22 21v-2a4 4 0 0 0-3-3.87M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0",
  document: "M14 2H4v20h16V8ZM14 2v6h6M8 12h8M8 16h8",
  money: "M3 5h18v14H3ZM7 9H6M18 15h-1M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0",
  tool: "m14 6 4 4 4-4a7 7 0 0 1-9 9L6 22l-4-4 7-7a7 7 0 0 1 9-9Z",
  chart: "M4 21V12M10 21V3M16 21V8M22 21H2",
  bell: "M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4",
  gear: "M9 3h6l1 4 4 1v8l-4 1-1 4H9l-1-4-4-1V8l4-1ZM15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0",
  link: "m9 15 6-6M7 17l-1 1a4 4 0 0 1-6-6l5-5a4 4 0 0 1 6 0M17 7l1-1a4 4 0 0 0-6-6L7 5",
  plus: "M12 5v14M5 12h14",
  arrow: "m9 5 7 7-7 7",
  logout: "M9 3H3v18h6M10 12h11M17 8l4 4-4 4",
  menu: "M3 6h18M3 12h18M3 18h18",
  calendar: "M4 5h16v16H4ZM8 2v6M16 2v6M4 11h16",
  check: "m5 12 4 4L20 5",
  shield: "M12 2 3 6v6c0 5 9 10 9 10s9-5 9-10V6ZM8 12l3 3 5-6",
};
export const icon = (name) =>
  `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="${paths[name] || paths.document}"/></svg>`;
export const btn = (label, action, extra = "", kind = "secondary") =>
  `<button class="btn ${kind}" data-action="${esc(action)}" ${extra}>${esc(t(label))}</button>`;
export const link = (label, href, kind = "secondary") =>
  `<a class="btn ${kind}" href="${esc(href)}">${esc(t(label))}</a>`;
export const badge = (status) =>
  `<span class="badge ${["BOUNCED", "ERROR", "TERMINATED"].includes(status) ? "danger" : ["OPEN", "SCHEDULED", "DEPOSITED", "UNREGISTERED", "DEV_UNSCANNED"].includes(status) ? "warning" : ["CANCELLED", "NOT_REQUESTED"].includes(status) ? "neutral" : ""}"><span class="dot"></span>${esc(t(status))}</span>`;
export function empty() {
  return `<div class="empty">${icon("document")}<h3>${esc(t("emptyTitle"))}</h3><p>${esc(t("emptyText"))}</p></div>`;
}
export function table(columns, rows, actions) {
  if (!rows.length) return empty();
  return `<div class="table-wrap"><table><thead><tr>${columns.map((c) => `<th scope="col">${esc(t(c.label || c.key))}</th>`).join("")}${actions ? `<th scope="col">${t("actions")}</th>` : ""}</tr></thead><tbody>${rows.map((r) => `<tr>${columns.map((c) => `<td>${c.render ? c.render(r) : c.type === "money" ? esc(money(r[c.key])) : c.type === "date" ? esc(date(r[c.key])) : c.type === "status" ? badge(r[c.key]) : esc(r[c.key] ?? "—")}</td>`).join("")}${actions ? `<td><div class="actions">${actions(r)}</div></td>` : ""}</tr>`).join("")}</tbody></table></div>`;
}
export const heading = (title, subtitle = "", action = "") =>
  `<div class="page-head"><div><h1>${esc(t(title))}</h1>${subtitle ? `<p>${esc(t(subtitle))}</p>` : ""}</div>${action}</div>`;
export const loading = () =>
  `<div class="skeleton" role="status"><div><div class="spinner"></div>${esc(t("loading"))}</div></div>`;
export function toast(message = t("saved")) {
  const el = document.querySelector("#toast");
  el.textContent = message;
  el.classList.add("show");
  setTimeout(() => el.classList.remove("show"), 4500);
}
export function errorBox(error) {
  return `<div class="error" role="alert">${esc(errorText(error.message || error))}</div>`;
}
export function field(f, value = "") {
  const label = t(f.label || f.key),
    required = !f.optional,
    attrs = `id="f-${esc(f.key)}" name="${esc(f.key)}" ${required ? "required" : ""}`;
  let control;
  if (f.type === "checkbox")
    return `<label class="field check-field ${f.wide ? "wide" : ""}"><input type="checkbox" id="f-${esc(f.key)}" name="${esc(f.key)}" ${value === true ? "checked" : ""}>${esc(label)}</label>`;
  if (f.options)
    control = `<select ${attrs}>${f.optional ? `<option value="">${t("noSelection")}</option>` : '<option value="">—</option>'}${f.options
      .map((o) => {
        const v = typeof o === "object" ? o.value : o,
          l = typeof o === "object" ? o.label : t(o);
        return `<option value="${esc(v)}" ${String(value) === String(v) ? "selected" : ""}>${esc(l)}</option>`;
      })
      .join("")}</select>`;
  else if (f.type === "textarea")
    control = `<textarea ${attrs} maxlength="${f.max || 2000}">${esc(value)}</textarea>`;
  else
    control = `<input ${attrs} type="${f.type === "money" ? "number" : f.type || "text"}" value="${esc(value)}" ${f.type === "money" ? 'min="0" step="0.001"' : f.type === "number" ? `min="${f.min ?? 0}" step="${f.step || 1}"` : ""} ${f.type === "password" ? `minlength="${f.key === "currentPassword" ? 1 : 12}" maxlength="72" autocomplete="${f.key === "currentPassword" ? "current-password" : "new-password"}"` : ""} ${f.type === "file" ? 'accept="application/pdf,image/png,image/jpeg"' : ""} ${f.max ? `maxlength="${f.max}"` : ""}>`;
  return `<label class="field ${f.wide ? "wide" : ""}" for="f-${esc(f.key)}"><span>${esc(label)}${f.optional ? ` <small>(${t("optional")})</small>` : ""}</span>${control}</label>`;
}
export function modal(
  title,
  fields,
  onSubmit,
  { values = {}, note = "", submit = "save" } = {},
) {
  const dialog = document.querySelector("#dialog");
  dialog.innerHTML = `<form id="modal-form"><div class="dialog-head"><h2 id="dialog-title">${esc(t(title))}</h2><button type="button" class="close" aria-label="${t("close")}">×</button></div>${note ? `<div class="notice-box">${esc(t(note))}</div>` : ""}<div class="form-grid">${fields.map((f) => field(f, values[f.key] ?? f.default ?? "")).join("")}</div><div id="form-error"></div><div class="form-actions"><button type="button" class="btn secondary close-dialog">${t("cancel")}</button><button class="btn" type="submit">${t(submit)}</button></div></form>`;
  dialog.querySelector(".close").onclick = () => dialog.close();
  dialog.querySelector(".close-dialog").onclick = () => dialog.close();
  dialog.showModal();
  const key = crypto.randomUUID();
  dialog.querySelector("form").onsubmit = async (event) => {
    event.preventDefault();
    const form = event.currentTarget,
      data = Object.fromEntries(new FormData(form));
    for (const f of fields)
      if (f.type === "checkbox") data[f.key] = form.elements[f.key].checked;
    data.idempotencyKey = key;
    const button = form.querySelector("[type=submit]");
    button.disabled = true;
    dialog.querySelector("#form-error").innerHTML = "";
    try {
      await onSubmit(data, form);
      dialog.close();
      toast();
    } catch (e) {
      dialog.querySelector("#form-error").innerHTML = errorBox(e);
    } finally {
      button.disabled = false;
    }
  };
}
export function confirmAction(action) {
  modal("confirmTitle", [], action, { note: "confirmText", submit: "confirm" });
}
