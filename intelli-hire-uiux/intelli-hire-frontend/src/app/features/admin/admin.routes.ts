import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/admin.guard';

export const ADMIN_ROUTES: Routes = [
  { path: '', redirectTo: 'accounts', pathMatch: 'full' },
  {
    path: 'accounts',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/accounts/accounts.component').then(m => m.AdminAccountsComponent)
  },
  {
    path: 'accounts/:id',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/account-detail/account-detail.component').then(m => m.AccountDetailComponent)
  },
  {
    path: 'questions',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-questions/admin-questions.component').then(m => m.AdminQuestionsComponent)
  }
];
