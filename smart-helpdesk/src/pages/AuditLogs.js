import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { fetchAuditLogs } from '../services/api';
import './AuditLogs.css';

function AuditLogs() {
  const user = JSON.parse(localStorage.getItem('user'));
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    fetchAuditLogs()
      .then(setLogs)
      .catch(err => alert(err.message || 'Failed to load audit logs'));
  }, []);

  const getActionBadge = (action) => {
    if (action === 'SLA_BREACH') return 'badge-high';
    if (action === 'TICKET_ASSIGNMENT') return 'badge-medium';
    if (action === 'STATUS_CHANGE') return 'badge-open';
    if (action === 'PRIORITY_CHANGE') return 'badge-progress';
    return 'badge-low';
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      <div className="dashboard-main">
        <Navbar title="Audit Logs" userName={user.name} />
        <div className="dashboard-content">
          <div className="issues-table-container">
            <table className="issues-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Action</th>
                  <th>Entity</th>
                  <th>Details</th>
                  <th>Performed By</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {logs.length === 0 ? (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '20px' }}>No audit logs found</td></tr>
                ) : (
                  logs.map(log => (
                    <tr key={log.id}>
                      <td>#{log.id}</td>
                      <td><span className={`badge ${getActionBadge(log.action)}`}>{log.action.replace('_', ' ')}</span></td>
                      <td>{log.entityType} #{log.entityId}</td>
                      <td>{log.details}</td>
                      <td>{log.performedBy}</td>
                      <td>{new Date(log.timestamp).toLocaleString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AuditLogs;
