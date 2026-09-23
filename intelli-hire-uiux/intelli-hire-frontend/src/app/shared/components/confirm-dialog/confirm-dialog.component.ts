import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslatePipe } from '@ngx-translate/core';

export interface ConfirmDialogData {
  title: string;
  message: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './confirm-dialog.component.html',
  styles: [`
    .dialog-wrapper {
      background: var(--bg-elevated);
      border-radius: 16px;
      padding: 28px;
      min-width: 380px;
      max-width: 480px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .dialog-header {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .dialog-icon {
      width: 42px;
      height: 42px;
      border-radius: 10px;
      background: rgba(255, 170, 0, 0.1);
      border: 1px solid rgba(255, 170, 0, 0.25);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #FFAA00;
      flex-shrink: 0;
    }

    .dialog-title {
      font-family: 'Syne', sans-serif;
      font-size: 17px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0;
      letter-spacing: -0.01em;
    }

    .dialog-body {
      padding: 0 0 0 4px;
    }

    .dialog-message {
      font-family: 'DM Sans', sans-serif;
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0;
      line-height: 1.6;
    }

    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 4px;
    }

    .btn-cancel {
      padding: 9px 20px;
      border-radius: 9px;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid var(--border);
      color: var(--text-secondary);
      font-family: 'DM Sans', sans-serif;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: var(--text-primary);
        border-color: rgba(255, 255, 255, 0.12);
      }
    }

    .btn-confirm {
      padding: 9px 20px;
      border-radius: 9px;
      background: rgba(255, 23, 68, 0.15);
      border: 1px solid rgba(255, 23, 68, 0.35);
      color: #FF3D71;
      font-family: 'DM Sans', sans-serif;
      font-size: 14px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 23, 68, 0.25);
        border-color: rgba(255, 23, 68, 0.5);
        box-shadow: 0 0 16px rgba(255, 23, 68, 0.2);
      }
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
