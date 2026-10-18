import { Injectable } from '@angular/core';
import { Client, IMessage, StompHeaders } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { AnswerMessage, QuestionMessage } from '../../../core/models/interview.model';

@Injectable({ providedIn: 'root' })
export class InterviewWebSocketService {
  private client: Client | null = null;
  private hasConnected = false;

  // Streams
  readonly question$ = new Subject<QuestionMessage>();
  readonly error$ = new Subject<string>();
  readonly connected$ = new Subject<boolean>();

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
        this.error$.next(frame.headers['message'] ?? 'Lỗi WebSocket');
      },
      onWebSocketClose: () => {
        this.connected$.next(false);
        if (!this.hasConnected) {
          this.error$.next('Không thể kết nối tới máy chủ phỏng vấn. Vui lòng kiểm tra dịch vụ đang chạy.');
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
