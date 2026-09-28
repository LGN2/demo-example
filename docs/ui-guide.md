# UI guide

Spring Boot serves `static/index.html`. Styles are in `static/css`, ES modules in `static/js`, bilingual dictionaries in `static/i18n/translations.js`, and the favicon in `static/assets`. No Node server is required at runtime.

Arabic and RTL are the default. The language switch updates the document direction and labels; only the language preference is stored in session storage. Money is displayed with three decimal places; dates use Asia/Muscat. The server owns all application data and authorization.

Log in using a development account from the README. Select a building where required, then use the navigation for properties, tenancy, finance, maintenance, operations and reports. Role-specific screens expose only relevant actions. Field errors and service errors appear in the selected language. See [demo script](demo-script.md) and [responsive checks](responsive-checklist.md).
