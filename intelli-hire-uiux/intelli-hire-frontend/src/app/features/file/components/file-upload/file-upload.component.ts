import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpEventType } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ResumeFileService } from '../../services/resume-file.service';
import { ResumeFile } from '../../../../core/models/resume-file.model';
import { ParseJobService } from '../../../parse-job/services/parse-job.service';
import { ParseJobStatusComponent } from '../../../parse-job/components/parse-job-status/parse-job-status.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { JobType } from '../../../../core/models/parse-job.model';

const ALLOWED_MIME = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
const MAX_SIZE_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
    MatSnackBarModule,
    MatDialogModule,
    ParseJobStatusComponent,
    TranslatePipe
  ],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.scss'
})
export class FileUploadComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;
  @Output() uploaded = new EventEmitter<void>();

  existingFile: ResumeFile | null = null;
  isDragging = false;
  uploadProgress: number | null = null;
  uploading = false;
  loading = false;
  activeJobId: string | null = null;

  constructor(
    private fileService: ResumeFileService,
    private parseJobService: ParseJobService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadFile();
  }

  loadFile(): void {
    this.loading = true;
    this.fileService.getByResumeId(this.resumeId).subscribe({
      next: f => { this.existingFile = f; this.loading = false; },
      error: () => { this.existingFile = null; this.loading = false; }
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) this.handleFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.handleFile(file);
    input.value = '';
  }

  private handleFile(file: File): void {
    if (!ALLOWED_MIME.includes(file.type)) {
      this.snackBar.open(
        this.translate.instant('resume.fileUpload.invalidTypeSnackbar'),
        this.translate.instant('resume.actions.close'),
        { duration: 4000 }
      );
      return;
    }
    if (file.size > MAX_SIZE_BYTES) {
      this.snackBar.open(
        this.translate.instant('resume.fileUpload.fileTooLargeSnackbar'),
        this.translate.instant('resume.actions.close'),
        { duration: 4000 }
      );
      return;
    }
    if (this.existingFile) {
      // Delete the existing file first, then upload the replacement
      this.uploading = true;
      this.fileService.delete(this.resumeId).subscribe({
        next: () => {
          this.existingFile = null;
          this.activeJobId = null;
          this.uploadFile(file);
        },
        error: () => {
          this.uploading = false;
          this.snackBar.open(
            this.translate.instant('resume.fileUpload.replaceFailedSnackbar'),
            this.translate.instant('resume.actions.close'),
            { duration: 4000 }
          );
        }
      });
    } else {
      this.uploadFile(file);
    }
  }

  private uploadFile(file: File): void {
    this.uploading = true;
    this.uploadProgress = 0;
    this.activeJobId = null;

    this.fileService.upload(this.resumeId, file).subscribe({
      next: event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.uploadProgress = Math.round(100 * (event.loaded / (event.total ?? event.loaded)));
        } else if (event.type === HttpEventType.Response) {
          this.uploading = false;
          this.uploadProgress = null;
          this.existingFile = (event.body as any)?.data ?? event.body as any;
          this.startParseJob();
          this.uploaded.emit();
        }
      },
      error: () => {
        this.uploading = false;
        this.uploadProgress = null;
      }
    });
  }

  private startParseJob(): void {
    this.parseJobService.create({ resume_id: this.resumeId, job_type: JobType.PARSE }).subscribe({
      next: job => { this.activeJobId = job.id; },
      error: () => {}
    });
  }

  download(): void {
    if (!this.existingFile) return;
    this.fileService.download(this.resumeId).subscribe(buffer => {
      const mimeType = this.existingFile!.file_type === 'DOCX'
        ? 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        : 'application/pdf';
      const blob = new Blob([buffer], { type: mimeType });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = this.existingFile!.original_name;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  deleteFile(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('resume.fileUpload.deleteDialogTitle'),
        message: this.translate.instant('resume.fileUpload.deleteDialogMessage', { name: this.existingFile?.original_name })
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.fileService.delete(this.resumeId).subscribe(() => {
          this.existingFile = null;
          this.activeJobId = null;
        });
      }
    });
  }

  formatBytes(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}
