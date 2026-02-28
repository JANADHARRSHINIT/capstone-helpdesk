import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { fetchAnalytics } from '../services/api';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import './Dashboard.css';

function Dashboard() {
  const user = JSON.parse(localStorage.getItem('user'));
  const [analytics, setAnalytics] = useState({
    totalTickets: 0,
    openTickets: 0,
    inProgressTickets: 0,
    closedTickets: 0,
    ticketsByStatus: [],
    ticketsByCategory: []
  });
  const COLORS = ['#0D6EFD', '#16A34A', '#0EA5E9'];

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const data = await fetchAnalytics();
        setAnalytics(data);
      } catch (error) {
        alert(error.message || 'Failed to load dashboard analytics');
      }
    };
    loadAnalytics();
  }, []);

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
              <h3>Tickets by Status</h3>
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
              <h3>Tickets by Category</h3>
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
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Dashboard;
