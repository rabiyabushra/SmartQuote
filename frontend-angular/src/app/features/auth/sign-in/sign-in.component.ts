import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { AuthService } from '../../../core/auth/auth.service';
import { OtpDialogComponent } from '../otp-dialog/otp-dialog.component';

@Component({
  selector: 'app-sign-in',
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
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.scss'
})
export class SignInComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);

  isLoading = false;
  hidePassword = true;

  signInForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onSubmit(): void {
    if (this.signInForm.invalid) {
      this.signInForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { email, password } = this.signInForm.value;

    // Step 1: Sign in (sends OTP)
    this.authService.signIn({ email, password }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open(response.message, 'Close', { duration: 3000 });
        
        // Open OTP dialog
        this.openOtpDialog(email);
      },
      error: (error) => {
        this.isLoading = false;
        const message = error.error?.message || 'Invalid credentials. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  private openOtpDialog(email: string): void {
    const dialogRef = this.dialog.open(OtpDialogComponent, {
      width: '400px',
      disableClose: true,
      data: { email, type: 'signin' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'success') {
        // Get return URL or navigate to dashboard
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigate([returnUrl]);
        this.snackBar.open('Welcome back!', 'Close', { duration: 3000 });
      }
    });
  }
}
