import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { calculateTax } from '../../../../../core/utils/quotation-tax.util';
import { QuotationService } from '../../../../../core/services/quotation.service';

@Component({
  selector: 'app-job-work-quotation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatRadioModule, MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  templateUrl: './job-work-quotation-form.component.html',
  styleUrls: ['../../quotation-document-shared.scss', './job-work-quotation-form.component.scss']
})
export class JobWorkQuotationFormComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private quotationService = inject(QuotationService);

  // TODO: replace with BusinessService.getProfile()
  businessInfo = signal({
    businessName: 'Sharma Fabrication Works',
    streetAddress: 'Plot 14, MIDC Industrial Area',
    cityPostal: 'Pune, MH 411019',
    gstin: '27ABCDE1234F1Z5',
    contact: '+91 98765 43210 · rajesh@sharmafab.com'
  });

  form: FormGroup = this.fb.group({
    quoteNumber: [this.generateQuoteNumber()],
    quoteDate: [new Date()],
    gstin: [this.businessInfo().gstin],
    validUntil: [null, Validators.required],

    customerName: ['', Validators.required],
    customerAddress: ['', Validators.required],
    contactNumber: ['', Validators.required],

    scopeOfJobWork: [
      'Fabrication of MS frame as per approved drawing no.XX\nDrilling, cutting, welding included\nSurface finishing excluded'
    ],

    materialSuppliedBy: ['client'],
    materialSuppliedByOther: [''],
    materialGrade: [''],
    materialQuantityReceived: [''],

    items: this.fb.array([this.createItemRow()]),

    termsAndConditions: [
      'Work will be executed as per approved drawing and technical specification.\nClient inspection required before dispatch.\nAny deviation must be reported within 3 days of delivery.\nRework will be undertaken only after written confirmation.'
    ]
  });

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  itemsValue = computed(() => this.items.value as any[]);

  taxSummary = computed(() => {
    const subtotal = this.itemsValue().reduce(
      (sum, row) => sum + (Number(row.qty) * Number(row.rate) || 0), 0
    );
    return calculateTax(subtotal);
  });

  private createItemRow(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      sacCode: [''],
      qty: [1, [Validators.required, Validators.min(1)]],
      rate: [0, [Validators.required, Validators.min(0)]]
    });
  }

  addItem(): void {
    this.items.push(this.createItemRow());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) this.items.removeAt(index);
  }

  rowAmount(index: number): number {
    const row = this.items.at(index).value;
    return (Number(row.qty) || 0) * (Number(row.rate) || 0);
  }

  private generateQuoteNumber(): string {
    // TODO: replace with server-generated quote number (QuotationService.getNextQuoteNumber())
    const year = new Date().getFullYear();
    const random = Math.floor(1000 + Math.random() * 9000);
    return `QT-${year}-${random}`;
  }

  saveDraft(): void {
  const id = this.quotationService.createLocal('job-work', this.form.value); // match template ID per file
  this.snackBar.open('Saved as draft', 'OK', { duration: 2000 });
  this.router.navigate(['/quotations']);
}

  saveAndPreview(): void {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    this.snackBar.open('Please fill all required fields', 'OK', { duration: 2500 });
    return;
  }
  const id = this.quotationService.createLocal('job-work', this.form.value); // 'fabrication' | 'supply' | 'service' | 'maintenance' in the other 4 files
  this.snackBar.open('Quotation saved', 'OK', { duration: 2000 });
  this.router.navigate(['/quotations', id, 'preview']);
}
}
