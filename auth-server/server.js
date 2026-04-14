require('dotenv').config();
const express = require('express');
const cors = require('cors');
const axios = require('axios');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 4000;
const SERVER_URL = process.env.SERVER_URL || 'http://localhost:4000';
console.log(`[VaultFS] Auth server URL: ${SERVER_URL}`);

// ─── Middleware ───────────────────────────────────────────────
app.use(cors());
app.use(express.json());

// ─── Request logger ─────────────────────────────────────────
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// ─── Session store ───────────────────────────────────────────
const sessions = new Map();

// Clean expired sessions every 60 seconds
setInterval(() => {
  const now = Date.now();
  for (const [id, session] of sessions.entries()) {
    if (session.expiresAt && now > session.expiresAt) {
      sessions.delete(id);
    }
  }
}, 60 * 1000);

// ─── Root landing page ──────────────────────────────────────
app.get('/', (req, res) => {
  res.send(`
  <!DOCTYPE html>
  <html>
  <head>
      <title>VaultFS Auth Server</title>
      <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body { background: #0a0a0a; color: #fff; font-family: 'Segoe UI', sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; }
          .card { background: #111; border: 1px solid #333; border-radius: 16px; padding: 48px; text-align: center; width: 420px; }
          h1 { font-size: 2rem; margin-bottom: 8px; }
          .badge { display: inline-block; background: #238636; color: #fff; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; margin-bottom: 24px; }
          p { color: #888; line-height: 1.6; }
          code { background: #1a1a1a; padding: 2px 8px; border-radius: 4px; color: #58a6ff; }
      </style>
  </head>
  <body>
      <div class="card">
          <h1>\uD83D\uDD10 VaultFS</h1>
          <div class="badge">Auth Server v1.0.3</div>
          <p>This is the VaultFS authentication server.</p>
          <p style="margin-top: 12px;">Install the CLI: <code>npm install -g vaultfs</code></p>
      </div>
  </body>
  </html>
  `);
});

// ─── Health check ────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', version: '1.0.3' });
});

// ─── Create new auth session ─────────────────────────────────
app.get('/auth/session/new', (req, res) => {
  const sessionId = uuidv4();
  sessions.set(sessionId, { status: 'pending', user: null, expiresAt: Date.now() + 5 * 60 * 1000 });
  // Also clean up via setTimeout as a fallback
  setTimeout(() => sessions.delete(sessionId), 5 * 60 * 1000);
  res.json({ sessionId });
});

// ─── Login page ─────────────────────────────────────────────
app.get('/auth/login', (req, res) => {
  const { sessionId } = req.query;
  res.send(`
  <!DOCTYPE html>
  <html>
  <head>
      <title>VaultFS Login</title>
      <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body { background: #0a0a0a; color: #fff; font-family: 'Segoe UI', sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; }
          .card { background: #111; border: 1px solid #333; border-radius: 16px; padding: 48px; text-align: center; width: 380px; }
          h1 { font-size: 2rem; margin-bottom: 8px; }
          p { color: #888; margin-bottom: 32px; }
          .btn { display: block; width: 100%; padding: 14px; border-radius: 8px; border: none; font-size: 1rem; font-weight: 600; cursor: pointer; margin-bottom: 12px; text-decoration: none; }
          .google { background: #fff; color: #000; }
          .github { background: #238636; color: #fff; }
          .guest { background: transparent; color: #888; border: 1px solid #333; }
      </style>
  </head>
  <body>
      <div class="card">
          <h1>\uD83D\uDD10 VaultFS</h1>
          <p>Secure File System Simulator</p>
          <a class="btn google" href="/auth/google?sessionId=${sessionId}">Continue with Google</a>
          <a class="btn github" href="/auth/github?sessionId=${sessionId}">Continue with GitHub</a>
          <a class="btn guest" href="/auth/guest?sessionId=${sessionId}">Continue as Guest</a>
      </div>
  </body>
  </html>
  `);
});

// ─── Guest login ─────────────────────────────────────────────
app.get('/auth/guest', (req, res) => {
  const { sessionId } = req.query;
  if (!sessionId || !sessions.has(sessionId)) {
    return res.status(400).json({ error: 'Invalid or missing sessionId' });
  }
  sessions.set(sessionId, {
    status: 'done',
    user: { name: 'Guest', email: 'guest@vaultfs.local', avatar: null, provider: 'guest' }
  });
  res.redirect(`${SERVER_URL}/auth/success?name=Guest&provider=guest`);
});

// ─── Google OAuth ────────────────────────────────────────────
app.get('/auth/google', (req, res) => {
  const { sessionId } = req.query;
  if (!sessionId || !sessions.has(sessionId)) {
    return res.status(400).json({ error: 'Invalid or missing sessionId' });
  }
  const params = new URLSearchParams({
    client_id: process.env.GOOGLE_CLIENT_ID,
    redirect_uri: `${SERVER_URL}/auth/google/callback`,
    response_type: 'code',
    scope: 'openid email profile',
    state: sessionId
  });
  res.redirect(`https://accounts.google.com/o/oauth2/v2/auth?${params}`);
});

app.get('/auth/google/callback', async (req, res) => {
  const { code, state } = req.query;
  if (!state || !sessions.has(state)) {
    return res.status(400).send('Invalid session. Please restart login.');
  }

  try {
    // Exchange code for tokens
    const tokenRes = await axios.post('https://oauth2.googleapis.com/token', {
      code,
      client_id: process.env.GOOGLE_CLIENT_ID,
      client_secret: process.env.GOOGLE_CLIENT_SECRET,
      redirect_uri: `${SERVER_URL}/auth/google/callback`,
      grant_type: 'authorization_code'
    });

    // Get user info
    const userRes = await axios.get('https://www.googleapis.com/oauth2/v3/userinfo', {
      headers: { Authorization: `Bearer ${tokenRes.data.access_token}` }
    });

    const { name, email, picture } = userRes.data;
    sessions.set(state, {
      status: 'done',
      user: { name, email, avatar: picture, provider: 'google' }
    });

    res.redirect(`${SERVER_URL}/auth/success?name=${encodeURIComponent(name)}&provider=google`);
  } catch (err) {
    console.error('[Google callback error]', err.response?.data || err.message);
    sessions.set(state, { status: 'error', user: null });
    res.status(500).send('Google authentication failed. Please try again.');
  }
});

// ─── GitHub OAuth ────────────────────────────────────────────
app.get('/auth/github', (req, res) => {
  const { sessionId } = req.query;
  if (!sessionId || !sessions.has(sessionId)) {
    return res.status(400).json({ error: 'Invalid or missing sessionId' });
  }
  const params = new URLSearchParams({
    client_id: process.env.GITHUB_CLIENT_ID,
    redirect_uri: `${SERVER_URL}/auth/github/callback`,
    scope: 'read:user user:email',
    state: sessionId
  });
  res.redirect(`https://github.com/login/oauth/authorize?${params}`);
});

app.get('/auth/github/callback', async (req, res) => {
  const { code, state } = req.query;
  if (!state || !sessions.has(state)) {
    return res.status(400).send('Invalid session. Please restart login.');
  }

  try {
    // Exchange code for access token
    const tokenRes = await axios.post('https://github.com/login/oauth/access_token', {
      client_id: process.env.GITHUB_CLIENT_ID,
      client_secret: process.env.GITHUB_CLIENT_SECRET,
      code,
      redirect_uri: `${SERVER_URL}/auth/github/callback`
    }, {
      headers: { Accept: 'application/json' }
    });

    const accessToken = tokenRes.data.access_token;

    // Get user info
    const userRes = await axios.get('https://api.github.com/user', {
      headers: { Authorization: `Bearer ${accessToken}` }
    });

    let email = userRes.data.email;

    // If email is private, fetch from /user/emails
    if (!email) {
      try {
        const emailRes = await axios.get('https://api.github.com/user/emails', {
          headers: { Authorization: `Bearer ${accessToken}` }
        });
        const primary = emailRes.data.find(e => e.primary && e.verified);
        if (primary) email = primary.email;
      } catch {
        // Email fetch failed — continue without it
      }
    }

    sessions.set(state, {
      status: 'done',
      user: {
        name: userRes.data.name || userRes.data.login,
        email: email || 'unknown',
        avatar: userRes.data.avatar_url,
        provider: 'github'
      }
    });

    const displayName = userRes.data.name || userRes.data.login;
    res.redirect(`${SERVER_URL}/auth/success?name=${encodeURIComponent(displayName)}&provider=github`);
  } catch (err) {
    console.error('[GitHub callback error]', err.response?.data || err.message);
    sessions.set(state, { status: 'error', user: null });
    res.status(500).send('GitHub authentication failed. Please try again.');
  }
});

// ─── Poll session status ─────────────────────────────────────
app.get('/auth/poll', (req, res) => {
  const { sessionId } = req.query;
  const session = sessions.get(sessionId);
  if (!session) return res.json({ status: 'expired' });
  if (session.status === 'done') {
    sessions.delete(sessionId); // one-time use
    return res.json({ status: 'done', user: session.user });
  }
  res.json({ status: session.status });
});

// ─── Success page ─────────────────────────────────────────
app.get('/auth/success', (req, res) => {
  const { name, provider } = req.query;
  res.send(`
  <!DOCTYPE html>
  <html>
  <head>
      <title>VaultFS \u2014 Login Successful</title>
      <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body { background: #0a0a0a; color: #fff; font-family: 'Segoe UI', sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; }
          .card { background: #111; border: 1px solid #238636; border-radius: 16px; padding: 48px; text-align: center; width: 400px; }
          h1 { font-size: 2rem; color: #238636; margin-bottom: 12px; }
          p { color: #888; line-height: 1.8; }
          .provider { display: inline-block; background: #1a1a1a; border: 1px solid #333; padding: 4px 12px; border-radius: 20px; font-size: 0.85rem; color: #58a6ff; margin-top: 8px; }
      </style>
  </head>
  <body>
      <div class="card">
          <h1>\u2705 Login Successful</h1>
          <p>Welcome, <strong>${name}</strong>!</p>
          <span class="provider">via ${provider}</span>
          <p style="margin-top: 24px; font-size: 0.9rem;">You can close this tab.<br/>Return to your terminal to use VaultFS.</p>
      </div>
  </body>
  </html>
  `);
});

// ─── Error handler ──────────────────────────────────────────
app.use((err, req, res, next) => {
  console.error(`[ERROR] ${err.message}`);
  res.status(500).json({ error: 'Internal server error' });
});

// ─── Start server ────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557`);
  console.log(`\u2551   VaultFS Auth Server v1.0.3         \u2551`);
  console.log(`\u2551   Running on port ${PORT}               \u2551`);
  console.log(`\u2551   Google OAuth: ${process.env.GOOGLE_CLIENT_ID ? '\u2705' : '\u274C'}                 \u2551`);
  console.log(`\u2551   GitHub OAuth: ${process.env.GITHUB_CLIENT_ID ? '\u2705' : '\u274C'}                 \u2551`);
  console.log(`\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D`);
});
