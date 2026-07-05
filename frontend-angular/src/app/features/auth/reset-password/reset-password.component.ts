import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/auth/auth.service';

function passwordsMatchValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const newPassword = group.get('newPassword')?.value;
    const confirmNewPassword = group.get('confirmNewPassword')?.value;

    if (!confirmNewPassword) return null;
    return newPassword === confirmNewPassword ? null : { passwordMismatch: true };
  };
}

@Component({
  selector: 'app-reset-password',
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
    MatSnackBarModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);

  isLoading = false;
  hideNewPassword = true;
  hideConfirmPassword = true;

  private email = '';
  invalidLink = false;

  resetPasswordForm: FormGroup = this.fb.group(
    {
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmNewPassword: ['', [Validators.required]]
    },
    { validators: passwordsMatchValidator() }
  );

  get otp() { return this.resetPasswordForm.get('otp'); }
  get newPassword() { return this.resetPasswordForm.get('newPassword'); }
  get confirmNewPassword() { return this.resetPasswordForm.get('confirmNewPassword'); }

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!this.email) {
      this.invalidLink = true;
      this.snackBar.open('Invalid reset link. Please try again.', 'Close', { duration: 5000 });
    }
  }

  toggleNewPasswordVisibility(): void {
    this.hideNewPassword = !this.hideNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  getDevOtp(): void {
    const key = `reset:${this.email}`;
    this.authService.getDevOtp(key).subscribe({
      next: (response) => {
        this.snackBar.open(`OTP: ${response.otp}`, 'Close', { duration: 10000 });
        this.resetPasswordForm.patchValue({ otp: response.otp });
      },
      error: () => {
        this.snackBar.open('Failed to get dev OTP', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || this.invalidLink) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { otp, newPassword } = this.resetPasswordForm.value;

    this.authService.resetPassword({
      email: this.email,
      otp,
      newPassword
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open(response.message, 'Close', { duration: 3000 });
        this.router.navigate(['/auth/sign-in']);
      },
      error: (error) => {
        this.isLoading = false;
        const message = error.error?.message || 'Failed to reset password. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }
}
