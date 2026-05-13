import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api.js'

function num(v, fallback = '—') {
  if (v === null || v === undefined) return fallback
  const n = typeof v === 'string' ? parseFloat(v) : v
  return Number.isFinite(n) ? n.toFixed(1) : fallback
}

function clamp(v) {
  const n = typeof v === 'string' ? parseFloat(v) : v
  if (!Number.isFinite(n)) return 0
  return Math.max(0, Math.min(100, n))
}

export default function Dashboard() {
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [recalcBusy, setRecalcBusy] = useState(false)
  const [lastSync, setLastSync] = useState(null)
  const navigate = useNavigate()

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = await api.listContractors()
      data.sort((a, b) => parseFloat(b.overallScore || 0) - parseFloat(a.overallScore || 0))
      setRows(data)
      setLastSync(new Date())
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function recalcAll() {
    setRecalcBusy(true)
    try {
      await api.recalcAll()
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setRecalcBusy(false)
    }
  }

  const total = rows.length
  const flagged = rows.filter(r => r.recommendation === 'AVOID').length
  const caution = rows.filter(r => r.recommendation === 'CAUTION').length
  const hire = rows.filter(r => r.recommendation === 'HIRE').length
  const avg = total
    ? (rows.reduce((s, r) => s + parseFloat(r.overallScore || 0), 0) / total).toFixed(1)
    : '—'
  const top = rows[0]
  const ts = lastSync ? lastSync.toTimeString().slice(0, 8) : '—'

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <div className="eyebrow">Roster · scoring engine v0.1</div>
          <h1 className="page-title">Contractor <em>performance</em> board</h1>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span className="muted">last sync {ts}</span>
          <button className="amber" onClick={recalcAll} disabled={recalcBusy}>
            {recalcBusy ? 'Recalculating…' : '⟳ Recalculate'}
          </button>
        </div>
      </div>

      <div className="kpi-strip">
        <div className="kpi">
          <div className="kpi-label">Active contractors</div>
          <div className="kpi-value">{total}</div>
          <div className="kpi-foot">{hire} hire · {caution} caution · {flagged} avoid</div>
        </div>
        <div className="kpi">
          <div className="kpi-label">Avg overall score</div>
          <div className="kpi-value">{avg}</div>
          <div className="kpi-foot">weighted: delay 25 / budget 25 / quality 30 / customer 20</div>
        </div>
        <div className={`kpi ${flagged > 0 ? 'bad' : ''}`}>
          <div className="kpi-label">Flagged · avoid</div>
          <div className="kpi-value">{flagged}</div>
          <div className="kpi-foot">{flagged === 0 ? 'no critical risks' : 'review before re-engaging'}</div>
        </div>
        <div className="kpi good">
          <div className="kpi-label">Top performer</div>
          <div className="kpi-value text">{top ? top.name : '—'}</div>
          <div className="kpi-foot">
            {top ? `${top.city} · ${top.trade} · ${num(top.overallScore)}/100` : ''}
          </div>
        </div>
      </div>

      {error && <div className="error-msg" style={{ marginBottom: 14 }}>{error}</div>}

      <div className="table-wrap">
        <div className="table-head-bar">
          <h3>Roster · ranked by overall</h3>
          <span className="muted">{rows.length} rows</span>
        </div>
        {loading ? <p style={{ padding: 24 }} className="muted">loading roster…</p> : (
          <table>
            <thead>
              <tr>
                <th>Contractor</th>
                <th>Trade</th>
                <th className="num">Projects</th>
                <th className="num">Delay</th>
                <th className="num">Budget</th>
                <th className="num">Quality</th>
                <th className="num">Customer</th>
                <th className="num">Overall</th>
                <th>Recommendation</th>
              </tr>
            </thead>
            <tbody>
              {rows.map(r => {
                const rec = r.recommendation || 'CAUTION'
                return (
                  <tr
                    key={r.contractorId}
                    className={`rec-${rec}`}
                    onClick={() => navigate(`/contractors/${r.contractorId}`)}
                  >
                    <td>
                      <div className="contractor-cell">
                        <span className="name">{r.name}</span>
                        <span className="meta">{r.city.toUpperCase()}</span>
                      </div>
                    </td>
                    <td><span className="muted">{r.trade}</span></td>
                    <td className="num"><span className="mono">{r.projectCount}</span></td>
                    <td className="num"><span className="mono">{num(r.delayScore)}</span></td>
                    <td className="num"><span className="mono">{num(r.budgetScore)}</span></td>
                    <td className="num"><span className="mono">{num(r.qualityScore)}</span></td>
                    <td className="num"><span className="mono">{num(r.customerScore)}</span></td>
                    <td className="num">
                      <div className="overall-cell">
                        <div className="meter">
                          <div className={`meter-fill ${rec}`} style={{ width: `${clamp(r.overallScore)}%` }} />
                        </div>
                        <span className="num">{num(r.overallScore)}</span>
                      </div>
                    </td>
                    <td><span className={`chip ${rec}`}>{rec}</span></td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
