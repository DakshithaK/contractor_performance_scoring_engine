import React from 'react'

function val(v) {
  if (v === null || v === undefined) return 0
  const n = typeof v === 'string' ? parseFloat(v) : v
  return Number.isFinite(n) ? n : 0
}

const DIMS = [
  { key: 'delayScore',    label: 'Delay',    weight: 25 },
  { key: 'budgetScore',   label: 'Budget',   weight: 25 },
  { key: 'qualityScore',  label: 'Quality',  weight: 30 },
  { key: 'customerScore', label: 'Customer', weight: 20 },
]

export default function ScoreBreakdown({ score }) {
  return (
    <ul className="dimension-list">
      {DIMS.map(d => {
        const v = val(score?.[d.key])
        const band = v >= 75 ? 'HIRE' : v >= 50 ? 'CAUTION' : 'AVOID'
        return (
          <li key={d.key}>
            <span className="dim-label">{d.label} · {d.weight}%</span>
            <div className="meter">
              <div className={`meter-fill ${band}`} style={{ width: `${Math.min(100, Math.max(0, v))}%` }} />
            </div>
            <span className="dim-value">{v.toFixed(1)}</span>
          </li>
        )
      })}
    </ul>
  )
}
