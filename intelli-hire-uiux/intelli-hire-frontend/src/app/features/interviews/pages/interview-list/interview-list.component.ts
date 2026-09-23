import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { InterviewService } from '../../services/interview.service';
import { InterviewSession, SessionStatus } from '../../../../core/models/interview.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-interview-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './interview-list.component.html',
  styleUrl: './interview-list.component.scss'
})
export class InterviewListComponent implements OnInit {
  sessions: InterviewSession[] = [];
  loading = false;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  readonly displayedColumns = ['topic', 'difficulty', 'status', 'createdAt', 'actions'];
  readonly SessionStatus = SessionStatus;

  constructor(
    private interviewService: InterviewService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions(): void {
    this.loading = true;
    this.interviewService.list(this.pageIndex, this.pageSize).subscribe({
      next: page => {
        this.sessions = page.content ?? [];
        this.totalElements = page.total_elements ?? 0;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSessions();
  }

  createNew(): void {
    this.router.navigate(['/interviews', 'create']);
  }

  openSession(session: InterviewSession): void {
    if (session.status === SessionStatus.CREATED || session.status === SessionStatus.ACTIVE) {
      this.router.navigate(['/interviews', session.id, 'room']);
    } else {
      this.router.navigate(['/interviews', session.id, 'detail']);
    }
  }

  endSession(session: InterviewSession): void {
    this.interviewService.endSession(session.id).subscribe({
      next: () => this.loadSessions(),
      error: () => {}
    });
  }

  deleteSession(session: InterviewSession): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Session',
        message: `Are you sure you want to delete the session "${session.topic}"? This action cannot be undone.`
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.interviewService.delete(session.id).subscribe({
          next: () => this.loadSessions(),
          error: () => {}
        });
      }
    });
  }

  getStatusClass(status: SessionStatus): string {
    const map: Record<SessionStatus, string> = {
      [SessionStatus.CREATED]: 'status-created',
      [SessionStatus.ACTIVE]: 'status-active',
      [SessionStatus.COMPLETED]: 'status-completed',
      [SessionStatus.EXPIRED]: 'status-expired'
    };
    return map[status] ?? '';
  }
}
