import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  DifficultyLevelResponse,
  PageResponse,
  QuestionResponse,
  QuestionType,
  TagResponse,
  TopicTreeResponse
} from '../../../../core/models/question.model';
import { AuthService } from '../../../../core/services/auth.service';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import {
  QUESTION_SORT_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  TopicOption,
  flattenTopicTree,
  formatDateTime,
  formatQuestionType
} from '../../questions.utils';

@Component({
  selector: 'app-question-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './question-list.component.html',
  styleUrl: './question-list.component.scss'
})
export class QuestionListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly displayedColumns = [
    'question_text',
    'topic',
    'difficulty',
    'type',
    'tags',
    'status',
    'updated',
    'actions'
  ];
  readonly questionTypes = QUESTION_TYPE_OPTIONS;
  readonly sortOptions = QUESTION_SORT_OPTIONS;
  private readonly destroyRef = inject(DestroyRef);

  loading = false;
  questions: QuestionResponse[] = [];
  topicTree: TopicTreeResponse[] = [];
  topicOptions: TopicOption[] = [];
  difficulties: DifficultyLevelResponse[] = [];
  tags: TagResponse[] = [];
  totalElements = 0;
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.group({
    keyword: [''],
    topicId: [''],
    difficultyId: [''],
    type: ['' as QuestionType | ''],
    tagIds: [[] as string[]],
    isActive: ['' as boolean | ''],
    sort: [this.sortOptions[0].value]
  });

  get isAdmin(): boolean {
    return this.auth.isAdmin;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private auth: AuthService,
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.patchFiltersFromQuery();
    this.loadCatalogOptions();
  }

  formatDate(value: string): string {
    return formatDateTime(value);
  }

  formatType(value: string | QuestionType | null | undefined): string {
    return formatQuestionType(value);
  }

  getTagPreview(question: QuestionResponse): string {
    return question.tags.length
      ? question.tags.map(tag => tag.name).join(', ')
      : 'No tags';
  }

  getStatusClass(question: QuestionResponse): string {
    return question.is_active ? 'active' : 'inactive';
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadQuestions();
  }

  resetFilters(): void {
    this.filterForm.reset({
      keyword: '',
      topicId: '',
      difficultyId: '',
      type: '',
      tagIds: [],
      isActive: '',
      sort: this.sortOptions[0].value
    });
    this.applyFilters();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadQuestions();
  }

  openQuestion(id: string): void {
    this.router.navigate(['/questions', id]);
  }

  editQuestion(event: Event, id: string): void {
    event.stopPropagation();
    this.router.navigate(['/questions', id, 'edit']);
  }

  archiveQuestion(event: Event, question: QuestionResponse): void {
    event.stopPropagation();

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Archive question',
        message: 'This action performs a soft delete in Question Service and removes the question from the active pool.'
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      this.questionBankService.delete(question.id).subscribe({
        next: () => this.loadQuestions(),
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  private loadCatalogOptions(): void {
    this.loading = true;

    forkJoin({
      topicTree: this.taxonomyService.getTopicTree().pipe(catchError(() => of([]))),
      difficulties: this.taxonomyService.getDifficulties().pipe(catchError(() => of([]))),
      tags: this.taxonomyService.getTags().pipe(catchError(() => of([])))
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: result => {
          this.topicTree = result.topicTree;
          this.topicOptions = flattenTopicTree(result.topicTree);
          this.difficulties = result.difficulties;
          this.tags = result.tags;
          this.loadQuestions();
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private loadQuestions(): void {
    this.loading = true;

    this.questionBankService.search({
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.filterForm.value.sort ?? this.sortOptions[0].value,
      topicId: this.filterForm.value.topicId ?? '',
      difficultyId: this.filterForm.value.difficultyId ?? '',
      type: this.filterForm.value.type ?? '',
      tagIds: this.filterForm.value.tagIds ?? [],
      keyword: this.filterForm.value.keyword ?? '',
      isActive: !this.isAdmin ? true : (this.filterForm.value.isActive === '' ? undefined : this.filterForm.value.isActive ?? undefined),
      status: 'APPROVED'
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page: PageResponse<QuestionResponse>) => {
          this.questions = page.content;
          this.totalElements = page.total_elements;
          this.loading = false;
        },
        error: () => {
          this.questions = [];
          this.totalElements = 0;
          this.loading = false;
        }
      });
  }

  private patchFiltersFromQuery(): void {
    const query = this.route.snapshot.queryParamMap;
    const tagId = query.get('tagId');

    this.filterForm.patchValue({
      topicId: query.get('topicId') ?? '',
      difficultyId: query.get('difficultyId') ?? '',
      tagIds: tagId ? [tagId] : [],
      keyword: query.get('keyword') ?? ''
    });
  }
}
