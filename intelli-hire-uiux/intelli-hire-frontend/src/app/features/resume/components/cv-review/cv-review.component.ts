import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ResumeFileService } from '../../../file/services/resume-file.service';
import { ResumeReviewService } from '../../services/resume-review.service';
import { ResumeReview, SectionScore } from '../../../../core/models/resume-review.model';

type ReviewState = 'loading' | 'no-file' | 'no-review' | 'generating' | 'done' | 'error';

@Component({
  selector: 'app-cv-review',
  standalone: true,
  imports: [CommonModule, MatButtonModule, TranslatePipe],
  templateUrl: './cv-review.component.html',
  styleUrl: './cv-review.component.scss'
})
export class CvReviewComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  state: ReviewState = 'loading';
  review: ResumeReview | null = null;
  errorMessage = '';

  readonly sectionLabels: Record<string, string> = {
    contact: 'resume.cvReview.sections.contact',
    experience: 'resume.cvReview.sections.experience',
    education: 'resume.cvReview.sections.education',
    skills: 'resume.cvReview.sections.skills',
    presentation: 'resume.cvReview.sections.presentation'
  };

  constructor(
    private fileService: ResumeFileService,
    private reviewService: ResumeReviewService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadInitial();
  }

  private loadInitial(): void {
    this.state = 'loading';
    this.fileService.getByResumeId(this.resumeId).subscribe({
      next: () => this.loadReview(),
      error: (err: HttpErrorResponse) => {
        this.state = err.status === 404 ? 'no-file' : 'error';
        if (err.status !== 404) this.errorMessage = this.translate.instant('resume.cvReview.loadFileError');
      }
    });
  }

  private loadReview(): void {
    this.reviewService.get(this.resumeId).subscribe({
      next: review => { this.review = review; this.state = 'done'; },
      error: (err: HttpErrorResponse) => {
        this.state = err.status === 404 ? 'no-review' : 'error';
        if (err.status !== 404) this.errorMessage = this.translate.instant('resume.cvReview.loadReviewError');
      }
    });
  }

  analyze(): void {
    this.state = 'generating';
    this.errorMessage = '';
    this.reviewService.generate(this.resumeId).subscribe({
      next: review => { this.review = review; this.state = 'done'; },
      error: () => {
        this.state = 'error';
        this.errorMessage = this.translate.instant('resume.cvReview.analyzeError');
      }
    });
  }

  retry(): void {
    this.loadInitial();
  }

  sectionEntries(): [string, SectionScore][] {
    if (!this.review?.sections) return [];
    return Object.entries(this.review.sections) as [string, SectionScore][];
  }

  scorePct(score: number): number {
    return Math.round(Math.min(Math.max(score * 10, 0), 100));
  }

  scoreClass(score: number): string {
    if (score >= 8) return 'score-good';
    if (score >= 6) return 'score-warn';
    return 'score-danger';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }
}
