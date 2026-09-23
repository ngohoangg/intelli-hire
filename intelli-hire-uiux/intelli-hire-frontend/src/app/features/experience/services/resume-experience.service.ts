import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeExperience, ResumeExperienceRequest } from '../../../core/models/experience.model';

@Injectable({ providedIn: 'root' })
export class ResumeExperienceService {
  constructor(private api: ApiService) {}

  list(resumeId: string): Observable<ResumeExperience[]> {
    return this.api.get<ResumeExperience[]>(`/v1/resumes/${resumeId}/experiences`);
  }

  create(resumeId: string, request: ResumeExperienceRequest): Observable<ResumeExperience> {
    return this.api.post<ResumeExperience>(`/v1/resumes/${resumeId}/experiences`, request);
  }

  update(resumeId: string, id: string, request: ResumeExperienceRequest): Observable<ResumeExperience> {
    return this.api.put<ResumeExperience>(`/v1/resumes/${resumeId}/experiences/${id}`, request);
  }

  delete(resumeId: string, id: string): Observable<void> {
    return this.api.delete<void>(`/v1/resumes/${resumeId}/experiences/${id}`);
  }
}
