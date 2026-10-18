import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { InterviewService } from '../../services/interview.service';
import { InterviewSessionDetail, SessionStatus } from '../../../../core/models/interview.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ReportService } from '../../../reports/services/report.service';
import { ReportResponse } from '../../../../core/models/report.model';

@Component({
  selector: 'app-interview-detail',
  standalone: true,
  imports: [
    CommonModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './interview-detail.component.html',
  styleUrl: './interview-detail.component.scss'
})
export class InterviewDetailComponent implements OnInit {
  session: InterviewSessionDetail | null = null;
  loading = false;
  reportId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private interviewService: InterviewService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    const sessionId = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.interviewService.getDetail(sessionId).subscribe({
      next: s => {
        this.session = s;
        this.loading = false;
        if (s.status === SessionStatus.COMPLETED) {
          this.reportService.getBySessionId(String(s.id)).subscribe({
            next: r => { this.reportId = r.id; },
            error: () => {}
          });
        }
      },
      error: () => { this.loading = false; }
    });
  }

  getAnswerForQuestion(questionId: string): string {
    return this.session?.answers.find(a => a.question_id === questionId)?.answer_text ?? '—';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      [SessionStatus.CREATED]: 'Đã tạo',
      [SessionStatus.ACTIVE]: 'Đang diễn ra',
      [SessionStatus.COMPLETED]: 'Hoàn thành',
      [SessionStatus.EXPIRED]: 'Hết hạn'
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      [SessionStatus.CREATED]: 'status-created',
      [SessionStatus.ACTIVE]: 'status-active',
      [SessionStatus.COMPLETED]: 'status-completed',
      [SessionStatus.EXPIRED]: 'status-expired'
    };
    return map[status] ?? '';
  }

  viewReport(): void {
    if (this.reportId) {
      this.router.navigate(['/reports', this.reportId]);
    }
  }

  back(): void {
    this.router.navigate(['/interviews']);
  }
}
