import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { tickets, analytics } from '../data/mockData';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import './Dashboard.css';

function Dashboard() {
  const user = JSON.parse(localStorage.getItem('user'));
  const COLORS = ['#3B5BDB', '#8B5CF6', '#22C55E'];

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      
      <div className="dashboard-main">
        <Navbar title="Dashboard" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon blue">🎫</div>
              <div className="stat-info">
                <h3>{analytics.totalTickets}</h3>
                <p>Total Tickets</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon yellow">📂</div>
              <div className="stat-info">
                <h3>{analytics.openTickets}</h3>
                <p>Open</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon purple">⚙️</div>
              <div className="stat-info">
                <h3>{analytics.inProgressTickets}</h3>
                <p>In Progress</p>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon green">✅</div>
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
                  <Bar dataKey="value" fill="#3B5BDB" />
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
