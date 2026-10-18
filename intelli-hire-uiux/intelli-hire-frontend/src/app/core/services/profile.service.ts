import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LinkedOAuthProvider } from '../models/auth.model';
import { Profile, UpdateProfileRequest } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private api: ApiService) {}

  getMyProfile(): Observable<Profile> {
    return this.api.get<Profile>('/v1/profiles/me');
  }

  updateMyProfile(request: UpdateProfileRequest): Observable<Profile> {
    return this.api.put<Profile>('/v1/profiles/me', request);
  }

  getLinkedProviders(): Observable<LinkedOAuthProvider[]> {
    return this.api.get<LinkedOAuthProvider[]>('/v1/oauth/me/providers');
  }

  unlinkProvider(provider: string): Observable<void> {
    return this.api.delete<void>(`/v1/oauth/${provider}/unlink`);
  }
}
