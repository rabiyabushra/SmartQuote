import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  // ---------- AUTH (public) ----------
  {
    path: 'auth',
    children: [
      {
        path: 'sign-in',
        loadComponent: () =>
          import('./features/auth/sign-in/sign-in.component').then(m => m.SignInComponent)
      },
      {
        path: 'sign-up',
        loadComponent: () =>
          import('./features/auth/sign-up/sign-up.component').then(m => m.SignUpComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () =>
          import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
      },
      { path: '', redirectTo: 'sign-in', pathMatch: 'full' }
    ]
  },

  // ---------- PROTECTED ----------
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile/profile.component').then(m => m.ProfileComponent)
  },

  // ---------- TEMPLATES ----------
  {
    path: 'templates',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/templates/template-gallery/template-gallery.component').then(m => m.TemplateGalleryComponent)
  },

  // ---------- QUOTATION (core feature) ----------
  {
    path: 'quotations',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/quotation/quotation-list/quotation-list.component').then(m => m.QuotationListComponent)
      },
      {
        path: 'new/:templateId',
        loadComponent: () =>
          import('./features/quotation/quotation-form/quotation-form.component').then(m => m.QuotationFormComponent)
      },
      {
        path: ':id/edit',
        loadComponent: () =>
          import('./features/quotation/quotation-form/quotation-form.component').then(m => m.QuotationFormComponent)
      },
      {
        path: ':id/preview',
        loadComponent: () =>
          import('./features/quotation/quotation-preview/quotation-preview.component').then(m => m.QuotationPreviewComponent)
      }
    ]
  },

  // ---------- CUSTOMERS ----------
  {
    path: 'customers',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/customers/customer-list/customer-list.component').then(m => m.CustomerListComponent)
  },

  // ---------- FALLBACK ----------
  { path: '**', redirectTo: 'auth/sign-in' }
];
