# Setup Verification & Troubleshooting

## Prerequisites Check

### 1. Java Installation
```bash
java -version
```
Expected: Java 17 or higher

### 2. Maven Installation
```bash
mvn -version
```
Expected: Maven 3.6+

### 3. MySQL Installation
```bash
mysql --version
```
Expected: MySQL 8.0+

### 4. Node.js Installation
```bash
node -version
npm -version
```
Expected: Node 16+, npm 8+

---

## Common Errors & Solutions

### Error: Maven not found
**Solution:**
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract and add to PATH
3. Or use IDE's embedded Maven (IntelliJ IDEA, Eclipse)

### Error: Java version mismatch
**Solution:**
```bash
# Check Java version
java -version

# If wrong version, install Java 17
# Download from: https://adoptium.net/
```

### Error: MySQL Connection Failed
**Solution:**
1. Start MySQL service:
   ```bash
   # Windows
   net start MySQL80
   
   # Linux/Mac
   sudo systemctl start mysql
   ```

2. Verify credentials in `application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   ```

3. Create database:
   ```sql
   CREATE DATABASE helpdesk_db;
   ```

### Error: Port 8080 already in use
**Solution:**
Change port in `application.properties`:
```properties
server.port=8081
```

Update frontend API URL in `src/services/api.js`:
```javascript
const API_URL = 'http://localhost:8081/api';
```

### Error: CORS Policy Error
**Solution:**
Verify `SecurityConfig.java` has correct frontend URL:
```java
config.setAllowedOrigins(List.of("http://localhost:3000"));
```

### Error: JWT Token Invalid
**Solution:**
1. Clear browser localStorage
2. Login again
3. Check token expiration (24 hours default)

### Error: npm install fails
**Solution:**
```bash
# Clear cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install
```

---

## Build & Run Commands

### Backend (Spring Boot)

**Using Maven:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Using IDE:**
- IntelliJ IDEA: Right-click `SmartDeskApplication.java` → Run
- Eclipse: Right-click project → Run As → Spring Boot App
- VS Code: Run → Start Debugging

### Frontend (React)

```bash
cd smart-helpdesk
npm install
npm start
```

---

## Verify Application is Running

### Backend Health Check
```bash
# Test if backend is running
curl http://localhost:8080/api/auth/login

# Should return 400 or 401 (means server is up)
```

### Frontend Check
Open browser: http://localhost:3000

---

## Database Verification

```sql
-- Connect to MySQL
mysql -u root -p

-- Check database exists
SHOW DATABASES;

-- Use database
USE helpdesk_db;

-- Check tables
SHOW TABLES;

-- Check users
SELECT * FROM users;
```

---

## IDE Setup

### IntelliJ IDEA
1. Open `backend` folder as project
2. Maven will auto-import dependencies
3. Right-click `SmartDeskApplication.java` → Run

### Eclipse
1. File → Import → Existing Maven Project
2. Select `backend` folder
3. Right-click project → Run As → Spring Boot App

### VS Code
1. Install extensions:
   - Spring Boot Extension Pack
   - Java Extension Pack
2. Open `backend` folder
3. Press F5 to run

---

## Testing the Application

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Create Ticket (use token from login)
```bash
curl -X POST http://localhost:8080/api/customer/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "issueType": "SOFTWARE",
    "description": "Test ticket",
    "priority": "HIGH"
  }'
```

---

## Logs Location

### Backend Logs
- Console output when running `mvn spring-boot:run`
- Or check IDE console

### Frontend Logs
- Browser console (F12)
- Terminal where `npm start` is running

---

## Reset Application

### Clear Database
```sql
USE helpdesk_db;
DROP TABLE comments;
DROP TABLE tickets;
DROP TABLE users;
```
Then restart backend (tables will be recreated)

### Clear Frontend Storage
```javascript
// In browser console
localStorage.clear();
```

---

## Production Deployment

### Backend
```bash
cd backend
mvn clean package
java -jar target/smartdesk-1.0.0.jar
```

### Frontend
```bash
cd smart-helpdesk
npm run build
# Deploy 'build' folder to web server
```

---

## Need More Help?

1. Check application logs
2. Verify all prerequisites are installed
3. Ensure MySQL is running
4. Check firewall settings
5. Review API_DOCUMENTATION.md for endpoint details
