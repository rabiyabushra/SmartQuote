import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { QuotationService } from '../../../../../core/services/quotation.service';
import { calculateTax, round2 } from '../../../../../core/utils/quotation-tax.util';

@Component({
  selector: 'app-service-quotation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  templateUrl: './service-quotation-form.component.html',
  styleUrls: ['../../quotation-document-shared.scss', './service-quotation-form.component.scss']
})
export class ServiceQuotationFormComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  private quotationService = inject(QuotationService);

  businessInfo = signal({
    businessName: 'Sharma Fabrication Works',
    streetAddress: 'Plot 14, MIDC Industrial Area',
    cityPostal: 'Pune, MH 411019',
    gstin: '27ABCDE1234F1Z5',
    contact: '+91 98765 43210 · info@sharmafab.com'
  });

  form: FormGroup = this.fb.group({
    quoteNumber: [this.generateQuoteNumber()],
    quoteDate: [new Date()],
    validUntil: [null, Validators.required],
    referenceNo: [''],
    paymentTerms: [''],

    customerName: ['', Validators.required],
    customerAddress: ['', Validators.required],
    contactPerson: [''],

    scopeOfService: [
      'Manpower deployment as per requirement\nService hours defined per shift\nSupervision included\nConsumables excluded'
    ],

    items: this.fb.array([this.createRow()]),

    discount: [0],
    advance: [0],

    termsAndConditions: [
      'The above rates are exclusive of taxes.\nClient must provide access and necessary approvals prior to deployment.\nAny additional work will be executed at extra cost upon approval.\nPayment terms: 50% advance, balance within 7 days of invoice.\nThis quotation is valid for 30 days from the date of issue.'
    ]
  });

  get items(): FormArray { return this.form.get('items') as FormArray; }

  private createRow(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      resources: [1, [Validators.required, Validators.min(0)]],
      unit: [''],
      rate: [0, [Validators.required, Validators.min(0)]]
    });
  }

  addItem(): void { this.items.push(this.createRow()); }
  removeItem(i: number): void { if (this.items.length > 1) this.items.removeAt(i); }

  rowAmount(index: number): number {
    const v = this.items.at(index).value;
    return (Number(v.resources) || 0) * (Number(v.rate) || 0);
  }

  private subtotal = computed(() =>
    this.items.controls.reduce((sum, row) => {
      const v = row.value;
      return sum + (Number(v.resources) || 0) * (Number(v.rate) || 0);
    }, 0)
  );

  taxSummary = computed(() => calculateTax(this.subtotal()));

  balanceDue = computed(() => {
    const t = this.taxSummary();
    const discount = Number(this.form.get('discount')?.value) || 0;
    const advance = Number(this.form.get('advance')?.value) || 0;
    return round2(t.total - discount - advance);
  });

  private generateQuoteNumber(): string {
    const year = new Date().getFullYear();
    return `QT-${year}-${Math.floor(1000 + Math.random() * 9000)}`;
  }

  saveDraft(): void {
    this.snackBar.open('Saved as draft', 'OK', { duration: 2000 });
    this.router.navigate(['/quotations']);
  }

  saveAndPreview(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Please fill all required fields', 'OK', { duration: 2500 });
      return;
    }
    this.snackBar.open('Quotation saved', 'OK', { duration: 2000 });
    this.router.navigate(['/quotations']);
  }
}
