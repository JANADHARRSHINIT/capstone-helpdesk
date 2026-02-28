# Smart IT Help Desk - Full Stack Application

A complete help desk management system with role-based access control for Admin, Employee, and Customer users.

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT
- **Build Tool**: Maven

### Frontend
- **Framework**: React 19
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **Styling**: CSS

---

## Project Structure

```
Capestone/
├── backend/                          # Spring Boot Backend
│   ├── src/main/java/com/helpdesk/smartdesk/
│   │   ├── controller/              # REST Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── AdminController.java
│   │   │   ├── EmployeeController.java
│   │   │   └── CustomerController.java
│   │   ├── service/                 # Business Logic
│   │   │   ├── AuthService.java
│   │   │   ├── TicketService.java
│   │   │   └── UserService.java
│   │   ├── repository/              # Data Access Layer
│   │   │   ├── UserRepository.java
│   │   │   ├── TicketRepository.java
│   │   │   └── CommentRepository.java
│   │   ├── model/                   # JPA Entities
│   │   │   ├── User.java
│   │   │   ├── Ticket.java
│   │   │   └── Comment.java
│   │   ├── dto/                     # Data Transfer Objects
│   │   ├── security/                # JWT & Security
│   │   │   ├── JwtUtil.java
│   │   │   └── JwtAuthFilter.java
│   │   ├── config/                  # Configuration
│   │   │   └── SecurityConfig.java
│   │   ├── exception/               # Exception Handling
│   │   └── SmartDeskApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── pom.xml
│   └── schema.sql
│
└── smart-helpdesk/                   # React Frontend
    ├── src/
    │   ├── components/
    │   │   ├── Login.js
    │   │   ├── Home.js
    │   │   ├── AdminDashboard.js
    │   │   ├── EmployeeDashboard.js
    │   │   ├── CustomerDashboard.js
    │   │   ├── Chatbot.js
    │   │   └── ProtectedRoute.js
    │   ├── services/
    │   │   └── api.js              # Axios API calls
    │   ├── App.js
    │   ├── App.css
    │   └── index.js
    └── package.json
```

---

## Database Schema

### Users Table
```sql
- id (BIGINT, Primary Key)
- name (VARCHAR)
- email (VARCHAR, Unique)
- password (VARCHAR, Hashed)
- role (ENUM: ADMIN, EMPLOYEE, CUSTOMER)
- created_at (TIMESTAMP)
```

### Tickets Table
```sql
- id (BIGINT, Primary Key)
- customer_id (BIGINT, Foreign Key → users.id)
- assigned_employee_id (BIGINT, Foreign Key → users.id, Nullable)
- issue_type (ENUM: NETWORK, SOFTWARE, HARDWARE)
- description (TEXT)
- priority (ENUM: LOW, MEDIUM, HIGH)
- status (ENUM: OPEN, IN_PROGRESS, RESOLVED, CLOSED)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Comments Table
```sql
- id (BIGINT, Primary Key)
- ticket_id (BIGINT, Foreign Key → tickets.id)
- user_id (BIGINT, Foreign Key → users.id)
- message (TEXT)
- created_at (TIMESTAMP)
```

---

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ and npm
- Git

### 1. Database Setup

**Start MySQL and create database:**
```bash
mysql -u root -p
```

```sql
CREATE DATABASE helpdesk_db;
USE helpdesk_db;
```

**Run the schema script:**
```bash
cd backend
mysql -u root -p helpdesk_db < schema.sql
```

**Or let Spring Boot auto-create tables** (configured in application.properties)

### 2. Backend Setup

**Navigate to backend directory:**
```bash
cd backend
```

**Configure database credentials:**
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

**Build and run:**
```bash
mvn clean install
mvn spring-boot:run
```

Backend will start on: `http://localhost:8080`

### 3. Frontend Setup

**Navigate to frontend directory:**
```bash
cd smart-helpdesk
```

**Install dependencies:**
```bash
npm install
```

**Start development server:**
```bash
npm start
```

Frontend will start on: `http://localhost:3000`

---

## API Endpoints

### Authentication APIs
```
POST   /api/auth/register    - Register new user
POST   /api/auth/login       - Login user
```

### Customer APIs (Role: CUSTOMER)
```
POST   /api/customer/tickets              - Create ticket
GET    /api/customer/tickets              - Get my tickets
POST   /api/customer/tickets/{id}/comments - Add comment
```

### Employee APIs (Role: EMPLOYEE)
```
GET    /api/employee/tickets                    - Get assigned tickets
PUT    /api/employee/tickets/{id}/status        - Update ticket status
POST   /api/employee/tickets/{id}/comments      - Add comment
```

### Admin APIs (Role: ADMIN)
```
GET    /api/admin/tickets                  - Get all tickets
PUT    /api/admin/tickets/{id}/assign      - Assign ticket to employee
PUT    /api/admin/tickets/{id}/priority    - Update ticket priority
GET    /api/admin/users                    - Get all users
GET    /api/admin/employees                - Get all employees
```

---

## Sample API Requests

### Register User
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "role": "CUSTOMER"
}
```

### Login
```json
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Create Ticket (Customer)
```json
POST http://localhost:8080/api/customer/tickets
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "issueType": "SOFTWARE",
  "description": "Unable to access email client",
  "priority": "HIGH"
}
```

**Response:**
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "assignedEmployeeName": null,
  "assignedEmployeeEmail": null,
  "issueType": "SOFTWARE",
  "description": "Unable to access email client",
  "priority": "HIGH",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Assign Ticket (Admin)
```json
PUT http://localhost:8080/api/admin/tickets/1/assign?employeeId=2
Authorization: Bearer <JWT_TOKEN>
```

### Update Status (Employee)
```json
PUT http://localhost:8080/api/employee/tickets/1/status?status=IN_PROGRESS
Authorization: Bearer <JWT_TOKEN>
```

---

## User Roles & Features

### ADMIN
- View all tickets
- Assign tickets to employees
- Change ticket priority
- View all users
- Dashboard with statistics

### EMPLOYEE
- View assigned tickets
- Update ticket status
- Add comments to tickets
- Filter tickets by status

### CUSTOMER
- Create new tickets
- View own tickets
- Track ticket status
- Add comments
- Access chatbot support

---

## Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Password Encryption**: BCrypt hashing
- **Role-Based Access Control**: Spring Security with method-level security
- **CORS Configuration**: Configured for React frontend
- **Protected Routes**: Frontend route guards

---

## Testing the Application

### 1. Register Users
Create users with different roles:
- Admin: role = "ADMIN"
- Employee: role = "EMPLOYEE"
- Customer: role = "CUSTOMER"

### 2. Login
Login with registered credentials to receive JWT token

### 3. Test Workflows

**Customer Workflow:**
1. Login as Customer
2. Create a ticket
3. View ticket status

**Admin Workflow:**
1. Login as Admin
2. View all tickets
3. Assign ticket to employee
4. Change priority

**Employee Workflow:**
1. Login as Employee
2. View assigned tickets
3. Update ticket status

---

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**MySQL Connection Error:**
- Verify MySQL is running
- Check credentials in application.properties
- Ensure database exists

**JWT Token Error:**
- Check token expiration (24 hours default)
- Verify JWT secret in application.properties

### Frontend Issues

**CORS Error:**
- Verify backend CORS configuration allows http://localhost:3000
- Check SecurityConfig.java

**API Connection Failed:**
- Ensure backend is running on port 8080
- Check API_URL in src/services/api.js

**Login Redirect Loop:**
- Clear localStorage
- Check token validity

---

## Development Notes

### Adding New Features

**Backend:**
1. Create entity in `model/`
2. Create repository in `repository/`
3. Create service in `service/`
4. Create controller in `controller/`
5. Add DTOs in `dto/`

**Frontend:**
1. Create component in `components/`
2. Add API calls in `services/api.js`
3. Add route in `App.js`
4. Style in `App.css`

---

## Production Deployment

### Backend
1. Update application.properties for production database
2. Change JWT secret
3. Build: `mvn clean package`
4. Deploy JAR: `java -jar target/smartdesk-1.0.0.jar`

### Frontend
1. Update API_URL in api.js to production backend URL
2. Build: `npm run build`
3. Deploy build folder to web server

---

## License
MIT License

## Support
For issues and questions, please create an issue in the repository.
"# capstone" 
