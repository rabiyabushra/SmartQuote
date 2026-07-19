import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SignUpRequest,
  SignInRequest,
  OtpVerifyRequest,
  AuthResponse,
  PasswordResetInitRequest,
  ResetPasswordRequest,
  MessageResponse,
  DevOtpResponse
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  
  private readonly API_URL = `${environment.apiBaseUrl}/api/auth`;
  private readonly TOKEN_KEY = 'smartquote_token';
  private readonly USER_KEY = 'smartquote_user';

  // Signal to track authentication state
  private isAuthenticatedSignal = signal<boolean>(this.hasToken());
  private currentUserSignal = signal<AuthResponse | null>(this.getUserFromStorage());

  isAuthenticated = this.isAuthenticatedSignal.asReadonly();
  currentUser = this.currentUserSignal.asReadonly();

  /**
   * Sign up a new user (returns token immediately)
   */
  signUp(request: SignUpRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/sign-up`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response))
      );
  }

  /**
   * Step 1: Sign in (sends OTP, does not return token)
   */
  signIn(request: SignInRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/sign-in`, request);
  }

  /**
   * Step 2: Verify OTP and get session token
   */
  verifyOtp(request: OtpVerifyRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/verify-otp`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response))
      );
  }

  /**
   * Get current user profile
   */
  me(): Observable<AuthResponse> {
    return this.http.get<AuthResponse>(`${this.API_URL}/me`);
  }

  /**
   * Sign out
   */
  signOut(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/sign-out`, {})
      .pipe(
        tap(() => this.handleSignOut())
      );
  }

  /**
   * Initiate password reset (sends OTP)
   */
  forgotPassword(request: PasswordResetInitRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/forgot-password`, request);
  }

  /**
   * Reset password with OTP
   */
  resetPassword(request: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/reset-password`, request);
  }

  /**
   * Dev only: Get OTP for testing
   */
  getDevOtp(key: string): Observable<DevOtpResponse> {
    return this.http.get<DevOtpResponse>(`${this.API_URL}/dev/otp`, {
      params: { key }
    });
  }

  /**
   * Get stored token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if user has token
   */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * Handle successful authentication
   */
  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
    this.isAuthenticatedSignal.set(true);
    this.currentUserSignal.set(response);
  }

  /**
   * Handle sign out
   */
  private handleSignOut(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isAuthenticatedSignal.set(false);
    this.currentUserSignal.set(null);
    this.router.navigate(['/auth/sign-in']);
  }

  /**
   * Get user from local storage
   */
  private getUserFromStorage(): AuthResponse | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch {
        return null;
      }
    }
    return null;
  }

  /**
   * Clear auth data (use for local sign out without API call)
   */
  clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isAuthenticatedSignal.set(false);
    this.currentUserSignal.set(null);
  }
}
