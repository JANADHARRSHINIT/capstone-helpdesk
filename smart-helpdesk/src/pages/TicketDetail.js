import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { formatDate } from '../utils/helpers';
import { addTicketComment, assignTicket, fetchTicketById, fetchUsers, updateTicketStatus } from '../services/api';
import './TicketDetail.css';

function TicketDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));

  const [ticket, setTicket] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [reply, setReply] = useState('');
  const [status, setStatus] = useState('OPEN');
  const [assignedEmployee, setAssignedEmployee] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [ticketData, employeeData] = await Promise.all([
          fetchTicketById(id),
          fetchUsers('EMPLOYEE')
        ]);
        setTicket(ticketData);
        setStatus(ticketData.status);
        setAssignedEmployee(ticketData.assignedEmployeeId || '');
        setEmployees(employeeData);
      } catch (error) {
        alert(error.message || 'Failed to load ticket');
      }
    };
    loadData();
  }, [id]);

  if (!ticket) {
    return <div>Loading ticket...</div>;
  }

  const handleReply = async (e) => {
    e.preventDefault();
    if (!reply.trim()) return;
    try {
      await addTicketComment(ticket.id, user.id, reply.trim());
      const updated = await fetchTicketById(id);
      setTicket(updated);
      setReply('');
    } catch (error) {
      alert(error.message || 'Failed to add reply');
    }
  };

  const handleStatusChange = async (newStatus) => {
    try {
      await updateTicketStatus(ticket.id, newStatus);
      setStatus(newStatus);
      setTicket((prev) => ({ ...prev, status: newStatus }));
    } catch (error) {
      alert(error.message || 'Failed to update status');
    }
  };

  const handleAssignEmployee = async (employeeId) => {
    try {
      await assignTicket(ticket.id, employeeId || null);
      setAssignedEmployee(employeeId);
      const employee = employees.find((emp) => emp.id === parseInt(employeeId, 10));
      setTicket((prev) => ({
        ...prev,
        assignedEmployeeId: employeeId || null,
        assignedEmployeeName: employee ? employee.name : null
      }));
    } catch (error) {
      alert(error.message || 'Failed to assign employee');
    }
  };

  const getPriorityClass = (priority) => {
    if (priority === 'HIGH') return 'badge-high';
    if (priority === 'MEDIUM') return 'badge-medium';
    return 'badge-low';
  };

  const getStatusClass = (value) => {
    if (value === 'OPEN') return 'badge-open';
    if (value === 'IN_PROGRESS') return 'badge-progress';
    if (value === 'RESOLVED') return 'badge-resolved';
    return 'badge-closed';
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />

      <div className="dashboard-main">
        <Navbar title="Ticket Details" userName={user.name} />

        <div className="dashboard-content">
          <button onClick={() => navigate(-1)} className="back-btn">&#8592; Back</button>

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
                    ticket.comments.map((comment) => (
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
                      {employees.map((emp) => (
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
