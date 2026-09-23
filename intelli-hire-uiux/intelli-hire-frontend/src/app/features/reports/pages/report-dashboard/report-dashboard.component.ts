import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ReportService } from '../../services/report.service';
import { UserStatsResponse, UserProgressResponse } from '../../../../core/models/report.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-report-dashboard',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent, MatSnackBarModule],
  templateUrl: './report-dashboard.component.html',
  styleUrl: './report-dashboard.component.scss'
})
export class ReportDashboardComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private snackBar = inject(MatSnackBar);

  stats: UserStatsResponse | null = null;
  progressList: UserProgressResponse[] = [];
  loading = false;

  constructor(
    private reportService: ReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    forkJoin({
      stats: this.reportService.getStats(),
      progress: this.reportService.getProgress()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: ({ stats, progress }) => {
        this.stats = stats;
        this.progressList = progress ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open('Failed to load dashboard data', 'Close', { duration: 4000 });
      }
    });
  }

  goToHistory(): void {
    this.router.navigate(['/reports', 'history']);
  }

  goToInterviews(): void {
    this.router.navigate(['/interviews', 'create']);
  }

  formatScore(score: number): string {
    return score != null ? score.toFixed(1) : '0.0';
  }

  getScoreColor(score: number): string {
    if (score >= 80) return 'var(--success)';
    if (score >= 50) return 'var(--warning)';
    return 'var(--danger)';
  }

  getTrendBarHeight(value: number, trend: number[]): number {
    const max = Math.max(...trend, 1);
    return (value / max) * 100;
  }
}
