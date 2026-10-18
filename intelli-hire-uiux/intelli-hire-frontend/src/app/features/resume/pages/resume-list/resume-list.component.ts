import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ResumeService } from '../../services/resume.service';
import { Resume } from '../../../../core/models/resume.model';
import { ResumeStatusBadgeComponent } from '../../components/resume-status-badge/resume-status-badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-resume-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    ResumeStatusBadgeComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './resume-list.component.html',
  styleUrl: './resume-list.component.scss'
})
export class ResumeListComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  resumes: Resume[] = [];
  loading = false;
  displayedColumns = ['title', 'status', 'created_at', 'actions'];

  constructor(
    private resumeService: ResumeService,
    public router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadResumes();
  }

  loadResumes(): void {
    this.loading = true;
    this.resumeService.list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: items => {
          this.resumes = items ?? [];
          this.loading = false;
        },
        error: () => { this.loading = false; }
      });
  }

  viewResume(id: string): void {
    this.router.navigate(['/resumes', id]);
  }

  editResume(event: Event, id: string): void {
    event.stopPropagation();
    this.router.navigate(['/resumes', id, 'edit']);
  }

  deleteResume(event: Event, resume: Resume): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Xóa hồ sơ',
        message: `Xóa "${resume.title}"? Hành động này không thể hoàn tác.`
      }
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.resumeService.delete(resume.id).subscribe(() => this.loadResumes());
      }
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', {
      year: 'numeric', month: '2-digit', day: '2-digit'
    });
  }

  get totalElements(): number { return this.resumes.length; }
}
