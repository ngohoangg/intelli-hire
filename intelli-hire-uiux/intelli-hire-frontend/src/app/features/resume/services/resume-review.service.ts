import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeReview } from '../../../core/models/resume-review.model';

@Injectable({ providedIn: 'root' })
export class ResumeReviewService {
  constructor(private api: ApiService) {}

  get(resumeId: string): Observable<ResumeReview> {
    return this.api.get<ResumeReview>(`/v1/resumes/${resumeId}/review`);
  }

  generate(resumeId: string): Observable<ResumeReview> {
    return this.api.post<ResumeReview>(`/v1/resumes/${resumeId}/review/generate`, {});
  }
}
