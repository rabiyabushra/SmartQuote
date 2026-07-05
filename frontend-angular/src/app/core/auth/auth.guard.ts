import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasToken()) {
    return true;
  }

  // Redirect to sign-in if not authenticated
  router.navigate(['/auth/sign-in'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};
