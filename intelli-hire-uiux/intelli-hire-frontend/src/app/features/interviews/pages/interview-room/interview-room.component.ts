import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Subscription, interval } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { InterviewWebSocketService } from '../../services/interview-websocket.service';
import { InterviewService } from '../../services/interview.service';
import { GradingService } from '../../services/grading.service';
import { AuthService } from '../../../../core/services/auth.service';
import { QuestionMessage } from '../../../../core/models/interview.model';
import { GradingResponse } from '../../../../core/models/grading.model';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

type RoomState = 'connecting' | 'ready' | 'question' | 'submitting' | 'grading' | 'finished' | 'error';

const POLL_INTERVAL_MS = 3000;
const MAX_POLL_ATTEMPTS = 40; // 40 × 3s = 2 minutes timeout

@Component({
  selector: 'app-interview-room',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatDialogModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './interview-room.component.html',
  styleUrl: './interview-room.component.scss'
})
export class InterviewRoomComponent implements OnInit, OnDestroy {
  sessionId!: number;
  userId!: string;

  state: RoomState = 'connecting';
  currentQuestion: QuestionMessage | null = null;
  errorMessage = '';

  gradingStatus: GradingResponse['status'] | null = null;
  gradingTotalQuestions = 0;

  answerForm: FormGroup;
  private subs = new Subscription();
  private pollSub: Subscription | null = null;
  private pollAttempts = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private wsService: InterviewWebSocketService,
    private interviewService: InterviewService,
    private gradingService: GradingService,
    private auth: AuthService,
    private dialog: MatDialog
  ) {
    this.answerForm = this.fb.group({
      answerText: ['', [Validators.required, Validators.maxLength(10000)]]
    });
  }

  ngOnInit(): void {
    this.sessionId = Number(this.route.snapshot.paramMap.get('id'));
    this.userId = String(this.auth.currentUser?.accountId ?? '');
    this.connectWebSocket();
  }

  private connectWebSocket(): void {
    this.state = 'connecting';

    this.subs.add(
      this.wsService.connected$.subscribe(connected => {
        if (connected) {
          this.state = 'ready';
        } else if (this.state === 'ready' || this.state === 'question' || this.state === 'submitting') {
          this.state = 'error';
          this.errorMessage = 'Mất kết nối. Vui lòng kết thúc phiên và thử lại.';
        }
      })
    );

    this.subs.add(
      this.wsService.question$.subscribe(msg => {
        if (msg.type === 'SESSION_END') {
          this.startGradingPoll();
        } else if (msg.type === 'QUESTION') {
          this.currentQuestion = msg;
          this.state = 'question';
          this.answerForm.reset();
        } else if (msg.type === 'ERROR') {
          this.state = 'error';
          this.errorMessage = msg.question_text ?? 'Lỗi không xác định';
        }
      })
    );

    this.subs.add(
      this.wsService.error$.subscribe(err => {
        this.state = 'error';
        this.errorMessage = err;
      })
    );

    this.wsService.connect(this.sessionId, this.userId);
  }

  private startGradingPoll(): void {
    this.state = 'grading';
    this.pollAttempts = 0;

    this.pollSub = interval(POLL_INTERVAL_MS).pipe(
      switchMap(() => this.gradingService.getBySessionId(String(this.sessionId)).pipe(
        catchError(() => of(null))
      ))
    ).subscribe(grading => {
      this.pollAttempts++;

      if (grading) {
        this.gradingStatus = grading.status;
        this.gradingTotalQuestions = grading.total_questions ?? 0;

        if (grading.status === 'COMPLETED') {
          this.stopGradingPoll();
          this.router.navigate(['/interviews', this.sessionId, 'detail']);
          return;
        }

        if (grading.status === 'FAILED') {
          this.stopGradingPoll();
          this.state = 'error';
          this.errorMessage = 'Chấm điểm thất bại. Vui lòng quay lại và liên hệ bộ phận hỗ trợ.';
          return;
        }
      }

      if (this.pollAttempts >= MAX_POLL_ATTEMPTS) {
        this.stopGradingPoll();
        this.state = 'finished';
      }
    });
  }

  private stopGradingPoll(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  startInterview(): void {
    this.wsService.startInterview(this.sessionId, this.userId);
  }

  submitAnswer(): void {
    if (this.answerForm.invalid || !this.currentQuestion?.question_id) return;
    this.state = 'submitting';
    this.wsService.submitAnswer(
      this.sessionId,
      {
        question_id: this.currentQuestion.question_id!,
        answer_text: this.answerForm.value.answerText
      },
      this.userId
    );
  }

  endSession(): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Kết thúc phiên', message: 'Bạn có chắc chắn muốn kết thúc phiên phỏng vấn này không?' }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.interviewService.endSession(this.sessionId).subscribe(() => {
          this.startGradingPoll();
        });
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/interviews']);
  }

  viewResults(): void {
    this.router.navigate(['/interviews', this.sessionId, 'detail']);
  }

  get progress(): number {
    if (!this.currentQuestion) return 0;
    const total = this.currentQuestion.total_questions ?? 1;
    const current = this.currentQuestion.question_order ?? 0;
    return (current / total) * 100;
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.wsService.disconnect();
    this.stopGradingPoll();
  }
}
