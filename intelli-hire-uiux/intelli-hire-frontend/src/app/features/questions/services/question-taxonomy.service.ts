import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  DifficultyLevelRequest,
  DifficultyLevelResponse,
  PageResponse,
  QuestionResponse,
  TagRequest,
  TagResponse,
  TopicRequest,
  TopicResponse,
  TopicTreeResponse
} from '../../../core/models/question.model';

@Injectable({ providedIn: 'root' })
export class QuestionTaxonomyService {
  constructor(private api: ApiService) {}

  getTopics(): Observable<TopicResponse[]> {
    return this.api.get<TopicResponse[]>('/v1/topics');
  }

  getTopicTree(): Observable<TopicTreeResponse[]> {
    return this.api.get<TopicTreeResponse[]>('/v1/topics/tree');
  }

  createTopic(request: TopicRequest): Observable<TopicResponse> {
    return this.api.post<TopicResponse>('/v1/topics', request);
  }

  updateTopic(id: string, request: TopicRequest): Observable<TopicResponse> {
    return this.api.put<TopicResponse>(`/v1/topics/${id}`, request);
  }

  deleteTopic(id: string): Observable<void> {
    return this.api.delete<void>(`/v1/topics/${id}`);
  }

  getTopicQuestions(
    id: string,
    page = 0,
    size = 10,
    sort = 'createdAt,desc'
  ): Observable<PageResponse<QuestionResponse>> {
    return this.api.get<PageResponse<QuestionResponse>>(`/v1/topics/${id}/questions`, {
      page,
      size,
      sort
    });
  }

  getTags(): Observable<TagResponse[]> {
    return this.api.get<TagResponse[]>('/v1/tags');
  }

  createTag(request: TagRequest): Observable<TagResponse> {
    return this.api.post<TagResponse>('/v1/tags', request);
  }

  updateTag(id: string, request: TagRequest): Observable<TagResponse> {
    return this.api.put<TagResponse>(`/v1/tags/${id}`, request);
  }

  deleteTag(id: string): Observable<void> {
    return this.api.delete<void>(`/v1/tags/${id}`);
  }

  getDifficulties(): Observable<DifficultyLevelResponse[]> {
    return this.api.get<DifficultyLevelResponse[]>('/v1/difficulties');
  }

  createDifficulty(request: DifficultyLevelRequest): Observable<DifficultyLevelResponse> {
    return this.api.post<DifficultyLevelResponse>('/v1/difficulties', request);
  }

  updateDifficulty(id: string, request: DifficultyLevelRequest): Observable<DifficultyLevelResponse> {
    return this.api.put<DifficultyLevelResponse>(`/v1/difficulties/${id}`, request);
  }

  deleteDifficulty(id: string): Observable<void> {
    return this.api.delete<void>(`/v1/difficulties/${id}`);
  }
}
