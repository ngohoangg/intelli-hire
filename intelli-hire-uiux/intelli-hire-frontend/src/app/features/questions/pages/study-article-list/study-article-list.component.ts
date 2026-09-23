import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  DifficultyLevelResponse,
  StudyArticleResponse
} from '../../../../core/models/question.model';
import { QuestionStudyArticleService } from '../../services/question-study-article.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import { TopicOption, flattenTopicTree } from '../../questions.utils';

type ArticleActiveFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';

@Component({
  selector: 'app-study-article-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatDialogModule,
    MatCheckboxModule,
    MatSelectModule,
    LoadingSpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './study-article-list.component.html',
  styleUrl: './study-article-list.component.scss'
})
export class StudyArticleListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly translate = inject(TranslateService);

  loading = false;
  saving = false;
  selectedArticle: StudyArticleResponse | null = null;

  topicOptions: TopicOption[] = [];
  difficulties: DifficultyLevelResponse[] = [];
  articles: StudyArticleResponse[] = [];
  filteredArticles: StudyArticleResponse[] = [];

  readonly filterForm = this.fb.group({
    topicId: [''],
    difficultyId: [''],
    activeState: ['ALL' as ArticleActiveFilter]
  });

  readonly editorForm = this.fb.group({
    topic_id: ['', [Validators.required]],
    difficulty_id: ['', [Validators.required]],
    title: ['', [Validators.required, Validators.maxLength(255)]],
    url: ['', [Validators.required, Validators.maxLength(500)]],
    description: [''],
    source: ['', [Validators.maxLength(100)]],
    is_active: [true]
  });

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private taxonomyService: QuestionTaxonomyService,
    private studyArticleService: QuestionStudyArticleService
  ) {}

  ngOnInit(): void {
    this.prefillFiltersFromQuery();
    this.loadPage();
  }

  get activeCount(): number {
    return this.articles.filter(article => article.is_active).length;
  }

  get uniqueSources(): number {
    return new Set(this.articles.map(article => article.source).filter(Boolean)).size;
  }

  startCreate(): void {
    this.selectedArticle = null;
    this.editorForm.reset({
      topic_id: this.filterForm.value.topicId ?? '',
      difficulty_id: this.filterForm.value.difficultyId ?? '',
      title: '',
      url: '',
      description: '',
      source: '',
      is_active: true
    });
  }

  selectArticle(article: StudyArticleResponse): void {
    this.selectedArticle = article;
    this.editorForm.patchValue({
      topic_id: article.topic_id,
      difficulty_id: article.difficulty_id,
      title: article.title,
      url: article.url,
      description: article.description ?? '',
      source: article.source ?? '',
      is_active: article.is_active
    });
  }

  applyFilters(): void {
    this.loadArticles();
  }

  resetFilters(): void {
    this.filterForm.reset({
      topicId: '',
      difficultyId: '',
      activeState: 'ALL'
    });
    this.loadArticles();
  }

  submit(): void {
    if (this.editorForm.invalid) {
      this.editorForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const request = {
      topic_id: this.editorForm.value.topic_id ?? '',
      difficulty_id: this.editorForm.value.difficulty_id ?? '',
      title: this.editorForm.value.title ?? '',
      url: this.editorForm.value.url ?? '',
      description: this.editorForm.value.description ?? '',
      source: this.editorForm.value.source ?? '',
      is_active: this.editorForm.value.is_active ?? true
    };

    const operation = this.selectedArticle
      ? this.studyArticleService.update(this.selectedArticle.id, request)
      : this.studyArticleService.create(request);

    operation.subscribe({
      next: article => {
        this.selectedArticle = article;
        this.loadArticles();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  deleteArticle(article: StudyArticleResponse, event?: Event): void {
    event?.stopPropagation();

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('questions.studyArticleList.deleteDialog.title'),
        message: this.translate.instant('questions.studyArticleList.deleteDialog.message')
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      this.studyArticleService.delete(article.id).subscribe({
        next: () => {
          if (this.selectedArticle?.id === article.id) {
            this.startCreate();
          }
          this.loadArticles();
        },
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  private prefillFiltersFromQuery(): void {
    const query = this.route.snapshot.queryParamMap;
    this.filterForm.patchValue({
      topicId: query.get('topicId') ?? '',
      difficultyId: query.get('difficultyId') ?? '',
      activeState: 'ALL'
    });
  }

  private loadPage(): void {
    this.loading = true;

    forkJoin({
      topicTree: this.taxonomyService.getTopicTree().pipe(catchError(() => of([]))),
      difficulties: this.taxonomyService.getDifficulties().pipe(catchError(() => of([])))
    }).subscribe({
      next: result => {
        this.topicOptions = flattenTopicTree(result.topicTree);
        this.difficulties = result.difficulties;
        this.startCreate();
        this.loadArticles();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private loadArticles(): void {
    this.loading = true;

    this.studyArticleService.getAll({
      topicId: this.filterForm.value.topicId ?? '',
      difficultyId: this.filterForm.value.difficultyId ?? '',
      page: 0,
      size: 100
    }).pipe(catchError(() => of({
      content: [],
      page: 0,
      size: 0,
      total_elements: 0,
      total_pages: 0,
      is_first: true,
      is_last: true
    }))).subscribe({
      next: page => {
        this.articles = page.content;
        this.filteredArticles = this.applyActiveStateFilter(page.content);
        this.loading = false;
        this.saving = false;
      },
      error: () => {
        this.loading = false;
        this.saving = false;
      }
    });
  }

  private applyActiveStateFilter(items: StudyArticleResponse[]): StudyArticleResponse[] {
    const activeState = this.filterForm.value.activeState ?? 'ALL';

    if (activeState === 'ACTIVE') {
      return items.filter(item => item.is_active);
    }

    if (activeState === 'INACTIVE') {
      return items.filter(item => !item.is_active);
    }

    return items;
  }
}
