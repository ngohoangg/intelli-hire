import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeSkillProfile, UpdateSkillProfileRequest } from '../../../core/models/skill.model';

@Injectable({ providedIn: 'root' })
export class ResumeSkillProfileService {
  constructor(private api: ApiService) {}

  get(resumeId: string): Observable<ResumeSkillProfile> {
    return this.api.get<ResumeSkillProfile>(`/v1/resumes/${resumeId}/skill-profile`);
  }

  generate(resumeId: string): Observable<ResumeSkillProfile> {
    return this.api.post<ResumeSkillProfile>(`/v1/resumes/${resumeId}/skill-profile/generate`, {});
  }

  update(resumeId: string, req: UpdateSkillProfileRequest): Observable<ResumeSkillProfile> {
    return this.api.put<ResumeSkillProfile>(`/v1/resumes/${resumeId}/skill-profile`, req);
  }
}
