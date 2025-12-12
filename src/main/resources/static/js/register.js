// js/register.js
const API_BASE = "/api";

async function registerHandler() {
  const msg = document.getElementById('registerMessage');
  msg.textContent = '';

  const name = (document.getElementById('name')||{}).value.trim();
  const username = (document.getElementById('username')||{}).value.trim();
  const email = (document.getElementById('email')||{}).value.trim();
  const password = (document.getElementById('password')||{}).value.trim();

  if (!username || !email || !password) {
    msg.textContent = 'Username, email and password are required.';
    return;
  }

  try {
    console.log('Register: sending request for', username);
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, username, email, password })
    });

    const text = await res.text();
    let body;
    try { body = JSON.parse(text); } catch(e) { body = text; }

    console.log('Register response', res.status, body);

    if (!res.ok) {
      const serverMsg = (body && (body.message || body.error)) ? (body.message || body.error) : `Status ${res.status}`;
      msg.textContent = `Registration failed: ${serverMsg}`;
      return;
    }

    msg.style.color = 'green';
    msg.textContent = 'Registration successful — redirecting to login...';
    setTimeout(() => { window.location.href = '/index.html'; }, 700);
  } catch (err) {
    console.error('Register fetch error', err);
    msg.textContent = 'Network/CORS error — check backend and CORS settings.';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('btnRegister')?.addEventListener('click', registerHandler);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      const active = document.activeElement;
      if (active && ['name','username','email','password'].includes(active.id)) {
        e.preventDefault();
        registerHandler();
      }
    }
  });
});

window.register = registerHandler;
