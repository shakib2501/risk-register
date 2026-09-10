import './App.css'

function App() {
  return (
    <main className="app-shell">
      <header className="page-header">
        <div>
          <p className="eyebrow">Governance workspace</p>
          <h1>Risk Register</h1>
          <p className="subtitle">Track, assess, and mitigate the risks that matter.</p>
        </div>
        <button type="button" className="primary-button">Add risk</button>
      </header>

      <section className="risk-panel" aria-labelledby="risk-list-heading">
        <div className="panel-heading">
          <div>
            <h2 id="risk-list-heading">Risks</h2>
            <p>Residual risk is shown after all mitigations are applied.</p>
          </div>
        </div>
        <div className="empty-state">
          <h3>No risks found</h3>
          <p>No risks found. Add your first risk to begin tracking.</p>
        </div>
      </section>
    </main>
  )
}

export default App
