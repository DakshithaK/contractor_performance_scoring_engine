const BASE = '/api/v1'

async function jsonOrThrow(res) {
  if (!res.ok) {
    const body = await res.text()
    throw new Error(`${res.status}: ${body}`)
  }
  if (res.status === 204) return null
  return res.json()
}

export const api = {
  listContractors: () => fetch(`${BASE}/contractors`).then(jsonOrThrow),
  getContractor: (id) => fetch(`${BASE}/contractors/${id}`).then(jsonOrThrow),
  createContractor: (body) =>
    fetch(`${BASE}/contractors`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).then(jsonOrThrow),
  ingestProject: (body) =>
    fetch(`${BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).then(jsonOrThrow),
  projectsByContractor: (id) =>
    fetch(`${BASE}/projects/contractor/${id}`).then(jsonOrThrow),
  uploadPhoto: (contractorId, projectId, file) => {
    const form = new FormData()
    form.append('file', file)
    const qp = projectId
      ? `?contractorId=${contractorId}&projectId=${projectId}`
      : `?contractorId=${contractorId}`
    return fetch(`${BASE}/photos/analyze${qp}`, { method: 'POST', body: form }).then(jsonOrThrow)
  },
  recalcOne: (id) =>
    fetch(`${BASE}/scores/calculate/${id}`, { method: 'POST' }).then(jsonOrThrow),
  recalcAll: () =>
    fetch(`${BASE}/scores/calculate-all`, { method: 'POST' }).then(jsonOrThrow),
  leaderboard: () => fetch(`${BASE}/leaderboard`).then(jsonOrThrow),
  flagged: () => fetch(`${BASE}/flagged`).then(jsonOrThrow),
}
