import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { fetchTickets } from '../services/api';
import './Issues.css';

function MyIssues() {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [tickets, setTickets] = useState([]);
  const user = JSON.parse(localStorage.getItem('user'));
  const navigate = useNavigate();

  const loadTickets = async () => {
    try {
      const data = await fetchTickets({
        status: statusFilter,
        priority: priorityFilter,
        search
      });
      setTickets(data);
    } catch (error) {
      alert(error.message || 'Failed to load tickets');
    }
  };

  useEffect(() => {
    loadTickets();
  }, [statusFilter, priorityFilter, search]);

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
        <Navbar title="My Issues" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="issues-header">
            <input
              type="text"
              placeholder="Search by ID or description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="search-input"
            />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="filter-select">
              <option value="ALL">All Status</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
            <select value={priorityFilter} onChange={(e) => setPriorityFilter(e.target.value)} className="filter-select">
              <option value="ALL">All Priority</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div className="issues-table-container">
            <table className="issues-table">
              <thead>
                <tr>
                  <th>Ticket ID</th>
                  <th>Issue Type</th>
                  <th>Description</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {tickets.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '20px' }}>
                      No tickets found. Use the chatbot to raise an issue.
                    </td>
                  </tr>
                ) : (
                  tickets.map(ticket => (
                    <tr key={ticket.id}>
                      <td>#{ticket.id}</td>
                      <td>{ticket.issueType}</td>
                      <td>{ticket.description.substring(0, 50)}...</td>
                      <td><span className={`badge ${getPriorityClass(ticket.priority)}`}>{ticket.priority}</span></td>
                      <td><span className={`badge ${getStatusClass(ticket.status)}`}>{ticket.status.replace('_', ' ')}</span></td>
                      <td>{new Date(ticket.createdAt).toLocaleDateString()}</td>
                      <td>
                        <button onClick={() => navigate(`/ticket/${ticket.id}`)} className="view-btn">View</button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <Chatbot onTicketCreated={loadTickets} />
    </div>
  );
}

export default MyIssues;
