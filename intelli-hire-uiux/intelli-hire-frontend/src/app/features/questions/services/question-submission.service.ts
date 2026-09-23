import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { PageResponse, QuestionResponse } from '../../../core/models/question.model';

const STORAGE_KEY_PREFIX = 'withdrawn_questions_';

@Injectable({ providedIn: 'root' })
export class QuestionSubmissionService {
  constructor(private api: ApiService, private auth: AuthService) {}

  getSubmitted(page = 0, size = 20): Observable<PageResponse<QuestionResponse>> {
    return this.api.get<PageResponse<QuestionResponse>>('/v1/questions/submitted', {
      page,
      size
    });
  }

  resubmit(id: string): Observable<QuestionResponse> {
    return this.api.patch<QuestionResponse>(`/v1/questions/${id}/moderation-status`, {
      status: 'PENDING'
    });
  }

  withdraw(id: string): Observable<void> {
    return this.api.delete<void>(`/v1/questions/${id}`);
  }

  markWithdrawn(id: string): void {
    const ids = this.loadWithdrawnIds();
    ids.add(id);
    this.saveWithdrawnIds(ids);
  }

  isWithdrawn(id: string): boolean {
    return this.loadWithdrawnIds().has(id);
  }

  private storageKey(): string {
    const accountId = this.auth.currentUser?.accountId ?? 'anonymous';
    return `${STORAGE_KEY_PREFIX}${accountId}`;
  }

  private loadWithdrawnIds(): Set<string> {
    try {
      const raw = localStorage.getItem(this.storageKey());
      const arr: string[] = raw ? JSON.parse(raw) : [];
      return new Set(arr);
    } catch {
      return new Set();
    }
  }

  private saveWithdrawnIds(ids: Set<string>): void {
    try {
      localStorage.setItem(this.storageKey(), JSON.stringify([...ids]));
    } catch {
      // localStorage không khả dụng — bỏ qua
    }
  }
}
