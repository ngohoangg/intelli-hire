import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeSkill, ResumeSkillRequest } from '../../../core/models/skill.model';

@Injectable({ providedIn: 'root' })
export class ResumeSkillService {
  constructor(private api: ApiService) {}

  list(resumeId: string): Observable<ResumeSkill[]> {
    return this.api.get<ResumeSkill[]>(`/v1/resumes/${resumeId}/skills`);
  }

  create(resumeId: string, req: ResumeSkillRequest): Observable<ResumeSkill> {
    return this.api.post<ResumeSkill>(`/v1/resumes/${resumeId}/skills`, req);
  }

  update(resumeId: string, id: string, req: ResumeSkillRequest): Observable<ResumeSkill> {
    return this.api.put<ResumeSkill>(`/v1/resumes/${resumeId}/skills/${id}`, req);
  }

  delete(resumeId: string, id: string): Observable<void> {
    return this.api.delete<void>(`/v1/resumes/${resumeId}/skills/${id}`);
  }
}
