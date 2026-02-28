# Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Start MySQL
```bash
# Start MySQL service
mysql -u root -p

# Create database
CREATE DATABASE helpdesk_db;
```

### Step 2: Start Backend
```bash
cd backend

# Update MySQL password in src/main/resources/application.properties
# spring.datasource.password=YOUR_PASSWORD

# Run Spring Boot
mvn spring-boot:run
```

✅ Backend running on http://localhost:8080

### Step 3: Start Frontend
```bash
cd smart-helpdesk

# Install dependencies (first time only)
npm install

# Start React app
npm start
```

✅ Frontend running on http://localhost:3000

### Step 4: Test the Application

1. **Register** a new user at http://localhost:3000/login
   - Click "Don't have an account? Sign Up"
   - Fill in details and select role (ADMIN, EMPLOYEE, or CUSTOMER)

2. **Login** with your credentials

3. **Test Features:**
   - **Customer**: Create tickets, view status
   - **Employee**: View assigned tickets, update status
   - **Admin**: View all tickets, assign to employees

---

## Default Test Credentials

After running schema.sql, you can use:

**Admin:**
- Email: admin@helpdesk.com
- Password: admin123

**Employee:**
- Email: employee@helpdesk.com
- Password: employee123

**Customer:**
- Email: customer@helpdesk.com
- Password: customer123

*(Note: Update schema.sql with proper BCrypt hashed passwords)*

---

## Common Commands

### Backend
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Run tests
mvn test
```

### Frontend
```bash
# Install dependencies
npm install

# Start dev server
npm start

# Build for production
npm run build

# Run tests
npm test
```

---

## Verify Setup

### Check Backend
```bash
curl http://localhost:8080/api/auth/login
```

### Check Frontend
Open browser: http://localhost:3000

---

## Need Help?

- Backend not starting? Check MySQL connection
- Frontend errors? Run `npm install` again
- CORS issues? Verify SecurityConfig.java
- See full README.md for detailed troubleshooting
