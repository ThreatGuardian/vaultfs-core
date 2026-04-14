# VaultFS Auth Server

Handles Google and GitHub OAuth for the VaultFS CLI.

## Setup

```bash
cp .env.example .env
# Fill in your OAuth credentials
npm install
npm start
```

## Deploy to Railway

1. Push this folder to GitHub
2. Create new project on railway.app
3. Connect your repo
4. Set environment variables from .env.example
5. Deploy — Railway auto-detects Node.js

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| GET | `/auth/session/new` | Create a new auth session |
| GET | `/auth/google?sessionId=xxx` | Start Google OAuth flow |
| GET | `/auth/github?sessionId=xxx` | Start GitHub OAuth flow |
| GET | `/auth/poll?sessionId=xxx` | Poll session status (CLI uses this) |
