import { useEffect, useState, type FormEvent } from 'react'
import { createRisk, getRisks, type Risk } from './api'
import './App.css'

function App() {
  const [risks, setRisks] = useState<Risk[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)

  useEffect(() => {
    getRisks().then(setRisks).catch(() => setError('Unable to load risks. Please try again.')).finally(() => setLoading(false))
  }, [])

  async function saveRisk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    try {
      const risk = await createRisk({
        title: String(values.get('title')),
        description: String(values.get('description')),
        category: String(values.get('category')),
        owner: String(values.get('owner')),
        likelihood: Number(values.get('likelihood')),
        impact: Number(values.get('impact')),
      })
      setRisks((currentRisks) => [risk, ...currentRisks])
      setShowForm(false)
    } catch {
      setError('Unable to create risk. Please try again.')
    }
  }

  return (
    <main className="app-shell">
      <header className="page-header">
        <div><p className="eyebrow">Governance workspace</p><h1>Risk Register</h1><p className="subtitle">Track, assess, and mitigate the risks that matter.</p></div>
        <button type="button" className="primary-button" onClick={() => setShowForm(true)}>Add risk</button>
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
      {showForm && <div className="modal-backdrop" role="presentation">
        <section className="risk-form" role="dialog" aria-modal="true" aria-labelledby="add-risk-heading">
          <div className="form-header"><h2 id="add-risk-heading">Add a risk</h2><button type="button" className="close-button" aria-label="Close form" onClick={() => setShowForm(false)}>×</button></div>
          <form onSubmit={saveRisk}>
            <label>Title<input name="title" required /></label>
            <label>Description<textarea name="description" required /></label>
            <label>Category<select name="category" defaultValue="OPERATIONAL"><option value="OPERATIONAL">Operational</option><option value="FINANCIAL">Financial</option><option value="COMPLIANCE">Compliance</option><option value="SECURITY">Security</option><option value="STRATEGIC">Strategic</option></select></label>
            <label>Owner<input name="owner" required /></label>
            <div className="form-grid"><label>Likelihood<select name="likelihood" defaultValue="3">{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label><label>Impact<select name="impact" defaultValue="3">{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label></div>
            <div className="form-actions"><button type="button" className="secondary-button" onClick={() => setShowForm(false)}>Cancel</button><button type="submit" className="primary-button">Save risk</button></div>
          </form>
        </section>
      </div>}
    </main>
  )
}

export default App
