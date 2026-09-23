import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { interval, Subscription } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';
import { ParseJob, JobStatus } from '../../../../core/models/parse-job.model';
import { ParseJobService } from '../../services/parse-job.service';

@Component({
  selector: 'app-parse-job-status',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, MatButtonModule, TranslatePipe],
  templateUrl: './parse-job-status.component.html',
  styleUrl: './parse-job-status.component.scss'
})
export class ParseJobStatusComponent implements OnInit, OnDestroy {
  @Input({ required: true }) jobId!: string;
  @Output() jobSucceeded = new EventEmitter<void>();

  job: ParseJob | null = null;
  private pollSub?: Subscription;
  private succeeded = false;

  readonly JobStatus = JobStatus;

  constructor(private parseJobService: ParseJobService) {}

  ngOnInit(): void {
    this.loadAndPoll();
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  private loadAndPoll(): void {
    // Load immediately, then poll every 3s until terminal state
    this.parseJobService.getById(this.jobId).subscribe(job => {
      this.job = job;
      this.notifyIfSucceeded(job);
      if (this.isTerminal(job.status)) return;
      this.startPolling();
    });
  }

  private startPolling(): void {
    this.pollSub = interval(3000).pipe(
      switchMap(() => this.parseJobService.getById(this.jobId)),
      takeWhile(job => !this.isTerminal(job.status), true)
    ).subscribe(job => {
      this.job = job;
      this.notifyIfSucceeded(job);
    });
  }

  private notifyIfSucceeded(job: ParseJob): void {
    if (!this.succeeded && job.status === JobStatus.SUCCEEDED) {
      this.succeeded = true;
      this.jobSucceeded.emit();
    }
  }

  private isTerminal(status: JobStatus): boolean {
    return [JobStatus.SUCCEEDED, JobStatus.FAILED, JobStatus.CANCELED].includes(status);
  }

  cancel(): void {
    if (!this.job) return;
    this.pollSub?.unsubscribe();
    this.parseJobService.cancel(this.job.id).subscribe(job => {
      this.job = job;
    });
  }

  get statusClass(): string {
    if (!this.job) return '';
    const map: Record<JobStatus, string> = {
      [JobStatus.QUEUED]:    'job-queued',
      [JobStatus.RUNNING]:   'job-running',
      [JobStatus.SUCCEEDED]: 'job-succeeded',
      [JobStatus.FAILED]:    'job-failed',
      [JobStatus.CANCELED]: 'job-cancelled'
    };
    return map[this.job.status] ?? '';
  }

  get statusLabel(): string {
    if (!this.job) return '';
    const map: Record<JobStatus, string> = {
      [JobStatus.QUEUED]:    'resume.parseJobStatus.statusQueued',
      [JobStatus.RUNNING]:   'resume.parseJobStatus.statusRunning',
      [JobStatus.SUCCEEDED]: 'resume.parseJobStatus.statusSucceeded',
      [JobStatus.FAILED]:    'resume.parseJobStatus.statusFailed',
      [JobStatus.CANCELED]: 'resume.parseJobStatus.statusCanceled'
    };
    return map[this.job.status] ?? this.job.status;
  }

  get progressMode(): 'determinate' | 'indeterminate' | 'buffer' {
    if (!this.job) return 'indeterminate';
    if (this.job.status === JobStatus.RUNNING && (this.job.progress ?? 0) > 0) return 'determinate';
    if (this.job.status === JobStatus.RUNNING) return 'indeterminate';
    return 'determinate';
  }

  get progressValue(): number {
    if (!this.job) return 0;
    if (this.job.status === JobStatus.SUCCEEDED) return 100;
    return this.job.progress ?? 0;
  }

  get isActive(): boolean {
    return this.job?.status === JobStatus.QUEUED || this.job?.status === JobStatus.RUNNING;
  }

  get canCancel(): boolean {
    return this.job?.status === JobStatus.QUEUED || this.job?.status === JobStatus.RUNNING;
  }
}
