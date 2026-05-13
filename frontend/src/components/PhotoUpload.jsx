import React, { useRef, useState } from 'react'
import { api } from '../api.js'

export default function PhotoUpload({ contractorId, onAnalyzed }) {
  const fileInput = useRef(null)
  const [busy, setBusy] = useState(false)
  const [status, setStatus] = useState(null)
  const [error, setError] = useState(null)

  async function handleUpload(e) {
    const file = e.target.files?.[0]
    if (!file) return
    setBusy(true)
    setStatus(null)
    setError(null)
    try {
      const result = await api.uploadPhoto(contractorId, null, file)
      setStatus(`✓ analyzed · quality ${result.qualityScore}/100`)
      onAnalyzed && onAnalyzed(result)
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  return (
    <div className="upload-zone">
      <p>
        Drop a site photo · Claude Vision rates cleanliness, safety, work
        quality. Score updates async via Kafka.
      </p>
      <input
        ref={fileInput}
        type="file"
        accept="image/*"
        onChange={handleUpload}
        disabled={busy}
        style={{ width: 'auto' }}
      />
      {busy && <p className="muted" style={{ marginTop: 12 }}>→ analyzing photo with claude-opus-4-6…</p>}
      {status && <p className="success-msg" style={{ marginTop: 12 }}>{status}</p>}
      {error && <div className="error-msg" style={{ marginTop: 12 }}>{error}</div>}
    </div>
  )
}
