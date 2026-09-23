import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ResumeEducationService } from '../../services/resume-education.service';
import { ResumeEducation } from '../../../../core/models/education.model';
import { EducationFormComponent, EducationFormData } from '../education-form/education-form.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-education-list',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    TranslatePipe
  ],
  templateUrl: './education-list.component.html',
  styleUrl: './education-list.component.scss'
})
export class EducationListComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  educations: ResumeEducation[] = [];
  loading = false;

  constructor(
    private eduService: ResumeEducationService,
    private dialog: MatDialog,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.eduService.list(this.resumeId).subscribe({
      next: items => { this.educations = items; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  openCreate(): void {
    const data: EducationFormData = { resumeId: this.resumeId };
    this.dialog.open(EducationFormComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.eduService.create(this.resumeId, result).subscribe(() => this.load());
        }
      });
  }

  openEdit(edu: ResumeEducation): void {
    const data: EducationFormData = { resumeId: this.resumeId, education: edu };
    this.dialog.open(EducationFormComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.eduService.update(this.resumeId, edu.id, result).subscribe(() => this.load());
        }
      });
  }

  delete(edu: ResumeEducation): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('resume.educationList.deleteDialogTitle'),
        message: this.translate.instant('resume.educationList.deleteDialogMessage', { school: edu.school })
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.eduService.delete(this.resumeId, edu.id).subscribe(() => this.load());
      }
    });
  }

  formatYearRange(edu: ResumeEducation): string {
    if (!edu.start_year && !edu.end_year) return '';
    if (edu.start_year && edu.end_year) return `${edu.start_year} – ${edu.end_year}`;
    if (edu.start_year) return `${edu.start_year} –`;
    return `– ${edu.end_year}`;
  }
}
