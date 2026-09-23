import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ReportService } from '../../services/report.service';
import { ReportSummary, ReportStatus } from '../../../../core/models/report.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-report-history',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSnackBarModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './report-history.component.html',
  styleUrl: './report-history.component.scss'
})
export class ReportHistoryComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private snackBar = inject(MatSnackBar);

  reports: ReportSummary[] = [];
  loading = false;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  activeFilter: string | undefined = undefined;

  readonly displayedColumns = ['topic', 'score', 'totalQuestions', 'status', 'createdAt', 'actions'];
  readonly ReportStatus = ReportStatus;
  readonly filters = ['ALL', 'COMPLETED', 'PENDING', 'FAILED'];

  constructor(
    private reportService: ReportService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.reportService.getHistory(this.pageIndex, this.pageSize, this.activeFilter)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => {
        this.reports = page.content ?? [];
        this.totalElements = page.total_elements ?? 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load report history', 'Close', { duration: 4000 });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadReports();
  }

  onFilterChange(filter: string): void {
    this.activeFilter = filter === 'ALL' ? undefined : filter;
    this.pageIndex = 0;
    this.loadReports();
  }

  openReport(report: ReportSummary): void {
    this.router.navigate(['/reports', report.id]);
  }

  deleteReport(report: ReportSummary, event: Event): void {
    event.stopPropagation();
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Report',
        message: `Delete report for "${report.topic || 'Untitled'}"? This cannot be undone.`
      }
    });
    ref.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(confirmed => {
      if (confirmed) {
        this.reportService.deleteBySessionId(report.session_id)
          .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => {
            this.snackBar.open('Report deleted', 'Close', { duration: 3000 });
            this.loadReports();
          },
          error: () => {
            this.snackBar.open('Failed to delete report', 'Close', { duration: 4000 });
          }
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/reports']);
  }

  getScoreText(report: ReportSummary): string {
    if (report.total_score == null || report.max_score == null) return '—';
    return `${report.total_score}/${report.max_score}`;
  }

  getScorePercent(report: ReportSummary): string {
    if (!report.max_score || report.max_score === 0) return '';
    return `(${(report.total_score / report.max_score * 100).toFixed(0)}%)`;
  }

  getStatusClass(status: ReportStatus): string {
    const map: Record<string, string> = {
      [ReportStatus.COMPLETED]: 'status-completed',
      [ReportStatus.PENDING]: 'status-pending',
      [ReportStatus.FAILED]: 'status-failed'
    };
    return map[status] ?? '';
  }
}
