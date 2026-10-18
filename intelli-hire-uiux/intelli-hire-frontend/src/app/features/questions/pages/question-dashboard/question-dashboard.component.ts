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
        title: 'Ngân hàng câu hỏi',
        value: `${totalQuestions.toLocaleString('vi-VN')} câu hỏi`,
        subtitle: `${this.metrics.active_questions.toLocaleString('vi-VN')} hoạt động / ${this.metrics.inactive_questions.toLocaleString('vi-VN')} ngừng hoạt động`,
        detail: 'Tìm kiếm, lọc và tuyển chọn kho câu hỏi phỏng vấn.',
        cta: 'Mở',
        route: '/questions'
      },
      {
        title: 'Chủ đề',
        value: `${this.metrics.topics.toLocaleString('vi-VN')} chủ đề`,
        subtitle: 'Cấu trúc cây',
        detail: 'Quản lý phân loại cha-con dùng chung cho luyện tập và báo cáo.',
        cta: 'Quản lý',
        route: '/questions/topics'
      },
      {
        title: 'Thẻ',
        value: `${this.metrics.tags.toLocaleString('vi-VN')} thẻ`,
        subtitle: 'Nhãn tái sử dụng',
        detail: 'Quản lý bộ lọc xuyên chủ đề dùng cho tìm kiếm và tạo câu hỏi ngẫu nhiên.',
        cta: 'Quản lý',
        route: '/questions/tags'
      },
      {
        title: 'Mức độ khó',
        value: `${this.metrics.difficulties.toLocaleString('vi-VN')} mức độ`,
        subtitle: 'Chấm điểm theo trọng số',
        detail: 'Giữ tên, trọng số và mô tả mức độ khó thống nhất với danh mục.',
        cta: 'Quản lý',
        route: '/questions/difficulties'
      },
      {
        title: 'Bài viết học tập',
        value: `${this.metrics.study_articles.toLocaleString('vi-VN')} tài liệu`,
        subtitle: 'Theo chủ đề và độ khó',
        detail: 'Tuyển chọn tài liệu học tập phục vụ xem trước khi chuẩn bị và ôn tập sau buổi phỏng vấn.',
        cta: 'Mở',
        route: '/questions/articles'
      }
    ];
  }

  private buildToolsCards(): ActionCard[] {
    return [
      {
        title: 'Trình tạo ngẫu nhiên',
        value: 'Phòng thử phỏng vấn',
        subtitle: 'Kiểm tra kho câu hỏi và bộ lọc',
        detail: 'Kiểm tra chất lượng chọn ngẫu nhiên, hành vi cache và các tổ hợp phân loại trước khi triển khai cho người dùng.',
        cta: 'Mở phòng thử',
        route: '/questions/random-lab'
      },
      {
        title: 'Nhập hàng loạt',
        value: 'Nhập lô JSON',
        subtitle: 'Tối đa 100 câu hỏi',
        detail: 'Dán dữ liệu hàng loạt, kiểm tra cấu trúc và xem lỗi từng dòng mà không cần rời mô-đun.',
        cta: 'Mở trang nhập',
        route: '/questions/bulk-import'
      }
    ];
  }

  private buildActivityItems(): ActivityItem[] {
    const questionItems = this.latestQuestions.slice(0, 3).map(question => ({
      title: `Câu hỏi ${formatQuestionType(question.question_type)} vừa được cập nhật`,
      detail: question.question_text,
      timestamp: question.last_updated_at,
      tone: 'accent' as const
    }));

    const articleItem = this.latestArticles[0]
      ? [{
          title: 'Có bài viết học tập',
          detail: `${this.latestArticles[0].title} • ${this.latestArticles[0].topic_name}`,
          timestamp: new Date().toISOString(),
          tone: 'success' as const
        }]
      : [];

    const coverageItem = [{
      title: 'Tổng quan phạm vi danh mục',
      detail: `${this.metrics.topics} chủ đề • ${this.metrics.tags} thẻ • ${this.metrics.difficulties} mức độ khó`,
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
