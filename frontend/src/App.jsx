import { useState } from 'react'
import MainView from './components/MainView'
import LoadingOverlay from './components/LoadingOverlay'

export default function App() {
  const [loadingText, setLoadingText] = useState('')
  const [loading, setLoading] = useState(false)

  const handleGoogle = () => {
    setLoadingText('Redirecting to Google...')
    setLoading(true)
    setTimeout(() => {
      window.location.href = '/auth/google'
    }, 400)
  }

  const handleGitHub = () => {
    setLoadingText('Redirecting to GitHub...')
    setLoading(true)
    setTimeout(() => {
      window.location.href = '/auth/github'
    }, 400)
  }

  const handleGuest = () => {
    setLoadingText('Setting up guest session...')
    setLoading(true)
    setTimeout(() => {
      window.location.href = '/callback'
    }, 400)
  }

  return (
    <>
      <LoadingOverlay text={loadingText} visible={loading} />

      <div className="page-wrapper">
        <MainView
          onGoogle={handleGoogle}
          onGitHub={handleGitHub}
          onGuest={handleGuest}
        />
      </div>

      <div className="footer">VaultFS &middot; Secure File System Simulator</div>
    </>
  )
}
