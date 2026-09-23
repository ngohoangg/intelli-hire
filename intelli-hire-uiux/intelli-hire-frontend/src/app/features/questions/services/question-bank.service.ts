import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import {
  BulkImportRequest,
  BulkImportResponse,
  GenerateQuestionsFromResumeRequest,
  GenerateQuestionsFromResumeResponse,
  PageResponse,
  QuestionDetail,
  QuestionRequest,
  QuestionResponse,
  QuestionSearchParams,
  RandomQuestionRequest,
  RubricRequest,
  RubricResponse
} from '../../../core/models/question.model';

@Injectable({ providedIn: 'root' })
export class QuestionBankService {
  private readonly path = '/v1/questions';

  constructor(private api: ApiService) {}

  search(params: QuestionSearchParams = {}): Observable<PageResponse<QuestionResponse>> {
    return this.api.get<PageResponse<QuestionResponse>>(`${this.path}/search`, {
      page: params.page ?? 0,
      size: params.size ?? 10,
      sort: params.sort ?? 'createdAt,desc',
      topicId: params.topicId,
      difficultyId: params.difficultyId,
      type: params.type,
      tagIds: params.tagIds,
      keyword: params.keyword,
      isActive: params.isActive
    });
  }

  getById(id: string): Observable<QuestionDetail> {
    return this.api.get<QuestionDetail>(`${this.path}/${id}`);
  }

  create(request: QuestionRequest): Observable<QuestionDetail> {
    return this.api.post<QuestionDetail>(this.path, request);
  }

  update(id: string, request: QuestionRequest): Observable<QuestionDetail> {
    return this.api.put<QuestionDetail>(`${this.path}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  getRubric(id: string): Observable<RubricResponse> {
    return this.api.get<RubricResponse>(`${this.path}/${id}/rubric`);
  }

  createRubric(id: string, request: RubricRequest): Observable<RubricResponse> {
    return this.api.post<RubricResponse>(`${this.path}/${id}/rubric`, request);
  }

  updateRubric(id: string, request: RubricRequest): Observable<RubricResponse> {
    return this.api.put<RubricResponse>(`${this.path}/${id}/rubric`, request);
  }

  getRandomQuestions(request: RandomQuestionRequest): Observable<QuestionResponse[]> {
    return this.api.get<QuestionResponse[]>(`${this.path}/random`, {
      topicId: request.topic_id,
      difficultyId: request.difficulty_id,
      type: request.type,
      tagIds: request.tag_ids,
      count: request.count,
      excludeIds: request.exclude_ids
    });
  }

  generateFromResume(request: GenerateQuestionsFromResumeRequest): Observable<GenerateQuestionsFromResumeResponse> {
    return this.api.post<GenerateQuestionsFromResumeResponse>(`${this.path}/generate`, {
      resume_id: request.resume_id,
      count: request.count,
      question_type: request.question_type || undefined,
      difficulty_name: request.difficulty_name || undefined,
      language: request.language || undefined,
      focus_areas: request.focus_areas?.length ? request.focus_areas : undefined
    });
  }

  bulkImport(request: BulkImportRequest): Observable<BulkImportResponse> {
    return this.api.post<BulkImportResponse>(`${this.path}/bulk`, request);
  }
}
