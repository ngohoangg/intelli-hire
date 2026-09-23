import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  ReportResponse,
  ReportSummary,
  ReportPageResponse,
  UserStatsResponse,
  UserProgressResponse,
  RegenerateRoadmapRequest
} from '../../../core/models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly basePath = '/v1/reports';

  constructor(private api: ApiService, private auth: AuthService) {}

  private getUserId(): string {
    const accountId = this.auth.currentUser?.accountId;
    if (!accountId) {
      console.warn('[ReportService] No authenticated user found');
      return '';
    }
    return String(accountId);
  }

  getHistory(page = 0, size = 10, status?: string): Observable<ReportPageResponse<ReportSummary>> {
    const params: Record<string, string> = {
      page: String(page),
      size: String(size),
      sort: 'createdAt,desc'
    };
    if (status) {
      params['status'] = status;
    }
    return this.api.get<ReportPageResponse<ReportSummary>>(
      `${this.basePath}/history/${this.getUserId()}`, params
    );
  }

  getById(id: string): Observable<ReportResponse> {
    return this.api.get<ReportResponse>(`${this.basePath}/${id}`);
  }

  getBySessionId(sessionId: string): Observable<ReportResponse> {
    return this.api.get<ReportResponse>(`${this.basePath}/session/${sessionId}`);
  }

  getStats(): Observable<UserStatsResponse> {
    return this.api.get<UserStatsResponse>(`${this.basePath}/stats/${this.getUserId()}`);
  }

  getProgress(): Observable<UserProgressResponse[]> {
    return this.api.get<UserProgressResponse[]>(`${this.basePath}/progress/${this.getUserId()}`);
  }

  getProgressBySkill(skillName: string): Observable<UserProgressResponse> {
    return this.api.get<UserProgressResponse>(
      `${this.basePath}/progress/${this.getUserId()}/${skillName}`
    );
  }

  regenerateRoadmap(id: string, roadmapJson: any): Observable<ReportResponse> {
    const body: RegenerateRoadmapRequest = { roadmap_json: roadmapJson };
    return this.api.post<ReportResponse>(`${this.basePath}/${id}/regenerate`, body);
  }

  deleteBySessionId(sessionId: string): Observable<void> {
    return this.api.delete<void>(`${this.basePath}/session/${sessionId}`);
  }
}
