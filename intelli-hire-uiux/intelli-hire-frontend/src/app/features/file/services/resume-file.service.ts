import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ResumeFile } from '../../../core/models/resume-file.model';

interface BaseApiResponse<T> {
  success: boolean;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ResumeFileService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Returns raw HttpEvent stream for progress tracking; body will be BaseApiResponse<ResumeFile>
  upload(resumeId: string, file: File): Observable<HttpEvent<BaseApiResponse<ResumeFile>>> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest(
      'POST',
      `${this.baseUrl}/v1/resumes/${resumeId}/files`,
      formData,
      { reportProgress: true, responseType: 'json' }
    );
    return this.http.request<BaseApiResponse<ResumeFile>>(req);
  }

  getByResumeId(resumeId: string): Observable<ResumeFile> {
    return this.http.get<BaseApiResponse<ResumeFile>>(
      `${this.baseUrl}/v1/resumes/${resumeId}/files`
    ).pipe(map(res => res.data));
  }

  download(resumeId: string): Observable<ArrayBuffer> {
    return this.http.get(
      `${this.baseUrl}/v1/resumes/${resumeId}/files/download`,
      { responseType: 'arraybuffer' }
    );
  }

  delete(resumeId: string): Observable<void> {
    return this.http.delete<BaseApiResponse<void>>(
      `${this.baseUrl}/v1/resumes/${resumeId}/files`
    ).pipe(map(() => undefined as void));
  }
}
