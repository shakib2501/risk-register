import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { createMitigation, createRisk, deleteMitigation, deleteRisk, getMitigations, getRisks, updateMitigation, updateRisk, type CreateRiskInput, type Mitigation, type Risk, type RiskCategory, type RiskStatus } from './api'
import './App.css'

const categories: RiskCategory[] = ['OPERATIONAL', 'FINANCIAL', 'COMPLIANCE', 'SECURITY', 'STRATEGIC']
const statuses: RiskStatus[] = ['OPEN', 'MITIGATING', 'CLOSED']

function App() {
  const [risks, setRisks] = useState<Risk[]>([])
  const [loading, setLoading] = useState(true)
  const [dashboardError, setDashboardError] = useState('')
  const [toastMessage, setToastMessage] = useState('')
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState('')
  const [formRisk, setFormRisk] = useState<Risk | null | undefined>(undefined)
  const [formLikelihood, setFormLikelihood] = useState(3)
  const [formImpact, setFormImpact] = useState(3)
  const [formStatus, setFormStatus] = useState<RiskStatus>('OPEN')
  const [initialMitigationDescription, setInitialMitigationDescription] = useState('')
  const [initialMitigationEffectiveness, setInitialMitigationEffectiveness] = useState(3)
  const [selectedRisk, setSelectedRisk] = useState<Risk | null>(null)
  const [mitigations, setMitigations] = useState<Mitigation[]>([])
  const [editingMitigation, setEditingMitigation] = useState<Mitigation | null>(null)

  const filteredRisks = useMemo(() => risks.filter((risk) => (!category || risk.category === category) && (!status || risk.status === status)), [risks, category, status])

  async function loadRisks() {
    try { setRisks(await getRisks()) } catch (reason) { setDashboardError(messageOf(reason)) } finally { setLoading(false) }
  }

  useEffect(() => { void loadRisks() }, [])

  function openNewRisk() {
    setFormRisk(null)
    setFormLikelihood(3)
    setFormImpact(3)
    setFormStatus('OPEN')
    setInitialMitigationDescription('')
    setInitialMitigationEffectiveness(3)
  }

  function openEditRisk(risk: Risk) {
    setFormRisk(risk)
    setFormLikelihood(risk.likelihood)
    setFormImpact(risk.impact)
    setFormStatus(risk.status)
  }

  function openMitigationDetails(risk: Risk) {
    setFormRisk(undefined)
    void selectRisk(risk)
  }

  async function selectRisk(risk: Risk) {
    setSelectedRisk(risk)
    try { setMitigations(await getMitigations(risk.id)) } catch (reason) { setToastMessage(messageOf(reason)) }
  }

  async function saveRisk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    const mitigationDescription = initialMitigationDescription.trim()
    const input: CreateRiskInput = {
      title: String(values.get('title')), description: String(values.get('description')),
      category: String(values.get('category')) as RiskCategory, owner: String(values.get('owner')),
      likelihood: Number(values.get('likelihood')), impact: Number(values.get('impact')),
      status: String(values.get('status')) as RiskStatus,
      ...(!formRisk && mitigationDescription ? { initialMitigation: { description: mitigationDescription, effectiveness: initialMitigationEffectiveness } } : {}),
    }
    try {
      const saved = formRisk ? await updateRisk(formRisk.id, input) : await createRisk(input)
      setRisks((current) => formRisk ? current.map((risk) => risk.id === saved.id ? saved : risk) : [saved, ...current])
      if (selectedRisk?.id === saved.id) setSelectedRisk(saved)
      setFormRisk(undefined)
      setToastMessage('')
    } catch (reason) { setToastMessage(messageOf(reason)) }
  }

  async function removeRisk(risk: Risk) {
    if (!window.confirm(`Delete “${risk.title}”?`)) return
    try {
      await deleteRisk(risk.id)
      setRisks((current) => current.filter((item) => item.id !== risk.id))
      if (selectedRisk?.id === risk.id) { setSelectedRisk(null); setMitigations([]) }
    } catch (reason) { setToastMessage(messageOf(reason)) }
  }

  async function saveMitigation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedRisk) return
    const values = new FormData(event.currentTarget)
    try {
      const input = { description: String(values.get('mitigationDescription')), effectiveness: Number(values.get('effectiveness')) }
      const savedRisk = editingMitigation
        ? await updateMitigation(selectedRisk.id, editingMitigation.id, input).then(() => getRisks()).then((items) => items.find((risk) => risk.id === selectedRisk.id)!)
        : await createMitigation(selectedRisk.id, input)
      setRisks((current) => current.map((risk) => risk.id === savedRisk.id ? savedRisk : risk))
      setSelectedRisk(savedRisk)
      setMitigations(await getMitigations(savedRisk.id))
      event.currentTarget.reset()
      setEditingMitigation(null)
    } catch (reason) { setToastMessage(messageOf(reason)) }
  }

  async function removeMitigation(mitigationId: number) {
    if (!selectedRisk || !window.confirm('Delete this mitigation?')) return
    try {
      await deleteMitigation(selectedRisk.id, mitigationId)
      const freshRisks = await getRisks()
      const refreshedRisk = freshRisks.find((risk) => risk.id === selectedRisk.id)
      setRisks(freshRisks)
      if (refreshedRisk) setSelectedRisk(refreshedRisk)
      setMitigations(await getMitigations(selectedRisk.id))
    } catch (reason) { setToastMessage(messageOf(reason)) }
  }

  const hasFormMitigation = formRisk ? formRisk.mitigationCount > 0 : initialMitigationDescription.trim().length > 0
  const canCloseFormRisk = hasFormMitigation

  return <main className="app-shell">
    <header className="page-header"><div><p className="eyebrow">Governance workspace</p><h1>Risk Register</h1><p className="subtitle">Track, assess, and mitigate the risks that matter.</p></div><button type="button" className="primary-button" onClick={openNewRisk}>Add risk</button></header>
    {toastMessage && <div className="toast" role="alert"><span>{toastMessage}</span><button type="button" aria-label="Dismiss notification" onClick={() => setToastMessage('')}>×</button></div>}
    <section className="risk-panel" aria-labelledby="risk-list-heading">
      <div className="panel-heading"><div><h2 id="risk-list-heading">Risks</h2><p>Sorted by residual risk, highest first.</p></div><div className="filters"><label>Category<select aria-label="Filter by category" value={category} onChange={(event) => setCategory(event.target.value)}><option value="">All categories</option>{categories.map((item) => <option key={item} value={item}>{format(item)}</option>)}</select></label><label>Status<select aria-label="Filter by status" value={status} onChange={(event) => setStatus(event.target.value)}><option value="">All statuses</option>{statuses.map((item) => <option key={item} value={item}>{format(item)}</option>)}</select></label></div></div>
      {loading && <p className="state-message">Loading risks…</p>}
      {dashboardError && <p className="state-message" role="alert">{dashboardError}</p>}
      {!loading && !dashboardError && filteredRisks.length === 0 && <div className="empty-state"><h3>No risks found</h3><p>No risks found. Add your first risk to begin tracking.</p></div>}
      {!loading && !dashboardError && filteredRisks.length > 0 && <div className="risk-list">{filteredRisks.map((risk) => <article className="risk-row" key={risk.id}><button type="button" className="risk-summary" onClick={() => void selectRisk(risk)}><span><strong>{risk.title}</strong><small>{format(risk.category)} · {risk.owner} · {format(risk.status)}</small><small>{risk.mitigationCount} {risk.mitigationCount === 1 ? 'mitigation' : 'mitigations'}</small></span><span className="risk-score"><strong>Inherent: {risk.inherentScore}</strong><span className={`severity severity-${risk.inherentSeverity?.toLowerCase()}`}>{format(risk.inherentSeverity ?? risk.residualSeverity)}</span><strong>Residual: {risk.residualScore}</strong><span className={`severity severity-${risk.residualSeverity.toLowerCase()}`}>{format(risk.residualSeverity)}</span></span></button><div className="row-actions"><button type="button" onClick={() => openEditRisk(risk)}>Edit</button><button type="button" className="danger-button" onClick={() => void removeRisk(risk)}>Delete</button></div></article>)}</div>}
    </section>
    {selectedRisk && <section className="detail-panel" aria-labelledby="risk-detail-heading"><div className="detail-heading"><div><p className="eyebrow">Risk details</p><h2 id="risk-detail-heading">{selectedRisk.title}</h2><p>{selectedRisk.description}</p></div><button type="button" className="secondary-button" onClick={() => setSelectedRisk(null)}>Close details</button></div><div className="metrics"><div><span>Inherent</span><strong>{selectedRisk.inherentScore}</strong><small>{format(selectedRisk.inherentSeverity)}</small></div><div><span>Residual</span><strong>{selectedRisk.residualScore}</strong><small>{format(selectedRisk.residualSeverity)}</small></div><div><span>Mitigations</span><strong>{selectedRisk.mitigationCount}</strong></div></div><div className="mitigation-section"><h3>Mitigations</h3>{mitigations.length === 0 ? <p className="muted">No mitigations recorded yet.</p> : <ul className="mitigation-list">{mitigations.map((mitigation) => <li key={mitigation.id}><span><strong>{mitigation.description}</strong><small>Effectiveness: {mitigation.effectiveness}/5</small></span><span><button type="button" onClick={() => setEditingMitigation(mitigation)}>Edit</button><button type="button" className="danger-button" onClick={() => void removeMitigation(mitigation.id)}>Delete</button></span></li>)}</ul>}<form key={editingMitigation?.id ?? 'new'} className="mitigation-form" onSubmit={saveMitigation}><label>Description<input aria-label="Description" name="mitigationDescription" defaultValue={editingMitigation?.description} required /></label><label>Effectiveness<select aria-label="Effectiveness" name="effectiveness" defaultValue={editingMitigation?.effectiveness ?? 3} required>{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label><button type="submit" className="primary-button">{editingMitigation ? 'Update mitigation' : 'Add mitigation'}</button>{editingMitigation && <button type="button" className="secondary-button" onClick={() => setEditingMitigation(null)}>Cancel</button>}</form></div></section>}
    {formRisk !== undefined && <div className="modal-backdrop" role="presentation"><section className="risk-form" role="dialog" aria-modal="true" aria-labelledby="risk-form-heading"><div className="form-header"><h2 id="risk-form-heading">{formRisk ? 'Edit risk' : 'Add a risk'}</h2><button type="button" className="close-button" aria-label="Close form" onClick={() => setFormRisk(undefined)}>×</button></div><form key={formRisk?.id ?? 'new'} onSubmit={saveRisk}><label>Title<input aria-label="Title" name="title" defaultValue={formRisk?.title} required /></label><label>Description<textarea aria-label="Description" name="description" defaultValue={formRisk?.description} required /></label><label>Category<select aria-label="Category" name="category" defaultValue={formRisk?.category ?? 'OPERATIONAL'} required>{categories.map((item) => <option key={item} value={item}>{format(item)}</option>)}</select></label><label>Owner<input aria-label="Owner" name="owner" defaultValue={formRisk?.owner} required /></label><div className="form-grid"><label>Likelihood<select aria-label="Likelihood" name="likelihood" value={formLikelihood} onChange={(event) => setFormLikelihood(Number(event.target.value))} required>{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label><label>Impact<select aria-label="Impact" name="impact" value={formImpact} onChange={(event) => setFormImpact(Number(event.target.value))} required>{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label></div><p className="live-score">Live inherent score: {formLikelihood * formImpact}</p>{!formRisk && <fieldset className="initial-mitigation"><legend>Initial mitigation (optional)</legend><label>Initial mitigation description<input aria-label="Initial mitigation description" name="initialMitigationDescription" value={initialMitigationDescription} onChange={(event) => { const description = event.target.value; setInitialMitigationDescription(description); if (description.trim() && formStatus === 'OPEN') setFormStatus('MITIGATING'); if (!description.trim() && formStatus !== 'OPEN') setFormStatus('OPEN') }} /></label><label>Initial mitigation effectiveness<select aria-label="Initial mitigation effectiveness" name="initialMitigationEffectiveness" value={initialMitigationEffectiveness} onChange={(event) => setInitialMitigationEffectiveness(Number(event.target.value))}>{[1, 2, 3, 4, 5].map((value) => <option key={value}>{value}</option>)}</select></label></fieldset>}{formRisk?.mitigationCount === 0 && <div className="closure-guidance"><strong>Add a mitigation before closing this risk.</strong><button type="button" className="secondary-button" onClick={() => openMitigationDetails(formRisk)}>Add mitigation first</button></div>}<label>Status<select aria-label="Status" name="status" value={formStatus} onChange={(event) => setFormStatus(event.target.value as RiskStatus)}>{statuses.map((item) => <option key={item} value={item} disabled={(item === 'CLOSED' && !canCloseFormRisk) || (item === 'OPEN' && hasFormMitigation)}>{format(item)}</option>)}</select></label><div className="form-actions"><button type="button" className="secondary-button" onClick={() => setFormRisk(undefined)}>Cancel</button><button type="submit" className="primary-button">Save risk</button></div></form></section></div>}
  </main>
}

function format(value: string) { return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase()) }
function messageOf(reason: unknown) { return reason instanceof Error ? reason.message : 'Something went wrong. Please try again.' }

export default App
