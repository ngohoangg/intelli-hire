import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EMPTY, Subscription, interval } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AdminQuestionService, AdminQuestionSearchParams } from '../../../../core/services/admin-question.service';
import { QuestionModerationStatus, QuestionResponse, QuestionType } from '../../../../core/models/question.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { RejectDialogComponent } from '../../../../shared/components/reject-dialog/reject-dialog.component';
import { extractApiErrorMessage } from '../../../../core/utils/api-error';

const POLL_INTERVAL_MS = 10_000;

@Component({
  selector: 'app-admin-questions',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, LoadingSpinnerComponent, TranslatePipe],
  templateUrl: './admin-questions.component.html',
  styleUrl: './admin-questions.component.scss'
})
export class AdminQuestionsComponent implements OnInit, OnDestroy {
  readonly Math = Math;

  questions: QuestionResponse[] = [];
  loading = false;
  totalElements = 0;
  page = 0;
  size = 20;
  actionError = '';

  statusFilter: QuestionModerationStatus | '' = '';
  keywordFilter = '';
  typeFilter: QuestionType | '' = '';

  private pollSub: Subscription | null = null;

  readonly statusTabs: Array<{ label: string; value: QuestionModerationStatus | '' }> = [
    { label: 'admin.adminQuestions.tabAll', value: '' },
    { label: 'admin.adminQuestions.tabPending', value: 'PENDING' },
    { label: 'admin.adminQuestions.tabApproved', value: 'APPROVED' },
    { label: 'admin.adminQuestions.tabRejected', value: 'REJECTED' },
    { label: 'admin.adminQuestions.tabArchived', value: 'ARCHIVED' }
  ];

  readonly questionTypeOptions: Array<{ label: string; value: QuestionType | '' }> = [
    { label: 'admin.adminQuestions.typeAllOption', value: '' },
    { label: 'admin.adminQuestions.type.technical', value: QuestionType.TECHNICAL },
    { label: 'admin.adminQuestions.type.coding', value: QuestionType.CODING },
    { label: 'admin.adminQuestions.type.behavioral', value: QuestionType.BEHAVIORAL }
  ];

  constructor(
    private adminQuestionService: AdminQuestionService,
    private dialog: MatDialog,
    private router: Router,
    private translate: TranslateService
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
    this.adminQuestionService.listAll(this.buildParams()).subscribe({
      next: res => {
        this.questions = res.content;
        this.totalElements = res.total_elements;
        this.loading = false;
        this.managePoll();
      },
      error: () => { this.loading = false; }
    });
  }

  setStatus(status: QuestionModerationStatus | ''): void {
    this.statusFilter = status;
    this.page = 0;
    this.load();
  }

  onSearch(): void {
    this.page = 0;
    this.load();
  }

  prevPage(): void {
    if (this.page > 0) { this.page--; this.load(); }
  }

  nextPage(): void {
    const maxPage = Math.ceil(this.totalElements / this.size) - 1;
    if (this.page < maxPage) { this.page++; this.load(); }
  }

  approve(question: QuestionResponse): void {
    this.adminQuestionService.patchModerationStatus(question.id, 'APPROVED').subscribe({
      next: updated => {
        this.actionError = '';
        this.replaceInList(updated);
        this.adminQuestionService.refreshPendingCount();
      },
      error: err => { this.actionError = extractApiErrorMessage(err) ?? this.translate.instant('admin.adminQuestions.actionFailed'); }
    });
  }

  openRejectDialog(question: QuestionResponse): void {
    this.dialog.open(RejectDialogComponent, {
      data: { questionText: question.question_text }
    }).afterClosed().subscribe((reason: string | null) => {
      if (!reason) return;
      this.adminQuestionService.patchModerationStatus(question.id, 'REJECTED', reason).subscribe({
        next: updated => {
          this.actionError = '';
          this.replaceInList(updated);
          this.adminQuestionService.refreshPendingCount();
        },
        error: err => { this.actionError = extractApiErrorMessage(err) ?? this.translate.instant('admin.adminQuestions.rejectionFailed'); }
      });
    });
  }

  archive(question: QuestionResponse): void {
    this.adminQuestionService.patchModerationStatus(question.id, 'ARCHIVED').subscribe({
      next: updated => {
        this.actionError = '';
        this.replaceInList(updated);
        this.adminQuestionService.refreshPendingCount();
      },
      error: err => { this.actionError = extractApiErrorMessage(err) ?? this.translate.instant('admin.adminQuestions.actionFailed'); }
    });
  }

  restore(question: QuestionResponse): void {
    this.adminQuestionService.patchModerationStatus(question.id, 'APPROVED').subscribe({
      next: updated => {
        this.actionError = '';
        this.replaceInList(updated);
        this.adminQuestionService.refreshPendingCount();
      },
      error: err => { this.actionError = extractApiErrorMessage(err) ?? this.translate.instant('admin.adminQuestions.actionFailed'); }
    });
  }

  navigateToCreate(): void {
    this.router.navigate(['/questions/create'], { state: { adminCreate: true } });
  }

  getStatusClass(status: QuestionModerationStatus): string {
    return status.toLowerCase();
  }

  statusLabelKey(status: QuestionModerationStatus): string {
    return 'admin.adminQuestions.status.' + status.toLowerCase();
  }

  typeLabelKey(type: QuestionType): string {
    return 'admin.adminQuestions.type.' + type.toLowerCase();
  }

  truncate(text: string, max = 80): string {
    return text.length > max ? text.slice(0, max) + '…' : text;
  }

  private replaceInList(updated: QuestionResponse): void {
    const i = this.questions.findIndex(q => q.id === updated.id);
    if (i >= 0) this.questions[i] = updated;
  }

  private buildParams(): AdminQuestionSearchParams {
    return {
      status: this.statusFilter,
      page: this.page,
      size: this.size,
      keyword: this.keywordFilter || undefined,
      type: this.typeFilter || undefined
    };
  }

  // Chỉ poll khi đang xem tab All hoặc Pending — nơi có thể xuất hiện submission mới từ user
  private managePoll(): void {
    const shouldPoll = this.statusFilter === '' || this.statusFilter === 'PENDING' || this.statusFilter === 'REJECTED';
    if (shouldPoll && !this.pollSub) {
      this.startPolling();
    } else if (!shouldPoll) {
      this.stopPolling();
    }
  }

  private startPolling(): void {
    this.pollSub = interval(POLL_INTERVAL_MS).pipe(
      switchMap(() => this.adminQuestionService.listAll(this.buildParams()).pipe(
        catchError(() => EMPTY)
      ))
    ).subscribe({
      next: res => {
        const hasChanges = res.total_elements !== this.totalElements
          || res.content.some(incoming => {
            const existing = this.questions.find(q => q.id === incoming.id);
            return !existing || existing.status !== incoming.status;
          });

        if (hasChanges) {
          this.questions = res.content;
          this.totalElements = res.total_elements;
          this.adminQuestionService.refreshPendingCount();
        }
      }
    });
  }

  private stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }
}
