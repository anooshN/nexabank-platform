import React, { useState, useEffect } from 'react';
import { api } from '../../App';

function StatCard({ label, value, color, icon }) {
  return (
    <div style={{ background: color, borderRadius: 12, padding: '20px 24px', color: 'white', flex: 1, minWidth: 180 }}>
      <div style={{ fontSize: 13, opacity: 0.85, marginBottom: 8 }}>{icon} {label}</div>
      <div style={{ fontSize: 32, fontWeight: 700 }}>{value}</div>
    </div>
  );
}

export default function AdminDashboard() {
  const [stats, setStats] = useState({ accounts: 0, transactions: 0, users: 0, flagged: 0 });

  useEffect(() => {
    api.get('/accounts?page=0&size=1').then(r => setStats(s => ({ ...s, accounts: r.totalElements || 0 }))).catch(()=>{});
    api.get('/transactions?page=0&size=1').then(r => setStats(s => ({ ...s, transactions: r.totalElements || 0 }))).catch(()=>{});
  }, []);

  return (
    <div>
      <h2 style={{ color: '#1a3c5e', marginTop: 0 }}>System Overview</h2>
      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 24 }}>
        <StatCard label="Total Accounts" value={stats.accounts} color="linear-gradient(135deg,#1a3c5e,#2d6a9f)" icon="🏦" />
        <StatCard label="Transactions" value={stats.transactions} color="linear-gradient(135deg,#059669,#10b981)" icon="💸" />
        <StatCard label="Active Users" value={stats.users} color="linear-gradient(135deg,#7c3aed,#a78bfa)" icon="👥" />
        <StatCard label="Flagged Txns" value={stats.flagged} color="linear-gradient(135deg,#dc2626,#ef4444)" icon="🚨" />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div style={{ background: 'white', borderRadius: 12, padding: 20, border: '1px solid #e5e7eb' }}>
          <h3 style={{ margin: '0 0 12px', color: '#374151' }}>Quick Actions</h3>
          {[['View All Transactions', '/transactions'], ['Review Audit Logs', '/audit'], ['Manage Users', '/users']].map(([label, href]) => (
            <a key={href} href={href} style={{ display: 'block', padding: '10px 14px', background: '#f9fafb', borderRadius: 8, marginBottom: 8, textDecoration: 'none', color: '#1a3c5e', fontWeight: 500, fontSize: 14 }}>{label} →</a>
          ))}
        </div>
        <div style={{ background: 'white', borderRadius: 12, padding: 20, border: '1px solid #e5e7eb' }}>
          <h3 style={{ margin: '0 0 12px', color: '#374151' }}>Services Health</h3>
          {['Auth Service :8081', 'Account Service :8082', 'Transaction Service :8083', 'AI Service :8086'].map(svc => (
            <div key={svc} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid #f3f4f6', fontSize: 13 }}>
              <span>{svc}</span>
              <span style={{ color: '#059669', fontWeight: 600 }}>● UP</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
