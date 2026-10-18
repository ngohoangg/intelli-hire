import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ParseJob, CreateParseJobRequest } from '../../../core/models/parse-job.model';

@Injectable({ providedIn: 'root' })
export class ParseJobService {
  private readonly path = '/v1/parse-jobs';

  constructor(private api: ApiService) {}

  create(request: CreateParseJobRequest): Observable<ParseJob> {
    return this.api.post<ParseJob>(this.path, request);
  }

  getById(id: string): Observable<ParseJob> {
    return this.api.get<ParseJob>(`${this.path}/${id}`);
  }

  cancel(id: string): Observable<ParseJob> {
    return this.api.put<ParseJob>(`${this.path}/${id}/cancel`, {});
  }
}
