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

import { calculateTax } from '../../../../../core/utils/quotation-tax.util';
import { QuotationService } from '../../../../../core/services/quotation.service';


@Component({
  selector: 'app-maintenance-quotation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  templateUrl: './maintenance-quotation-form.component.html',
  styleUrls: ['../../quotation-document-shared.scss', './maintenance-quotation-form.component.scss']
})
export class MaintenanceQuotationFormComponent {
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
    paymentTermsShort: [''],

    projectName: [''],
    siteLocation: [''],
    clientContact: [''],
    scopeReference: [''],
    subject: [''],
    clientName: [''],
    address: [''],
    phone: [''],
    email: [''],

    scopeOfWork: [
      'Maintenance tasks described in bullet points\nInspection and assessment before work begins\nConsumables provided separately (if applicable)\nWork to be carried out as per schedule and requirements'
    ],

    items: this.fb.array([this.createRow()]),

    paymentTerms: [
      '50% Advance\n30% After Mid-Stage Completion\n20% Upon Final Handover'
    ],

    termsAndConditions: [
      'All tasks will be completed as per approved schedule.\nQuote is based on actual site conditions, final costs may vary if there are any changes beyond the original scope of work.\nOur team will adhere to site safety and regulations at all times.\nThis quotation is valid for 30 days from the date of issue.\nElectricity to be provided by client. Working hours as per agreed schedule.'
    ]
  });

  get items(): FormArray { return this.form.get('items') as FormArray; }

  private createRow(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      unit: [''],
      qty: [1, [Validators.required, Validators.min(0)]],
      unitRate: [0, [Validators.required, Validators.min(0)]],
      taxPercent: [9]
    });
  }

  addItem(): void { this.items.push(this.createRow()); }
  removeItem(i: number): void { if (this.items.length > 1) this.items.removeAt(i); }

  rowAmount(index: number): number {
    const v = this.items.at(index).value;
    return (Number(v.qty) || 0) * (Number(v.unitRate) || 0);
  }

  taxSummary = computed(() => {
    const subtotal = this.items.controls.reduce((sum, row) => {
      const v = row.value;
      return sum + (Number(v.qty) || 0) * (Number(v.unitRate) || 0);
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
