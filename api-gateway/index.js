require('dotenv').config();
const express = require('express');
const axios = require('axios');
const morgan = require('morgan');

const AUTH_URL = process.env.AUTH_URL || 'http://localhost:8080';
const PROFILE_URL = process.env.PROFILE_URL || 'http://localhost:8081';
const NOTIF_URL = process.env.NOTIF_URL || ''; // optional

const app = express();
app.use(express.json());
app.use(morgan('tiny'));

// Health
app.get('/health', (req, res) => res.json({ status: 'ok', service: 'api-gateway' }));

// Auth proxy: login
app.post('/api/auth/login', async (req, res) => {
  try {
    const r = await axios.post(`${AUTH_URL}/api/users/login`, req.body, { headers: forwardHeaders(req) });
    res.status(r.status).json(r.data);
  } catch (e) {
    handleAxiosError(e, res);
  }
});

// Auth proxy: register
app.post('/api/auth/register', async (req, res) => {
  try {
    const r = await axios.post(`${AUTH_URL}/api/users`, req.body, { headers: forwardHeaders(req) });
    res.status(r.status).json(r.data);
  } catch (e) {
    handleAxiosError(e, res);
  }
});

// Auth proxy: delete user and publish event to notification-service if configured
app.delete('/api/auth/users/:id', async (req, res) => {
  const id = req.params.id;
  try {
    const r = await axios.delete(`${AUTH_URL}/api/users/${id}`, { headers: forwardHeaders(req) });
    // Emit event to notification service if configured (best-effort)
    if (NOTIF_URL) {
      try {
        await axios.post(`${NOTIF_URL}/api/notifications`, {
          channel: 'system',
          recipientEmail: req.body?.email || '',
          recipientPhone: req.body?.phone || '',
          content: `User ${id} deleted`
        });
      } catch (err) {
        console.warn('Failed to notify notification-service:', err.message);
      }
    }
    res.status(r.status).json(r.data);
  } catch (e) {
    handleAxiosError(e, res);
  }
});

// Combined read: fetch auth user (from list) and profile, then unify
app.get('/api/users/combined/:userId', async (req, res) => {
  const userId = req.params.userId;
  try {
    const [authListResp, profileResp] = await Promise.all([
      axios.get(`${AUTH_URL}/api/users?page=0&size=1000`, { headers: forwardHeaders(req) }).catch(e => ({ data: null })),
      axios.get(`${PROFILE_URL}/v1/profiles/${userId}`, { headers: forwardHeaders(req) }).catch(e => ({ data: null }))
    ]);

    let user = undefined;
    if (authListResp?.data) {
      // authListResp.data is a Page-like object: content field or content array
      const page = authListResp.data;
      const candidates = page.content || page; // tolerant
      if (Array.isArray(candidates)) user = candidates.find(u => u.id === userId) || null;
    }

    const profile = profileResp?.data || null;
    res.json({ user, profile });
  } catch (e) {
    handleAxiosError(e, res);
  }
});

// Combined update: accepts { auth?: {...}, profile?: {...} }
app.put('/api/users/combined/:userId', async (req, res) => {
  const userId = req.params.userId;
  const { auth, profile } = req.body || {};
  const results = {};
  try {
    if (auth) {
      const r = await axios.put(`${AUTH_URL}/api/users/${userId}`, auth, { headers: forwardHeaders(req) });
      results.auth = r.data;
    }
    if (profile) {
      const r = await axios.put(`${PROFILE_URL}/v1/profiles/${userId}`, profile, { headers: forwardHeaders(req) });
      results.profile = r.data;
    }
    res.json({ updated: results });
  } catch (e) {
    handleAxiosError(e, res);
  }
});

function forwardHeaders(req) {
  const headers = {};
  if (req.headers.authorization) headers.authorization = req.headers.authorization;
  return headers;
}

function handleAxiosError(e, res) {
  if (e.response) {
    res.status(e.response.status).json(e.response.data);
  } else {
    res.status(500).json({ error: e.message });
  }
}

const port = process.env.PORT || 8090;
app.listen(port, () => console.log(`api-gateway listening on ${port} (AUTH=${AUTH_URL}, PROFILE=${PROFILE_URL})`));
