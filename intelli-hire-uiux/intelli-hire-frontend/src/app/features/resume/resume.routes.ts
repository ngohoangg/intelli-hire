import { Routes } from '@angular/router';

export const RESUME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/resume-list/resume-list.component').then(m => m.ResumeListComponent)
  },
  {
    path: 'create',
    loadComponent: () => import('./pages/resume-create/resume-create.component').then(m => m.ResumeCreateComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/resume-detail/resume-detail.component').then(m => m.ResumeDetailComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./pages/resume-edit/resume-edit.component').then(m => m.ResumeEditComponent)
  }
];
