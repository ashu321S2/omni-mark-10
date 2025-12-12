// js/login.js
const API_BASE = "/api";

async function login() {
  const msgEl = document.getElementById("loginMessage");
  const okEl = document.getElementById("loginOk");
  if (msgEl) msgEl.textContent = "";
  if (okEl) okEl.textContent = "";

  const username = (document.getElementById("username") || {}).value?.trim() || "";
  const password = (document.getElementById("password") || {}).value?.trim() || "";

  console.log("login() called with", { usernameExists: !!username });

  if (!username || !password) {
    if (msgEl) msgEl.textContent = "Enter username & password";
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    const text = await res.text();
    let body;
    try { body = JSON.parse(text); } catch { body = text; }

    console.log("Login response:", res.status, body);

    if (!res.ok) {
      if (msgEl) msgEl.textContent = "Login failed: " + (body?.message || body?.error || res.status);
      return;
    }

    const token = body && (body.accessToken || body.token || body.jwt);
    if (!token) {
      if (msgEl) msgEl.textContent = "No token returned.";
      return;
    }

    console.log("Saving token length:", token.length);
    localStorage.setItem("accessToken", token);

    if (okEl) okEl.textContent = "Login successful — redirecting...";
    // allow UI to update briefly
    setTimeout(() => { window.location.href = "/posts.html"; }, 200);

  } catch (err) {
    console.error("Login error:", err);
    if (msgEl) msgEl.textContent = "Network error.";
  }
}

window.login = login; // keep available from console or inline onclick if needed

// Attach handler to button (safe, idempotent)
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("btnLogin");
  if (btn) {
    // ensure it's a non-submitting button
    btn.type = "button";
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      login();
    });
    console.log("login.js: attached click handler to #btnLogin");
  } else {
    console.warn("login.js: #btnLogin not found");
  }
});
