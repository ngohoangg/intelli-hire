import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule],
  templateUrl: './reject-dialog.component.html',
  styleUrl: './reject-dialog.component.scss'
})
export class RejectDialogComponent {
  reason = '';

  constructor(
    public dialogRef: MatDialogRef<RejectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { questionText: string }
  ) {}

  confirm(): void {
    if (!this.reason.trim()) return;
    this.dialogRef.close(this.reason.trim());
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
