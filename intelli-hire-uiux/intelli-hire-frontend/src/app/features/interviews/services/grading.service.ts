import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { GradingResponse } from '../../../core/models/grading.model';

@Injectable({ providedIn: 'root' })
export class GradingService {
  private readonly basePath = '/v1/gradings';

  constructor(private api: ApiService) {}

  getBySessionId(sessionId: string): Observable<GradingResponse> {
    return this.api.get<GradingResponse>(`${this.basePath}/session/${sessionId}`);
  }
}
