import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  PageResponse,
  StudyArticleRequest,
  StudyArticleResponse,
  StudyArticleSearchParams
} from '../../../core/models/question.model';

@Injectable({ providedIn: 'root' })
export class QuestionStudyArticleService {
  private readonly path = '/v1/study-articles';

  constructor(private api: ApiService) {}

  getAll(params: StudyArticleSearchParams = {}): Observable<PageResponse<StudyArticleResponse>> {
    return this.api.get<PageResponse<StudyArticleResponse>>(this.path, {
      topicId: params.topicId,
      difficultyId: params.difficultyId,
      page: params.page ?? 0,
      size: params.size ?? 10
    });
  }

  getById(id: string): Observable<StudyArticleResponse> {
    return this.api.get<StudyArticleResponse>(`${this.path}/${id}`);
  }

  create(request: StudyArticleRequest): Observable<StudyArticleResponse> {
    return this.api.post<StudyArticleResponse>(this.path, request);
  }

  update(id: string, request: StudyArticleRequest): Observable<StudyArticleResponse> {
    return this.api.put<StudyArticleResponse>(`${this.path}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }
}
