import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { IdleService } from '../services/idle.service';

describe('authInterceptor — SESSION_INACTIVE handling', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authSpy: jasmine.SpyObj<AuthService>;
  let idleSpy: jasmine.SpyObj<IdleService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['logout', 'refreshTokens'], {
      isLoggedIn: true,
      accessToken: 'test-token',
      refreshToken: 'refresh-token',
      currentUser: { accountId: 1 }
    });
    idleSpy = jasmine.createSpyObj('IdleService', ['resetTimer', 'stop']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authSpy },
        { provide: IdleService, useValue: idleSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('SESSION_INACTIVE 401 calls logout without attempting refresh', () => {
    http.get('/api/test').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/test');
    req.flush(
      { code: 'AUTH_1009', message: 'Session expired due to inactivity' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(authSpy.logout).toHaveBeenCalledTimes(1);
    expect(authSpy.refreshTokens).not.toHaveBeenCalled();
    expect(idleSpy.stop).toHaveBeenCalledTimes(1);
  });

  it('non-SESSION_INACTIVE 401 attempts token refresh', () => {
    authSpy.refreshTokens.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401 }))
    );

    http.get('/api/test').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/test');
    req.flush(
      { code: 'AUTH_1001', message: 'Invalid credentials' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(authSpy.refreshTokens).toHaveBeenCalledTimes(1);
  });
});
