# SmartQuote API — Postman Testing Guide

Base URL: **`http://localhost:8080`**

> **Quick start:** Import `SmartQuote.postman_collection.json` into Postman (File → Import). It includes all requests below with sample bodies and auto-saves `token`, `businessId`, `contactId`, etc.

---

## Global conventions

| Item | Value |
|------|-------|
| Base URL | `http://localhost:8080` |
| JSON content type | `Content-Type: application/json` |
| Auth header (protected routes) | `Authorization: Bearer <token>` |
| Token source | Sign Up response, or Sign In → Verify OTP |
| Error format | `{ "message": "..." }` (400/409) |

**Protected controllers** require the Bearer token on every request:
Auth (`/me`, `/sign-out`), Contacts, Customers, Inventory, Materials, Templates, Quotations, WhatsApp Share, Voice.

**Public routes:** all other `/api/auth/**` endpoints.

---

## Recommended test order

```
1. Auth: Sign Up  (or Sign In → Dev OTP → Verify OTP)
2. Contacts: Create → List
3. Inventory: Create → List
4. Templates: List  (copy templateId)
5. Quotations: Create → Generate PDF
6. WhatsApp Share: Get Link → Log Share
7. Voice: Transcribe (optional, needs audio file + WHISPER_API_KEY)
```

---

## 1. AuthController — `/api/auth`

### POST `/api/auth/sign-up`
**Auth:** None  
**Body:**
```json
{
  "businessName": "Acme Traders",
  "ownerName": "John Doe",
  "email": "owner@acme.com",
  "phone": "9876543210",
  "password": "password123"
}
```
**Notes:** password min 8 chars. Returns `201` with `token`, `businessId`, `userId`.

---

### POST `/api/auth/sign-in`
**Auth:** None  
**Body:**
```json
{
  "email": "owner@acme.com",
  "password": "password123"
}
```
**Notes:** Step 1 — sends OTP. Does not return a token yet.

---

### GET `/api/auth/dev/otp?key={email}`
**Auth:** None  
**Query:** `key` = email used in sign-in (e.g. `owner@acme.com`)  
**Notes:** Dev-only. Returns `{ "otp": "123456" }`. Use before Verify OTP.

---

### POST `/api/auth/verify-otp`
**Auth:** None  
**Body:**
```json
{
  "email": "owner@acme.com",
  "otp": "123456"
}
```
**Notes:** Step 2 — returns session `token`.

---

### GET `/api/auth/me`
**Auth:** `Bearer <token>`  
**Body:** None

---

### POST `/api/auth/sign-out`
**Auth:** `Bearer <token>`  
**Body:** None  
**Response:** `204 No Content`

---

### POST `/api/auth/forgot-password`
**Auth:** None  
**Body:**
```json
{
  "email": "owner@acme.com"
}
```

---

### GET `/api/auth/dev/otp?key=reset:{email}`
**Auth:** None  
**Query:** `key=reset:owner@acme.com` (note the `reset:` prefix)

---

### POST `/api/auth/reset-password`
**Auth:** None  
**Body:**
```json
{
  "email": "owner@acme.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

---

## 2. ContactController — `/api/contacts`

All routes require `Authorization: Bearer <token>`.

| Method | URL | Body |
|--------|-----|------|
| GET | `/api/contacts` | — |
| GET | `/api/contacts/{id}` | — |
| POST | `/api/contacts` | See below |
| PUT | `/api/contacts/{id}` | Partial contact JSON |
| DELETE | `/api/contacts/{id}` | — (soft delete, status → deleted) |

**POST body example:**
```json
{
  "contactPerson": "Raj Kumar",
  "companyName": "Beta Industries",
  "email": "raj@beta.com",
  "phone": "9123456789",
  "gstType": "regular",
  "gstin": "27AABCU9603R1ZM",
  "address": "12 MG Road",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "pincode": "400001"
}
```
`gstType`: `regular` | `composition` | `unregistered`. `businessId` is auto-set from session.

---

## 3. CustomerController — `/api/customers`

**Alias of Contacts** — same data, same body shapes. Replace `/api/contacts` with `/api/customers` in the table above.

---

## 4. InventoryController — `/api/inventory`

All routes require `Authorization: Bearer <token>`.

| Method | URL | Body |
|--------|-----|------|
| GET | `/api/inventory` | — |
| GET | `/api/inventory/{id}` | — |
| POST | `/api/inventory` | See below |
| PUT | `/api/inventory/{id}` | Partial inventory JSON |
| DELETE | `/api/inventory/{id}` | — (soft delete) |

**POST body example:**
```json
{
  "itemName": "Premium Widget",
  "description": "High-quality widget",
  "hsnSacCode": "8471",
  "unitOfMeasurement": "pcs",
  "mrp": 1500.00,
  "sellingPrice": 1200.00,
  "taxPercentage": 18.00,
  "stockQuantity": 100
}
```

---

## 5. MaterialController — `/api/materials`

**Alias of Inventory** — same endpoints and bodies as section 4, with `/api/materials` prefix.

---

## 6. TemplateController — `/api/templates`

All routes require `Authorization: Bearer <token>`. Read-only in MVP.

| Method | URL |
|--------|-----|
| GET | `/api/templates` |
| GET | `/api/templates/{id}` |

Copy a template `id` from the list response for quotation creation.

---

## 7. QuotationController — `/api/quotations`

All routes require `Authorization: Bearer <token>`.

| Method | URL | Body |
|--------|-----|------|
| GET | `/api/quotations` | — |
| GET | `/api/quotations/{id}` | — |
| POST | `/api/quotations` | See below |
| PUT | `/api/quotations/{id}` | QuotationRequestDto |
| PATCH | `/api/quotations/{id}/status` | `{ "status": "sent" }` |
| DELETE | `/api/quotations/{id}` | — |
| POST | `/api/quotations/{id}/pdf` | — |

**POST body example:**
```json
{
  "businessId": "<your-businessId-from-auth>",
  "contactId": "<contact-id>",
  "templateId": "<template-id>",
  "quotationDate": "2026-07-03",
  "dueDate": "2026-07-17",
  "priority": "normal",
  "billingAddress": "12 MG Road, Mumbai",
  "deliveryAddress": "12 MG Road, Mumbai",
  "discountPercentage": 5.00,
  "items": [
    {
      "lineNumber": 1,
      "itemId": "<inventory-id>",
      "itemDescription": "Premium Widget",
      "hsnSacCode": "8471",
      "quantity": 10,
      "unit": "pcs",
      "rate": 1200.00,
      "taxPercentage": 18.00
    }
  ]
}
```

**Status values:** `draft`, `sent`, `accepted`, `rejected`, `expired`

**Generate PDF** returns:
```json
{ "pdfUrl": "http://localhost:8080/files/..." }
```

---

## 8. WhatsAppShareController — `/api/quotations/{quotationId}/share`

All routes require `Authorization: Bearer <token>`.

| Method | URL | Body / Query |
|--------|-----|--------------|
| POST | `/api/quotations/{quotationId}/share` | See below |
| GET | `/api/quotations/{quotationId}/share` | — (share history) |
| GET | `/api/quotations/{quotationId}/share/whatsapp-link?phone=9876543210` | query: `phone` |

**POST body:**
```json
{
  "channel": "whatsapp",
  "phone": "9876543210",
  "email": "customer@example.com"
}
```
`channel`: `whatsapp` | `email` | `sms` | `link`

**Note:** Generate PDF before calling `whatsapp-link`.

---

## 9. VoiceController — `/api/voice`

### POST `/api/voice/transcribe`
**Auth:** `Bearer <token>`  
**Content-Type:** `multipart/form-data` (not JSON)

| Form field | Type | Value |
|------------|------|-------|
| quotationId | text | quotation MongoDB id |
| fieldName | text | `itemDescription`, `quantity`, or `rate` |
| audio | file | `.webm`, `.mp3`, or `.wav` recording |

**Notes:** Set `WHISPER_API_KEY` env var for real transcription.

---

## Postman collection variables

After import, edit collection variables (or let test scripts fill them):

| Variable | Set by |
|----------|--------|
| `baseUrl` | Default `http://localhost:8080` |
| `token` | Sign Up / Verify OTP test scripts |
| `businessId` | Sign Up / Verify OTP |
| `email`, `password` | Your test credentials |
| `contactId` | Create Contact script |
| `inventoryId` | Create Inventory script |
| `templateId` | List Templates script |
| `quotationId` | Create Quotation script |
| `otp` | Dev Get OTP scripts |
