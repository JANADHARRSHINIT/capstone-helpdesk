import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { createTicket } from '../services/api';
import './RaiseTicket.css';

function RaiseTicket() {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const permissions = JSON.parse(localStorage.getItem('permissions') || '{}');
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    description: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const description = formData.description.trim();

    if (!description) {
      setError('Please describe the issue before submitting.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      const ticket = await createTicket({
        requesterId: user.id,
        description
      });
      navigate(`/ticket/${ticket.id}`);
    } catch (submitError) {
      setError(submitError.message || 'Failed to raise the ticket');
    } finally {
      setSubmitting(false);
    }
  };

  const raiseTicketEnabled = permissions.RAISE_TICKET !== false;

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />

      <div className="dashboard-main">
        <Navbar title="Raise Ticket" userName={user.name} />

        <div className="dashboard-content">
          <div className="raise-ticket-shell">
            <section className="raise-ticket-hero">
              <span className="raise-ticket-kicker">User Support Module</span>
              <h2>Submit a new IT support request</h2>
              <p>
                Describe the symptoms clearly. The system will identify the category, set the
                priority, and route the ticket to the right support team.
              </p>
              <div className="raise-ticket-meta">
                <div className="raise-ticket-meta-card">
                  <strong>{user.email}</strong>
                  <span>Ticket requester</span>
                </div>
                <div className={`raise-ticket-meta-card ${raiseTicketEnabled ? 'enabled' : 'disabled'}`}>
                  <strong>{raiseTicketEnabled ? 'Enabled' : 'Restricted'}</strong>
                  <span>Raise Ticket permission</span>
                </div>
              </div>
            </section>

            <section className="raise-ticket-card">
              {!raiseTicketEnabled ? (
                <div className="raise-ticket-locked">
                  <h3>Ticket creation is not available for this account</h3>
                  <p>Your current module permissions do not allow raising tickets.</p>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="raise-ticket-form">
                  <label className="raise-ticket-field">
                    <span>Description</span>
                    <textarea
                      name="description"
                      rows="8"
                      value={formData.description}
                      onChange={handleChange}
                      placeholder="Describe what happened, what you expected, and any error messages you saw."
                    />
                  </label>

                  {error && <p className="raise-ticket-error">{error}</p>}

                  <div className="raise-ticket-actions">
                    <button type="button" className="raise-ticket-secondary" onClick={() => navigate('/my-issues')}>
                      Cancel
                    </button>
                    <button type="submit" className="raise-ticket-primary" disabled={submitting}>
                      {submitting ? 'Submitting...' : 'Create Ticket'}
                    </button>
                  </div>
                </form>
              )}
            </section>
          </div>
        </div>
      </div>
    </div>
  );
}

export default RaiseTicket;
