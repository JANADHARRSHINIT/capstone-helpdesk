export const users = [
  { id: 1, name: 'Admin User', email: 'admin@helpdesk.com', role: 'ADMIN' },
  { id: 2, name: 'John Employee', email: 'john@helpdesk.com', role: 'EMPLOYEE' },
  { id: 3, name: 'Jane Employee', email: 'jane@helpdesk.com', role: 'EMPLOYEE' },
  { id: 4, name: 'Alice Customer', email: 'alice@company.com', role: 'CUSTOMER' },
  { id: 5, name: 'Bob Customer', email: 'bob@company.com', role: 'CUSTOMER' }
];

export const tickets = [
  {
    id: 1001,
    customerId: 4,
    customerName: 'Alice Customer',
    assignedEmployeeId: 2,
    assignedEmployeeName: 'John Employee',
    issueType: 'SOFTWARE',
    description: 'Unable to access email client. Getting authentication error.',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    createdAt: '2024-01-15T10:30:00',
    updatedAt: '2024-01-15T14:20:00',
    comments: [
      { id: 1, userId: 4, userName: 'Alice Customer', message: 'I cannot login to my email', timestamp: '2024-01-15T10:30:00' },
      { id: 2, userId: 2, userName: 'John Employee', message: 'Working on this issue', timestamp: '2024-01-15T11:00:00' }
    ]
  },
  {
    id: 1002,
    customerId: 5,
    customerName: 'Bob Customer',
    assignedEmployeeId: 3,
    assignedEmployeeName: 'Jane Employee',
    issueType: 'HARDWARE',
    description: 'Printer not working in office',
    priority: 'MEDIUM',
    status: 'OPEN',
    createdAt: '2024-01-16T09:15:00',
    updatedAt: '2024-01-16T09:15:00',
    comments: []
  },
  {
    id: 1003,
    customerId: 4,
    customerName: 'Alice Customer',
    assignedEmployeeId: 2,
    assignedEmployeeName: 'John Employee',
    issueType: 'NETWORK',
    description: 'Slow internet connection',
    priority: 'LOW',
    status: 'RESOLVED',
    createdAt: '2024-01-14T08:00:00',
    updatedAt: '2024-01-14T16:30:00',
    comments: [
      { id: 3, userId: 4, userName: 'Alice Customer', message: 'Internet is very slow', timestamp: '2024-01-14T08:00:00' },
      { id: 4, userId: 2, userName: 'John Employee', message: 'Fixed the router issue', timestamp: '2024-01-14T16:30:00' }
    ]
  },
  {
    id: 1004,
    customerId: 5,
    customerName: 'Bob Customer',
    assignedEmployeeId: null,
    assignedEmployeeName: null,
    issueType: 'SOFTWARE',
    description: 'Need MS Office installation',
    priority: 'MEDIUM',
    status: 'OPEN',
    createdAt: '2024-01-17T11:00:00',
    updatedAt: '2024-01-17T11:00:00',
    comments: []
  },
  {
    id: 1005,
    customerId: 4,
    customerName: 'Alice Customer',
    assignedEmployeeId: 3,
    assignedEmployeeName: 'Jane Employee',
    issueType: 'HARDWARE',
    description: 'Laptop screen flickering',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    createdAt: '2024-01-17T13:45:00',
    updatedAt: '2024-01-17T14:00:00',
    comments: []
  },
  {
    id: 1006,
    customerId: 5,
    customerName: 'Bob Customer',
    assignedEmployeeId: 2,
    assignedEmployeeName: 'John Employee',
    issueType: 'NETWORK',
    description: 'Cannot connect to VPN',
    priority: 'HIGH',
    status: 'OPEN',
    createdAt: '2024-01-17T15:20:00',
    updatedAt: '2024-01-17T15:20:00',
    comments: []
  }
];

export const analytics = {
  totalTickets: 6,
  openTickets: 3,
  inProgressTickets: 2,
  closedTickets: 1,
  highPriorityTickets: 3,
  ticketsByStatus: [
    { name: 'Open', value: 3 },
    { name: 'In Progress', value: 2 },
    { name: 'Resolved', value: 1 }
  ],
  ticketsByCategory: [
    { name: 'Software', value: 2 },
    { name: 'Hardware', value: 2 },
    { name: 'Network', value: 2 }
  ]
};

export const chatbotResponses = {
  'password': 'To reset your password, go to Settings > Security > Reset Password or contact your admin.',
  'internet': 'Try restarting your router. If the issue persists, create a ticket for network support.',
  'printer': 'Check if the printer is connected and has paper. Restart the print spooler service.',
  'slow': 'Clear your browser cache, close unused applications, and restart your computer.',
  'email': 'Check your internet connection and verify your email settings. Contact IT if issue persists.',
  'vpn': 'Ensure you have the latest VPN client installed. Check your credentials and try reconnecting.',
  'default': 'I can help with common IT issues. Try asking about password, internet, printer, or email problems. For complex issues, please create a ticket.'
};
