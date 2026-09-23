import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeEducation, ResumeEducationRequest } from '../../../core/models/education.model';

@Injectable({ providedIn: 'root' })
export class ResumeEducationService {
  constructor(private api: ApiService) {}

  list(resumeId: string): Observable<ResumeEducation[]> {
    return this.api.get<ResumeEducation[]>(`/v1/resumes/${resumeId}/educations`);
  }

  create(resumeId: string, request: ResumeEducationRequest): Observable<ResumeEducation> {
    return this.api.post<ResumeEducation>(`/v1/resumes/${resumeId}/educations`, request);
  }

  update(resumeId: string, id: string, request: ResumeEducationRequest): Observable<ResumeEducation> {
    return this.api.put<ResumeEducation>(`/v1/resumes/${resumeId}/educations/${id}`, request);
  }

  delete(resumeId: string, id: string): Observable<void> {
    return this.api.delete<void>(`/v1/resumes/${resumeId}/educations/${id}`);
  }
}
