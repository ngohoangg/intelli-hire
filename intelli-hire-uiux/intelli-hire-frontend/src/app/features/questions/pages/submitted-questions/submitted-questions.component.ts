import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { EMPTY, Subscription, interval } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { QuestionSubmissionService } from '../../services/question-submission.service';
import { QuestionResponse } from '../../../../core/models/question.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

const POLL_INTERVAL_MS = 10_000;

@Component({
  selector: 'app-submitted-questions',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './submitted-questions.component.html',
  styleUrl: './submitted-questions.component.scss'
})
export class SubmittedQuestionsComponent implements OnInit, OnDestroy {
  readonly Math = Math;

  questions: QuestionResponse[] = [];
  loading = false;
  totalElements = 0;
  page = 0;
  size = 20;
  resubmittingId: string | null = null;
  withdrawingId: string | null = null;

  private pollSub: Subscription | null = null;

  constructor(
    private questionSubmissionService: QuestionSubmissionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  load(): void {
    this.stopPolling();
    this.loading = true;
    this.questionSubmissionService.getSubmitted(this.page, this.size).subscribe({
      next: res => {
        this.questions = res.content.filter(q =>
          !this.questionSubmissionService.isWithdrawn(q.id) && q.status !== 'APPROVED' && q.status !== 'ARCHIVED'
        );
        this.totalElements = this.questions.length;
        this.loading = false;
        this.managePoll();
      },
      error: () => { this.loading = false; }
    });
  }

  edit(id: string): void {
    this.router.navigate(['/questions', id, 'edit']);
  }

  viewDetail(id: string): void {
    this.router.navigate(['/questions', id]);
  }

  resubmit(question: QuestionResponse): void {
    this.resubmittingId = question.id;
    this.questionSubmissionService.resubmit(question.id).subscribe({
      next: updated => {
        const i = this.questions.findIndex(q => q.id === updated.id);
        if (i >= 0) this.questions[i] = updated;
        this.resubmittingId = null;
        this.managePoll();
      },
      error: () => { this.resubmittingId = null; }
    });
  }

  withdraw(question: QuestionResponse): void {
    this.withdrawingId = question.id;
    this.questionSubmissionService.withdraw(question.id).subscribe({
      next: () => {
        this.questionSubmissionService.markWithdrawn(question.id);
        this.questions = this.questions.filter(q => q.id !== question.id);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.withdrawingId = null;
        this.managePoll();
      },
      error: () => { this.withdrawingId = null; }
    });
  }

  prevPage(): void {
    if (this.page > 0) { this.page--; this.load(); }
  }

  nextPage(): void {
    const maxPage = Math.ceil(this.totalElements / this.size) - 1;
    if (this.page < maxPage) { this.page++; this.load(); }
  }

  private managePoll(): void {
    const hasPending = this.questions.some(q => q.status === 'PENDING');
    if (hasPending && !this.pollSub) {
      this.startPolling();
    } else if (!hasPending) {
      this.stopPolling();
    }
  }

  private startPolling(): void {
    this.pollSub = interval(POLL_INTERVAL_MS).pipe(
      switchMap(() => this.questionSubmissionService.getSubmitted(this.page, this.size).pipe(
        catchError(() => EMPTY)
      ))
    ).subscribe({
      next: res => {
        const filtered = res.content.filter(q =>
          !this.questionSubmissionService.isWithdrawn(q.id) && q.status !== 'APPROVED' && q.status !== 'ARCHIVED'
        );
        const statusChanged = filtered.some(incoming => {
          const existing = this.questions.find(q => q.id === incoming.id);
          return existing !== undefined && existing.status !== incoming.status;
        }) || this.questions.some(existing => !filtered.find(f => f.id === existing.id));

        if (statusChanged) {
          this.questions = filtered;
          this.totalElements = filtered.length;
          this.managePoll();
        }
      }
    });
  }

  private stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }
}
