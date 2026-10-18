import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, tap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { IdleService } from '../services/idle.service';

/** Returns true if the JWT token is expired or will expire within the next 30 seconds. */
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const expMs = payload['exp'] * 1000;
    return Date.now() >= expMs - 30_000;
  } catch {
    return false;
  }
}

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const auth = inject(AuthService);
  const idle = inject(IdleService);

  const addAuthHeaders = (r: HttpRequest<unknown>, token: string, accountId?: number) => {
    const headers: Record<string, string> = { Authorization: `Bearer ${token}` };
    if (accountId != null) {
      headers['X-User-Id'] = String(accountId);
    }
    return r.clone({ setHeaders: headers });
  };

  const token = auth.accessToken;
  const accountId = auth.currentUser?.accountId;

  // Proactive refresh: if token is about to expire, refresh before sending the request
  if (token && isTokenExpired(token) && auth.refreshToken && !req.url.includes('/v1/auth/refresh')) {
    return auth.refreshTokens().pipe(
      catchError(refreshErr => {
        auth.logout();
        return throwError(() => refreshErr);
      }),
      switchMap(res => {
        const refreshedReq = addAuthHeaders(req, res.access_token, auth.currentUser?.accountId);
        return next(refreshedReq).pipe(
          tap({ next: () => { if (auth.isLoggedIn) idle.resetTimer(); } })
        );
      })
    );
  }

  const authReq = token ? addAuthHeaders(req, token, accountId) : req;

  return next(authReq).pipe(
    tap({ next: () => { if (auth.isLoggedIn) idle.resetTimer(); } }),
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && err.error?.code === 'AUTH_1009') {
        idle.stop();
        auth.logout();
        return throwError(() => err);
      }
      if (err.status === 401 && auth.refreshToken && !req.url.includes('/v1/auth/refresh')) {
        return auth.refreshTokens().pipe(
          catchError(refreshErr => {
            auth.logout();
            return throwError(() => refreshErr);
          }),
          switchMap(res => next(addAuthHeaders(req, res.access_token, auth.currentUser?.accountId)))
        );
      }
      if (err.status === 401) {
        idle.stop();
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
