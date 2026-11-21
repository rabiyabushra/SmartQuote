import { Component, Inject, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Customer, CustomerService } from '../../../core/services/customer.service';

@Component({
  selector: 'app-customer-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ isEditMode ? 'Edit Customer' : 'Add New Customer' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="customer-dialog-form">
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Company / Business Name *</mat-label>
            <input matInput formControlName="companyName" placeholder="e.g. Rajesh Industries" />
            @if (form.get('companyName')?.hasError('required')) {
              <mat-error>Company name is required</mat-error>
            }
          </mat-form-field>
        </div>

        <div class="form-row two-cols">
          <mat-form-field appearance="outline">
            <mat-label>Contact Person *</mat-label>
            <input matInput formControlName="contactPerson" placeholder="e.g. Rajesh Kumar" />
            @if (form.get('contactPerson')?.hasError('required')) {
              <mat-error>Contact person is required</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Phone Number *</mat-label>
            <input matInput formControlName="phone" placeholder="+91 98765 43210" />
            @if (form.get('phone')?.hasError('required')) {
              <mat-error>Phone is required</mat-error>
            }
          </mat-form-field>
        </div>

        <div class="form-row two-cols">
          <mat-form-field appearance="outline">
            <mat-label>Email Address</mat-label>
            <input matInput type="email" formControlName="email" placeholder="client@example.com" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>GST Type</mat-label>
            <mat-select formControlName="gstType">
              <mat-option value="regular">Regular</mat-option>
              <mat-option value="composition">Composition</mat-option>
              <mat-option value="unregistered">Unregistered</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <div class="form-row two-cols">
          <mat-form-field appearance="outline">
            <mat-label>GSTIN</mat-label>
            <input matInput formControlName="gstin" placeholder="e.g. 29ABCDE1234F1Z5" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>State (determines CGST/SGST vs IGST) *</mat-label>
            <input matInput formControlName="state" placeholder="e.g. Karnataka" />
            @if (form.get('state')?.hasError('required')) {
              <mat-error>State is required for GST determination</mat-error>
            }
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Address</mat-label>
            <textarea matInput formControlName="address" rows="2" placeholder="Street, Industrial Area"></textarea>
          </mat-form-field>
        </div>

        <div class="form-row two-cols">
          <mat-form-field appearance="outline">
            <mat-label>City</mat-label>
            <input matInput formControlName="city" placeholder="e.g. Bengaluru" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Pincode</mat-label>
            <input matInput formControlName="pincode" placeholder="e.g. 560001" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="onCancel()" [disabled]="isSaving">Cancel</button>
      <button mat-flat-button color="primary" type="button" (click)="onSave()" [disabled]="form.invalid || isSaving">
        @if (isSaving) {
          <mat-spinner diameter="18" style="display:inline-block; margin-right: 6px;"></mat-spinner>
        }
        Save Customer
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .customer-dialog-form {
      display: flex;
      flex-direction: column;
      gap: 12px;
      min-width: 480px;
      max-width: 600px;
      padding-top: 8px;
    }
    .form-row {
      display: flex;
      width: 100%;
    }
    .form-row.two-cols {
      gap: 16px;
    }
    .form-row.two-cols mat-form-field {
      flex: 1;
    }
    .full-width {
      width: 100%;
    }
    @media (max-width: 600px) {
      .customer-dialog-form {
        min-width: 100%;
      }
      .form-row.two-cols {
        flex-direction: column;
        gap: 8px;
      }
    }
  `]
})
export class CustomerDialogComponent {
  private fb = inject(FormBuilder);
  private customerService = inject(CustomerService);
  private dialogRef = inject(MatDialogRef<CustomerDialogComponent>);

  isEditMode = false;
  isSaving = false;
  form: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { customer?: Customer } | null) {
    this.isEditMode = !!data?.customer?.id;

    this.form = this.fb.group({
      companyName: [data?.customer?.companyName || '', Validators.required],
      contactPerson: [data?.customer?.contactPerson || '', Validators.required],
      phone: [data?.customer?.phone || '', Validators.required],
      email: [data?.customer?.email || ''],
      gstType: [data?.customer?.gstType || 'regular'],
      gstin: [data?.customer?.gstin || ''],
      state: [data?.customer?.state || '', Validators.required],
      address: [data?.customer?.address || ''],
      city: [data?.customer?.city || ''],
      country: [data?.customer?.country || 'India'],
      pincode: [data?.customer?.pincode || '']
    });
  }

  onSave(): void {
    if (this.form.invalid) return;

    this.isSaving = true;
    const formValue = this.form.value;

    if (this.isEditMode && this.data?.customer?.id) {
      this.customerService.update(this.data.customer.id, formValue).subscribe({
        next: (saved) => {
          this.isSaving = false;
          this.dialogRef.close(saved);
        },
        error: (err) => {
          this.isSaving = false;
          console.error('Failed to update customer', err);
        }
      });
    } else {
      this.customerService.create(formValue).subscribe({
        next: (saved) => {
          this.isSaving = false;
          this.dialogRef.close(saved);
        },
        error: (err) => {
          this.isSaving = false;
          console.error('Failed to create customer', err);
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
