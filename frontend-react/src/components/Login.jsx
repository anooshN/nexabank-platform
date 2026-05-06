import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault(); setLoading(true); setError('');
    try {
      const res = await fetch(`${API}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Login failed');
      if (!data.role?.includes('ADMIN')) throw new Error('Admin access required');
      localStorage.setItem('nexabank_admin_token', data.accessToken);
      navigate('/dashboard');
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg,#1a3c5e,#2d6a9f)' }}>
      <div style={{ background: 'white', borderRadius: 16, padding: 40, width: 400, boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ width: 56, height: 56, borderRadius: 14, background: '#f59e0b', color: '#1a3c5e', fontSize: 26, fontWeight: 800, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px' }}>N</div>
          <h1 style={{ margin: 0, color: '#1a3c5e' }}>Admin Portal</h1>
          <p style={{ color: '#6b7280', fontSize: 13, margin: '4px 0 0' }}>NexaBank Administration</p>
        </div>
        {error && <div style={{ background: '#fee2e2', color: '#991b1b', padding: '10px 14px', borderRadius: 8, marginBottom: 16, fontSize: 13 }}>{error}</div>}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <input value={form.username} onChange={e => setForm({...form, username: e.target.value})} placeholder="Username" required style={{ padding: '12px 14px', borderRadius: 8, border: '1px solid #d1d5db', fontSize: 14, outline: 'none' }} />
          <input value={form.password} onChange={e => setForm({...form, password: e.target.value})} type="password" placeholder="Password" required style={{ padding: '12px 14px', borderRadius: 8, border: '1px solid #d1d5db', fontSize: 14 }} />
          <button type="submit" disabled={loading} style={{ padding: '13px', background: '#1a3c5e', color: 'white', border: 'none', borderRadius: 8, fontSize: 15, fontWeight: 600, cursor: 'pointer' }}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <div style={{ marginTop: 20, padding: 12, background: '#f9fafb', borderRadius: 8, fontSize: 12, color: '#6b7280' }}>
          <strong>Demo admin:</strong> admin / Admin@1234
        </div>
      </div>
    </div>
  );
}
