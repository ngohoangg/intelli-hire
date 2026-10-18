import { Component, DestroyRef, OnInit, OnDestroy, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Subscription, interval } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';
import { ResumeService } from '../../services/resume.service';
import { Resume, ResumeStatus } from '../../../../core/models/resume.model';
import { ResumeSkillProfileService } from '../../../skill-profile/services/resume-skill-profile.service';
import { ResumeSkillProfile } from '../../../../core/models/skill.model';
import { ResumeStatusBadgeComponent } from '../../components/resume-status-badge/resume-status-badge.component';
import { ParseJobStatusComponent } from '../../../parse-job/components/parse-job-status/parse-job-status.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { FileUploadComponent } from '../../../file/components/file-upload/file-upload.component';
import { ContactFormComponent } from '../../../contact/components/contact-form/contact-form.component';
import { ExperienceListComponent } from '../../../experience/components/experience-list/experience-list.component';
import { EducationListComponent } from '../../../education/components/education-list/education-list.component';
import { ResumeSkillListComponent } from '../../../resume-skills/components/resume-skill-list/resume-skill-list.component';
import { SkillProfileCardComponent } from '../../../skill-profile/components/skill-profile-card/skill-profile-card.component';
import { CvReviewComponent } from '../../components/cv-review/cv-review.component';

@Component({
  selector: 'app-resume-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    ResumeStatusBadgeComponent,
    ParseJobStatusComponent,
    LoadingSpinnerComponent,
    FileUploadComponent,
    ContactFormComponent,
    ExperienceListComponent,
    EducationListComponent,
    ResumeSkillListComponent,
    SkillProfileCardComponent,
    CvReviewComponent
  ],
  templateUrl: './resume-detail.component.html',
  styleUrl: './resume-detail.component.scss'
})
export class ResumeDetailComponent implements OnInit, OnDestroy {
  private destroyRef = inject(DestroyRef);

  resume: Resume | null = null;
  profile: ResumeSkillProfile | null = null;
  loading = false;
  jobId: string | null = null;
  detailsReady = true;
  reprocessing = false;
  private reprocessPoll?: Subscription;

  readonly ResumeStatus = ResumeStatus;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private resumeService: ResumeService,
    private skillProfileService: ResumeSkillProfileService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.jobId = this.route.snapshot.queryParamMap.get('jobId');
    this.loadResume(id);
  }

  loadResume(id: string): void {
    this.loading = true;
    this.resumeService.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: r => {
          this.resume = r;
          this.loading = false;
          if (r.status === ResumeStatus.COMPLETED) {
            this.loadSkillProfile(id);
          }
        },
        error: () => { this.loading = false; this.router.navigate(['/resumes']); }
      });
  }

  loadSkillProfile(id: string): void {
    this.skillProfileService.get(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: p => { this.profile = p; },
        error: () => { this.profile = null; }
      });
  }

  onJobSucceeded(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    // Refresh resume so status is current before tabs are recreated
    this.resumeService.getById(id).subscribe({
      next: r => {
        this.resume = r;
        if (r.status === ResumeStatus.COMPLETED) {
          this.loadSkillProfile(id);
        }
        this.detailsReady = false;
        setTimeout(() => { this.detailsReady = true; });
      }
    });
  }

  ngOnDestroy(): void {
    this.reprocessPoll?.unsubscribe();
  }

  reprocess(): void {
    if (!this.resume || this.reprocessing) return;
    this.reprocessing = true;
    const resumeId = this.resume.id;

    this.resumeService.reprocess(resumeId).subscribe({
      next: r => {
        this.resume = r;
        // Poll every 4 seconds until status leaves PARSING
        this.reprocessPoll = interval(4000).pipe(
          switchMap(() => this.resumeService.getById(resumeId)),
          takeWhile(r2 => r2.status === ResumeStatus.PARSING, true)
        ).subscribe({
          next: r2 => {
            this.resume = r2;
            if (r2.status !== ResumeStatus.PARSING) {
              this.reprocessing = false;
              if (r2.status === ResumeStatus.COMPLETED) {
                this.loadSkillProfile(resumeId);
              }
              // Re-initialize all lazy tabs so they reload fresh data
              this.detailsReady = false;
              setTimeout(() => { this.detailsReady = true; });
            }
          },
          error: () => { this.reprocessing = false; }
        });
      },
      error: () => { this.reprocessing = false; }
    });
  }

  onFileUploaded(): void {
    if (!this.resume) return;
    this.loadResume(this.resume.id);
    this.loadSkillProfile(this.resume.id);
  }

  deleteResume(): void {
    if (!this.resume) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Xóa hồ sơ',
        message: `Xóa "${this.resume.title}"? Hành động này không thể hoàn tác.`
      }
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.resumeService.delete(this.resume!.id).subscribe(() => {
          this.router.navigate(['/resumes']);
        });
      }
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit'
    });
  }

  seniorityLabel(seniority: string): string {
    const map: Record<string, string> = {
      JUNIOR: 'Junior',
      MID: 'Middle',
      SENIOR: 'Senior',
      PRINCIPAL: 'Principal'
    };
    return map[seniority] ?? seniority;
  }

  get isParsing(): boolean {
    return this.resume?.status === ResumeStatus.PARSING;
  }

  get showJobStatus(): boolean {
    return !!this.jobId;
  }
}
