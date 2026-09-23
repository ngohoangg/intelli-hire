import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ReportService } from '../../services/report.service';
import { ReportResponse, ReportStatus } from '../../../../core/models/report.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-report-detail',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent, MatSnackBarModule],
  templateUrl: './report-detail.component.html',
  styleUrl: './report-detail.component.scss'
})
export class ReportDetailComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private snackBar = inject(MatSnackBar);

  report: ReportResponse | null = null;
  loading = false;
  regenerating = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.loading = true;
    this.reportService.getById(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: r => { this.report = r; this.loading = false; },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load report', 'Close', { duration: 4000 });
      }
    });
  }

  back(): void {
    this.router.navigate(['/reports', 'history']);
  }

  regenerateRoadmap(): void {
    if (!this.report) return;
    this.regenerating = true;
    this.reportService.regenerateRoadmap(this.report.id, this.report.roadmap_json)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: r => {
        this.report = r;
        this.regenerating = false;
        this.snackBar.open('Roadmap regenerated', 'Close', { duration: 3000 });
      },
      error: () => {
        this.regenerating = false;
        this.snackBar.open('Failed to regenerate roadmap', 'Close', { duration: 4000 });
      }
    });
  }

  getScorePercent(): number {
    if (!this.report?.max_score || this.report.max_score === 0) return 0;
    return this.report.total_score / this.report.max_score * 100;
  }

  getSkillPercent(score: number, maxScore: number): number {
    if (!maxScore || maxScore === 0) return 0;
    return score / maxScore * 100;
  }

  getStatusClass(status: ReportStatus): string {
    const map: Record<string, string> = {
      [ReportStatus.COMPLETED]: 'status-completed',
      [ReportStatus.PENDING]: 'status-pending',
      [ReportStatus.FAILED]: 'status-failed'
    };
    return map[status] ?? '';
  }

  getRoadmapEntries(): [string, any][] {
    if (!this.report?.roadmap_json) return [];
    if (typeof this.report.roadmap_json === 'object' && !Array.isArray(this.report.roadmap_json)) {
      return Object.entries(this.report.roadmap_json);
    }
    if (Array.isArray(this.report.roadmap_json)) {
      return this.report.roadmap_json.map((item, i) => [`Step ${i + 1}`, item]);
    }
    return [['Roadmap', this.report.roadmap_json]];
  }
}
