import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ResumeExperienceService } from '../../services/resume-experience.service';
import { ResumeExperience } from '../../../../core/models/experience.model';
import { ExperienceFormComponent, ExperienceFormData } from '../experience-form/experience-form.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-experience-list',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule
  ],
  templateUrl: './experience-list.component.html',
  styleUrl: './experience-list.component.scss'
})
export class ExperienceListComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  experiences: ResumeExperience[] = [];
  loading = false;

  constructor(
    private expService: ResumeExperienceService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.expService.list(this.resumeId).subscribe({
      next: items => {
        this.experiences = items.slice().sort((a, b) => {
          // Current jobs always first
          if (a.is_current !== b.is_current) return a.is_current ? -1 : 1;
          // Then by start_date descending
          return new Date(b.start_date).getTime() - new Date(a.start_date).getTime();
        });
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  openCreate(): void {
    const data: ExperienceFormData = { resumeId: this.resumeId };
    this.dialog.open(ExperienceFormComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.expService.create(this.resumeId, result).subscribe(() => this.load());
        }
      });
  }

  openEdit(exp: ResumeExperience): void {
    const data: ExperienceFormData = { resumeId: this.resumeId, experience: exp };
    this.dialog.open(ExperienceFormComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.expService.update(this.resumeId, exp.id, result).subscribe(() => this.load());
        }
      });
  }

  delete(exp: ResumeExperience): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Xóa kinh nghiệm', message: `Xóa "${exp.title}" tại ${exp.company}?` }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.expService.delete(this.resumeId, exp.id).subscribe(() => this.load());
      }
    });
  }

  formatDateRange(exp: ResumeExperience): string {
    const start = this.formatMonth(exp.start_date);
    const end = exp.is_current ? 'Hiện tại' : (exp.end_date ? this.formatMonth(exp.end_date) : '—');
    return `${start} – ${end}`;
  }

  private formatMonth(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit' });
  }
}
