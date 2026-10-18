import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Resume, CreateResumeRequest, UpdateResumeRequest } from '../../../core/models/resume.model';

@Injectable({ providedIn: 'root' })
export class ResumeService {
  private readonly path = '/v1/resumes';

  constructor(private api: ApiService) {}

  list(): Observable<Resume[]> {
    return this.api.get<Resume[]>(this.path);
  }

  getById(id: string): Observable<Resume> {
    return this.api.get<Resume>(`${this.path}/${id}`);
  }

  create(request: CreateResumeRequest): Observable<Resume> {
    return this.api.post<Resume>(this.path, request);
  }

  update(id: string, request: UpdateResumeRequest): Observable<Resume> {
    return this.api.put<Resume>(`${this.path}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  reprocess(id: string): Observable<Resume> {
    return this.api.post<Resume>(`${this.path}/${id}/reprocess`, {});
  }
}
