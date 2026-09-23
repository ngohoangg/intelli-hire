import { Routes } from '@angular/router';

export const INTERVIEW_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/interview-list/interview-list.component')
        .then(m => m.InterviewListComponent)
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./pages/interview-create/interview-create.component')
        .then(m => m.InterviewCreateComponent)
  },
  {
    path: ':id/room',
    loadComponent: () =>
      import('./pages/interview-room/interview-room.component')
        .then(m => m.InterviewRoomComponent)
  },
  {
    path: ':id/detail',
    loadComponent: () =>
      import('./pages/interview-detail/interview-detail.component')
        .then(m => m.InterviewDetailComponent)
  }
];
