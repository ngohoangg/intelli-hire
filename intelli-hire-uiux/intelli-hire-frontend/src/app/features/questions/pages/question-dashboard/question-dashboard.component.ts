import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
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
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent, TranslatePipe],
  templateUrl: './question-dashboard.component.html',
  styleUrl: './question-dashboard.component.scss'
})
export class QuestionDashboardComponent implements OnInit {
  private readonly translate = inject(TranslateService);

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
    return formatDateTime(value, this.translate.instant('questions.placeholders.notAvailable'));
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
    const t = (key: string, params?: Record<string, unknown>) => this.translate.instant(key, params);

    return [
      {
        title: t('questions.dashboard.cards.questionBank.title'),
        value: t('questions.dashboard.cards.questionBank.value', { count: totalQuestions.toLocaleString() }),
        subtitle: t('questions.dashboard.cards.questionBank.subtitle', {
          active: this.metrics.active_questions.toLocaleString(),
          inactive: this.metrics.inactive_questions.toLocaleString()
        }),
        detail: t('questions.dashboard.cards.questionBank.detail'),
        cta: t('questions.dashboard.cards.questionBank.cta'),
        route: '/questions'
      },
      {
        title: t('questions.dashboard.cards.topics.title'),
        value: t('questions.dashboard.cards.topics.value', { count: this.metrics.topics.toLocaleString() }),
        subtitle: t('questions.dashboard.cards.topics.subtitle'),
        detail: t('questions.dashboard.cards.topics.detail'),
        cta: t('questions.dashboard.cards.topics.cta'),
        route: '/questions/topics'
      },
      {
        title: t('questions.dashboard.cards.tags.title'),
        value: t('questions.dashboard.cards.tags.value', { count: this.metrics.tags.toLocaleString() }),
        subtitle: t('questions.dashboard.cards.tags.subtitle'),
        detail: t('questions.dashboard.cards.tags.detail'),
        cta: t('questions.dashboard.cards.tags.cta'),
        route: '/questions/tags'
      },
      {
        title: t('questions.dashboard.cards.difficulties.title'),
        value: t('questions.dashboard.cards.difficulties.value', { count: this.metrics.difficulties.toLocaleString() }),
        subtitle: t('questions.dashboard.cards.difficulties.subtitle'),
        detail: t('questions.dashboard.cards.difficulties.detail'),
        cta: t('questions.dashboard.cards.difficulties.cta'),
        route: '/questions/difficulties'
      },
      {
        title: t('questions.dashboard.cards.articles.title'),
        value: t('questions.dashboard.cards.articles.value', { count: this.metrics.study_articles.toLocaleString() }),
        subtitle: t('questions.dashboard.cards.articles.subtitle'),
        detail: t('questions.dashboard.cards.articles.detail'),
        cta: t('questions.dashboard.cards.articles.cta'),
        route: '/questions/articles'
      }
    ];
  }

  private buildToolsCards(): ActionCard[] {
    const t = (key: string) => this.translate.instant(key);

    return [
      {
        title: t('questions.dashboard.cards.randomGenerator.title'),
        value: t('questions.dashboard.cards.randomGenerator.value'),
        subtitle: t('questions.dashboard.cards.randomGenerator.subtitle'),
        detail: t('questions.dashboard.cards.randomGenerator.detail'),
        cta: t('questions.dashboard.cards.randomGenerator.cta'),
        route: '/questions/random-lab'
      },
      {
        title: t('questions.dashboard.cards.bulkImportTool.title'),
        value: t('questions.dashboard.cards.bulkImportTool.value'),
        subtitle: t('questions.dashboard.cards.bulkImportTool.subtitle'),
        detail: t('questions.dashboard.cards.bulkImportTool.detail'),
        cta: t('questions.dashboard.cards.bulkImportTool.cta'),
        route: '/questions/bulk-import'
      }
    ];
  }

  private buildActivityItems(): ActivityItem[] {
    const questionItems = this.latestQuestions.slice(0, 3).map(question => ({
      title: this.translate.instant('questions.dashboard.activity.questionRefreshed', {
        type: this.translate.instant(formatQuestionType(question.question_type))
      }),
      detail: question.question_text,
      timestamp: question.last_updated_at,
      tone: 'accent' as const
    }));

    const articleItem = this.latestArticles[0]
      ? [{
          title: this.translate.instant('questions.dashboard.activity.studyArticleAvailable'),
          detail: this.translate.instant('questions.dashboard.activity.articleDetail', {
            title: this.latestArticles[0].title,
            topic: this.latestArticles[0].topic_name
          }),
          timestamp: new Date().toISOString(),
          tone: 'success' as const
        }]
      : [];

    const coverageItem = [{
      title: this.translate.instant('questions.dashboard.activity.coverageSnapshot'),
      detail: this.translate.instant('questions.dashboard.activity.coverageDetail', {
        topics: this.metrics.topics,
        tags: this.metrics.tags,
        difficulties: this.metrics.difficulties
      }),
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
