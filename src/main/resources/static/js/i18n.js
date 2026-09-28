import { copy, errors } from "../i18n/translations.js";

let language = sessionStorage.getItem("bayt-language") === "en" ? "en" : "ar";
export const lang = () => language;
export function setLanguage(value) {
  language = value === "en" ? "en" : "ar";
  sessionStorage.setItem("bayt-language", language);
  document.documentElement.lang = language;
  document.documentElement.dir = language === "ar" ? "rtl" : "ltr";
}
export const t = (key) => copy[key]?.[language === "ar" ? 0 : 1] ?? key;
export const errorText = (code) =>
  errors[code]?.[language === "ar" ? 0 : 1] ?? t("NETWORK_ERROR");
export const money = (value) =>
  new Intl.NumberFormat(language === "ar" ? "ar-OM" : "en-OM", {
    minimumFractionDigits: 3,
    maximumFractionDigits: 3,
  }).format(Number(value || 0));
export function date(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat(language === "ar" ? "ar-OM" : "en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: "Asia/Muscat",
  }).format(new Date(value.length === 10 ? value + "T12:00:00Z" : value));
}
export function today() {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Muscat",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date());
}
export const dictionaries = copy;
setLanguage(language);
