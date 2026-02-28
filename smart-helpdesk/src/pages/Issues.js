import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { tickets } from '../data/mockData';
import './Issues.css';

function Issues() {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const user = JSON.parse(localStorage.getItem('user'));
  const navigate = useNavigate();

  let filteredTickets = tickets;
  if (statusFilter !== 'ALL') filteredTickets = filteredTickets.filter(t => t.status === statusFilter);
  if (priorityFilter !== 'ALL') filteredTickets = filteredTickets.filter(t => t.priority === priorityFilter);
  if (search) filteredTickets = filteredTickets.filter(t => 
    t.id.toString().includes(search) || t.customerName.toLowerCase().includes(search.toLowerCase())
  );

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
        <Navbar title="All Issues" userName={user.name} />
        
        <div className="dashboard-content">
          <div className="issues-header">
            <input
              type="text"
              placeholder="Search by ID or customer..."
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
                  <th>User</th>
                  <th>Issue Type</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredTickets.map(ticket => (
                  <tr key={ticket.id}>
                    <td>#{ticket.id}</td>
                    <td>{ticket.customerName}</td>
                    <td>{ticket.issueType}</td>
                    <td><span className={`badge ${getPriorityClass(ticket.priority)}`}>{ticket.priority}</span></td>
                    <td><span className={`badge ${getStatusClass(ticket.status)}`}>{ticket.status.replace('_', ' ')}</span></td>
                    <td>{new Date(ticket.createdAt).toLocaleDateString()}</td>
                    <td>
                      <button onClick={() => navigate(`/ticket/${ticket.id}`)} className="view-btn">View</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default Issues;
