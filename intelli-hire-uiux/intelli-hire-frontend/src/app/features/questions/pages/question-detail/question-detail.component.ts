import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, of, switchMap } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  PageResponse,
  QuestionDetail,
  RubricResponse,
  StudyArticleResponse
} from '../../../../core/models/question.model';
import { QuestionBankService } from '../../services/question-bank.service';
import { QuestionStudyArticleService } from '../../services/question-study-article.service';
import { AuthService } from '../../../../core/services/auth.service';
import {
  createQuestionDuplicationDraft,
  formatDateTime,
  formatQuestionType
} from '../../questions.utils';

@Component({
  selector: 'app-question-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatDialogModule, LoadingSpinnerComponent],
  templateUrl: './question-detail.component.html',
  styleUrl: './question-detail.component.scss'
})
export class QuestionDetailComponent implements OnInit {
  question: QuestionDetail | null = null;
  rubric: RubricResponse | null = null;
  relatedArticles: StudyArticleResponse[] = [];
  loading = false;

  get isAdmin(): boolean {
    return this.auth.isAdmin;
  }

  get canEdit(): boolean {
    if (!this.question) return false;
    const status = this.question.status;
    if (status !== 'PENDING' && status !== 'REJECTED') return false;
    return this.auth.currentUser?.accountId === this.question.submitted_by_id;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private questionBankService: QuestionBankService,
    private studyArticleService: QuestionStudyArticleService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/questions']);
      return;
    }

    this.loadQuestion(id);
  }

  formatDate(value: string): string {
    return formatDateTime(value);
  }

  formatType(value: string): string {
    return formatQuestionType(value);
  }

  getStatusClass(): string {
    return this.question?.is_active ? 'active' : 'inactive';
  }

  edit(): void {
    if (!this.question) {
      return;
    }

    this.router.navigate(['/questions', this.question.id, 'edit']);
  }

  archive(): void {
    if (!this.question) {
      return;
    }

    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Archive question',
        message: 'The question will be soft deleted from Question Service and removed from active catalog usage.'
      }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed || !this.question) {
        return;
      }

      this.loading = true;
      this.questionBankService.delete(this.question.id).subscribe({
        next: () => this.router.navigate(['/questions']),
        error: () => {
          this.loading = false;
        }
      });
    });
  }

  duplicate(): void {
    if (!this.question) {
      return;
    }

    this.router.navigate(['/questions/create'], {
      state: {
        draft: createQuestionDuplicationDraft(this.question)
      }
    });
  }

  openRandomLab(): void {
    if (!this.question) {
      return;
    }

    this.router.navigate(['/questions/random-lab'], {
      queryParams: {
        topicId: this.question.topic_id,
        difficultyId: this.question.difficulty_id,
        type: this.question.question_type
      }
    });
  }

  openTopicQuestions(): void {
    if (!this.question) {
      return;
    }

    this.router.navigate(['/questions'], {
      queryParams: {
        topicId: this.question.topic_id
      }
    });
  }

  openRelatedArticles(): void {
    if (!this.question) {
      return;
    }

    this.router.navigate(['/questions/articles'], {
      queryParams: {
        topicId: this.question.topic_id,
        difficultyId: this.question.difficulty_id
      }
    });
  }

  private loadQuestion(id: string): void {
    this.loading = true;

    this.questionBankService.getById(id).pipe(
      switchMap(question => {
        this.question = question;
        return forkJoin({
          rubric: this.questionBankService.getRubric(id).pipe(catchError(() => of(null))),
          related: this.studyArticleService.getAll({
            topicId: question.topic_id,
            difficultyId: question.difficulty_id,
            page: 0,
            size: 3
          }).pipe(catchError(() => of(this.emptyPage<StudyArticleResponse>())))
        });
      })
    ).subscribe({
      next: result => {
        this.rubric = result.rubric;
        this.relatedArticles = result.related.content;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/questions']);
      }
    });
  }

  private emptyPage<T>(): PageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 0,
      total_elements: 0,
      total_pages: 0,
      is_first: true,
      is_last: true
    };
  }
}
