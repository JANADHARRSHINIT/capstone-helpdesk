const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

function getAuthHeaders() {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  if (!user?.id || !user?.role) {
    return {};
  }

  return {
    'X-User-Id': String(user.id),
    'X-User-Role': user.role
  };
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...(options.headers || {})
    },
    ...options
  });

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

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  if (!text) {
    return null;
  }

  return JSON.parse(text);
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

export function fetchAnalytics({ assignedToMe } = {}) {
  const params = new URLSearchParams();
  if (assignedToMe) params.set('assignedToMe', 'true');
  const suffix = params.toString() ? `?${params.toString()}` : '';
  return apiRequest(`/dashboard/analytics${suffix}`);
}

export function fetchTickets({ status, priority, search, assignedToMe } = {}) {
  const params = new URLSearchParams();
  if (status && status !== 'ALL') params.set('status', status);
  if (priority && priority !== 'ALL') params.set('priority', priority);
  if (search) params.set('search', search);
  if (assignedToMe) params.set('assignedToMe', 'true');
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

export function fetchNotifications(userId) {
  return apiRequest(`/notifications/${userId}`);
}

export function fetchUnreadNotificationCount(userId) {
  return apiRequest(`/notifications/${userId}/unread-count`);
}

export function markNotificationAsRead(id) {
  return apiRequest(`/notifications/${id}/read`, {
    method: 'PUT'
  });
}
