import React from 'react';
export default function UserManagement() {
  return (
    <div>
      <h2 style={{ color:'#1a3c5e',marginTop:0 }}>User Management</h2>
      <div style={{ background:'white',borderRadius:12,padding:32,border:'1px solid #e5e7eb',textAlign:'center',color:'#6b7280' }}>
        <div style={{ fontSize:48,marginBottom:12 }}>👥</div>
        <p>User management endpoints are provided by the auth-service REST API.</p>
        <p>Use the Swagger UI at <strong>http://localhost:8081/swagger-ui.html</strong> to manage users.</p>
      </div>
    </div>
  );
}
