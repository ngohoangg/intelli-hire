import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe } from '@ngx-translate/core';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import {
  DifficultyLevelResponse,
  QuestionResponse,
  QuestionType,
  TagResponse
} from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import {
  QUESTION_TYPE_OPTIONS,
  TopicOption,
  flattenTopicTree,
  formatQuestionType
} from '../../questions.utils';

@Component({
  selector: 'app-random-lab',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatSelectModule,
    LoadingSpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './random-lab.component.html',
  styleUrl: './random-lab.component.scss'
})
export class RandomLabComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly questionTypes = QUESTION_TYPE_OPTIONS;

  loading = false;
  topicOptions: TopicOption[] = [];
  difficulties: DifficultyLevelResponse[] = [];
  tags: TagResponse[] = [];
  generatedQuestions: QuestionResponse[] = [];

  readonly form = this.fb.group({
    topic_id: [''],
    difficulty_id: [''],
    type: ['' as QuestionType | ''],
    tag_ids: [[] as string[]],
    count: [5, [Validators.required, Validators.min(1), Validators.max(50)]],
    exclude_ids: ['']
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.prefillFromQuery();
    this.loadOptions();
  }

  formatType(value: string): string {
    return formatQuestionType(value);
  }

  runLab(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.questionBankService.getRandomQuestions({
      topic_id: this.form.value.topic_id || undefined,
      difficulty_id: this.form.value.difficulty_id || undefined,
      type: this.form.value.type ?? '',
      tag_ids: this.form.value.tag_ids ?? [],
      count: Number(this.form.value.count ?? 5),
      exclude_ids: this.parseExcludeIds(this.form.value.exclude_ids ?? '')
    }).subscribe({
      next: questions => {
        this.generatedQuestions = questions;
        this.loading = false;
      },
      error: () => {
        this.generatedQuestions = [];
        this.loading = false;
      }
    });
  }

  openQuestion(questionId: string): void {
    this.router.navigate(['/questions', questionId]);
  }

  private prefillFromQuery(): void {
    const query = this.route.snapshot.queryParamMap;
    this.form.patchValue({
      topic_id: query.get('topicId') ?? '',
      difficulty_id: query.get('difficultyId') ?? '',
      type: (query.get('type') as QuestionType | '') ?? ''
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

  private parseExcludeIds(raw: string): string[] {
    return raw
      .split(/[\n,]/)
      .map(item => item.trim())
      .filter(Boolean);
  }
}
