// js/register.js
const API_BASE = "/api";

async function registerHandler(e) {
  // Prevent form submit / page reload
  if (e) e.preventDefault();

  const msg = document.getElementById("registerMessage");
  msg.textContent = "";
  msg.style.color = "red";

  const nameEl = document.getElementById("name");
  const usernameEl = document.getElementById("username");
  const emailEl = document.getElementById("email");
  const passwordEl = document.getElementById("password");

  const name = nameEl ? nameEl.value.trim() : "";
  const username = usernameEl ? usernameEl.value.trim() : "";
  const email = emailEl ? emailEl.value.trim() : "";
  const password = passwordEl ? passwordEl.value.trim() : "";

  if (!username || !email || !password) {
    msg.textContent = "Username, email and password are required.";
    return;
  }

  try {
    console.log("Register: sending request for", username);

    const res = await fetch(`${API_BASE}/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        username,
        email,
        password
      })
    });

    let data;
    try {
      data = await res.json();
    } catch {
      data = {};
    }

    console.log("Register response", res.status, data);

    if (!res.ok) {
      msg.textContent =
        data.message ||
        data.error ||
        `Registration failed (status ${res.status})`;
      return;
    }

    msg.style.color = "green";
    msg.textContent = "Registration successful — redirecting to login...";

    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1000);

  } catch (err) {
    console.error("Register fetch error", err);
    msg.textContent = "Network error — backend not reachable.";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // Handle button click
  document
    .getElementById("btnRegister")
    ?.addEventListener("click", registerHandler);

  // Handle Enter key inside inputs
  document.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      const active = document.activeElement;
      if (
        active &&
        ["name", "username", "email", "password"].includes(active.id)
      ) {
        e.preventDefault();
        registerHandler(e);
      }
    }
  });
});

// Optional global access
window.register = registerHandler;
