# SmartQuote - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- Node.js 18+ and npm
- Java 17+
- MongoDB 4.4+
- Maven 3.8+

---

## 1. Start MongoDB

```bash
# Windows
net start MongoDB

# Mac/Linux
sudo systemctl start mongod

# Or use Docker
docker run -d -p 27017:27017 mongo:latest
```

---

## 2. Start Backend (Spring Boot)

```bash
cd backend-springboot

# Configure MongoDB (if needed)
# Edit src/main/resources/application.properties
# spring.data.mongodb.uri=mongodb://localhost:27017/smartquote

# Run
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

**Backend will start on:** `http://localhost:8080`

**Check health:** http://localhost:8080/actuator/health

---

## 3. Start Frontend (Angular)

```bash
cd frontend-angular

# Install dependencies (first time only)
npm install

# Run development server
ng serve

# Or
npm start
```

**Frontend will start on:** `http://localhost:4200`

---

## 4. Access Application

**Open Browser:** `http://localhost:4200`

### First Time Setup:
1. Click **"Sign Up"**
2. Fill business details:
   - Business Name
   - Owner Name
   - Email
   - Phone
   - Password
3. Click **"Sign Up"** - You'll be automatically signed in
4. Explore the dashboard!

---

## 📱 Quick Tour

### Dashboard (`/dashboard`)
- View all quotations
- See statistics
- Create new quotation
- Search and filter

### Quotations (`/quotations`)
- List all quotations
- Filter by status
- Generate PDF
- Edit or delete

### Customers (`/customers`)
- View customer list
- Search customers
- Manage contacts

### Profile (`/profile`)
- View business details
- Select default template
- Edit terms & conditions

### Templates (`/templates`)
- Browse quotation templates
- Preview designs
- Create quotation from template

---

## 🎯 Test the Application

### Create Your First Quotation:

1. **Go to Dashboard** → Click "New Quotation"
2. **Select Template** → Choose any template
3. **Fill Form:**
   - Select customer from dropdown (or add new)
   - Set quotation date
   - Set valid until date
   - Add items with description, quantity, rate
4. **Save:**
   - "Save as Draft" → Goes to quotation list
   - "Save & Preview" → Shows quotation preview

5. **Generate PDF:**
   - From preview page, click "Download PDF"
   - PDF opens in new tab

---

## 🔑 Default Test Credentials

After sign-up, use your credentials:
- Email: your-email@example.com
- Password: your-password

---

## 📊 API Endpoints

### Authentication
```
POST /api/auth/sign-up          # Register
POST /api/auth/sign-in          # Login (sends OTP)
POST /api/auth/verify-otp       # Verify OTP
GET  /api/auth/me               # Get profile
POST /api/auth/sign-out         # Logout
```

### Quotations
```
GET    /api/quotations          # List all
GET    /api/quotations/{id}     # Get single
POST   /api/quotations          # Create
PUT    /api/quotations/{id}     # Update
DELETE /api/quotations/{id}     # Delete
POST   /api/quotations/{id}/pdf # Generate PDF
```

### Customers
```
GET    /api/customers           # List all
GET    /api/customers/{id}      # Get single
POST   /api/customers           # Create
PUT    /api/customers/{id}      # Update
DELETE /api/customers/{id}      # Delete
```

### Templates
```
GET    /api/templates           # List all
GET    /api/templates/{id}      # Get single
```

---

## 🐛 Troubleshooting

### Backend won't start
**Error:** Port 8080 already in use  
**Solution:** Stop other apps on port 8080 or change port in `application.properties`

**Error:** MongoDB connection failed  
**Solution:** Ensure MongoDB is running on port 27017

### Frontend won't start
**Error:** Port 4200 already in use  
**Solution:** `ng serve --port 4300`

**Error:** Module not found  
**Solution:** `npm install`

### CORS errors in browser
**Solution:** Backend CORS already configured. Check `WebConfig.java`

### Can't sign in
**Check:**
1. Backend is running
2. MongoDB is running
3. User registered successfully
4. Check browser console for errors
5. Check backend logs

### PDF generation fails
**Check:**
1. Quotation has all required data
2. Backend PdfService is configured
3. Check backend logs for errors

---

## 🔧 Development Commands

### Backend
```bash
# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Run JAR
java -jar target/smartquote-backend.jar
```

### Frontend
```bash
# Run dev server
ng serve

# Build for production
ng build --configuration=production

# Run tests
ng test

# Run linter
ng lint
```

---

## 📁 Important Files

### Configuration
```
backend-springboot/src/main/resources/
  └── application.properties       # Backend config

frontend-angular/src/
  └── environments/
      ├── environment.ts            # Dev config
      └── environment.prod.ts       # Prod config
```

### Database
```
MongoDB Database: smartquote
Collections:
  - BUSINESSES      # Business profiles
  - USERS           # User accounts
  - QUOTATIONS      # Quotations
  - CONTACTS        # Customers/Contacts
  - INVENTORY       # Materials/Products
  - TEMPLATES       # Quotation templates
```

---

## 🎨 Key Features

✅ **Authentication:** Sign-up, Sign-in with OTP, Password reset  
✅ **Dashboard:** Real-time statistics, quotation management  
✅ **Quotations:** 5 template types, PDF generation  
✅ **Customers:** Full CRUD operations  
✅ **Templates:** Dynamic template selection  
✅ **Profile:** Business information management

---

## 📖 Documentation

- **Integration Guide:** `INTEGRATION_COMPLETE.md`
- **Remaining Tasks:** `REMAINING_TASKS.md`
- **Form Integration:** `FORM_INTEGRATION_GUIDE.md`
- **Project Status:** `PROJECT_STATUS.md`

---

## 🚀 Production Deployment

### Build Backend
```bash
cd backend-springboot
./mvnw clean package
# Output: target/smartquote-backend.jar
```

### Build Frontend
```bash
cd frontend-angular
ng build --configuration=production
# Output: dist/smartquote-frontend/
```

### Environment Variables
```properties
# Backend
MONGODB_URI=mongodb://your-mongo-uri
SERVER_PORT=8080
JWT_SECRET=your-secret-key

# Frontend
API_BASE_URL=https://your-api-domain.com
```

---

## 💡 Pro Tips

1. **Use Chrome DevTools** → Network tab to debug API calls
2. **Check Browser Console** for frontend errors
3. **Check Backend Logs** in terminal for backend errors
4. **Use Postman** to test APIs independently
5. **MongoDB Compass** for database visualization

---

## 🎓 Learning Resources

- **Angular:** https://angular.io/docs
- **Spring Boot:** https://spring.io/projects/spring-boot
- **MongoDB:** https://docs.mongodb.com/
- **Material Design:** https://material.angular.io/

---

## 📞 Support

For issues or questions:
1. Check documentation files in `/implementation/`
2. Review backend logs
3. Check browser console
4. Review API responses in Network tab

---

**Happy Coding! 🚀**

---

## Quick Command Reference

```bash
# Backend
cd backend-springboot && ./mvnw spring-boot:run

# Frontend
cd frontend-angular && ng serve

# MongoDB
docker run -d -p 27017:27017 mongo:latest

# Build
cd backend-springboot && ./mvnw package
cd frontend-angular && ng build --prod

# Test
cd backend-springboot && ./mvnw test
cd frontend-angular && ng test
```
