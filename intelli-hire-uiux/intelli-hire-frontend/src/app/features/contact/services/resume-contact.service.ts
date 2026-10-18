import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumeContact, ResumeContactRequest } from '../../../core/models/contact.model';

@Injectable({ providedIn: 'root' })
export class ResumeContactService {
  constructor(private api: ApiService) {}

  getByResumeId(resumeId: string): Observable<ResumeContact> {
    return this.api.get<ResumeContact>(`/v1/resumes/${resumeId}/contact`);
  }

  upsert(resumeId: string, request: ResumeContactRequest): Observable<ResumeContact> {
    return this.api.put<ResumeContact>(`/v1/resumes/${resumeId}/contact`, request);
  }
}
