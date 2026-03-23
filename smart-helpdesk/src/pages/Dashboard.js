import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { fetchAnalytics, fetchTickets } from '../services/api';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import './Dashboard.css';

function Dashboard() {
  const user = JSON.parse(localStorage.getItem('user'));
  const navigate = useNavigate();
  const isEmployee = user.role === 'EMPLOYEE';
  const [analytics, setAnalytics] = useState({
    totalTickets: 0,
    openTickets: 0,
    inProgressTickets: 0,
    closedTickets: 0,
    ticketsByStatus: [],
    ticketsByCategory: []
  });
  const [assignedTickets, setAssignedTickets] = useState([]);
  const COLORS = ['#0D6EFD', '#16A34A', '#0EA5E9'];

  const loadDashboard = useCallback(async () => {
    try {
      const [analyticsData, ticketData] = await Promise.all([
        fetchAnalytics({ assignedToMe: isEmployee }),
        isEmployee ? fetchTickets({ assignedToMe: true }) : Promise.resolve([])
      ]);
      setAnalytics(analyticsData);
      setAssignedTickets(ticketData);
    } catch (error) {
      alert(error.message || 'Failed to load dashboard analytics');
    }
  }, [isEmployee]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const getPriorityClass = (priority) => {
    if (priority === 'HIGH') return 'badge-high';
    if (priority === 'MEDIUM') return 'badge-medium';
    return 'badge-low';
  };

  const getStatusClass = (status) => {
    if (status === 'OPEN') return 'badge-open';
    if (status === 'IN_PROGRESS') return 'badge-progress';
    if (status === 'RESOLVED') return 'badge-resolved';
    return 'badge-closed';
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />

      <div className="dashboard-main">
        <Navbar title="Dashboard" userName={user.name} />

        <div className="dashboard-content">
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon blue">&#127915;</div>
              <div className="stat-info">
                <h3>{analytics.totalTickets}</h3>
                <p>Total Tickets</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon yellow">&#128194;</div>
              <div className="stat-info">
                <h3>{analytics.openTickets}</h3>
                <p>Open</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon purple">&#9881;&#65039;</div>
              <div className="stat-info">
                <h3>{analytics.inProgressTickets}</h3>
                <p>In Progress</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon green">&#9989;</div>
              <div className="stat-info">
                <h3>{analytics.closedTickets}</h3>
                <p>Closed</p>
              </div>
            </div>
          </div>

          <div className="charts-grid">
            <div className="chart-card">
              <h3>{isEmployee ? 'My Tickets by Status' : 'Tickets by Status'}</h3>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={analytics.ticketsByStatus} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {analytics.ticketsByStatus.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="chart-card">
              <h3>{isEmployee ? 'My Tickets by Category' : 'Tickets by Category'}</h3>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={analytics.ticketsByCategory}>
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="value" fill="#0D6EFD" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {isEmployee && (
            <div className="dashboard-assigned-card">
              <div className="dashboard-assigned-header">
                <div>
                  <h3>Assigned to {user.name}</h3>
                  <p>Only tickets currently assigned to you are shown here.</p>
                </div>
              </div>

              <div className="dashboard-ticket-list">
                {assignedTickets.length === 0 ? (
                  <p className="dashboard-empty">No tickets are assigned to you right now.</p>
                ) : (
                  assignedTickets.map((ticket) => (
                    <div key={ticket.id} className="dashboard-ticket-item">
                      <div className="dashboard-ticket-main">
                        <div className="dashboard-ticket-top">
                          <strong>Ticket #{ticket.id}</strong>
                          <span className="dashboard-ticket-team">{ticket.routingTeam || ticket.issueType} Team</span>
                        </div>
                        <p className="dashboard-ticket-meta">
                          {ticket.customerName} · {ticket.issueType} · {new Date(ticket.createdAt).toLocaleDateString()}
                        </p>
                        <p className="dashboard-ticket-description">{ticket.description}</p>
                      </div>
                      <div className="dashboard-ticket-side">
                        <span className={`badge ${getPriorityClass(ticket.priority)}`}>{ticket.priority}</span>
                        <span className={`badge ${getStatusClass(ticket.status)}`}>{ticket.status.replace('_', ' ')}</span>
                        <button className="view-btn" onClick={() => navigate(`/ticket/${ticket.id}`)}>
                          View Details
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Dashboard;
