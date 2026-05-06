import React, { useState, useEffect } from 'react';
import { api } from '../../App';

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    api.get(`/audit?page=${page}&size=20`).then(r => {
      setLogs(r.content || []); setTotal(r.totalElements || 0);
    }).catch(()=>{});
  }, [page]);

  const downloadCsv = () => {
    const from = new Date(Date.now() - 30*24*60*60*1000).toISOString();
    const to = new Date().toISOString();
    window.open(`${api.base}/audit/export?from=${from}&to=${to}`, '_blank');
  };

  const statusColor = (type) => ({ TRANSACTION_COMPLETED: '#059669', TRANSACTION_FLAGGED: '#dc2626', ACCOUNT_CREATED: '#2d6a9f' }[type] || '#6b7280');

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2 style={{ margin: 0, color: '#1a3c5e' }}>Audit Logs ({total})</h2>
        <button onClick={downloadCsv} style={{ padding: '8px 16px', background: '#1a3c5e', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>Export CSV</button>
      </div>
      <div style={{ background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead style={{ background: '#f9fafb' }}>
            <tr>{['Timestamp','Event Type','Transaction ID','User','Amount','Status'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', color: '#6b7280', fontWeight: 600, borderBottom: '1px solid #e5e7eb' }}>{h}</th>)}</tr>
          </thead>
          <tbody>
            {logs.map((log, i) => (
              <tr key={log.id} style={{ background: i%2===0 ? 'white' : '#fafafa' }}>
                <td style={{ padding: '10px 16px' }}>{new Date(log.timestamp).toLocaleString()}</td>
                <td style={{ padding: '10px 16px' }}><span style={{ background: statusColor(log.eventType)+'20', color: statusColor(log.eventType), padding: '2px 8px', borderRadius: 10, fontSize: 11, fontWeight: 600 }}>{log.eventType}</span></td>
                <td style={{ padding: '10px 16px', fontFamily: 'monospace', fontSize: 11 }}>{log.transactionId?.slice(0,12)}...</td>
                <td style={{ padding: '10px 16px' }}>{log.username}</td>
                <td style={{ padding: '10px 16px', fontWeight: 600 }}>{log.amount ? `${log.currency} ${log.amount}` : '-'}</td>
                <td style={{ padding: '10px 16px' }}>{log.status || '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div style={{ padding: '12px 16px', display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #e5e7eb' }}>
          <button onClick={() => setPage(Math.max(0, page-1))} disabled={page===0} style={{ padding: '6px 16px', border: '1px solid #d1d5db', borderRadius: 6, cursor: 'pointer' }}>← Prev</button>
          <span style={{ fontSize: 13, color: '#6b7280' }}>Page {page+1}</span>
          <button onClick={() => setPage(page+1)} disabled={(page+1)*20 >= total} style={{ padding: '6px 16px', border: '1px solid #d1d5db', borderRadius: 6, cursor: 'pointer' }}>Next →</button>
        </div>
      </div>
    </div>
  );
}
