import { Injectable } from '@angular/core';
import { Client, IMessage, StompHeaders } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AnswerMessage, QuestionMessage } from '../../../core/models/interview.model';

@Injectable({ providedIn: 'root' })
export class InterviewWebSocketService {
  private client: Client | null = null;
  private hasConnected = false;

  // Streams
  readonly question$ = new Subject<QuestionMessage>();
  readonly error$ = new Subject<string>();
  readonly connected$ = new Subject<boolean>();

  constructor(private translate: TranslateService) {}

  connect(sessionId: number, userId: string): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.hasConnected = false;
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws/interview'),
      connectHeaders: { 'X-User-Id': userId },
      reconnectDelay: 0,
      onConnect: () => {
        this.hasConnected = true;
        this.connected$.next(true);
        this.client!.subscribe(
          `/topic/interview/${sessionId}`,
          (msg: IMessage) => {
            const payload: QuestionMessage = JSON.parse(msg.body);
            this.question$.next(payload);
          }
        );
      },
      onStompError: (frame) => {
        this.error$.next(frame.headers['message'] ?? this.translate.instant('interviews.interviewRoom.websocketError'));
      },
      onWebSocketClose: () => {
        this.connected$.next(false);
        if (!this.hasConnected) {
          this.error$.next(this.translate.instant('interviews.interviewRoom.connectFailed'));
        }
      }
    });
    this.client.activate();
  }

  get isConnected(): boolean {
    return this.client?.connected ?? false;
  }

  startInterview(sessionId: number, userId: string): void {
    if (!this.isConnected) return;
    const headers: StompHeaders = { 'X-User-Id': userId };
    this.client!.publish({
      destination: `/app/interview/${sessionId}/start`,
      headers
    });
  }

  submitAnswer(sessionId: number, answer: AnswerMessage, userId: string): void {
    if (!this.isConnected) return;
    const headers: StompHeaders = { 'X-User-Id': userId };
    this.client!.publish({
      destination: `/app/interview/${sessionId}/answer`,
      headers,
      body: JSON.stringify(answer)
    });
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
  }
}
