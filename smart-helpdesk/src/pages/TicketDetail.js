import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { tickets, users } from '../data/mockData';
import { formatDate } from '../utils/helpers';
import './TicketDetail.css';

function TicketDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));
  const ticket = tickets.find(t => t.id === parseInt(id));
  const employees = users.filter(u => u.role === 'EMPLOYEE');

  const [reply, setReply] = useState('');
  const [status, setStatus] = useState(ticket?.status || 'OPEN');
  const [assignedEmployee, setAssignedEmployee] = useState(ticket?.assignedEmployeeId || '');

  if (!ticket) {
    return <div>Ticket not found</div>;
  }

  const handleReply = (e) => {
    e.preventDefault();
    if (reply.trim()) {
      alert('Reply added: ' + reply);
      setReply('');
    }
  };

  const handleStatusChange = (newStatus) => {
    setStatus(newStatus);
    alert('Status updated to: ' + newStatus);
  };

  const handleAssignEmployee = (employeeId) => {
    setAssignedEmployee(employeeId);
    const employee = employees.find(e => e.id === parseInt(employeeId));
    alert('Ticket assigned to: ' + employee?.name);
  };

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
        <Navbar title="Ticket Details" userName={user.name} />
        
        <div className="dashboard-content">
          <button onClick={() => navigate(-1)} className="back-btn">← Back</button>

          <div className="ticket-detail-grid">
            <div className="ticket-detail-left">
              <div className="ticket-info-card">
                <div className="ticket-header">
                  <div>
                    <h2>Ticket #{ticket.id}</h2>
                    <p className="ticket-date">Created {formatDate(ticket.createdAt)}</p>
                  </div>
                  <div className="ticket-badges">
                    <span className={`badge ${getPriorityClass(ticket.priority)}`}>{ticket.priority}</span>
                    <span className={`badge ${getStatusClass(status)}`}>{status.replace('_', ' ')}</span>
                  </div>
                </div>

                <div className="ticket-info-section">
                  <div className="info-row">
                    <label>Customer</label>
                    <span>{ticket.customerName}</span>
                  </div>
                  <div className="info-row">
                    <label>Issue Type</label>
                    <span>{ticket.issueType}</span>
                  </div>
                  <div className="info-row">
                    <label>Description</label>
                    <p className="description-text">{ticket.description}</p>
                  </div>
                </div>
              </div>

              <div className="comments-card">
                <h3>Conversation</h3>
                <div className="comments-list">
                  {ticket.comments.length === 0 ? (
                    <p className="no-comments">No comments yet</p>
                  ) : (
                    ticket.comments.map(comment => (
                      <div key={comment.id} className="comment-item">
                        <div className="comment-header">
                          <strong>{comment.userName}</strong>
                          <span className="comment-time">{formatDate(comment.timestamp)}</span>
                        </div>
                        <p className="comment-text">{comment.message}</p>
                      </div>
                    ))
                  )}
                </div>

                <form onSubmit={handleReply} className="reply-form">
                  <label>Add Reply</label>
                  <textarea
                    value={reply}
                    onChange={(e) => setReply(e.target.value)}
                    placeholder="Type your reply..."
                    rows="4"
                  />
                  <button type="submit" className="btn-primary">Send Reply</button>
                </form>
              </div>
            </div>

            <div className="ticket-detail-right">
              <div className="actions-card">
                <h3>Actions</h3>

                <div className="action-group">
                  <label>Change Status</label>
                  <select value={status} onChange={(e) => handleStatusChange(e.target.value)}>
                    <option value="OPEN">Open</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="RESOLVED">Resolved</option>
                    <option value="CLOSED">Closed</option>
                  </select>
                </div>

                {user.role === 'ADMIN' && (
                  <div className="action-group">
                    <label>Assign Employee</label>
                    <select value={assignedEmployee} onChange={(e) => handleAssignEmployee(e.target.value)}>
                      <option value="">Unassigned</option>
                      {employees.map(emp => (
                        <option key={emp.id} value={emp.id}>{emp.name}</option>
                      ))}
                    </select>
                  </div>
                )}

                <div className="action-group">
                  <label>Last Updated</label>
                  <p className="info-text">{formatDate(ticket.updatedAt)}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Chatbot />
    </div>
  );
}

export default TicketDetail;
