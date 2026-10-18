import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { HttpErrorResponse } from '@angular/common/http';
import { ResumeFileService } from '../../../file/services/resume-file.service';
import { ResumeReviewService } from '../../services/resume-review.service';
import { ResumeReview, SectionScore } from '../../../../core/models/resume-review.model';

type ReviewState = 'loading' | 'no-file' | 'no-review' | 'generating' | 'done' | 'error';

@Component({
  selector: 'app-cv-review',
  standalone: true,
  imports: [CommonModule, MatButtonModule],
  templateUrl: './cv-review.component.html',
  styleUrl: './cv-review.component.scss'
})
export class CvReviewComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  state: ReviewState = 'loading';
  review: ResumeReview | null = null;
  errorMessage = '';

  readonly sectionLabels: Record<string, string> = {
    contact: 'Liên hệ',
    experience: 'Kinh nghiệm',
    education: 'Học vấn',
    skills: 'Kỹ năng',
    presentation: 'Trình bày'
  };

  constructor(
    private fileService: ResumeFileService,
    private reviewService: ResumeReviewService
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
        if (err.status !== 404) this.errorMessage = 'Không thể kiểm tra trạng thái tệp.';
      }
    });
  }

  private loadReview(): void {
    this.reviewService.get(this.resumeId).subscribe({
      next: review => { this.review = review; this.state = 'done'; },
      error: (err: HttpErrorResponse) => {
        this.state = err.status === 404 ? 'no-review' : 'error';
        if (err.status !== 404) this.errorMessage = 'Không thể tải kết quả đánh giá.';
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
        this.errorMessage = 'Đánh giá bằng AI thất bại. Vui lòng thử lại.';
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
      year: 'numeric', month: '2-digit', day: '2-digit'
    });
  }
}
