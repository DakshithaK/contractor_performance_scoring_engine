import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api.js'

export default function ProjectForm() {
  const navigate = useNavigate()
  const [contractors, setContractors] = useState([])
  const [form, setForm] = useState({
    contractorId: '',
    projectName: '',
    city: '',
    startDate: '',
    endDate: '',
    plannedEndDate: '',
    budgetPlanned: '',
    budgetActual: '',
    customerRating: '5',
    completionStatus: 'COMPLETED',
  })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.listContractors()
      .then(rows => setContractors(rows))
      .catch(e => setError(e.message))
  }, [])

  function update(k, v) {
    setForm(prev => ({ ...prev, [k]: v }))
  }

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const payload = {
        ...form,
        budgetPlanned: parseFloat(form.budgetPlanned),
        budgetActual: form.budgetActual ? parseFloat(form.budgetActual) : null,
        customerRating: form.customerRating ? parseInt(form.customerRating, 10) : null,
        endDate: form.endDate || null,
      }
      await api.ingestProject(payload)
      navigate(`/contractors/${form.contractorId}`)
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page">
      <div className="eyebrow">Ingestion</div>
      <h1 className="page-title">Log a <em>project</em></h1>
      <p className="muted" style={{ marginTop: 8, marginBottom: 22 }}>
        save → kafka event → score recalculation consumer fires.
      </p>

      <form onSubmit={submit} className="panel" style={{ maxWidth: 820 }}>
        <div className="form-grid">
          <div>
            <label>Contractor</label>
            <select required value={form.contractorId} onChange={e => update('contractorId', e.target.value)}>
              <option value="">— select —</option>
              {contractors.map(c => (
                <option key={c.contractorId} value={c.contractorId}>
                  {c.name} · {c.city} ({c.trade})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label>Project name</label>
            <input required value={form.projectName} onChange={e => update('projectName', e.target.value)} />
          </div>
          <div>
            <label>City</label>
            <input required value={form.city} onChange={e => update('city', e.target.value)} />
          </div>
          <div>
            <label>Completion status</label>
            <select value={form.completionStatus} onChange={e => update('completionStatus', e.target.value)}>
              <option value="COMPLETED">COMPLETED</option>
              <option value="ABANDONED">ABANDONED</option>
              <option value="ONGOING">ONGOING</option>
            </select>
          </div>
          <div>
            <label>Start date</label>
            <input type="date" required value={form.startDate} onChange={e => update('startDate', e.target.value)} />
          </div>
          <div>
            <label>Planned end date</label>
            <input type="date" required value={form.plannedEndDate} onChange={e => update('plannedEndDate', e.target.value)} />
          </div>
          <div>
            <label>Actual end date</label>
            <input type="date" value={form.endDate} onChange={e => update('endDate', e.target.value)} />
          </div>
          <div>
            <label>Customer rating (1-5)</label>
            <input type="number" min="1" max="5" value={form.customerRating} onChange={e => update('customerRating', e.target.value)} />
          </div>
          <div>
            <label>Budget planned (₹)</label>
            <input type="number" required value={form.budgetPlanned} onChange={e => update('budgetPlanned', e.target.value)} />
          </div>
          <div>
            <label>Budget actual (₹)</label>
            <input type="number" value={form.budgetActual} onChange={e => update('budgetActual', e.target.value)} />
          </div>
        </div>
        {error && <div className="error-msg" style={{ marginTop: 16 }}>{error}</div>}
        <div style={{ marginTop: 22, display: 'flex', gap: 10 }}>
          <button className="amber" type="submit" disabled={busy}>{busy ? 'Saving…' : 'Save & publish event'}</button>
          <button type="button" className="ghost" onClick={() => navigate('/')}>Cancel</button>
        </div>
      </form>
    </div>
  )
}
