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

  isLoading = false;
  hideNewPassword = true;
  hideConfirmPassword = true;

  /** Comes from the reset-link query param, e.g. /auth/reset-password?token=xyz */
  private resetToken = '';
  invalidLink = false;

  resetPasswordForm: FormGroup = this.fb.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmNewPassword: ['', [Validators.required]]
    },
    { validators: passwordsMatchValidator() }
  );

  get newPassword() { return this.resetPasswordForm.get('newPassword'); }
  get confirmNewPassword() { return this.resetPasswordForm.get('confirmNewPassword'); }

  ngOnInit(): void {
    this.resetToken = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.resetToken) {
      this.invalidLink = true;
    }
  }

  toggleNewPasswordVisibility(): void {
    this.hideNewPassword = !this.hideNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || this.invalidLink) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { newPassword } = this.resetPasswordForm.value;

    // TODO: replace with AuthService.resetPassword(this.resetToken, newPassword)
    setTimeout(() => {
      this.isLoading = false;
      this.snackBar.open('Password updated. Please sign in.', 'OK', { duration: 3000 });
      this.router.navigate(['/auth/sign-in']);
    }, 800);
  }
}
