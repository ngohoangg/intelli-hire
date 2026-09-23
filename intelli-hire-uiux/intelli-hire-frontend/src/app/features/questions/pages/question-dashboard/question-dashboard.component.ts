import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import {
  PageResponse,
  QuestionResponse,
  StudyArticleResponse
} from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionStudyArticleService } from '../../services/question-study-article.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import { ActionCard, formatDateTime, formatQuestionType } from '../../questions.utils';

interface DashboardMetrics {
  active_questions: number;
  inactive_questions: number;
  topics: number;
  tags: number;
  difficulties: number;
  study_articles: number;
}

interface ActivityItem {
  title: string;
  detail: string;
  timestamp: string;
  tone: 'accent' | 'success' | 'warning';
}

@Component({
  selector: 'app-question-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './question-dashboard.component.html',
  styleUrl: './question-dashboard.component.scss'
})
export class QuestionDashboardComponent implements OnInit {
  loading = false;

  metrics: DashboardMetrics = {
    active_questions: 0,
    inactive_questions: 0,
    topics: 0,
    tags: 0,
    difficulties: 0,
    study_articles: 0
  };

  catalogCards: ActionCard[] = [];
  toolsCards: ActionCard[] = [];
  activityItems: ActivityItem[] = [];
  latestQuestions: QuestionResponse[] = [];
  latestArticles: StudyArticleResponse[] = [];

  constructor(
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService,
    private studyArticleService: QuestionStudyArticleService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  formatDate(value: string): string {
    return formatDateTime(value);
  }

  formatType(value: string): string {
    return formatQuestionType(value);
  }

  private loadDashboard(): void {
    this.loading = true;

    forkJoin({
      activePage: this.questionBankService.search({ page: 0, size: 1, isActive: true }).pipe(
        catchError(() => of(this.emptyPage<QuestionResponse>()))
      ),
      inactivePage: this.questionBankService.search({ page: 0, size: 1, isActive: false }).pipe(
        catchError(() => of(this.emptyPage<QuestionResponse>()))
      ),
      latestQuestions: this.questionBankService.search({ page: 0, size: 4, sort: 'lastUpdatedAt,desc' }).pipe(
        catchError(() => of(this.emptyPage<QuestionResponse>()))
      ),
      topics: this.taxonomyService.getTopics().pipe(catchError(() => of([]))),
      tags: this.taxonomyService.getTags().pipe(catchError(() => of([]))),
      difficulties: this.taxonomyService.getDifficulties().pipe(catchError(() => of([]))),
      articles: this.studyArticleService.getAll({ page: 0, size: 4 }).pipe(
        catchError(() => of(this.emptyPage<StudyArticleResponse>()))
      )
    }).subscribe({
      next: result => {
        this.metrics = {
          active_questions: result.activePage.total_elements,
          inactive_questions: result.inactivePage.total_elements,
          topics: result.topics.length,
          tags: result.tags.length,
          difficulties: result.difficulties.length,
          study_articles: result.articles.total_elements
        };

        this.latestQuestions = result.latestQuestions.content;
        this.latestArticles = result.articles.content;
        this.catalogCards = this.buildCatalogCards();
        this.toolsCards = this.buildToolsCards();
        this.activityItems = this.buildActivityItems();
        this.loading = false;
      },
      error: () => {
        this.catalogCards = this.buildCatalogCards();
        this.toolsCards = this.buildToolsCards();
        this.activityItems = [];
        this.loading = false;
      }
    });
  }

  private buildCatalogCards(): ActionCard[] {
    const totalQuestions = this.metrics.active_questions + this.metrics.inactive_questions;

    return [
      {
        title: 'Question Bank',
        value: `${totalQuestions.toLocaleString()} questions`,
        subtitle: `${this.metrics.active_questions.toLocaleString()} active / ${this.metrics.inactive_questions.toLocaleString()} inactive`,
        detail: 'Search, filter and curate the interview question pool.',
        cta: 'Open',
        route: '/questions'
      },
      {
        title: 'Topics',
        value: `${this.metrics.topics.toLocaleString()} topics`,
        subtitle: 'Tree hierarchy',
        detail: 'Manage parent-child taxonomy shared across practice and report flows.',
        cta: 'Manage',
        route: '/questions/topics'
      },
      {
        title: 'Tags',
        value: `${this.metrics.tags.toLocaleString()} tags`,
        subtitle: 'Reusable labels',
        detail: 'Control cross-topic filters used by question search and random generation.',
        cta: 'Manage',
        route: '/questions/tags'
      },
      {
        title: 'Difficulty Levels',
        value: `${this.metrics.difficulties.toLocaleString()} levels`,
        subtitle: 'Weighted scoring',
        detail: 'Keep difficulty naming, weights and descriptions aligned with the catalog.',
        cta: 'Manage',
        route: '/questions/difficulties'
      },
      {
        title: 'Study Articles',
        value: `${this.metrics.study_articles.toLocaleString()} resources`,
        subtitle: 'By topic and difficulty',
        detail: 'Curate learning resources that power preparation previews and post-session study.',
        cta: 'Open',
        route: '/questions/articles'
      }
    ];
  }

  private buildToolsCards(): ActionCard[] {
    return [
      {
        title: 'Random Generator',
        value: 'Mock interview lab',
        subtitle: 'Validate pool and filters',
        detail: 'Probe random selection quality, cache behavior and taxonomy combinations before client rollout.',
        cta: 'Open Lab',
        route: '/questions/random-lab'
      },
      {
        title: 'Bulk Import',
        value: 'JSON batch ingest',
        subtitle: 'Up to 100 questions',
        detail: 'Paste a bulk payload, validate structure and inspect row-level failures without leaving the module.',
        cta: 'Open Import',
        route: '/questions/bulk-import'
      }
    ];
  }

  private buildActivityItems(): ActivityItem[] {
    const questionItems = this.latestQuestions.slice(0, 3).map(question => ({
      title: `${formatQuestionType(question.question_type)} question refreshed`,
      detail: question.question_text,
      timestamp: question.last_updated_at,
      tone: 'accent' as const
    }));

    const articleItem = this.latestArticles[0]
      ? [{
          title: 'Study article available',
          detail: `${this.latestArticles[0].title} • ${this.latestArticles[0].topic_name}`,
          timestamp: new Date().toISOString(),
          tone: 'success' as const
        }]
      : [];

    const coverageItem = [{
      title: 'Catalog coverage snapshot',
      detail: `${this.metrics.topics} topics • ${this.metrics.tags} tags • ${this.metrics.difficulties} difficulty levels`,
      timestamp: new Date().toISOString(),
      tone: 'warning' as const
    }];

    return [...questionItems, ...articleItem, ...coverageItem].slice(0, 5);
  }

  private emptyPage<T>(): PageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 0,
      total_elements: 0,
      total_pages: 0,
      is_first: true,
      is_last: true
    };
  }
}
