import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { DifficultyLevelResponse } from '../../../../core/models/question.model';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';

@Component({
  selector: 'app-difficulty-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './difficulty-management.component.html',
  styleUrl: './difficulty-management.component.scss'
})
export class DifficultyManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  loading = false;
  saving = false;
  selectedDifficulty: DifficultyLevelResponse | null = null;
  difficulties: DifficultyLevelResponse[] = [];

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    weight: [10, [Validators.required, Validators.min(1), Validators.max(100)]],
    description: ['', [Validators.maxLength(255)]]
  });

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.loadDifficulties();
  }

  get maxWeight(): number {
    return this.difficulties.reduce((max, item) => Math.max(max, item.weight), 0);
  }

  startCreate(): void {
    this.selectedDifficulty = null;
    this.form.reset({
      name: '',
      weight: 10,
      description: ''
    });
  }

  selectDifficulty(difficulty: DifficultyLevelResponse): void {
    this.selectedDifficulty = difficulty;
    this.form.patchValue({
      name: difficulty.name,
      weight: difficulty.weight,
      description: difficulty.description ?? ''
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const request = {
      name: this.form.value.name ?? '',
      weight: Number(this.form.value.weight ?? 10),
      description: this.form.value.description ?? ''
    };
    const operation = this.selectedDifficulty
      ? this.taxonomyService.updateDifficulty(this.selectedDifficulty.id, request)
      : this.taxonomyService.createDifficulty(request);

    operation.subscribe({
      next: difficulty => {
        this.selectedDifficulty = difficulty;
        this.loadDifficulties();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  deleteDifficulty(difficulty: DifficultyLevelResponse, event?: Event): void {
    event?.stopPropagation();

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete difficulty level',
        message: 'Removing a difficulty level affects question scoring and study article classification. Continue only when it is unused.'
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      this.taxonomyService.deleteDifficulty(difficulty.id).subscribe({
        next: () => {
          if (this.selectedDifficulty?.id === difficulty.id) {
            this.startCreate();
          }
          this.loadDifficulties();
        },
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  openQuestionBank(): void {
    if (!this.selectedDifficulty) {
      return;
    }

    this.router.navigate(['/questions'], {
      queryParams: {
        difficultyId: this.selectedDifficulty.id
      }
    });
  }

  private loadDifficulties(): void {
    this.loading = true;

    this.taxonomyService.getDifficulties().pipe(catchError(() => of([]))).subscribe({
      next: difficulties => {
        this.difficulties = [...difficulties].sort((a, b) => a.weight - b.weight);
        this.loading = false;
        this.saving = false;
      },
      error: () => {
        this.loading = false;
        this.saving = false;
      }
    });
  }
}
