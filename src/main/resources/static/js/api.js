let csrf;
export async function renewCsrf() {
  const response = await fetch("/api/auth/csrf", {
    credentials: "same-origin",
  });
  if (!response.ok) throw new Error("NETWORK_ERROR");
  csrf = await response.json();
  return csrf;
}
export async function api(path, { method = "GET", body, form = false } = {}) {
  const headers = { Accept: "application/json" };
  if (method !== "GET") {
    if (!csrf) await renewCsrf();
    headers[csrf.headerName] = csrf.token;
  }
  if (body !== undefined && !form) headers["Content-Type"] = "application/json";
  let response;
  try {
    response = await fetch("/api" + path, {
      method,
      headers,
      credentials: "same-origin",
      body: body === undefined ? undefined : form ? body : JSON.stringify(body),
    });
  } catch {
    throw new Error("NETWORK_ERROR");
  }
  if (!response.ok) {
    let error;
    try {
      error = await response.json();
    } catch {}
    throw new Error(
      error?.code ||
        (response.status === 401 ? "UNAUTHENTICATED" : "NETWORK_ERROR"),
    );
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}
export async function signIn(username, password) {
  await renewCsrf();
  const body = new URLSearchParams({ username, password });
  const response = await fetch("/api/auth/login", {
    method: "POST",
    credentials: "same-origin",
    headers: {
      [csrf.headerName]: csrf.token,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body,
  });
  if (!response.ok) throw new Error("LOGIN_FAILED");
  await renewCsrf();
  return api("/auth/me");
}
