# API Documentation

Base URL: `http://localhost:8080/api`

## Authentication

All endpoints except `/auth/*` require JWT token in Authorization header:
```
Authorization: Bearer <token>
```

---

## Auth Endpoints

### Register User
**POST** `/auth/register`

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

**Roles:** `ADMIN`, `EMPLOYEE`, `CUSTOMER`

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "role": "CUSTOMER"
}
```

---

### Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "role": "CUSTOMER"
}
```

---

## Customer Endpoints

### Create Ticket
**POST** `/customer/tickets`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "issueType": "SOFTWARE",
  "description": "Cannot access email",
  "priority": "HIGH"
}
```

**Issue Types:** `NETWORK`, `SOFTWARE`, `HARDWARE`
**Priorities:** `LOW`, `MEDIUM`, `HIGH`

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "assignedEmployeeName": null,
  "assignedEmployeeEmail": null,
  "issueType": "SOFTWARE",
  "description": "Cannot access email",
  "priority": "HIGH",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

### Get My Tickets
**GET** `/customer/tickets`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "assignedEmployeeName": "Jane Employee",
    "assignedEmployeeEmail": "jane@example.com",
    "issueType": "SOFTWARE",
    "description": "Cannot access email",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

---

### Add Comment to Ticket
**POST** `/customer/tickets/{id}/comments`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "message": "Still facing the issue"
}
```

**Response:** `200 OK`

---

## Employee Endpoints

### Get Assigned Tickets
**GET** `/employee/tickets`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "assignedEmployeeName": "Jane Employee",
    "assignedEmployeeEmail": "jane@example.com",
    "issueType": "SOFTWARE",
    "description": "Cannot access email",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

---

### Update Ticket Status
**PUT** `/employee/tickets/{id}/status?status=IN_PROGRESS`

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `status`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "assignedEmployeeName": "Jane Employee",
  "assignedEmployeeEmail": "jane@example.com",
  "issueType": "SOFTWARE",
  "description": "Cannot access email",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

---

### Add Comment to Ticket
**POST** `/employee/tickets/{id}/comments`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "message": "Working on this issue"
}
```

**Response:** `200 OK`

---

## Admin Endpoints

### Get All Tickets
**GET** `/admin/tickets`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "assignedEmployeeName": "Jane Employee",
    "assignedEmployeeEmail": "jane@example.com",
    "issueType": "SOFTWARE",
    "description": "Cannot access email",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

---

### Assign Ticket to Employee
**PUT** `/admin/tickets/{id}/assign?employeeId=2`

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `employeeId`: ID of the employee

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "assignedEmployeeName": "Jane Employee",
  "assignedEmployeeEmail": "jane@example.com",
  "issueType": "SOFTWARE",
  "description": "Cannot access email",
  "priority": "HIGH",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

---

### Update Ticket Priority
**PUT** `/admin/tickets/{id}/priority?priority=HIGH`

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `priority`: `LOW`, `MEDIUM`, `HIGH`

**Response:** `200 OK`
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "assignedEmployeeName": "Jane Employee",
  "assignedEmployeeEmail": "jane@example.com",
  "issueType": "SOFTWARE",
  "description": "Cannot access email",
  "priority": "HIGH",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

---

### Get All Users
**GET** `/admin/users`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN",
    "createdAt": "2024-01-15T10:00:00"
  },
  {
    "id": 2,
    "name": "Jane Employee",
    "email": "jane@example.com",
    "role": "EMPLOYEE",
    "createdAt": "2024-01-15T10:05:00"
  }
]
```

---

### Get All Employees
**GET** `/admin/employees`

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "name": "Jane Employee",
    "email": "jane@example.com",
    "role": "EMPLOYEE",
    "createdAt": "2024-01-15T10:05:00"
  }
]
```

---

## Error Responses

### 400 Bad Request
```json
{
  "message": "Validation failed",
  "status": 400
}
```

### 401 Unauthorized
```json
{
  "message": "Invalid credentials",
  "status": 401
}
```

### 403 Forbidden
```json
{
  "message": "Access denied",
  "status": 403
}
```

### 404 Not Found
```json
{
  "message": "Ticket not found",
  "status": 404
}
```

### 500 Internal Server Error
```json
{
  "message": "Internal server error",
  "status": 500
}
```

---

## Testing with cURL

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123","role":"CUSTOMER"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

### Create Ticket
```bash
curl -X POST http://localhost:8080/api/customer/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"issueType":"SOFTWARE","description":"Cannot access email","priority":"HIGH"}'
```

### Get Tickets
```bash
curl -X GET http://localhost:8080/api/customer/tickets \
  -H "Authorization: Bearer YOUR_TOKEN"
```
