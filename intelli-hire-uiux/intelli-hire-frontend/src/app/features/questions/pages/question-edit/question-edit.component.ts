import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import {
  DifficultyLevelResponse,
  QuestionDetail,
  QuestionType,
  RubricResponse,
  TagResponse
} from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import {
  QUESTION_TYPE_OPTIONS,
  TopicOption,
  flattenTopicTree,
  formatQuestionType,
  parseListInput
} from '../../questions.utils';

@Component({
  selector: 'app-question-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCheckboxModule,
    MatSelectModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './question-edit.component.html',
  styleUrl: './question-edit.component.scss'
})
export class QuestionEditComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly questionTypes = QUESTION_TYPE_OPTIONS;

  questionId = '';
  loading = false;
  saving = false;
  creatingTag = false;
  rubricExists = false;

  topicOptions: TopicOption[] = [];
  difficulties: DifficultyLevelResponse[] = [];
  tags: TagResponse[] = [];

  readonly quickTagControl = this.fb.control('', [Validators.required, Validators.maxLength(100)]);

  readonly form = this.fb.group({
    question_text: ['', [Validators.required, Validators.maxLength(5000)]],
    topic_id: ['', [Validators.required]],
    difficulty_id: ['', [Validators.required]],
    question_type: [QuestionType.TECHNICAL, [Validators.required]],
    tag_ids: [[] as string[]],
    is_active: [true],
    criteria: [''],
    keywords: [''],
    example_answer: [''],
    common_mistakes: [''],
    hints: [''],
    max_score: [10, [Validators.required, Validators.min(1), Validators.max(100)]]
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.questionId = this.route.snapshot.paramMap.get('id') ?? '';
    if (!this.questionId) {
      this.router.navigate(['/questions']);
      return;
    }

    this.loadPage();
  }

  formatType(value: string): string {
    return formatQuestionType(value);
  }

  selectedTags(): TagResponse[] {
    const selectedIds = this.form.value.tag_ids ?? [];
    return this.tags.filter(tag => selectedIds.includes(tag.id));
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
    if (this.form.invalid || !this.questionId) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;

    this.questionBankService.update(this.questionId, {
      topic_id: this.form.value.topic_id ?? '',
      difficulty_id: this.form.value.difficulty_id ?? '',
      question_text: this.form.value.question_text ?? '',
      question_type: this.form.value.question_type ?? QuestionType.TECHNICAL,
      tag_ids: this.form.value.tag_ids ?? [],
      is_active: this.form.value.is_active ?? true
    }).pipe(
      switchMap(question => {
        const rubricPayload = this.buildRubricPayload();
        if (!rubricPayload) {
          return of(question);
        }

        const rubricRequest = this.rubricExists
          ? this.questionBankService.updateRubric(this.questionId, rubricPayload)
          : this.questionBankService.createRubric(this.questionId, rubricPayload);

        return rubricRequest.pipe(
          map(() => question),
          catchError(() => of(question))
        );
      })
    ).subscribe({
      next: question => {
        this.router.navigate(['/questions', question.id]);
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  private loadPage(): void {
    this.loading = true;

    forkJoin({
      topicTree: this.taxonomyService.getTopicTree().pipe(catchError(() => of([]))),
      difficulties: this.taxonomyService.getDifficulties().pipe(catchError(() => of([]))),
      tags: this.taxonomyService.getTags().pipe(catchError(() => of([]))),
      question: this.questionBankService.getById(this.questionId),
      rubric: this.questionBankService.getRubric(this.questionId).pipe(catchError(() => of(null)))
    }).subscribe({
      next: result => {
        // Redirect if question is no longer editable
        if (result.question.status === 'APPROVED' || result.question.status === 'ARCHIVED') {
          this.router.navigate(['/questions', this.questionId]);
          return;
        }

        this.topicOptions = flattenTopicTree(result.topicTree);
        this.difficulties = result.difficulties;
        this.tags = result.tags;
        this.patchForm(result.question, result.rubric);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/questions']);
      }
    });
  }

  private patchForm(question: QuestionDetail, rubric: RubricResponse | null): void {
    this.rubricExists = !!rubric;
    this.form.patchValue({
      question_text: question.question_text,
      topic_id: question.topic_id,
      difficulty_id: question.difficulty_id,
      question_type: question.question_type,
      tag_ids: question.tags.map((tag: TagResponse) => tag.id),
      is_active: question.is_active,
      criteria: rubric?.criteria ?? '',
      keywords: rubric?.keywords.join('\n') ?? '',
      example_answer: rubric?.example_answer ?? '',
      common_mistakes: rubric?.common_mistakes.join('\n') ?? '',
      hints: rubric?.hints.join('\n') ?? '',
      max_score: rubric?.max_score ?? 10
    });
  }

  private buildRubricPayload() {
    const criteria = (this.form.value.criteria ?? '').trim();
    if (!criteria) {
      return null;
    }

    return {
      criteria,
      keywords: parseListInput(this.form.value.keywords ?? ''),
      example_answer: this.form.value.example_answer ?? '',
      common_mistakes: parseListInput(this.form.value.common_mistakes ?? ''),
      hints: parseListInput(this.form.value.hints ?? ''),
      max_score: Number(this.form.value.max_score ?? 10)
    };
  }
}
