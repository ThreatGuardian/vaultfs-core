import { useEffect } from 'react'
import '../App.css'

export default function SuccessView() {
  useEffect(() => {
    const timer = setTimeout(() => {
      try { window.close() } catch (e) { /* ignore */ }
    }, 3000)
    return () => clearTimeout(timer)
  }, [])

  return (
    <div className="success-container">
      <div className="check-ring">
        <svg viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="20 6 9 17 4 12" />
        </svg>
      </div>
      <h1>You're all set.</h1>
      <p>Authentication successful. You can close this tab and return to your terminal.</p>
      <div className="success-hint">This window will close automatically.</div>
    </div>
  )
}
