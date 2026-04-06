import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import Chatbot from '../components/Chatbot';
import { createTicket, analyzeTicket } from '../services/api';
import './CreateTicket.css';

function CreateTicket() {
  const user = JSON.parse(localStorage.getItem('user'));
  const navigate = useNavigate();

  const [form, setForm] = useState({ description: '', issueType: '', priority: '' });
  const [aiSuggestion, setAiSuggestion] = useState(null);
  const [duplicates, setDuplicates] = useState([]);
  const [analyzing, setAnalyzing] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [debounceTimer, setDebounceTimer] = useState(null);

  const runAIAnalysis = useCallback(async (description) => {
    if (description.length < 20) return;
    setAnalyzing(true);
    try {
      const result = await analyzeTicket(description);
      setAiSuggestion(result);
      setDuplicates(result.possibleDuplicates || []);
      setForm(prev => ({
        ...prev,
        issueType: result.suggestedCategory,
        priority: prev.priority || result.suggestedPriority
      }));
    } catch (e) {
      // silently fail AI analysis
    } finally {
      setAnalyzing(false);
    }
  }, []);

  const handleDescriptionChange = (e) => {
    const value = e.target.value;
    setForm(prev => ({ ...prev, description: value }));
    if (debounceTimer) clearTimeout(debounceTimer);
    const timer = setTimeout(() => runAIAnalysis(value), 800);
    setDebounceTimer(timer);
  };

  useEffect(() => () => { if (debounceTimer) clearTimeout(debounceTimer); }, [debounceTimer]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.description || !form.priority) {
      alert('Please fill all required fields');
      return;
    }
    setSubmitting(true);
    try {
      const ticket = await createTicket({
        requesterId: user.id,
        description: form.description,
        priority: form.priority
      });
      alert(ticket.assignedEmployeeName
        ? `Ticket created and routed to ${ticket.assignedEmployeeName}.`
        : `Ticket created and routed to the ${ticket.issueType} team queue.`);
      navigate('/my-issues');
    } catch (error) {
      alert(error.message || 'Failed to create ticket');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="dashboard-layout">
      <Sidebar role={user.role} />
      <div className="dashboard-main">
        <Navbar title="Create Ticket" userName={user.name} />
        <div className="dashboard-content">
          <div className="create-ticket-grid">

            <div className="create-ticket-form-card">
              <h3>New Support Ticket</h3>
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label>Description <span className="required">*</span></label>
                  <textarea
                    value={form.description}
                    onChange={handleDescriptionChange}
                    placeholder="Describe your issue in detail..."
                    rows="5"
                    required
                  />
                  {analyzing && <span className="ai-analyzing">🤖 AI analyzing...</span>}
                </div>

                <div className="form-group">
                  <label>Detected Department</label>
                  {aiSuggestion && (
                    <span className="ai-tag">🤖 AI suggested: {aiSuggestion.suggestedCategory}</span>
                  )}
                  <input
                    type="text"
                    value={form.issueType ? `${form.issueType} Team` : 'Will be classified automatically'}
                    readOnly
                  />
                </div>

                <div className="form-group">
                  <label>Priority <span className="required">*</span></label>
                  {aiSuggestion && (
                    <span className="ai-tag">🤖 AI suggested: {aiSuggestion.suggestedPriority}</span>
                  )}
                  <select value={form.priority} onChange={(e) => setForm(prev => ({ ...prev, priority: e.target.value }))} required>
                    <option value="">Select priority</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>

                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? 'Submitting...' : 'Submit Ticket'}
                </button>
              </form>
            </div>

            <div className="create-ticket-sidebar">
              {aiSuggestion && (
                <div className="ai-suggestion-card">
                  <h4>🤖 AI Suggestion</h4>
                  <p>{aiSuggestion.suggestedSolution}</p>
                </div>
              )}

              {duplicates.length > 0 && (
                <div className="duplicates-card">
                  <h4>⚠️ Similar Tickets Found</h4>
                  <p className="duplicate-note">These tickets may be related to your issue:</p>
                  {duplicates.map(dup => (
                    <div key={dup.id} className="duplicate-item" onClick={() => navigate(`/ticket/${dup.id}`)}>
                      <span className="duplicate-id">#{dup.id}</span>
                      <span className="duplicate-desc">{dup.description.substring(0, 60)}...</span>
                      <span className={`badge badge-${dup.status === 'OPEN' ? 'open' : dup.status === 'IN_PROGRESS' ? 'progress' : 'resolved'}`}>
                        {dup.status.replace('_', ' ')}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              <div className="tips-card">
                <h4>💡 Tips for faster resolution</h4>
                <ul>
                  <li>Describe the issue clearly</li>
                  <li>Mention error messages if any</li>
                  <li>Include steps to reproduce</li>
                  <li>Specify affected device/software</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
      <Chatbot />
    </div>
  );
}

export default CreateTicket;
