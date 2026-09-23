import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { AnswerMessage, QuestionMessage, SessionStatus } from '../models/interview.model';
import { getMockSession, saveMockSession } from './interview-mock.store';

const CONNECT_DELAY_MS = 800;
const QUESTION_DELAY_MS = 900;

/**
 * TEMPORARY — stands in for InterviewWebSocketService (STOMP over SockJS) so the interview room
 * works with mock sessions without the interview-service WebSocket. Same public API.
 * Registered in app.config.ts when `environment.mockInterview` is true.
 */
@Injectable()
export class MockInterviewWebSocketService {
  readonly question$ = new Subject<QuestionMessage>();
  readonly error$ = new Subject<string>();
  readonly connected$ = new Subject<boolean>();

  private sessionId: number | null = null;
  private connected = false;
  private timers: ReturnType<typeof setTimeout>[] = [];

  connect(sessionId: number, _userId: string): void {
    this.disconnect();
    this.sessionId = sessionId;

    this.later(CONNECT_DELAY_MS, () => {
      const session = getMockSession(sessionId);
      if (!session) {
        this.error$.next('Interview session not found. Please create a new session.');
        return;
      }
      this.connected = true;
      this.connected$.next(true);
      // Resume an interview already in progress (e.g. after a page reload)
      if (session.status === SessionStatus.ACTIVE && session.currentIndex >= 0) {
        this.emitQuestion(session.currentIndex);
      }
    });
  }

  get isConnected(): boolean {
    return this.connected;
  }

  startInterview(sessionId: number, _userId: string): void {
    const session = this.connected ? getMockSession(sessionId) : undefined;
    if (!session) return;

    if (session.status === SessionStatus.CREATED) {
      session.status = SessionStatus.ACTIVE;
      session.startedAt = new Date().toISOString();
      session.currentIndex = 0;
      saveMockSession(session);
    }
    this.later(QUESTION_DELAY_MS, () => this.emitQuestion(session.currentIndex));
  }

  submitAnswer(sessionId: number, answer: AnswerMessage, _userId: string): void {
    const session = this.connected ? getMockSession(sessionId) : undefined;
    if (!session || session.status !== SessionStatus.ACTIVE) return;

    const now = new Date().toISOString();
    session.answers = [
      ...session.answers.filter(a => a.question_id !== answer.question_id),
      { question_id: answer.question_id, answer_text: answer.answer_text, submitted_at: now }
    ];
    session.currentIndex++;
    const finished = session.currentIndex >= session.questions.length;
    if (finished) {
      session.status = SessionStatus.COMPLETED;
      session.endedAt = now;
    }
    saveMockSession(session);

    this.later(QUESTION_DELAY_MS, () => {
      if (finished) {
        this.question$.next({
          type: 'SESSION_END',
          question_id: null,
          question_text: null,
          question_order: null,
          total_questions: session.questions.length
        });
      } else {
        this.emitQuestion(session.currentIndex);
      }
    });
  }

  disconnect(): void {
    this.timers.forEach(timer => clearTimeout(timer));
    this.timers = [];
    this.connected = false;
    this.sessionId = null;
  }

  private emitQuestion(index: number): void {
    const session = this.sessionId !== null ? getMockSession(this.sessionId) : undefined;
    const question = session?.questions[index];
    if (!session || !question) return;
    this.question$.next({
      type: 'QUESTION',
      question_id: question.id,
      question_text: question.text,
      question_order: index + 1,
      total_questions: session.questions.length
    });
  }

  private later(delayMs: number, fn: () => void): void {
    this.timers.push(setTimeout(fn, delayMs));
  }
}
