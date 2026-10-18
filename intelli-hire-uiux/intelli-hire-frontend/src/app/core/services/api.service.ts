import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

interface BaseApiResponse<T> {
  success: boolean;
  error_code: string | null;
  data: T;
  errors: unknown[] | null;
  timestamp: string;
}

type QueryParamPrimitive = string | number | boolean;
type QueryParamValue =
  | QueryParamPrimitive
  | readonly QueryParamPrimitive[]
  | null
  | undefined;
type QueryParams = Record<string, QueryParamValue>;

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(path: string, params?: QueryParams): Observable<T> {
    const httpParams = this.buildHttpParams(params);
    return this.http.get<BaseApiResponse<T>>(`${this.baseUrl}${path}`, { params: httpParams })
      .pipe(map(res => res.data));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<BaseApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(res => res.data));
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<BaseApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(res => res.data));
  }

  patch<T>(path: string, body: unknown): Observable<T> {
    return this.http.patch<BaseApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(res => res.data));
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<BaseApiResponse<T> | null>(`${this.baseUrl}${path}`)
      .pipe(map(res => res?.data as T));
  }

  private buildHttpParams(params?: QueryParams): HttpParams | undefined {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }

      if (Array.isArray(value)) {
        value
          .filter(item => item !== undefined && item !== null && item !== '')
          .forEach(item => {
            httpParams = httpParams.append(key, String(item));
          });
        return;
      }

      httpParams = httpParams.set(key, String(value));
    });

    return httpParams;
  }
}
