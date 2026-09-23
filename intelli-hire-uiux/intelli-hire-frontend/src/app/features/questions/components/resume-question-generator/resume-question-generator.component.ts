import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  DifficultyLevelResponse,
  GenerateQuestionsFromResumeResponse,
  QuestionType
} from '../../../../core/models/question.model';
import { ResumeSkillProfile } from '../../../../core/models/skill.model';
import { formatDateTime, formatQuestionType, parseListInput, QUESTION_TYPE_OPTIONS } from '../../questions.utils';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';

@Component({
  selector: 'app-resume-question-generator',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe
  ],
  templateUrl: './resume-question-generator.component.html',
  styleUrl: './resume-question-generator.component.scss'
})
export class ResumeQuestionGeneratorComponent implements OnInit, OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly translate = inject(TranslateService);

  @Input({ required: true }) resumeId!: string;
  @Input() profile: ResumeSkillProfile | null = null;

  readonly questionTypes = QUESTION_TYPE_OPTIONS;
  readonly languageOptions = [
    { value: 'en', label: 'questions.resumeGenerator.languageEn' },
    { value: 'vi', label: 'questions.resumeGenerator.languageVi' }
  ] as const;

  loading = false;
  loadingDifficulties = false;
  errorMessage = '';
  difficulties: DifficultyLevelResponse[] = [];
  result: GenerateQuestionsFromResumeResponse | null = null;

  readonly form = this.fb.nonNullable.group({
    count: [8, [Validators.required, Validators.min(1), Validators.max(20)]],
    question_type: ['' as QuestionType | ''],
    difficulty_name: [''],
    language: ['en', [Validators.required]],
    focus_areas: ['']
  });

  constructor(
    private questionBankService: QuestionBankService,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.loadDifficulties();
    this.prefillFocusAreas();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['profile']) {
      this.prefillFocusAreas();
    }
  }

  get suggestedFocusAreas(): string[] {
    return Array.from(
      new Set(
        Object.keys(this.profile?.top_skills ?? {})
          .map(item => item.trim())
          .filter(Boolean)
      )
    ).slice(0, 6);
  }

  get focusAreasPreview(): string[] {
    return parseListInput(this.form.controls.focus_areas.value);
  }

  get generatedCount(): number {
    return this.result?.total_questions ?? this.result?.questions.length ?? 0;
  }

  formatType(value: string | QuestionType | null | undefined): string {
    return formatQuestionType(value);
  }

  formatGeneratedAt(value: string | null | undefined): string {
    return formatDateTime(value, this.translate.instant('questions.placeholders.notAvailable'));
  }

  generate(): void {
    if (this.form.invalid || !this.resumeId) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.questionBankService.generateFromResume({
      resume_id: this.resumeId,
      count: Number(this.form.controls.count.value),
      question_type: this.form.controls.question_type.value || undefined,
      difficulty_name: this.form.controls.difficulty_name.value || undefined,
      language: this.form.controls.language.value,
      focus_areas: this.focusAreasPreview.length ? this.focusAreasPreview : undefined
    }).subscribe({
      next: result => {
        this.result = result;
        this.loading = false;
      },
      error: () => {
        this.result = null;
        this.loading = false;
        this.errorMessage = this.translate.instant('questions.resumeGenerator.errorMessage');
      }
    });
  }

  applySuggestedFocusAreas(): void {
    this.form.controls.focus_areas.setValue(this.suggestedFocusAreas.join('\n'));
    this.form.controls.focus_areas.markAsPristine();
  }

  toggleSuggestedFocusArea(area: string): void {
    const next = new Set(this.focusAreasPreview);

    if (next.has(area)) {
      next.delete(area);
    } else {
      next.add(area);
    }

    this.form.controls.focus_areas.setValue(Array.from(next).join('\n'));
    this.form.controls.focus_areas.markAsDirty();
  }

  isFocusAreaSelected(area: string): boolean {
    return this.focusAreasPreview.includes(area);
  }

  resetForm(): void {
    this.form.reset({
      count: 8,
      question_type: '',
      difficulty_name: '',
      language: 'en',
      focus_areas: this.suggestedFocusAreas.join('\n')
    });
    this.errorMessage = '';
  }

  private loadDifficulties(): void {
    this.loadingDifficulties = true;

    this.taxonomyService.getDifficulties().subscribe({
      next: difficulties => {
        this.difficulties = difficulties;
        this.loadingDifficulties = false;
      },
      error: () => {
        this.difficulties = [];
        this.loadingDifficulties = false;
      }
    });
  }

  private prefillFocusAreas(): void {
    const control = this.form.controls.focus_areas;

    if (control.dirty || control.value.trim()) {
      return;
    }

    control.setValue(this.suggestedFocusAreas.join('\n'));
  }
}
