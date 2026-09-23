// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, share, tap, finalize } from 'rxjs';
import { ApiService } from './api.service';
import { AuthUser, LoginRequest, LoginResponse, OAuthProvider, RegisterRequest, RegisterResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const AUTH_USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();
  private refreshInProgress$: Observable<LoginResponse> | null = null;

  constructor(private api: ApiService, private router: Router, private http: HttpClient) {}

  get isLoggedIn(): boolean { return !!this.currentUserSubject.value; }
  get currentUser(): AuthUser | null { return this.currentUserSubject.value; }
  get accessToken(): string | null { return localStorage.getItem(ACCESS_TOKEN_KEY); }
  get refreshToken(): string | null { return localStorage.getItem(REFRESH_TOKEN_KEY); }
  get isAdmin(): boolean { return this.currentUser?.roles.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN' || r === 'ADMIN' || r === 'SUPER_ADMIN') ?? false; }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/v1/auth/login', request).pipe(
      tap(res => this.saveSession(res))
    );
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.api.post<RegisterResponse>('/v1/auth/register', request);
  }

  logout(): void {
    const rt = this.refreshToken;
    if (rt) {
      this.api.post<void>('/v1/auth/logout', { refresh_token: rt }).subscribe({ error: () => {} });
    }
    this.clearSession();
    this.router.navigate(['/auth/login']);
  }

  refreshTokens(): Observable<LoginResponse> {
    if (this.refreshInProgress$) return this.refreshInProgress$;
    this.refreshInProgress$ = this.api.post<LoginResponse>('/v1/auth/refresh', { refresh_token: this.refreshToken }).pipe(
      tap(res => this.saveSession(res)),
      share(),
      finalize(() => { this.refreshInProgress$ = null; })
    );
    return this.refreshInProgress$;
  }

  changePassword(currentPassword: string, newPassword: string, confirmPassword: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/v1/accounts/me/change-password`,
      { currentPassword, newPassword, confirmPassword }
    );
  }

  forgotPassword(email: string): Observable<void> {
    return this.api.post<void>('/v1/auth/forgot-password', { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.api.post<void>('/v1/auth/reset-password', { token, new_password: newPassword });
  }

  verifyEmail(token: string): Observable<void> {
    return this.api.post<void>('/v1/auth/verify-email', { token });
  }

  getOAuthProviders(): Observable<OAuthProvider[]> {
    return this.api.get<OAuthProvider[]>('/v1/oauth/providers');
  }

  getOAuthAuthorizationUrl(provider: string) {
    return this.api.get<{ authorization_url: string; state: string }>(`/v1/oauth/${provider}/authorize`);
  }

  handleOAuthCallback(provider: string, code: string, state: string): Observable<LoginResponse> {
    return this.api.post<LoginResponse>(`/v1/oauth/${provider}/callback`, { code, state }).pipe(
      tap(res => this.saveSession(res))
    );
  }

  private saveSession(res: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, res.access_token);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refresh_token);
    const roles = this.extractRoles(res.access_token, res.roles);
    const user: AuthUser = { accountId: res.account_id, email: res.email, username: res.username, roles };
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private extractRoles(token: string, fallback: string[]): string[] {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      const fromToken: string[] = payload['roles'] ?? payload['authorities'] ?? [];
      return fromToken.length > 0 ? fromToken : (fallback ?? []);
    } catch {
      return fallback ?? [];
    }
  }

  private clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
    this.currentUserSubject.next(null);
  }

  private loadUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem(AUTH_USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}
