import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import {
  PageResponse,
  QuestionModerationStatus,
  QuestionRequest,
  QuestionResponse
} from '../models/question.model';

export interface AdminQuestionSearchParams {
  status?: QuestionModerationStatus | '';
  page?: number;
  size?: number;
  topicId?: string;
  type?: string;
  keyword?: string;
  submittedBy?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminQuestionService {
  private readonly path = '/v1/admin/questions';

  readonly pendingCount$ = new BehaviorSubject<number>(0);

  constructor(private api: ApiService) {}

  listAll(params: AdminQuestionSearchParams = {}): Observable<PageResponse<QuestionResponse>> {
    return this.api.get<PageResponse<QuestionResponse>>(this.path, {
      status: params.status ?? '',
      page: params.page ?? 0,
      size: params.size ?? 20,
      topicId: params.topicId,
      type: params.type,
      keyword: params.keyword,
      submittedBy: params.submittedBy
    });
  }

  getById(id: string): Observable<QuestionResponse> {
    return this.api.get<QuestionResponse>(`${this.path}/${id}`);
  }

  update(id: string, request: QuestionRequest): Observable<QuestionResponse> {
    return this.api.put<QuestionResponse>(`${this.path}/${id}`, request);
  }

  patchModerationStatus(
    id: string,
    status: QuestionModerationStatus,
    rejectionReason?: string
  ): Observable<QuestionResponse> {
    return this.api.patch<QuestionResponse>(`${this.path}/${id}/moderation-status`, {
      status,
      ...(rejectionReason ? { rejection_reason: rejectionReason } : {})
    });
  }

  getPendingCount(): Observable<number> {
    return this.listAll({ status: 'PENDING', page: 0, size: 0 }).pipe(
      map(res => res.total_elements)
    );
  }

  refreshPendingCount(): void {
    this.getPendingCount().subscribe({
      next: count => this.pendingCount$.next(count),
      error: () => {}
    });
  }
}
