import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/auth/auth.service';
import { OtpDialogComponent } from '../otp-dialog/otp-dialog.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  isLoading = false;
  emailSent = false;

  forgotPasswordForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  get email() {
    return this.forgotPasswordForm.get('email');
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { email } = this.forgotPasswordForm.value;

    this.authService.forgotPassword({ email }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.emailSent = true;
        this.snackBar.open(response.message, 'Close', { duration: 3000 });
        
        // Open OTP dialog for password reset
        this.openResetOtpDialog(email);
      },
      error: (error) => {
        this.isLoading = false;
        const message = error.error?.message || 'Failed to send reset OTP. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  private openResetOtpDialog(email: string): void {
    const dialogRef = this.dialog.open(OtpDialogComponent, {
      width: '400px',
      disableClose: true,
      data: { email, type: 'reset' }
    });

    dialogRef.componentInstance.otpForm.valueChanges.subscribe(() => {
      // User will verify OTP through a different flow
      // Navigate to reset password with email stored
    });

    // For password reset, we need to collect OTP and new password together
    // So we'll navigate to reset-password page with email as query param
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/auth/reset-password'], {
          queryParams: { email }
        });
      }
    });
  }
}
