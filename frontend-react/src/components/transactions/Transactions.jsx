import React, { useState, useEffect } from 'react';
import { api } from '../../App';

export default function Transactions() {
  const [txns, setTxns] = useState([]); const [page, setPage] = useState(0); const [total, setTotal] = useState(0);
  useEffect(() => { api.get(`/transactions?page=${page}&size=20`).then(r => { setTxns(r.content||[]); setTotal(r.totalElements||0); }).catch(()=>{}); }, [page]);
  const statusStyle = (s) => ({ COMPLETED:'#059669',FLAGGED:'#dc2626',PENDING:'#d97706',FAILED:'#6b7280' }[s]||'#6b7280');
  return (
    <div>
      <h2 style={{ color:'#1a3c5e',marginTop:0 }}>All Transactions ({total})</h2>
      <div style={{ background:'white',borderRadius:12,border:'1px solid #e5e7eb',overflow:'hidden' }}>
        <table style={{ width:'100%',borderCollapse:'collapse',fontSize:13 }}>
          <thead style={{ background:'#f9fafb' }}>
            <tr>{['Date','Ref #','From','To','Amount','Type','Status','AI Score'].map(h=><th key={h} style={{ padding:'12px 16px',textAlign:'left',color:'#6b7280',fontWeight:600,borderBottom:'1px solid #e5e7eb' }}>{h}</th>)}</tr>
          </thead>
          <tbody>
            {txns.map((t,i) => (
              <tr key={t.id} style={{ background:i%2===0?'white':'#fafafa' }}>
                <td style={{ padding:'10px 16px' }}>{new Date(t.createdAt).toLocaleString()}</td>
                <td style={{ padding:'10px 16px',fontFamily:'monospace',fontSize:11 }}>{t.referenceNumber}</td>
                <td style={{ padding:'10px 16px' }}>{t.fromAccountNumber}</td>
                <td style={{ padding:'10px 16px' }}>{t.toAccountNumber}</td>
                <td style={{ padding:'10px 16px',fontWeight:700 }}>{t.currency} {parseFloat(t.amount).toFixed(2)}</td>
                <td style={{ padding:'10px 16px' }}>{t.type}</td>
                <td style={{ padding:'10px 16px' }}><span style={{ color:statusStyle(t.status),fontWeight:600 }}>{t.status}</span></td>
                <td style={{ padding:'10px 16px' }}>{t.fraudScore ? `${(t.fraudScore*100).toFixed(0)}%` : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div style={{ padding:'12px 16px',display:'flex',justifyContent:'space-between',borderTop:'1px solid #e5e7eb' }}>
          <button onClick={()=>setPage(Math.max(0,page-1))} disabled={page===0} style={{ padding:'6px 16px',border:'1px solid #d1d5db',borderRadius:6,cursor:'pointer' }}>← Prev</button>
          <span style={{ fontSize:13,color:'#6b7280' }}>Page {page+1}</span>
          <button onClick={()=>setPage(page+1)} disabled={(page+1)*20>=total} style={{ padding:'6px 16px',border:'1px solid #d1d5db',borderRadius:6,cursor:'pointer' }}>Next →</button>
        </div>
      </div>
    </div>
  );
}
