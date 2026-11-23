import { Component, OnInit, Input, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { QuotationItemRowComponent, QuotationItemRowValue } from '../quotation-item-row/quotation-item-row.component';
import { QuotationPreviewComponent } from '../quotation-preview/quotation-preview.component';
import { VoiceQuotationMicComponent } from '../../../shared/components/voice-quotation-mic/voice-quotation-mic.component';
import { ParsedQuotationResponse } from '../../../core/models/parsed-quotation.model';
import { QuotationService } from '../../../core/services/quotation.service';
import { CustomerService, Customer } from '../../../core/services/customer.service';
import { TemplateService, QuotationTemplate } from '../../../core/services/template.service';
import { QuotationRequest, QuotationItemDto } from '../../../core/models/quotation-backend.model';

export interface ItemRowModel {
  lineNumber: number;
  itemDescription: string;
  quantity: number | null;
  rate: number | null;
  unit: string;
  hsnSacCode?: string;
}

@Component({
  selector: 'app-quotation-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    QuotationItemRowComponent,
    QuotationPreviewComponent,
    VoiceQuotationMicComponent
  ],
  templateUrl: './quotation-form.component.html',
  styleUrl: './quotation-form.component.scss'
})
export class QuotationFormComponent implements OnInit {
  private quotationService = inject(QuotationService);
  private customerService = inject(CustomerService);
  private templateService = inject(TemplateService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  @Input() templateId?: string;
  @Input() id?: string;

  quotationId = signal<string>('QT-' + Math.floor(100000 + Math.random() * 900000));
  selectedContactId = signal<string>('');
  customerName = signal<string>('Ravi Traders');
  customerPhone = signal<string>('+91 98765 43210');
  customerState = signal<string>('');
  selectedTemplateId = signal<string>('fabrication');
  quotationDate = signal<string>(new Date().toISOString().substring(0, 10));
  dueDate = signal<string>('');
  gstRate = signal<number>(18);
  discountPercentage = signal<number>(0);
  isReviewMode = signal<boolean>(false);
  isSaving = signal<boolean>(false);
  parseErrorMessage = signal<string>('');

  customers = signal<Customer[]>([]);
  templates = signal<QuotationTemplate[]>([]);

  items = signal<ItemRowModel[]>([
    { lineNumber: 1, itemDescription: 'Stainless Steel Fabrication Job', quantity: 10, rate: 1500, unit: 'pcs', hsnSacCode: '7308' },
    { lineNumber: 2, itemDescription: 'Assembly and Surface Finishing', quantity: 1, rate: 2500, unit: 'job', hsnSacCode: '9988' }
  ]);

  subtotal = computed(() => {
    return this.items().reduce((sum, item) => {
      const qty = item.quantity ?? 0;
      const rate = item.rate ?? 0;
      return sum + (qty * rate);
    }, 0);
  });

  discountAmount = computed(() => {
    return (this.subtotal() * (this.discountPercentage() || 0)) / 100;
  });

  gstAmount = computed(() => {
    const discounted = this.subtotal() - this.discountAmount();
    return (discounted * this.gstRate()) / 100;
  });

  grandTotal = computed(() => {
    return this.subtotal() - this.discountAmount() + this.gstAmount();
  });

  ngOnInit(): void {
    if (this.templateId) {
      this.selectedTemplateId.set(this.templateId);
    }

    this.loadCustomers();
    this.loadTemplates();

    if (this.id) {
      this.loadExistingQuotation(this.id);
    }
  }

  loadCustomers(): void {
    this.customerService.list().subscribe({
      next: (data) => this.customers.set(data),
      error: () => {}
    });
  }

  loadTemplates(): void {
    this.templateService.list().subscribe({
      next: (data) => this.templates.set(data),
      error: () => {}
    });
  }

  loadExistingQuotation(quoteId: string): void {
    this.quotationService.get(quoteId).subscribe({
      next: (q) => {
        this.quotationId.set(q.quotationNumber || q.id);
        this.selectedContactId.set(q.contactId || '');
        if (q.templateId) this.selectedTemplateId.set(q.templateId);
        if (q.quotationDate) this.quotationDate.set(q.quotationDate);
        if (q.dueDate) this.dueDate.set(q.dueDate);
        if (q.discountPercentage) this.discountPercentage.set(q.discountPercentage);

        if (q.items && q.items.length > 0) {
          this.items.set(q.items.map((item, idx) => ({
            lineNumber: idx + 1,
            itemDescription: item.itemDescription,
            quantity: item.quantity,
            rate: item.rate,
            unit: item.unit || 'pcs',
            hsnSacCode: item.hsnSacCode
          })));
        }
      },
      error: (err) => console.error('Failed to load existing quotation', err)
    });
  }

  onCustomerSelect(contactId: string): void {
    this.selectedContactId.set(contactId);
    const selected = this.customers().find(c => c.id === contactId);
    if (selected) {
      this.customerName.set(selected.companyName || selected.contactPerson);
      this.customerPhone.set(selected.phone || '');
      this.customerState.set(selected.state || '');
    }
  }

  onQuotationParsed(parsed: ParsedQuotationResponse): void {
    this.parseErrorMessage.set('');

    if (parsed.customerName) {
      this.customerName.set(parsed.customerName);
    }
    if (parsed.customerPhone) {
      this.customerPhone.set(parsed.customerPhone);
    }
    if (parsed.taxPercentage !== undefined && parsed.taxPercentage !== null) {
      this.gstRate.set(parsed.taxPercentage);
    }

    if (parsed.items && parsed.items.length > 0) {
      const newRows: ItemRowModel[] = parsed.items.map((item, idx) => ({
        lineNumber: idx + 1,
        itemDescription: item.itemDescription,
        quantity: item.quantity,
        rate: item.rate,
        unit: item.unit || 'pcs'
      }));
      this.items.set(newRows);
    }

    this.isReviewMode.set(true);
  }

  onParseError(error: string): void {
    this.parseErrorMessage.set(error);
  }

  addItemRow(): void {
    const current = this.items();
    const nextLineNumber = current.length > 0 ? Math.max(...current.map(i => i.lineNumber)) + 1 : 1;
    this.items.set([
      ...current,
      { lineNumber: nextLineNumber, itemDescription: '', quantity: null, rate: null, unit: 'pcs' }
    ]);
  }

  onItemChange(val: QuotationItemRowValue): void {
    this.items.update(rows =>
      rows.map(r => r.lineNumber === val.lineNumber ? { ...r, ...val } : r)
    );
  }

  onRemoveItem(lineNumber: number): void {
    this.items.update(rows => {
      const filtered = rows.filter(r => r.lineNumber !== lineNumber);
      return filtered.map((item, index) => ({ ...item, lineNumber: index + 1 }));
    });
  }

  toggleReviewMode(): void {
    this.isReviewMode.update(v => !v);
  }

  saveQuotation(previewImmediately = false): void {
    const validItems = this.items().filter(i => i.itemDescription && (i.quantity ?? 0) > 0);
    if (validItems.length === 0) {
      this.snackBar.open('Please add at least one line item with description and quantity.', 'Close', { duration: 4000 });
      return;
    }

    this.isSaving.set(true);

    const quotationItems: QuotationItemDto[] = validItems.map((item, idx) => ({
      lineNumber: idx + 1,
      itemDescription: item.itemDescription,
      quantity: Number(item.quantity || 1),
      rate: Number(item.rate || 0),
      unit: item.unit || 'pcs',
      hsnSacCode: item.hsnSacCode || '7308',
      taxPercentage: Number(this.gstRate())
    }));

    const payload: QuotationRequest = {
      contactId: this.selectedContactId() || undefined,
      customerName: this.customerName(),
      customerPhone: this.customerPhone(),
      templateId: this.selectedTemplateId(),
      quotationDate: this.quotationDate(),
      dueDate: this.dueDate() || undefined,
      discountPercentage: this.discountPercentage() || 0,
      items: quotationItems
    };

    if (this.id) {
      this.quotationService.update(this.id, payload).subscribe({
        next: (res) => {
          this.isSaving.set(false);
          this.snackBar.open('Quotation updated successfully!', 'Close', { duration: 3000 });
          if (previewImmediately) {
            this.router.navigate(['/quotations', res.id, 'preview']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg = err.error?.message || 'Failed to update quotation.';
          this.snackBar.open(msg, 'Close', { duration: 5000 });
        }
      });
    } else {
      this.quotationService.create(payload).subscribe({
        next: (res) => {
          this.isSaving.set(false);
          this.quotationId.set(res.quotationNumber || res.id);
          this.snackBar.open('Quotation created successfully!', 'Close', { duration: 3000 });
          if (previewImmediately) {
            this.router.navigate(['/quotations', res.id, 'preview']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg = err.error?.message || 'Failed to create quotation.';
          this.snackBar.open(msg, 'Close', { duration: 5000 });
        }
      });
    }
  }
}
