import { Routes } from '@angular/router';

export const QUESTION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/question-list/question-list.component')
        .then(m => m.QuestionListComponent)
  },
  {
    path: 'overview',
    loadComponent: () =>
      import('./pages/question-dashboard/question-dashboard.component')
        .then(m => m.QuestionDashboardComponent)
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./pages/question-create/question-create.component')
        .then(m => m.QuestionCreateComponent)
  },
  {
    path: 'topics',
    loadComponent: () =>
      import('./pages/topic-management/topic-management.component')
        .then(m => m.TopicManagementComponent)
  },
  {
    path: 'tags',
    loadComponent: () =>
      import('./pages/tag-management/tag-management.component')
        .then(m => m.TagManagementComponent)
  },
  {
    path: 'difficulties',
    loadComponent: () =>
      import('./pages/difficulty-management/difficulty-management.component')
        .then(m => m.DifficultyManagementComponent)
  },
  {
    path: 'articles',
    loadComponent: () =>
      import('./pages/study-article-list/study-article-list.component')
        .then(m => m.StudyArticleListComponent)
  },
  {
    path: 'random-lab',
    loadComponent: () =>
      import('./pages/random-lab/random-lab.component')
        .then(m => m.RandomLabComponent)
  },
  {
    path: 'bulk-import',
    loadComponent: () =>
      import('./pages/bulk-import/bulk-import.component')
        .then(m => m.BulkImportComponent)
  },
  {
    path: 'submitted',
    loadComponent: () =>
      import('./pages/submitted-questions/submitted-questions.component')
        .then(m => m.SubmittedQuestionsComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./pages/question-edit/question-edit.component')
        .then(m => m.QuestionEditComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/question-detail/question-detail.component')
        .then(m => m.QuestionDetailComponent)
  }
];
