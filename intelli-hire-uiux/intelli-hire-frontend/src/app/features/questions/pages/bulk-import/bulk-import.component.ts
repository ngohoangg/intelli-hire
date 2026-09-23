import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { BulkImportRequest, BulkImportResponse } from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { buildBulkImportSample } from '../../questions.utils';

@Component({
  selector: 'app-bulk-import',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './bulk-import.component.html',
  styleUrl: './bulk-import.component.scss'
})
export class BulkImportComponent {
  private readonly fb = inject(FormBuilder);

  loading = false;
  result: BulkImportResponse | null = null;
  parseError = '';

  readonly form = this.fb.group({
    payload: [buildBulkImportSample(), [Validators.required]]
  });

  constructor(private questionBankService: QuestionBankService) {}

  useSample(): void {
    this.form.patchValue({ payload: buildBulkImportSample() });
    this.parseError = '';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.parseError = '';
    this.result = null;

    let request: BulkImportRequest;
    try {
      request = JSON.parse(this.form.value.payload ?? '') as BulkImportRequest;
      if (!Array.isArray(request.questions)) {
        throw new Error('Payload must contain a "questions" array.');
      }
    } catch (error) {
      this.parseError = error instanceof Error ? error.message : 'Invalid JSON payload.';
      return;
    }

    this.loading = true;
    this.questionBankService.bulkImport(request).subscribe({
      next: response => {
        this.result = response;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
