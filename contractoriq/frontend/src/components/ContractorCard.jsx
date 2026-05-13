import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api.js'
import ScoreBreakdown from './ScoreBreakdown.jsx'
import PhotoUpload from './PhotoUpload.jsx'

function fmt(v) {
  if (v === null || v === undefined) return '—'
  const n = typeof v === 'string' ? parseFloat(v) : v
  return Number.isFinite(n) ? n.toFixed(1) : '—'
}

export default function ContractorCard() {
  const { id } = useParams()
  const [score, setScore] = useState(null)
  const [projects, setProjects] = useState([])
  const [error, setError] = useState(null)
  const [recalcBusy, setRecalcBusy] = useState(false)

  async function load() {
    setError(null)
    try {
      const [scoreData, projectData] = await Promise.all([
        api.getContractor(id),
        api.projectsByContractor(id),
      ])
      setScore(scoreData)
      setProjects(projectData || [])
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => { load() }, [id])

  async function recalc() {
    setRecalcBusy(true)
    try {
      const refreshed = await api.recalcOne(id)
      setScore(refreshed)
    } catch (e) {
      setError(e.message)
    } finally {
      setRecalcBusy(false)
    }
  }

  if (error) return <div className="page"><div className="error-msg">{error}</div></div>
  if (!score) return <div className="page"><p className="muted">loading…</p></div>

  const rec = score.recommendation || 'CAUTION'

  return (
    <div className="page">
      <Link to="/" className="back-link">← back to roster</Link>
      <div className="detail-head">
        <div>
          <div className="name">{score.name}</div>
          <div className="meta-row">
            <span>{score.city}</span>
            <span>{score.trade}</span>
            <span>{score.projectCount} projects</span>
          </div>
        </div>
        <button className="ghost" onClick={recalc} disabled={recalcBusy}>
          {recalcBusy ? 'Recalculating…' : '⟳ Recalculate score'}
        </button>
      </div>

      <div className="detail-grid">
        <div>
          <div className="panel">
            <div className="score-hero">
              <div className="label">Overall score</div>
              <div>
                <span className="score">{fmt(score.overallScore)}</span>
                <span className="denom">/100</span>
              </div>
              <div style={{ marginTop: 10 }}>
                <span className={`chip ${rec}`}>{rec}</span>
              </div>
            </div>
          </div>
          <div className="panel">
            <div className="panel-title">Score breakdown</div>
            <ScoreBreakdown score={score} />
            {score.qualityUnverified &&
              <p className="muted" style={{ marginTop: 12 }}>
                Quality unverified — no site photos analyzed.
              </p>}
          </div>
        </div>

        <div>
          <div className="panel">
            <div className="panel-title">Operations manager · Claude's read</div>
            <p className="reasoning">
              {score.recommendationReasoning ||
                <span className="muted" style={{ fontStyle: 'normal' }}>
                  No reasoning generated yet — run recalculate.
                </span>}
            </p>
          </div>

          <div className="panel">
            <div className="panel-title">Site photo intake · Claude Vision</div>
            <PhotoUpload contractorId={id} onAnalyzed={() => setTimeout(recalc, 500)} />
          </div>

          <div className="panel">
            <div className="panel-title">Recent projects ({projects.length})</div>
            {projects.length === 0
              ? <p className="muted">no projects logged.</p>
              : <ul className="project-list">
                  {projects.slice(0, 8).map(p => (
                    <li key={p.id}>
                      <div>
                        <div className="pname">{p.projectName}</div>
                        <div className="pmeta">{p.city.toUpperCase()} · {p.completionStatus}</div>
                      </div>
                      <div className="stars">
                        {p.customerRating ? '★'.repeat(p.customerRating) + '☆'.repeat(5 - p.customerRating) : ''}
                      </div>
                    </li>
                  ))}
                </ul>}
          </div>
        </div>
      </div>
    </div>
  )
}
