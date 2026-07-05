import { Component, Inject, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/auth/auth.service';

export interface OtpDialogData {
  email: string;
  type: 'signin' | 'reset';
}

@Component({
  selector: 'app-otp-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title>Enter OTP</h2>
    <mat-dialog-content>
      <p>An OTP has been sent to <strong>{{ data.email }}</strong></p>
      <form [formGroup]="otpForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>OTP</mat-label>
          <input
            matInput
            type="text"
            formControlName="otp"
            placeholder="Enter 6-digit OTP"
            maxlength="6"
            autocomplete="one-time-code" />
          <mat-error *ngIf="otpForm.get('otp')?.hasError('required')">
            OTP is required
          </mat-error>
          <mat-error *ngIf="otpForm.get('otp')?.hasError('pattern')">
            OTP must be 6 digits
          </mat-error>
        </mat-form-field>
      </form>
      <p class="dev-note" *ngIf="!isProduction">
        <small>
          <strong>Dev Mode:</strong> 
          <a href="javascript:void(0)" (click)="getDevOtp()">Click here to get OTP</a>
        </small>
      </p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" [disabled]="isLoading">Cancel</button>
      <button
        mat-flat-button
        color="primary"
        (click)="onVerify()"
        [disabled]="isLoading || otpForm.invalid">
        <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
        <span *ngIf="!isLoading">Verify</span>
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width {
      width: 100%;
    }
    .dev-note {
      margin-top: 8px;
      color: #666;
    }
    .dev-note a {
      color: #1976d2;
      text-decoration: underline;
      cursor: pointer;
    }
    mat-spinner {
      display: inline-block;
      margin: 0 auto;
    }
  `]
})
export class OtpDialogComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<OtpDialogComponent>);

  isLoading = false;
  isProduction = false; // Set to true in production

  otpForm: FormGroup = this.fb.group({
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  constructor(@Inject(MAT_DIALOG_DATA) public data: OtpDialogData) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onVerify(): void {
    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const otp = this.otpForm.value.otp;

    this.authService.verifyOtp({ email: this.data.email, otp }).subscribe({
      next: () => {
        this.isLoading = false;
        this.dialogRef.close('success');
      },
      error: (error) => {
        this.isLoading = false;
        const message = error.error?.message || 'Invalid OTP. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  getDevOtp(): void {
    const key = this.data.type === 'reset' ? `reset:${this.data.email}` : this.data.email;
    this.authService.getDevOtp(key).subscribe({
      next: (response) => {
        this.snackBar.open(`OTP: ${response.otp}`, 'Close', { duration: 10000 });
        this.otpForm.patchValue({ otp: response.otp });
      },
      error: () => {
        this.snackBar.open('Failed to get dev OTP', 'Close', { duration: 3000 });
      }
    });
  }
}
