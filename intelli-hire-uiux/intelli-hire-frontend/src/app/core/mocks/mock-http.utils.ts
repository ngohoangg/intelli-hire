import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, of, throwError, timer } from 'rxjs';
import { delay, map, mergeMap } from 'rxjs/operators';

/** TEMPORARY helpers shared by the UI mock interceptors in this folder. */

export type MockResult = Observable<HttpEvent<unknown>> | null;

const LATENCY_MS = 300;

/** Returns the path after `/api/v1` (e.g. `/resumes/1`), or null for non-API requests. */
export function apiPath(url: string): string | null {
  const match = /^\/api\/v1(\/.*)$/.exec(url);
  return match ? match[1] : null;
}

/** Successful response wrapped in the backend's BaseApiResponse envelope. */
export function ok<T>(data: T, status = 200, latencyMs = LATENCY_MS): Observable<HttpEvent<unknown>> {
  const body = {
    success: true,
    error_code: null,
    data: structuredClone(data),
    errors: null,
    timestamp: new Date().toISOString()
  };
  return of(new HttpResponse({ status, body })).pipe(delay(latencyMs));
}

export function notFound(req: HttpRequest<unknown>, message: string): Observable<HttpEvent<unknown>> {
  const error = new HttpErrorResponse({
    status: 404,
    statusText: 'Not Found',
    url: req.url,
    error: { success: false, error_code: 'NOT_FOUND', data: null, errors: [message], timestamp: new Date().toISOString() }
  });
  return timer(LATENCY_MS).pipe(mergeMap(() => throwError(() => error)));
}

/** Sends the request to the real backend and rewrites `data` in its response. */
export function patchData<T>(req: HttpRequest<unknown>, next: HttpHandlerFn, patch: (data: T) => unknown): Observable<HttpEvent<unknown>> {
  return next(req).pipe(
    map(event => {
      if (!(event instanceof HttpResponse) || !event.body) return event;
      const body = event.body as { data: T };
      return event.clone({ body: { ...body, data: patch(body.data) } });
    })
  );
}

export function newId(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
}
