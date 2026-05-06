import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom';
import AdminDashboard from './components/dashboard/AdminDashboard';
import UserManagement from './components/users/UserManagement';
import AuditLogs from './components/audit/AuditLogs';
import Transactions from './components/transactions/Transactions';
import Login from './components/Login';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const api = {
  base: API_BASE,
  getToken: () => localStorage.getItem('nexabank_admin_token'),
  headers: () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('nexabank_admin_token')}`
  }),
  get: (path) => fetch(`${API_BASE}${path}`, { headers: api.headers() }).then(r => r.json()),
  post: (path, body) => fetch(`${API_BASE}${path}`, { method: 'POST', headers: api.headers(), body: JSON.stringify(body) }).then(r => r.json()),
};

function Sidebar() {
  const location = useLocation();
  const links = [
    { to: '/dashboard', icon: '📊', label: 'Dashboard' },
    { to: '/users', icon: '👥', label: 'Users' },
    { to: '/transactions', icon: '💳', label: 'Transactions' },
    { to: '/audit', icon: '📋', label: 'Audit Logs' },
  ];
  return (
    <aside style={{ width: 220, background: '#1a3c5e', minHeight: '100vh', padding: '0', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px 16px', display: 'flex', alignItems: 'center', gap: 10, borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
        <div style={{ width: 36, height: 36, borderRadius: 8, background: '#f59e0b', color: '#1a3c5e', fontWeight: 800, fontSize: 18, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>N</div>
        <div>
          <div style={{ color: 'white', fontWeight: 700, fontSize: 16 }}>NexaBank</div>
          <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: 11 }}>Admin Panel</div>
        </div>
      </div>
      <nav style={{ padding: '12px 8px', flex: 1 }}>
        {links.map(({ to, icon, label }) => (
          <Link key={to} to={to} style={{
            display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px',
            borderRadius: 8, marginBottom: 4, textDecoration: 'none',
            color: location.pathname === to ? 'white' : 'rgba(255,255,255,0.65)',
            background: location.pathname === to ? 'rgba(255,255,255,0.12)' : 'transparent',
            fontSize: 14, fontWeight: location.pathname === to ? 600 : 400
          }}>
            <span>{icon}</span><span>{label}</span>
          </Link>
        ))}
      </nav>
      <div style={{ padding: '12px 16px', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
        <button onClick={() => { localStorage.removeItem('nexabank_admin_token'); window.location.href = '/login'; }}
          style={{ background: 'none', border: 'none', color: 'rgba(255,255,255,0.6)', cursor: 'pointer', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6 }}>
          🚪 Sign Out
        </button>
      </div>
    </aside>
  );
}

function Layout({ children }) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, background: '#f4f6f9', overflow: 'auto' }}>
        <div style={{ padding: 24 }}>{children}</div>
      </main>
    </div>
  );
}

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('nexabank_admin_token');
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<ProtectedRoute><Layout><AdminDashboard /></Layout></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute><Layout><UserManagement /></Layout></ProtectedRoute>} />
        <Route path="/transactions" element={<ProtectedRoute><Layout><Transactions /></Layout></ProtectedRoute>} />
        <Route path="/audit" element={<ProtectedRoute><Layout><AuditLogs /></Layout></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
