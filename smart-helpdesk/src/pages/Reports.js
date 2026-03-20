import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { fetchAnalytics, fetchTickets } from '../services/api';
import './Reports.css';

function Reports() {
  const user = JSON.parse(localStorage.getItem('user'));
  const [analytics, setAnalytics] = useState(null);
  const [tickets, setTickets] = useState([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [analyticsData, ticketsData] = await Promise.all([
        fetchAnalytics(),
        fetchTickets({})
      ]);
      setAnalytics(analyticsData);
      setTickets(ticketsData);
    } catch (error) {
      alert(error.message || 'Failed to load reports');
    }
  };

  const calculateSLACompliance = () => {
    const total = tickets.length;
    const resolved = tickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED').length;
    return total > 0 ? ((resolved / total) * 100).toFixed(1) : 0;
  };

  const calculateAvgResolutionTime = () => {
    const resolved = tickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED');
    if (resolved.length === 0) return 'N/A';
    
    const totalHours = resolved.reduce((sum, t) => {
      const created = new Date(t.createdAt);
      const updated = new Date(t.updatedAt);
      const hours = (updated - created) / (1000 * 60 * 60);
      return sum + hours;
    }, 0);
    
    return `${(totalHours / resolved.length).toFixed(1)} hrs`;
  };

  const exportToCSV = () => {
    const headers = ['ID', 'Customer', 'Issue Type', 'Priority', 'Status', 'Created At'];
    const rows = tickets.map(t => [
      t.id,
      t.customerName,
      t.issueType,
      t.priority,
      t.status,
      new Date(t.createdAt).toLocaleDateString()
    ]);
    
    const csv = [headers, ...rows].map(row => row.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `helpdesk-report-${Date.now()}.csv`;
    a.click();
  };

  if (!analytics) return <div>Loading...</div>;

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      
      <div className="dashboard-main">
        <Navbar title="Reports & Analytics" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="reports-header">
            <h3>System Performance Report</h3>
            <button onClick={exportToCSV} className="btn-primary">Export CSV</button>
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon blue">📊</div>
              <div className="stat-info">
                <h3>{analytics.totalTickets}</h3>
                <p>Total Tickets</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon green">✅</div>
              <div className="stat-info">
                <h3>{calculateSLACompliance()}%</h3>
                <p>SLA Compliance</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon purple">⏱️</div>
              <div className="stat-info">
                <h3>{calculateAvgResolutionTime()}</h3>
                <p>Avg Resolution Time</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon yellow">📈</div>
              <div className="stat-info">
                <h3>{analytics.openTickets}</h3>
                <p>Pending Tickets</p>
              </div>
            </div>
          </div>

          <div className="report-section">
            <h4>Ticket Distribution by Priority</h4>
            <table className="report-table">
              <thead>
                <tr>
                  <th>Priority</th>
                  <th>Count</th>
                  <th>Percentage</th>
                </tr>
              </thead>
              <tbody>
                {['HIGH', 'MEDIUM', 'LOW'].map(priority => {
                  const count = tickets.filter(t => t.priority === priority).length;
                  const percentage = tickets.length > 0 ? ((count / tickets.length) * 100).toFixed(1) : 0;
                  return (
                    <tr key={priority}>
                      <td><span className={`badge badge-${priority.toLowerCase()}`}>{priority}</span></td>
                      <td>{count}</td>
                      <td>{percentage}%</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="report-section">
            <h4>Ticket Distribution by Status</h4>
            <table className="report-table">
              <thead>
                <tr>
                  <th>Status</th>
                  <th>Count</th>
                  <th>Percentage</th>
                </tr>
              </thead>
              <tbody>
                {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(status => {
                  const count = tickets.filter(t => t.status === status).length;
                  const percentage = tickets.length > 0 ? ((count / tickets.length) * 100).toFixed(1) : 0;
                  return (
                    <tr key={status}>
                      <td>{status.replace('_', ' ')}</td>
                      <td>{count}</td>
                      <td>{percentage}%</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Reports;
