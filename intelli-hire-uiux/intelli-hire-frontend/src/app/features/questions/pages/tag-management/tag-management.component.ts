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
import { TagResponse } from '../../../../core/models/question.model';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';

@Component({
  selector: 'app-tag-management',
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
  templateUrl: './tag-management.component.html',
  styleUrl: './tag-management.component.scss'
})
export class TagManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  loading = false;
  saving = false;
  selectedTag: TagResponse | null = null;
  tags: TagResponse[] = [];

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]]
  });

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.loadTags();
  }

  startCreate(): void {
    this.selectedTag = null;
    this.form.reset({ name: '' });
  }

  selectTag(tag: TagResponse): void {
    this.selectedTag = tag;
    this.form.patchValue({ name: tag.name });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const request = { name: this.form.value.name ?? '' };
    const operation = this.selectedTag
      ? this.taxonomyService.updateTag(this.selectedTag.id, request)
      : this.taxonomyService.createTag(request);

    operation.subscribe({
      next: tag => {
        this.selectedTag = tag;
        this.loadTags();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  deleteTag(tag: TagResponse, event?: Event): void {
    event?.stopPropagation();

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete tag',
        message: 'Deleting a tag can break saved filters and question classification. Continue only if the tag is unused.'
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      this.taxonomyService.deleteTag(tag.id).subscribe({
        next: () => {
          if (this.selectedTag?.id === tag.id) {
            this.startCreate();
          }
          this.loadTags();
        },
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  openQuestionBank(): void {
    if (!this.selectedTag) {
      return;
    }

    this.router.navigate(['/questions'], {
      queryParams: {
        tagId: this.selectedTag.id
      }
    });
  }

  private loadTags(): void {
    this.loading = true;

    this.taxonomyService.getTags().pipe(catchError(() => of([]))).subscribe({
      next: tags => {
        this.tags = [...tags].sort((a, b) => a.name.localeCompare(b.name));
        this.saving = false;
        this.loading = false;
      },
      error: () => {
        this.saving = false;
        this.loading = false;
      }
    });
  }
}
