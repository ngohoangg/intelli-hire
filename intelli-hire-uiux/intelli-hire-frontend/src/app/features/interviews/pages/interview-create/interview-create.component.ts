import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InterviewService } from '../../services/interview.service';
import { ResumeService } from '../../../resume/services/resume.service';
import { CreateSessionRequest, Difficulty } from '../../../../core/models/interview.model';
import { Resume, ResumeStatus } from '../../../../core/models/resume.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

type QuestionSource = 'bank' | 'resume';

@Component({
  selector: 'app-interview-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatSliderModule,
    MatSnackBarModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './interview-create.component.html',
  styleUrl: './interview-create.component.scss'
})
export class InterviewCreateComponent implements OnInit {
  form: FormGroup;
  saving = false;
  loadingResumes = false;
  questionSource: QuestionSource = 'bank';
  resumes: Resume[] = [];

  readonly difficulties = [
    { value: Difficulty.JUNIOR, label: 'Junior (Mới vào nghề)', apiLabel: 'Junior' },
    { value: Difficulty.MID, label: 'Mid (Trung cấp)', apiLabel: 'Mid' },
    { value: Difficulty.SENIOR, label: 'Senior (Cao cấp)', apiLabel: 'Senior' }
  ];

  readonly questionTypes = [
    { value: 'technical', label: 'Kỹ thuật' },
    { value: 'behavioral', label: 'Hành vi' },
    { value: 'mixed', label: 'Kết hợp' }
  ];

  readonly languages = [
    { value: 'English', label: 'Tiếng Anh' },
    { value: 'Vietnamese', label: 'Tiếng Việt' }
  ];

  constructor(
    private fb: FormBuilder,
    private interviewService: InterviewService,
    private resumeService: ResumeService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      // Bank fields
      topic: ['', [Validators.required, Validators.maxLength(100)]],
      difficulty: [Difficulty.MID, [Validators.required]],
      // Resume fields
      resume_id: [''],
      question_type: ['technical'],
      language: ['English'],
      difficulty_name: [Difficulty.MID],
      // Common
      question_count: [5, [Validators.required, Validators.min(1), Validators.max(20)]]
    });
  }

  ngOnInit(): void {
    this.loadResumes();
  }

  private loadResumes(): void {
    this.loadingResumes = true;
    this.resumeService.list().subscribe({
      next: (resumes) => {
        this.resumes = resumes.filter(r => r.status === ResumeStatus.COMPLETED);
        this.loadingResumes = false;
      },
      error: () => { this.loadingResumes = false; }
    });
  }

  setSource(source: QuestionSource): void {
    this.questionSource = source;
    const topicCtrl = this.form.get('topic')!;
    const diffCtrl = this.form.get('difficulty')!;
    const resumeCtrl = this.form.get('resume_id')!;

    if (source === 'bank') {
      topicCtrl.setValidators([Validators.required, Validators.maxLength(100)]);
      diffCtrl.setValidators([Validators.required]);
      resumeCtrl.clearValidators();
    } else {
      topicCtrl.clearValidators();
      diffCtrl.clearValidators();
      resumeCtrl.setValidators([Validators.required]);
    }
    topicCtrl.updateValueAndValidity();
    diffCtrl.updateValueAndValidity();
    resumeCtrl.updateValueAndValidity();
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const v = this.form.value;

    let request: CreateSessionRequest;

    if (this.questionSource === 'bank') {
      request = {
        topic: v.topic,
        difficulty: v.difficulty,
        question_source: 'bank',
        question_count: v.question_count
      };
    } else {
      const selectedResume = this.resumes.find(r => r.id === v.resume_id);
      const diffLabel = this.difficulties.find(d => d.value === v.difficulty_name)?.apiLabel ?? 'Mid';
      request = {
        topic: selectedResume?.title ?? '',
        difficulty: v.difficulty_name,
        question_source: 'resume',
        resume_id: v.resume_id,
        question_type: v.question_type,
        language: v.language,
        difficulty_name: diffLabel,
        question_count: v.question_count
      };
    }

    this.interviewService.create(request).subscribe({
      next: session => {
        this.saving = false;
        this.router.navigate(['/interviews', session.id, 'room']);
      },
      error: (err) => {
        this.saving = false;
        const msg = err?.error?.message ?? 'Không thể tạo phiên phỏng vấn. Vui lòng kiểm tra dịch vụ phỏng vấn đang chạy.';
        this.snackBar.open(msg, 'Đóng', { duration: 5000 });
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/interviews']);
  }
}
