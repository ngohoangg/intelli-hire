import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import {
  DifficultyLevelResponse,
  QuestionRequest,
  QuestionType,
  TagResponse
} from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import { AdminQuestionService } from '../../../../core/services/admin-question.service';
import { AuthService } from '../../../../core/services/auth.service';
import {
  QUESTION_TYPE_OPTIONS,
  TopicOption,
  flattenTopicTree,
  formatQuestionType
} from '../../questions.utils';

@Component({
  selector: 'app-question-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCheckboxModule,
    MatSelectModule,
    LoadingSpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './question-create.component.html',
  styleUrl: './question-create.component.scss'
})
export class QuestionCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly translate = inject(TranslateService);

  readonly questionTypes = QUESTION_TYPE_OPTIONS;

  loading = false;
  saving = false;
  creatingTag = false;
  successMessage = '';

  topicOptions: TopicOption[] = [];
  difficulties: DifficultyLevelResponse[] = [];
  tags: TagResponse[] = [];
  selectedTagList: TagResponse[] = [];

  get isAdminCreate(): boolean {
    return this.authService.isAdmin;
  }

  readonly quickTagControl = this.fb.control('', [Validators.required, Validators.maxLength(100)]);

  readonly form = this.fb.group({
    question_text: ['', [Validators.required, Validators.maxLength(5000)]],
    topic_id: ['', [Validators.required]],
    difficulty_id: ['', [Validators.required]],
    question_type: [QuestionType.TECHNICAL, [Validators.required]],
    tag_ids: [[] as string[]],
    is_active: [true]
  });

  constructor(
    private router: Router,
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService,
    private adminQuestionService: AdminQuestionService
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.patchDraftFromNavigation();
    this.form.controls.tag_ids.valueChanges.subscribe(ids => {
      const selected = ids ?? [];
      this.selectedTagList = this.tags.filter(tag => selected.includes(tag.id));
    });
  }

  formatType(value: string): string {
    return formatQuestionType(value);
  }

  createTag(): void {
    if (this.quickTagControl.invalid || this.creatingTag) {
      this.quickTagControl.markAsTouched();
      return;
    }

    this.creatingTag = true;

    this.taxonomyService.createTag({ name: this.quickTagControl.value ?? '' }).subscribe({
      next: tag => {
        this.tags = [...this.tags, tag].sort((a, b) => a.name.localeCompare(b.name));
        this.form.patchValue({
          tag_ids: [...(this.form.value.tag_ids ?? []), tag.id]
        });
        this.quickTagControl.reset('');
        this.creatingTag = false;
      },
      error: () => {
        this.creatingTag = false;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.quickTagControl.markAsTouched();
      return;
    }

    this.saving = true;

    const create$ = this.questionBankService.create(this.buildQuestionPayload());
    const withApproval$ = this.isAdminCreate
      ? create$.pipe(
          switchMap(question => this.adminQuestionService.patchModerationStatus(question.id, 'APPROVED').pipe(
            map(() => question),
            catchError(() => of(question))
          ))
        )
      : create$;

    withApproval$.subscribe({
      next: () => {
        this.saving = false;
        if (this.isAdminCreate) {
          this.successMessage = this.translate.instant('questions.questionCreate.successApproved');
          this.adminQuestionService.refreshPendingCount();
          setTimeout(() => this.router.navigate(['/admin/questions']), 1500);
        } else {
          this.successMessage = this.translate.instant('questions.questionCreate.successSubmitted');
          setTimeout(() => this.router.navigate(['/questions/submitted']), 1500);
        }
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  private loadOptions(): void {
    this.loading = true;

    forkJoin({
      topicTree: this.taxonomyService.getTopicTree().pipe(catchError(() => of([]))),
      difficulties: this.taxonomyService.getDifficulties().pipe(catchError(() => of([]))),
      tags: this.taxonomyService.getTags().pipe(catchError(() => of([])))
    }).subscribe({
      next: result => {
        this.topicOptions = flattenTopicTree(result.topicTree);
        this.difficulties = result.difficulties;
        this.tags = result.tags;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private patchDraftFromNavigation(): void {
    const draft = window.history.state?.draft as Partial<QuestionRequest> | undefined;
    if (!draft) {
      return;
    }

    this.form.patchValue({
      topic_id: draft.topic_id ?? '',
      difficulty_id: draft.difficulty_id ?? '',
      question_text: draft.question_text ?? '',
      question_type: draft.question_type ?? QuestionType.TECHNICAL,
      tag_ids: draft.tag_ids ?? [],
      is_active: draft.is_active ?? true
    });
  }

  private buildQuestionPayload(): QuestionRequest {
    return {
      topic_id: this.form.value.topic_id ?? '',
      difficulty_id: this.form.value.difficulty_id ?? '',
      question_text: this.form.value.question_text ?? '',
      question_type: this.form.value.question_type ?? QuestionType.TECHNICAL,
      tag_ids: this.form.value.tag_ids ?? [],
      is_active: this.form.value.is_active ?? true
    };
  }
}
