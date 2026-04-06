const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

async function apiRequest(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
  } catch (_) {
    throw new Error(`Cannot connect to backend at ${API_BASE_URL}. Make sure the Spring Boot server is running.`);
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const data = await response.json();
      message = data.message || data.detail || data.error || data.title || message;
    } catch (_) {
      try {
        const text = await response.text();
        if (text) message = text;
      } catch (__ ) {
        // Ignore non-JSON and non-text error bodies.
      }
    }
    throw new Error(message);
  }

  return response.json();
}

export function login(email, password) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  });
}

export function demoLogin(role) {
  return apiRequest(`/auth/demo/${role}`, { method: 'POST' });
}

export function signup(payload) {
  return apiRequest('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function fetchAnalytics() {
  return apiRequest('/dashboard/analytics');
}

export function fetchTickets({ status, priority, search } = {}) {
  const params = new URLSearchParams();
  if (status && status !== 'ALL') params.set('status', status);
  if (priority && priority !== 'ALL') params.set('priority', priority);
  if (search) params.set('search', search);
  const suffix = params.toString() ? `?${params.toString()}` : '';
  return apiRequest(`/tickets${suffix}`);
}

export function fetchTicketById(id) {
  return apiRequest(`/tickets/${id}`);
}

export function updateTicketStatus(id, status) {
  return apiRequest(`/tickets/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status })
  });
}

export function updateTicketPriority(id, priority) {
  return apiRequest(`/tickets/${id}/priority`, {
    method: 'PUT',
    body: JSON.stringify({ priority })
  });
}

export function assignTicket(id, employeeId) {
  return apiRequest(`/tickets/${id}/assign`, {
    method: 'PUT',
    body: JSON.stringify({ employeeId: employeeId || null })
  });
}

export function addTicketComment(id, userId, message) {
  return apiRequest(`/tickets/${id}/comments`, {
    method: 'POST',
    body: JSON.stringify({ userId, message })
  });
}

export function fetchUsers(role) {
  const suffix = role ? `?role=${role}` : '';
  return apiRequest(`/users${suffix}`);
}

export function fetchPermissions(role) {
  return apiRequest(`/permissions/${role}`);
}

export function createTicket(payload) {
  return apiRequest('/tickets', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function analyzeTicket(description) {
  return apiRequest('/ai/analyze', {
    method: 'POST',
    body: JSON.stringify({ description })
  });
}

export function fetchAuditLogs() {
  return apiRequest('/audit-logs');
}

export function fetchTicketAuditLogs(ticketId) {
  return apiRequest(`/audit-logs/ticket/${ticketId}`);
}
