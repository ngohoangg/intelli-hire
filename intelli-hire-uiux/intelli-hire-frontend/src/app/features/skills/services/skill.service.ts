import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Skill } from '../../../core/models/skill.model';

@Injectable({ providedIn: 'root' })
export class SkillService {
  constructor(private api: ApiService) {}

  list(category?: string): Observable<Skill[]> {
    const params = category ? { category } : undefined;
    return this.api.get<Skill[]>('/v1/skills', params);
  }

  getById(id: string): Observable<Skill> {
    return this.api.get<Skill>(`/v1/skills/${id}`);
  }

  search(q: string): Observable<Skill[]> {
    return this.api.get<Skill[]>('/v1/skills/search', { q });
  }
}
