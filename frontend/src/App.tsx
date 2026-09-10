import { useEffect, useState } from 'react'
import { getRisks, type Risk } from './api'
import './App.css'

function App() {
  const [risks, setRisks] = useState<Risk[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getRisks().then(setRisks).catch(() => setError('Unable to load risks. Please try again.')).finally(() => setLoading(false))
  }, [])

  return (
    <main className="app-shell">
      <header className="page-header">
        <div><p className="eyebrow">Governance workspace</p><h1>Risk Register</h1><p className="subtitle">Track, assess, and mitigate the risks that matter.</p></div>
        <button type="button" className="primary-button">Add risk</button>
      </header>
      <section className="risk-panel" aria-labelledby="risk-list-heading">
        <div className="panel-heading"><div><h2 id="risk-list-heading">Risks</h2><p>Residual risk is shown after all mitigations are applied.</p></div></div>
        {loading && <p className="state-message">Loading risks…</p>}
        {error && <p className="state-message" role="alert">{error}</p>}
        {!loading && !error && risks.length === 0 && <div className="empty-state"><h3>No risks found</h3><p>No risks found. Add your first risk to begin tracking.</p></div>}
        {!loading && !error && risks.length > 0 && <div className="risk-list">
          {risks.map((risk) => <article className="risk-row" key={risk.id}>
            <div><h3>{risk.title}</h3><p>{risk.category} · {risk.owner} · {risk.status}</p></div>
            <div className="risk-score"><strong>Residual: {risk.residualScore}</strong><span className={`severity severity-${risk.residualSeverity.toLowerCase()}`}>{risk.residualSeverity}</span></div>
          </article>)}
        </div>}
      </section>
    </main>
  )
}

export default App
