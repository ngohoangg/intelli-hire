import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  CreateSessionRequest,
  InterviewSession,
  InterviewSessionDetail,
  PageResponse,
  SubmitAnswerRequest
} from '../../../core/models/interview.model';

@Injectable({ providedIn: 'root' })
export class InterviewService {
  // NOTE: path bắt đầu bằng '/v1/...' (không có '/api') vì ApiService tự prepend '/api' từ environment.apiUrl.
  // Kết quả: ApiService tạo URL '/api/v1/interviews/sessions' → proxy rule '/api/v1/interviews' match → forward tới port 8083.
  // Khác với các service khác (ví dụ resume) vì proxy interviews KHÔNG có pathRewrite — nên không cần double prefix.
  private readonly path = '/v1/interviews/sessions';

  constructor(private api: ApiService) {}

  list(page = 0, size = 10): Observable<PageResponse<InterviewSession>> {
    return this.api.get<PageResponse<InterviewSession>>(this.path, {
      page: String(page),
      size: String(size)
    });
  }

  // GET /{sessionId} luôn trả SessionDetailResponse (full Q&A) — dùng cho cả room lẫn detail page
  getDetail(sessionId: number): Observable<InterviewSessionDetail> {
    return this.api.get<InterviewSessionDetail>(`${this.path}/${sessionId}`);
  }

  create(request: CreateSessionRequest): Observable<InterviewSession> {
    return this.api.post<InterviewSession>(this.path, request);
  }

  endSession(sessionId: number): Observable<void> {
    return this.api.put<void>(`${this.path}/${sessionId}/end`, {});
  }

  deleteSession(sessionId: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${sessionId}`);
  }

  submitAnswer(sessionId: number, request: SubmitAnswerRequest): Observable<void> {
    return this.api.post<void>(`${this.path}/${sessionId}/answers`, request);
  }

  delete(sessionId: number): Observable<void> {
    return this.api.delete<void>(`${this.path}/${sessionId}`);
  }
}
