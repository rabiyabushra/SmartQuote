import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

const TOKEN_KEY = 'smartquote_token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);

  let authReq = req;
  if (token && !req.headers.has('Authorization')) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const errorMsg = typeof error.error === 'string' 
        ? error.error 
        : (error.error?.message || '');

      const isAuthError = error.status === 401 || 
        (error.status === 400 && (
          errorMsg.toLowerCase().includes('expired session token') ||
          errorMsg.toLowerCase().includes('invalid session token')
        ));

      if (isAuthError && !req.url.includes('/auth/sign-in') && !req.url.includes('/auth/sign-up')) {
        localStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem('smartquote_user');
        router.navigate(['/auth/sign-in']);
      }

      return throwError(() => error);
    })
  );
};

export function storeAuthToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearAuthToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
}

export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
}
