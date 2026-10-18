import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TopicResponse } from '../../../../core/models/question.model';
import { QuestionTaxonomyService } from '../../services/question-taxonomy.service';
import { TopicOption, flattenTopicTree, formatDateTime } from '../../questions.utils';

interface TopicListItem extends TopicResponse {
  depth: number;
  label: string;
}

@Component({
  selector: 'app-topic-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatDialogModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './topic-management.component.html',
  styleUrl: './topic-management.component.scss'
})
export class TopicManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  loading = false;
  saving = false;
  selectedTopicId: string | null = null;

  topics: TopicResponse[] = [];
  topicOptions: TopicOption[] = [];
  topicList: TopicListItem[] = [];

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(1000)]],
    parent_id: [''],
    order: [0, [Validators.required]],
    is_active: [true]
  });

  constructor(
    private router: Router,
    private dialog: MatDialog,
    private taxonomyService: QuestionTaxonomyService
  ) {}

  ngOnInit(): void {
    this.loadTopics();
  }

  get activeCount(): number {
    return this.topics.filter(topic => topic.is_active).length;
  }

  get rootCount(): number {
    return this.topics.filter(topic => !topic.parent_id).length;
  }

  formatDate(value: string): string {
    return formatDateTime(value);
  }

  isSelected(topic: TopicListItem): boolean {
    return this.selectedTopicId === topic.id;
  }

  startCreate(): void {
    this.selectedTopicId = null;
    this.form.reset({
      name: '',
      description: '',
      parent_id: '',
      order: 0,
      is_active: true
    });
  }

  selectTopic(topic: TopicListItem): void {
    this.selectedTopicId = topic.id;
    this.form.patchValue({
      name: topic.name,
      description: topic.description ?? '',
      parent_id: topic.parent_id ?? '',
      order: topic.order ?? 0,
      is_active: topic.is_active
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
      description: this.form.value.description ?? '',
      parent_id: this.form.value.parent_id || null,
      order: Number(this.form.value.order ?? 0),
      is_active: this.form.value.is_active ?? true
    };

    const operation = this.selectedTopicId
      ? this.taxonomyService.updateTopic(this.selectedTopicId, request)
      : this.taxonomyService.createTopic(request);

    operation.subscribe({
      next: topic => {
        this.selectedTopicId = topic.id;
        this.loadTopics();
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  deleteTopic(topic: TopicListItem, event?: Event): void {
    event?.stopPropagation();

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Xóa chủ đề',
        message: 'Việc xóa chủ đề có thể ảnh hưởng đến phân loại câu hỏi và các bài viết học tập liên quan. Chỉ tiếp tục nếu chủ đề không còn được sử dụng.'
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.loading = true;
      this.taxonomyService.deleteTopic(topic.id).subscribe({
        next: () => {
          if (this.selectedTopicId === topic.id) {
            this.startCreate();
          }
          this.loadTopics();
        },
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  openTopicQuestions(): void {
    if (!this.selectedTopicId) {
      return;
    }

    this.router.navigate(['/questions'], {
      queryParams: {
        topicId: this.selectedTopicId
      }
    });
  }

  private loadTopics(): void {
    this.loading = true;

    forkJoin({
      topics: this.taxonomyService.getTopics().pipe(catchError(() => of([]))),
      topicTree: this.taxonomyService.getTopicTree().pipe(catchError(() => of([])))
    }).subscribe({
      next: result => {
        this.topics = result.topics.sort((a, b) => a.name.localeCompare(b.name));
        this.topicOptions = flattenTopicTree(result.topicTree);
        this.topicList = this.topicOptions
          .map(option => {
            const topic = this.topics.find(item => item.id === option.id);
            return topic
              ? {
                  ...topic,
                  depth: option.depth,
                  label: option.label
                }
              : null;
          })
          .filter((item): item is TopicListItem => item !== null);

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
