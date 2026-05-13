import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import Dashboard from './components/Dashboard.jsx'
import ContractorCard from './components/ContractorCard.jsx'
import ProjectForm from './components/ProjectForm.jsx'

export default function App() {
  return (
    <>
      <header className="app-header">
        <div className="brand">
          <div className="brand-mark">C</div>
          <div>
            <div className="brand-name">ContractorIQ<span className="brand-tag">// field ops</span></div>
          </div>
          <span className="live-pill"><span className="live-dot" />SCORING.LIVE</span>
        </div>
        <nav>
          <Link to="/">Roster</Link>
          <Link to="/projects/new">Log project</Link>
        </nav>
      </header>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/contractors/:id" element={<ContractorCard />} />
        <Route path="/projects/new" element={<ProjectForm />} />
      </Routes>
    </>
  )
}
