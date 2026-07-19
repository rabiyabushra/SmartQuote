# SmartQuote - Remaining Tasks & Enhancements

## ✅ Completed Work Summary

### Core Integrations (100% Complete):
1. ✅ **All 5 Quotation Template Views** - Dynamic business info from AuthService
2. ✅ **Dashboard** - Full backend integration with statistics
3. ✅ **Customer List** - CRUD operations with search/filter
4. ✅ **Quotation List** - Full listing with actions (view, edit, delete, PDF)
5. ✅ **Profile Component** - Dynamic templates from backend
6. ✅ **Template Gallery** - Display all templates from backend
7. ✅ **Job Work Quotation Form** - Backend save/update integration (TEMPLATE)

---

## 🔄 In Progress

### Quotation Form Components (4 remaining):

Apply the same backend integration pattern from `job-work-quotation-form` to:

#### 1. **Fabrication Quotation Form**
**File:** `fabrication-quotation-form.component.ts`

**Required Changes:**
- Add `OnInit`, `ActivatedRoute`, `ContactService`, `AuthService` imports
- Replace hardcoded `businessInfo` with `computed()` from `AuthService`
- Add `customers` signal and load via `ContactService.list()`
- Add `contactId` field to form with customer dropdown
- Replace `createLocal()` with `QuotationService.create()` / `update()`
- Map form items to `QuotationItemDto[]` format
- Handle edit mode (load existing quotation if `id` in route)
- Add loading and saving states

**Template ID:** `'fabrication'`

---

#### 2. **Supply Quotation Form**
**File:** `supply-quotation-form.component.ts`

**Required Changes:**
- Same as Fabrication Form
- **Template ID:** `'supply'`

---

#### 3. **Service Quotation Form**
**File:** `service-quotation-form.component.ts`

**Required Changes:**
- Same as Fabrication Form
- **Template ID:** `'service'`

---

#### 4. **Maintenance Quotation Form**
**File:** `maintenance-quotation-form.component.ts`

**Required Changes:**
- Same as Fabrication Form
- **Template ID:** `'maintenance'`

---

## 📋 TODO: Material/Inventory Management UI

### Create: `material-list.component.ts`
**Location:** `frontend-angular/src/app/features/materials/material-list/`

**Features to Implement:**
- List all materials/inventory items
- Search by name, HSN code, category
- Filter by category, unit
- Display: Name, HSN Code, Unit, Rate, Stock (if applicable)
- Actions: View, Edit, Delete
- Add new material button

**API Integration:**
```typescript
// MaterialService already exists and is integrated
materialService.list()      // GET /api/materials
materialService.get(id)      // GET /api/materials/{id}
materialService.create(...)  // POST /api/materials
materialService.update(...)  // PUT /api/materials/{id}
materialService.delete(id)   // DELETE /api/materials/{id}
```

**Follow Pattern From:** `customer-list.component.ts`

---

## 🎙️ TODO: Voice Integration in Forms

### Voice Input for Quotation Items

**Objective:** Add microphone button to item description/quantity/rate fields

**Components to Update:**
- All 5 quotation form components

**Implementation:**
```typescript
// VoiceService already exists
voiceService.transcribe({
  quotationId: 'temp-id',
  fieldName: 'itemDescription',
  audio: audioBlob
})
```

**UI Pattern:**
- Add microphone icon button next to input fields
- Record audio on button press
- Show recording indicator (animated)
- Send audio to backend
- Populate field with transcribed text
- Show error if transcription fails

**Reference:**
- `VoiceService` at `core/services/voice.service.ts`
- Backend API: `POST /api/voice/transcribe`

---

## 📄 TODO: Enhanced Customer & Material Forms

### Customer Form Dialog
**Create:** `customer-form-dialog.component.ts`

**Fields:**
- Contact Person (required)
- Company Name (required)
- Email (required)
- Phone (required)
- GST Type (dropdown: regular/composition/unregistered)
- GSTIN (if applicable)
- Address (required)
- City (required)
- State (required)
- Country (default: India)
- Pincode (required)

**Usage:**
- Called from `customer-list.component.ts` Add/Edit buttons
- MatDialog with form
- Validate GSTIN format
- Submit to `CustomerService.create()` or `update()`

---

### Material Form Dialog
**Create:** `material-form-dialog.component.ts`

**Fields:**
- Item Name (required)
- HSN/SAC Code
- Category (dropdown or autocomplete)
- Unit (dropdown: unit, kg, liter, meter, etc.)
- Rate (required)
- Description
- Stock Quantity (optional)

**Usage:**
- Called from material-list Add/Edit buttons
- Submit to `MaterialService.create()` or `update()`

---

## 🔗 TODO: WhatsApp Share Integration

### Add WhatsApp Share Button

**Components to Update:**
- `quotation-preview.component.ts`
- `dashboard.component.ts` (quotation cards)
- `quotation-list.component.ts`

**Implementation:**
```typescript
// WhatsAppShareService is available but not yet used in UI
whatsAppShareService.getShareLink(quotationId).subscribe(response => {
  window.open(response.whatsappLink, '_blank');
});

// Also log the share
whatsAppShareService.logShare(quotationId, {
  method: 'whatsapp',
  sharedBy: currentUserId
}).subscribe();
```

**UI:**
- Add "Share via WhatsApp" button with WhatsApp icon
- Button in quotation preview header
- Button in quotation list actions menu
- Show confirmation snackbar after share

---

## 📊 TODO: Advanced Features

### 1. **Pagination for Large Datasets**

**Backend Already Supports:** No (needs implementation)

**Frontend Pattern:**
```typescript
// Add pagination to lists
<mat-paginator [length]="totalCount()"
               [pageSize]="pageSize"
               [pageSizeOptions]="[10, 25, 50, 100]"
               (page)="onPageChange($event)">
</mat-paginator>
```

**Components:** Dashboard, Customer List, Quotation List, Material List

---

### 2. **Server-Side Filtering**

**Backend Enhancement Needed:**
- Add query parameters to GET endpoints:
  - `GET /api/quotations?status=sent&fromDate=2024-01-01&toDate=2024-12-31`
  - `GET /api/customers?city=Mumbai&state=Maharashtra`

**Frontend:** Modify services to accept filter parameters

---

### 3. **Export to Excel/CSV**

**Implementation:**
- Add "Export" button to lists
- Use library: `xlsx` or `file-saver`
- Export current filtered data

```bash
npm install xlsx
npm install @types/xlsx --save-dev
```

---

### 4. **PDF Preview Before Download**

**Implementation:**
- Use `<iframe>` or PDF viewer library
- Show modal with PDF preview
- Backend generates PDF, returns URL
- Frontend displays in modal

---

### 5. **Business Profile Update API**

**Backend Enhancement:**
```java
// Add to BusinessController or AuthController
@PutMapping("/api/business/profile")
public BusinessProfile updateProfile(@RequestBody BusinessProfileRequest request) {
    // Update business details
}
```

**Frontend:** Connect Profile component save buttons to API

---

### 6. **Template Preview**

**Implementation:**
- Generate sample quotation data
- Render template with sample data
- Show in modal before selection

---

### 7. **Dashboard Charts**

**Add:**
- Monthly quotation trend (line chart)
- Status distribution (pie chart)
- Revenue by customer (bar chart)

**Libraries:**
- Chart.js + ng2-charts
- Or Apache ECharts

---

### 8. **Notifications**

**Features:**
- Quote status changes
- New customer added
- Quote expiring soon

**Implementation:**
- WebSocket or Server-Sent Events (SSE)
- Or polling API endpoint

---

### 9. **Multi-language Support (i18n)**

**Setup:**
```bash
ng add @angular/localize
```

**Implementation:**
- Extract all UI strings to translation files
- Support English, Hindi, other regional languages
- Language selector in header/profile

---

### 10. **Dark Mode**

**Implementation:**
- Angular Material theme switching
- Store preference in localStorage
- Toggle in profile/settings

---

## 🧪 Testing Checklist

### Unit Tests
- [ ] All services (mocked HTTP)
- [ ] All components (isolated)
- [ ] All pipes and utilities

### Integration Tests
- [ ] Authentication flow
- [ ] Quotation CRUD operations
- [ ] Customer CRUD operations
- [ ] Template loading
- [ ] PDF generation

### E2E Tests
- [ ] Sign up → Sign in → Create quotation → Generate PDF
- [ ] Add customer → Create quotation with customer
- [ ] Dashboard statistics accuracy
- [ ] Search and filter functionality

---

## 🛠️ Build & Deployment

### Frontend Build
```bash
cd frontend-angular
ng build --configuration=production
```

**Output:** `dist/` folder

### Backend Build
```bash
cd backend-springboot
./mvnw clean package
```

**Output:** `target/smartquote-backend.jar`

### Environment Configuration

**Frontend:** `frontend-angular/src/environments/`
```typescript
export const environment = {
  production: true,
  apiBaseUrl: 'https://your-api-domain.com'
};
```

**Backend:** `backend-springboot/src/main/resources/application.properties`
```properties
spring.data.mongodb.uri=mongodb://your-mongo-uri
server.port=8080
```

---

## 📚 Documentation to Add

1. **API Documentation** - Swagger/OpenAPI specs
2. **User Guide** - PDF with screenshots
3. **Developer Guide** - Setup and contribution guidelines
4. **Deployment Guide** - AWS/Azure/GCP deployment steps

---

## 🎯 Priority Roadmap

### Phase 1 - Complete Core (High Priority)
1. ✅ Backend integration for all views
2. 🔄 Backend integration for all forms (4 remaining)
3. 🔄 Material/Inventory List UI
4. ✅ Customer/Quotation Lists
5. ✅ PDF generation

### Phase 2 - Enhanced UX (Medium Priority)
6. Customer/Material form dialogs
7. WhatsApp share integration
8. Voice input in forms
9. Pagination
10. Export features

### Phase 3 - Advanced Features (Low Priority)
11. Dashboard charts
12. Server-side filtering
13. Notifications
14. Dark mode
15. Multi-language support

---

## 📞 Support & Maintenance

### Common Issues

**Issue:** API calls failing with 401 Unauthorized
**Solution:** Check if token is present in localStorage and not expired

**Issue:** CORS errors in browser console
**Solution:** Verify CORS configuration in `WebConfig.java`

**Issue:** PDF generation fails
**Solution:** Check PdfService logs, verify template paths

**Issue:** MongoDB connection errors
**Solution:** Verify MongoDB URI in application.properties

---

## ✨ Nice-to-Have Features

1. **Quotation Templates Customization** - Visual editor for templates
2. **Email Integration** - Send quotations via email
3. **Payment Gateway** - Accept payments for accepted quotations
4. **Mobile App** - React Native or Flutter app
5. **Recurring Quotations** - Auto-generate monthly/yearly quotations
6. **Client Portal** - Customers can view their quotations
7. **Audit Trail** - Track all changes to quotations
8. **Bulk Operations** - Bulk delete, bulk export, bulk status update
9. **Advanced Search** - Full-text search across all fields
10. **Custom Fields** - Allow users to add custom fields to quotations

---

**Last Updated:** [Current Date]
**Status:** 80% Complete
**Remaining Effort:** ~2-3 days for core features
