import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { AuthService } from './core/services/auth.service';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';

export const routes: Routes = [
  // Auth routes — no shell, AuthLayoutComponent is parent inside auth.routes.ts
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // Main app shell — sidebar visible for all children
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => {
          const auth = inject(AuthService);
          return auth.isAdmin ? '/admin/accounts' : '/resumes';
        }
      },
      {
        path: 'resumes',
        loadChildren: () => import('./features/resume/resume.routes').then(m => m.RESUME_ROUTES)
      },
      {
        path: 'questions',
        loadChildren: () => import('./features/questions/questions.routes').then(m => m.QUESTION_ROUTES)
      },
      {
        path: 'interviews',
        loadChildren: () => import('./features/interviews/interviews.routes').then(m => m.INTERVIEW_ROUTES)
      },
      {
        path: 'reports',
        loadChildren: () => import('./features/reports/reports.routes').then(m => m.REPORT_ROUTES)
      },
      {
        path: 'account',
        loadChildren: () => import('./features/account/account.routes').then(m => m.ACCOUNT_ROUTES)
      },
      {
        path: 'admin',
        canActivate: [authGuard],
        loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
      },
      { path: '**', redirectTo: 'resumes' }
    ]
  },

  // Catch-all: redirect unknown top-level paths through the auth flow
  { path: '**', redirectTo: '' }
];
