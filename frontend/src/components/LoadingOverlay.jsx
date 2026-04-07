import '../App.css'

export default function LoadingOverlay({ text, visible }) {
  return (
    <div className={`loading-overlay ${visible ? 'active' : ''}`}>
      <div className="spinner" />
      <div className="loading-text">{text}</div>
    </div>
  )
}
