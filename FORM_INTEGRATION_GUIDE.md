# Quotation Form Backend Integration Guide

## Pattern: Job Work Form (✅ Complete Reference)

The `job-work-quotation-form.component.ts` has been fully integrated with backend APIs. Use it as a reference template for the remaining 4 forms.

---

## 🔄 Apply to Remaining Forms

### 1. Fabrication Quotation Form
### 2. Supply Quotation Form
### 3. Service Quotation Form
### 4. Maintenance Quotation Form

---

## Step-by-Step Integration Pattern

### Step 1: Update Imports

```typescript
import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { QuotationService } from '../../../../../core/services/quotation.service';
import { ContactService } from '../../../../../core/services/contact.service';
import { AuthService } from '../../../../../core/auth/auth.service';
import { QuotationRequest, QuotationItemDto } from '../../../../../core/models/quotation-backend.model';
```

---

### Step 2: Add Services & State

```typescript
export class YourQuotationFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);  // ← ADD
  private snackBar = inject(MatSnackBar);
  private quotationService = inject(QuotationService);
  private contactService = inject(ContactService);  // ← ADD
  private authService = inject(AuthService);  // ← ADD

  // State signals
  isLoading = signal(false);  // ← ADD
  isSaving = signal(false);  // ← ADD
  editMode = signal(false);  // ← ADD
  quotationId = signal<string | null>(null);  // ← ADD
  customers = signal<any[]>([]);  // ← ADD
```

---

### Step 3: Replace Hardcoded Business Info

**REMOVE:**
```typescript
businessInfo = signal({
  businessName: 'Sharma Fabrication Works',
  // ... hardcoded values
});
```

**REPLACE WITH:**
```typescript
// Get business info from authenticated user
businessInfo = computed(() => {
  const user = this.authService.currentUser();
  return {
    businessName: user?.businessName || '—',
    streetAddress: user?.address?.split(',')[0] || '—',
    cityPostal: user?.address?.split(',').slice(1).join(',').trim() || '—',
    gstin: user?.gstin || '—',
    contact: user?.phone || user?.email || '—',
    businessId: user?.businessId || ''
  };
});
```

---

### Step 4: Add Customer Dropdown to Form

**UPDATE FORM:**
```typescript
form: FormGroup = this.fb.group({
  // Remove: quoteNumber (backend generates this)
  quoteDate: [new Date(), Validators.required],
  validUntil: [null, Validators.required],
  
  // ADD customer selection
  contactId: ['', Validators.required],
  
  // Keep existing customer fields (will be auto-filled)
  customerName: ['', Validators.required],
  customerAddress: ['', Validators.required],
  contactNumber: ['', Validators.required],
  
  // ... rest of your form fields
  items: this.fb.array([this.createItemRow()]),
  termsAndConditions: ['...']
});
```

---

### Step 5: Implement ngOnInit

```typescript
ngOnInit(): void {
  this.loadCustomers();
  
  // Check if we're editing an existing quotation
  const id = this.route.snapshot.paramMap.get('id');
  if (id) {
    this.editMode.set(true);
    this.quotationId.set(id);
    this.loadQuotation(id);
  }
}
```

---

### Step 6: Load Customers

```typescript
private loadCustomers(): void {
  this.contactService.list().subscribe({
    next: (contacts) => {
      this.customers.set(contacts);
    },
    error: (error) => {
      console.error('Error loading customers:', error);
      this.snackBar.open('Failed to load customers', 'Close', { duration: 3000 });
    }
  });
}
```

---

### Step 7: Load Existing Quotation (for Edit Mode)

```typescript
private loadQuotation(id: string): void {
  this.isLoading.set(true);
  this.quotationService.get(id).subscribe({
    next: (quotation) => {
      // Populate form with quotation data
      this.form.patchValue({
        quoteDate: new Date(quotation.quotationDate),
        validUntil: quotation.dueDate ? new Date(quotation.dueDate) : null,
        contactId: quotation.contactId,
        // TODO: Map your specific form fields
      });

      // Populate items
      this.items.clear();
      quotation.items.forEach(item => {
        this.items.push(this.fb.group({
          description: [item.itemDescription, Validators.required],
          // Map to your form fields
          qty: [item.quantity, [Validators.required, Validators.min(1)]],
          rate: [item.rate, [Validators.required, Validators.min(0)]]
        }));
      });

      this.isLoading.set(false);
    },
    error: (error) => {
      this.isLoading.set(false);
      this.snackBar.open('Failed to load quotation', 'Close', { duration: 5000 });
    }
  });
}
```

---

### Step 8: Customer Selection Handler

```typescript
onCustomerSelect(contactId: string): void {
  const customer = this.customers().find(c => c.id === contactId);
  if (customer) {
    this.form.patchValue({
      customerName: customer.companyName || customer.contactPerson,
      customerAddress: `${customer.address}, ${customer.city}, ${customer.state}`,
      contactNumber: customer.phone
    });
  }
}
```

---

### Step 9: Update Save Methods

**REMOVE:**
```typescript
saveDraft(): void {
  const id = this.quotationService.createLocal('template-id', this.form.value);
  // ...
}
```

**REPLACE WITH:**
```typescript
saveDraft(): void {
  this.saveQuotation('draft');
}

saveAndPreview(): void {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    this.snackBar.open('Please fill all required fields', 'OK', { duration: 2500 });
    return;
  }
  this.saveQuotation('sent');
}

private saveQuotation(status: 'draft' | 'sent'): void {
  if (this.isSaving()) return;

  this.isSaving.set(true);
  const formValue = this.form.value;

  // Map form data to backend DTO
  const quotationRequest: QuotationRequest = {
    businessId: this.businessInfo().businessId,
    contactId: formValue.contactId,
    templateId: 'YOUR-TEMPLATE-ID', // ← CHANGE THIS
    quotationDate: formValue.quoteDate.toISOString().split('T')[0],
    dueDate: formValue.validUntil ? formValue.validUntil.toISOString().split('T')[0] : undefined,
    priority: 'medium',
    items: formValue.items.map((item: any, index: number): QuotationItemDto => ({
      lineNumber: index + 1,
      itemDescription: item.description, // ← MAP TO YOUR FIELD
      hsnSacCode: item.sacCode || item.hsnCode || '', // ← ADJUST
      quantity: Number(item.qty), // ← MAP TO YOUR FIELD
      unit: 'unit', // ← ADJUST IF NEEDED
      rate: Number(item.rate) || Number(item.price) || Number(item.unitPrice), // ← ADJUST
      taxPercentage: 18 // Default GST
    }))
  };

  const saveObservable = this.editMode()
    ? this.quotationService.update(this.quotationId()!, quotationRequest)
    : this.quotationService.create(quotationRequest);

  saveObservable.subscribe({
    next: (response) => {
      this.isSaving.set(false);
      
      // Update status if needed
      if (status === 'sent') {
        this.quotationService.updateStatus(response.id, 'sent').subscribe({
          next: () => {
            this.snackBar.open('Quotation saved and sent', 'OK', { duration: 2000 });
            this.router.navigate(['/quotations', response.id, 'preview']);
          },
          error: () => {
            this.snackBar.open('Quotation saved', 'OK', { duration: 2000 });
            this.router.navigate(['/quotations', response.id, 'preview']);
          }
        });
      } else {
        this.snackBar.open('Saved as draft', 'OK', { duration: 2000 });
        this.router.navigate(['/quotations']);
      }
    },
    error: (error) => {
      this.isSaving.set(false);
      const message = error.error?.message || 'Failed to save quotation';
      this.snackBar.open(message, 'Close', { duration: 5000 });
      console.error('Error saving quotation:', error);
    }
  });
}
```

---

## Template IDs for Each Form

| Form | Template ID |
|------|-------------|
| Job Work | `'job-work'` |
| Fabrication | `'fabrication'` |
| Supply | `'supply'` |
| Service | `'service'` |
| Maintenance | `'maintenance'` |

---

## HTML Template Updates

### Add Customer Dropdown

**Add to your form HTML:**
```html
<mat-form-field appearance="outline">
  <mat-label>Select Customer</mat-label>
  <mat-select formControlName="contactId" (selectionChange)="onCustomerSelect($event.value)">
    <mat-option *ngFor="let customer of customers()" [value]="customer.id">
      {{ customer.companyName || customer.contactPerson }}
    </mat-option>
  </mat-select>
  <mat-error *ngIf="form.get('contactId')?.hasError('required')">
    Customer is required
  </mat-error>
</mat-form-field>
```

### Add Loading Spinner

**Wrap form in loading check:**
```html
<div *ngIf="!isLoading(); else loading">
  <!-- Your existing form -->
</div>

<ng-template #loading>
  <div class="loading-container">
    <mat-spinner diameter="50"></mat-spinner>
    <p>Loading...</p>
  </div>
</ng-template>
```

### Disable Save Buttons While Saving

```html
<button mat-button (click)="saveDraft()" [disabled]="isSaving()">
  <mat-spinner *ngIf="isSaving()" diameter="20"></mat-spinner>
  <span *ngIf="!isSaving()">Save as Draft</span>
</button>

<button mat-raised-button color="primary" (click)="saveAndPreview()" [disabled]="isSaving()">
  <mat-spinner *ngIf="isSaving()" diameter="20"></mat-spinner>
  <span *ngIf="!isSaving()">Save & Preview</span>
</button>
```

---

## Item Field Mapping Reference

### Common Field Names Across Forms:

| Your Form Field | Backend DTO Field |
|----------------|-------------------|
| `description` | `itemDescription` |
| `hsnCode` / `sacCode` | `hsnSacCode` |
| `qty` / `quantity` | `quantity` |
| `rate` / `price` / `unitPrice` / `unitRate` | `rate` |
| `unit` | `unit` |

### Form-Specific Adjustments:

**Fabrication Form:**
- May have `materialItems` and `laborItems` arrays
- Map both to single `items` array with different descriptions

**Supply Form:**
- Field: `unitPrice` → DTO: `rate`

**Service Form:**
- Field: `resources` (number of resources)
- Can be mapped to `quantity`

**Maintenance Form:**
- Field: `unitRate` → DTO: `rate`

---

## Testing Checklist for Each Form

After integration, test:

1. ✅ Form loads without errors
2. ✅ Business info displays from AuthService
3. ✅ Customer dropdown populates
4. ✅ Selecting customer auto-fills fields
5. ✅ Add/remove item rows work
6. ✅ Tax calculation displays correctly
7. ✅ Validation shows errors when required fields empty
8. ✅ Save as draft redirects to quotations list
9. ✅ Save & preview creates quotation and shows preview
10. ✅ Edit mode loads existing quotation data
11. ✅ Update saves changes correctly
12. ✅ Error messages display on save failure

---

## Common Issues & Solutions

### Issue: `businessInfo().businessId` is undefined
**Solution:** Ensure user is authenticated and `AuthService.currentUser()` returns data with `businessId`

### Issue: Customer dropdown empty
**Solution:** Check network tab for `/api/customers` or `/api/contacts` API call. Verify backend is running.

### Issue: Save fails with 400 Bad Request
**Solution:** Check that all required fields in `QuotationRequest` are populated. Verify `businessId` and `contactId` are not empty.

### Issue: Items not mapping correctly
**Solution:** Review your item form field names and adjust mapping in `saveQuotation()` method. Use exact field names from your form.

### Issue: Date format errors
**Solution:** Ensure dates are converted to ISO string format: `formValue.date.toISOString().split('T')[0]`

---

## Quick Implementation Checklist

For each form component:

- [ ] Add imports (OnInit, ActivatedRoute, services)
- [ ] Add state signals (isLoading, isSaving, etc.)
- [ ] Replace hardcoded businessInfo with computed from AuthService
- [ ] Add customers signal
- [ ] Add contactId field to form
- [ ] Implement ngOnInit
- [ ] Implement loadCustomers()
- [ ] Implement loadQuotation() for edit mode
- [ ] Implement onCustomerSelect()
- [ ] Update saveDraft() and saveAndPreview()
- [ ] Implement saveQuotation() with correct templateId
- [ ] Map item fields correctly in QuotationRequest
- [ ] Add customer dropdown to HTML template
- [ ] Add loading spinner to HTML
- [ ] Add saving state to save buttons
- [ ] Test create and edit flows
- [ ] Test validation and error handling

---

**Estimated Time Per Form:** 30-45 minutes
**Total for 4 Forms:** ~2-3 hours

**Reference File:** `job-work-quotation-form.component.ts` (already completed)
