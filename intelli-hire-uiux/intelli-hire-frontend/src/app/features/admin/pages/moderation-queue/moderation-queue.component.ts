import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AdminQuestionService } from '../../../../core/services/admin-question.service';
import { QuestionResponse } from '../../../../core/models/question.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { RejectDialogComponent } from '../../../../shared/components/reject-dialog/reject-dialog.component';

@Component({
  selector: 'app-moderation-queue',
  standalone: true,
  imports: [CommonModule, RouterLink, MatDialogModule, LoadingSpinnerComponent],
  templateUrl: './moderation-queue.component.html',
  styleUrl: './moderation-queue.component.scss'
})
export class ModerationQueueComponent implements OnInit {
  questions: QuestionResponse[] = [];
  loading = false;
  actionError = '';

  constructor(
    private adminQuestionService: AdminQuestionService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get hasMore(): boolean {
    return this.questions.length >= 50;
  }

  load(): void {
    this.loading = true;
    this.adminQuestionService.listAll({ status: 'PENDING', page: 0, size: 50 }).subscribe({
      next: res => { this.questions = res.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  approve(question: QuestionResponse): void {
    this.adminQuestionService.patchModerationStatus(question.id, 'APPROVED').subscribe({
      next: () => { this.actionError = ''; this.questions = this.questions.filter(q => q.id !== question.id); },
      error: err => { this.actionError = err?.error?.message ?? 'Action failed. Please try again.'; }
    });
  }

  openRejectDialog(question: QuestionResponse): void {
    this.dialog.open(RejectDialogComponent, {
      data: { questionText: question.question_text }
    }).afterClosed().subscribe((reason: string | null) => {
      if (!reason) return;
      this.adminQuestionService.patchModerationStatus(question.id, 'REJECTED', reason).subscribe({
        next: () => { this.actionError = ''; this.questions = this.questions.filter(q => q.id !== question.id); },
        error: err => { this.actionError = err?.error?.message ?? 'Action failed. Please try again.'; }
      });
    });
  }
}
