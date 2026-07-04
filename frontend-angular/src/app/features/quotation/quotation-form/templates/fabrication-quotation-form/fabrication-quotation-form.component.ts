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

import { round2 } from '../../../../../core/utils/quotation-tax.util';

@Component({
  selector: 'app-fabrication-quotation-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  templateUrl: './fabrication-quotation-form.component.html',
  styleUrls: ['../../quotation-document-shared.scss', './fabrication-quotation-form.component.scss']
})
export class FabricationQuotationFormComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  // TODO: replace with BusinessService.getProfile()
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

    customerName: ['', Validators.required],
    customerAddress: ['', Validators.required],
    contactPerson: [''],
    jobRefNo: [''],
    contact: [''],
    email: [''],

    materialItems: this.fb.array([this.createRow()]),
    laborItems: this.fb.array([this.createRow()]),

    termsAndConditions: [
      'Work will be executed as per approved drawing.\nClient inspection required before dispatch.\nRework only after written confirmation.'
    ]
  });

  get materialItems(): FormArray { return this.form.get('materialItems') as FormArray; }
  get laborItems(): FormArray { return this.form.get('laborItems') as FormArray; }

  private createRow(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      qty: [1, [Validators.required, Validators.min(0)]],
      price: [0, [Validators.required, Validators.min(0)]],
      cgstPercent: [9]
    });
  }

  addMaterialRow(): void { this.materialItems.push(this.createRow()); }
  removeMaterialRow(i: number): void { if (this.materialItems.length > 1) this.materialItems.removeAt(i); }

  addLaborRow(): void { this.laborItems.push(this.createRow()); }
  removeLaborRow(i: number): void { if (this.laborItems.length > 1) this.laborItems.removeAt(i); }

  rowAmount(row: FormGroup | number): number {
    const rowValue = typeof row === 'number' ? this.materialItems.at(row).value : row.value;
    const base = (Number(rowValue.qty) || 0) * (Number(rowValue.price) || 0);
    return round2(base * (1 + (Number(rowValue.cgstPercent) || 0) / 100));
  }

  materialTotal = computed(() =>
    round2(this.materialItems.controls.reduce((sum, row, index) => sum + this.rowAmount(index), 0))
  );

  laborTotal = computed(() =>
    round2(this.laborItems.controls.reduce((sum, row, index) => sum + this.rowAmount(index), 0))
  );

  grandTotal = computed(() => round2(this.materialTotal() + this.laborTotal()));

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
