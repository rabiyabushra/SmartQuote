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

import { calculateTax, round2 } from '../../../../../core/utils/quotation-tax.util';
import { QuotationService } from '../../../../../core/services/quotation.service';

@Component({
  selector: 'app-supply-quotation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  templateUrl: './supply-quotation-form.component.html',
  styleUrls: ['../../quotation-document-shared.scss', './supply-quotation-form.component.scss']
})
export class SupplyQuotationFormComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

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
    gstin: [this.businessInfo().gstin],
    referenceNo: [''],

    customerName: ['', Validators.required],
    customerAddress: ['', Validators.required],
    contactPerson: [''],
    contact: [''],
    paymentTerms: [''],

    items: this.fb.array([this.createRow()]),

    termsAndConditions: [
      'Prices are in INR and valid until the mentioned date.\nDelivery will be made within 7 working days from the date of order confirmation.\n50% advance payment required, balance within 7 days of delivery.'
    ]
  });

  get items(): FormArray { return this.form.get('items') as FormArray; }

  private createRow(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      qty: [1, [Validators.required, Validators.min(0)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]]
    });
  }

  addItem(): void { this.items.push(this.createRow()); }
  removeItem(i: number): void { if (this.items.length > 1) this.items.removeAt(i); }

  rowAmount(index: number): number {
    const row = this.items.at(index).value;
    const base = (Number(row.qty) || 0) * (Number(row.unitPrice) || 0);
    return round2(base);
  }

  taxSummary = computed(() => {
    const subtotal = this.items.controls.reduce((sum, row) => {
      const v = row.value;
      return sum + (Number(v.qty) || 0) * (Number(v.unitPrice) || 0);
    }, 0);
    return calculateTax(subtotal);
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
