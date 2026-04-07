import { useState } from 'react'
import '../App.css'

const GITHUB_ICON = (
  <svg viewBox="0 0 24 24" width="14" height="14">
    <path fill="currentColor" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844a9.59 9.59 0 0 1 2.504.337c1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0 0 22 12.017C22 6.484 17.522 2 12 2z" />
  </svg>
)

export default function GitHubFlow({ onBack, onComplete }) {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')

  const submit = () => {
    const u = username.trim()
    if (!u) return
    const e = email.trim() || (u.toLowerCase().replace(/\s+/g, '') + '@github.com')
    onComplete(u, e, 'GitHub')
  }

  return (
    <div className="animate-in" style={{ width: '100%', maxWidth: 400 }}>
      <button className="back-btn" onClick={onBack}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        Back
      </button>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div className="provider-badge">
          {GITHUB_ICON}
          <span>GitHub</span>
        </div>

        <div className="flow-heading">
          <h2>Sign in to GitHub</h2>
          <p>Enter your GitHub credentials to continue.</p>
        </div>
      </div>

      <div className="github-form">
        <div className="input-group">
          <label className="input-label">Username or email address</label>
          <input
            type="text"
            className="text-input"
            placeholder="username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            autoFocus
            autoComplete="off"
            spellCheck="false"
          />
        </div>

        <div className="input-group">
          <label className="input-label">Email (optional)</label>
          <input
            type="email"
            className="text-input"
            placeholder="you@example.com"
            value={email}
            onChange={e => setEmail(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && submit()}
            autoComplete="off"
            spellCheck="false"
          />
        </div>

        <button
          className="submit-btn"
          onClick={submit}
          disabled={!username.trim()}
        >
          Sign In
        </button>
      </div>
    </div>
  )
}
