# SmartQuote – Voice-Ready Quotation Builder for MSMEs

SmartQuote is a full-stack, enterprise-grade quotation management and generation platform tailored specifically for Indian Micro, Small, and Medium Enterprises (MSMEs) in manufacturing, engineering, job work, and services.

It streamlines the entire commercial quotation lifecycle: from voice-assisted line-item entry to automated dual-mode GST calculation (intra-state CGST+SGST vs. inter-state IGST), strict XHTML PDF document generation, native WhatsApp quotation dispatch, and real-time quotation status tracking.

---

## 1. Key Features

- **5 Predefined MSME Quotation Templates:**
  1. *Fabrication Work Quotation* (Structural steel, fabrication, industrial enclosures)
  2. *Job Work Quotation* (Machining, CNC turning, surface treatment, Section 143 CGST compliance)
  3. *Service Quotation* (Consulting, engineering services, milestone billing, SAC 9983)
  4. *Supply Quotation* (Commercial goods supply, catalogue items, trade distribution)
  5. *Maintenance Quotation* (Annual Maintenance Contracts / AMC, quarterly visits, SLA support)
- **Voice-Assisted Field Entry:**
  - Dedicated microphone button beside line item description, quantity, and rate fields.
  - Native browser Web Speech API for instantaneous real-time transcription with graceful server-side audio upload fallback.
  - Number normalization utility converting spoken phrases (e.g., "twenty five pieces at fifteen hundred rupees") to structured numeric values.
- **Authoritative GST Financial Calculation Engine:**
  - Automated determination of intra-state (`CGST` + `SGST` equally split) vs inter-state (`IGST` full rate) by comparing business state and customer destination state.
  - High-precision financial arithmetic using `BigDecimal` (`HALF_UP` rounding to 2 decimal places).
  - Handles line-item discounts, missing fields, zero quantities, and mixed tax rates across items.
- **Strict XHTML-Compliant PDF Engine:**
  - Powered by Thymeleaf templates and `OpenHTMLToPDF` (Flying Saucer).
  - Clean page-break handling, corporate branding, client billing snapshot, terms and conditions, bank details, and signature blocks.
  - Streamed directly via `/files/quotations/{id}.pdf` with local and cloud-extensible storage support.
- **Native WhatsApp Sharing & Status Lifecycle:**
  - Builds pre-formatted WhatsApp Web and native mobile share intents with quotation reference and PDF access link.
  - Full status lifecycle management: `draft` ➔ `sent` ➔ `accepted` / `rejected`.
- **Customer & Business Profile Management:**
  - Comprehensive customer directory with add, edit, search, and soft-delete capabilities.
  - Business profile persistence including GSTIN, company address, bank account details, and custom logo.

---

## 2. Technology Stack

### Frontend
- **Framework:** Angular 19 (Standalone Components, Signals, Computed properties)
- **Language:** TypeScript 5.7
- **UI & Components:** Angular Material 19, Material Icons, Responsive SCSS
- **Forms & Validation:** Reactive Forms & Template-driven forms
- **HTTP Client:** Angular `HttpClient` with session bearer `authInterceptor`
- **Speech Recognition:** Web Speech API (`webkitSpeechRecognition` / `SpeechRecognition`) with MediaRecorder fallback

### Backend
- **Platform:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4
- **Security:** Spring Security with BCrypt password hashing and custom session token authentication
- **Data Persistence:** Spring Data MongoDB
- **PDF Generation:** OpenHTMLToPDF (`openhtmltopdf-pdfbox` 1.0.10) & Thymeleaf 3.1
- **Validation:** Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`)
- **Speech-to-Text Integration:** Whisper REST client with retry logic and non-configured fallback handler

### Database
- **Database:** MongoDB 7.0+ (Local or MongoDB Atlas)
- **Primary Collections:**
  - `USERS` – User accounts, authentication credentials, and session tokens
  - `BUSINESSES` – Enterprise profiles, GSTIN, and company addresses
  - `CONTACTS` – Customer / client directory records
  - `QUOTATIONS` – Commercial quotations with embedded line items and GST breakdown
  - `QUOTATION_TEMPLATES` – System and custom quotation templates
  - `BANK_DETAILS` – Bank account and remittance details
  - `QUOTATION_SHARE_LOGS` – Audit trail for WhatsApp and email dispatch actions

---

## 3. Project Directory Structure

```
smartquote/
├── backend-springboot/               # Spring Boot 3.4 backend
│   ├── pom.xml                       # Maven build configuration
│   ├── src/main/java/com/smartquote/
│   │   ├── config/                   # MongoDB, Security, TemplateInitializer
│   │   ├── controller/               # REST API Controllers (Auth, Quotation, Business, etc.)
│   │   ├── dto/                      # Data Transfer Objects & Requests
│   │   ├── model/                    # MongoDB Document Entities
│   │   ├── repository/               # Spring Data Mongo Repositories
│   │   └── service/                  # Business logic & GST / PDF services
│   └── src/main/resources/
│       ├── application.yml           # Application configuration & profiles
│       └── templates/pdf/            # Thymeleaf XHTML PDF templates
│           ├── fabrication_work_quotation.html
│           ├── job_work_quotation.html
│           ├── maintenance_quotation.html
│           ├── service_quotation.html
│           └── supply_quotation.html
├── frontend-angular/                 # Angular 19 frontend
│   ├── package.json                  # NPM dependencies and scripts
│   ├── angular.json                  # Angular CLI configuration
│   └── src/
│       ├── app/
│       │   ├── core/                 # Auth, Guards, Interceptors, Services, Models
│       │   ├── features/             # Feature modules (Auth, Dashboard, Quotations, Customers, Profile)
│       │   └── shared/               # Reusable components (MicButton, VoiceQuotationMic)
│       ├── environments/             # Environment configs (dev/prod API URLs)
│       └── styles.scss               # Global stylesheet & Material themes
└── README.md                         # Project documentation
```

---

## 4. Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK):** Version 21 or higher (`java -version`)
- **Apache Maven:** Version 3.9 or higher (`mvn -version`)
- **Node.js:** Version 20 LTS or Version 22 LTS (`node -v`)
- **NPM:** Version 10 or higher (`npm -v`)
- **MongoDB Server:** Version 6.0 or higher running locally on port `27017` (`mongod`)

---

## 5. Installation & Setup

### Step 1: Clone the Repository
```bash
git clone https://github.com/rabiyabushra/SmartQuote.git
cd SmartQuote
```

### Step 2: Database Setup
Ensure MongoDB service is active. On Windows:
```powershell
net start MongoDB
```
Or start MongoDB manually:
```bash
mongod --dbpath <path-to-data-directory>
```
*Note: On first startup, the backend will automatically seed the 5 standard quotation templates (`fabrication`, `job-work`, `service`, `supply`, `maintenance`) into MongoDB.*

### Step 3: Backend Configuration
Configure `backend-springboot/src/main/resources/application.yml` or set environment variables:
```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/smartquote
  mail:
    username: ${GMAIL_USERNAME:noreply@smartquote.com}
    password: ${GMAIL_PASSWORD:}

app:
  auth:
    token-expiry-days: 30

storage:
  local:
    base-dir: ./storage
  base-url: http://localhost:8080/files
```

### Step 4: Frontend Configuration
Check `frontend-angular/src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080'
};
```

---

## 6. Running the Application

### 1. Start the Spring Boot Backend
Open a terminal in `backend-springboot/`:
```bash
cd backend-springboot
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`.

### 2. Start the Angular Frontend
Open a separate terminal in `frontend-angular/`:
```bash
cd frontend-angular
npm install
npm start
```
The frontend application will be available at `http://localhost:4200`.

---

## 7. REST API Endpoints Overview

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/sign-up` | Create business and admin user account | No |
| `POST` | `/api/auth/sign-in` | Validate email/password and send OTP | No |
| `POST` | `/api/auth/verify-otp` | Verify OTP and establish 30-day session | No |
| `GET` | `/api/auth/me` | Fetch currently authenticated user profile | Yes |
| `GET` | `/api/business/profile` | Retrieve business details (GSTIN, address) | Yes |
| `PUT` | `/api/business/profile` | Update business profile details | Yes |
| `GET` | `/api/customers` | List all customer contacts for business | Yes |
| `POST` | `/api/customers` | Create new customer contact | Yes |
| `PUT` | `/api/customers/{id}` | Update existing customer contact | Yes |
| `DELETE` | `/api/customers/{id}` | Soft delete customer contact | Yes |
| `GET` | `/api/templates` | List all predefined quotation templates | Yes |
| `GET` | `/api/templates/{id}` | Get quotation template by ID or slug | Yes |
| `GET` | `/api/quotations` | List all quotations for current business | Yes |
| `POST` | `/api/quotations` | Create new quotation & run GST calculation | Yes |
| `GET` | `/api/quotations/{id}` | Get quotation by ID | Yes |
| `PUT` | `/api/quotations/{id}` | Update quotation & re-run GST calculation | Yes |
| `PATCH` | `/api/quotations/{id}/status` | Update status (`draft`, `sent`, `accepted`, `rejected`) | Yes |
| `DELETE` | `/api/quotations/{id}` | Soft delete quotation | Yes |
| `POST` | `/api/quotations/{id}/pdf` | Generate and store XHTML PDF document | Yes |
| `GET` | `/files/quotations/{fileName}` | Stream PDF document inline or for download | No |
| `POST` | `/api/voice/transcribe` | Transcribe voice recording audio | Yes |
| `POST` | `/api/whatsapp/share-link` | Generate native WhatsApp wa.me share link | Yes |

---

## 8. Testing Instructions

### Run Backend Tests
Run the complete JUnit test suite:
```bash
cd backend-springboot
mvn test
```
*All 23 backend unit and integration tests execute against Spring Boot test slice and pass with 0 failures.*

### Run Frontend Unit Tests
Run Karma / Chrome headless test suite:
```bash
cd frontend-angular
npm run test:ci
```

### Build Frontend Production Bundle
```bash
cd frontend-angular
npm run build
```
Generates an optimized, warning-free distribution bundle in `frontend-angular/dist/`.

---

## 9. Common Troubleshooting

1. **MongoDB Connection Refused (`localhost:27017`):**
   - Ensure the MongoDB daemon is active (`net start MongoDB` on Windows or `sudo systemctl start mongod` on Linux).
2. **Mail Server Startup Warning:**
   - Fallback defaults (`noreply@smartquote.com`) are configured in `application.yml` so an external SMTP server is not required for local development.
3. **Microphone Access Blocked:**
   - Voice input requires browser microphone permission. When prompted in Chrome/Edge, allow access to microphone.
4. **CORS Errors:**
   - The backend CORS configuration is set to allow `http://localhost:4200` by default. If using another port, adjust `CorsConfiguration` in `backend-springboot`.

---

## 10. License & Attribution
Developed for MSMEs by Suktha Solutions. Distributed for demonstration and commercial quotation generation.
