import { Routes } from '@angular/router';

// Parent route in app.routes.ts already guards with authGuard — no need to repeat here
export const ACCOUNT_ROUTES: Routes = [
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'security',
    loadComponent: () => import('./pages/security/security.component').then(m => m.SecurityComponent)
  }
];
