import { useState, useEffect } from 'react'
import '../App.css'

const GOOGLE_ICON = (
  <svg viewBox="0 0 24 24" width="14" height="14">
    <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
    <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
    <path fill="currentColor" d="M5.84 14.09A6.98 6.98 0 0 1 5.49 12c0-.72.13-1.43.35-2.09V7.07H2.18A11.01 11.01 0 0 0 1 12c0 1.78.43 3.45 1.18 4.93l3.66-2.84z" />
    <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
  </svg>
)

function getInitial(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

export default function GoogleFlow({ onBack, onComplete }) {
  const [subView, setSubView] = useState('picker')
  const [customEmail, setCustomEmail] = useState('')
  const [customName, setCustomName] = useState('')
  const [accounts, setAccounts] = useState([])

  useEffect(() => {
    // Fetch Google accounts detected on this machine from the Java backend
    fetch('/google-accounts')
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setAccounts(data)
        }
      })
      .catch(() => {
        // No accounts detected — user will use "Use another account"
      })
  }, [])

  const selectAccount = (account) => {
    onComplete(account.name, account.email, 'Google')
  }

  const submitCustom = () => {
    const email = customEmail.trim()
    const name = customName.trim() || email.split('@')[0]
    if (!email) return
    onComplete(name, email, 'Google')
  }

  return (
    <div className="animate-in" style={{ width: '100%', maxWidth: 400 }}>
      <button className="back-btn" onClick={subView === 'custom' ? () => setSubView('picker') : onBack}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        Back
      </button>

      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div className="provider-badge">
          {GOOGLE_ICON}
          <span>Google</span>
        </div>

        {subView === 'picker' && (
          <>
            <div className="flow-heading">
              <h2>Choose an account</h2>
              <p>Select a Google account to continue to VaultFS.</p>
            </div>

            <div className="account-list">
              {accounts.map((account, i) => (
                <button key={i} className="account-item" onClick={() => selectAccount(account)}>
                  <div className="account-avatar">{getInitial(account.name)}</div>
                  <div className="account-info">
                    <div className="account-name">{account.name}</div>
                    <div className="account-email">{account.email}</div>
                  </div>
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--gray-600)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="9 18 15 12 9 6" />
                  </svg>
                </button>
              ))}

              <button className="add-account-btn" onClick={() => setSubView('custom')}>
                <div className="add-icon">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19" />
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </div>
                Use another account
              </button>
            </div>
          </>
        )}

        {subView === 'custom' && (
          <>
            <div className="flow-heading">
              <h2>Sign in with Google</h2>
              <p>Enter your Google account details.</p>
            </div>

            <div style={{ width: '100%' }}>
              <input
                type="text"
                className="text-input"
                placeholder="Full name"
                value={customName}
                onChange={e => setCustomName(e.target.value)}
                autoComplete="off"
                spellCheck="false"
              />
              <input
                type="email"
                className="text-input"
                placeholder="Email address"
                value={customEmail}
                onChange={e => setCustomEmail(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && submitCustom()}
                autoFocus
                autoComplete="off"
                spellCheck="false"
              />
              <button
                className="submit-btn"
                onClick={submitCustom}
                disabled={!customEmail.trim()}
              >
                Continue
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
