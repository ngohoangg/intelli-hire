import { Routes } from '@angular/router';

export const REPORT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/report-dashboard/report-dashboard.component')
        .then(m => m.ReportDashboardComponent)
  },
  {
    path: 'history',
    loadComponent: () =>
      import('./pages/report-history/report-history.component')
        .then(m => m.ReportHistoryComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/report-detail/report-detail.component')
        .then(m => m.ReportDetailComponent)
  }
];
