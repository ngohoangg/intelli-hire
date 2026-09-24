import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideTranslateLoader, provideTranslateService, TranslateService } from '@ngx-translate/core';

import { errorInterceptor } from '../interceptors/error.interceptor';
import { I18N_NAMESPACES } from './i18n-namespaces';
import { I18nTranslateLoader } from './i18n.loader';

describe('I18nTranslateLoader', () => {
  let translate: TranslateService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // Wired like app.config.ts: the real error interceptor injects TranslateService,
    // so a loader that fetched through the interceptor chain would hit NG0200 here.
    TestBed.configureTestingModule({
      providers: [
        provideNoopAnimations(),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        provideTranslateService({ fallbackLang: 'vi', loader: provideTranslateLoader(I18nTranslateLoader) })
      ]
    });
    translate = TestBed.inject(TranslateService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads every namespace file and nests it under its namespace', () => {
    let loaded = false;
    translate.use('vi').subscribe(() => (loaded = true));

    for (const ns of I18N_NAMESPACES) {
      httpMock.expectOne(`/i18n/vi/${ns}.json`).flush(ns === 'auth' ? { login: { title: 'Đăng nhập' } } : {});
    }

    expect(loaded).toBeTrue();
    expect(translate.instant('auth.login.title')).toBe('Đăng nhập');
  });

  it('degrades a failing namespace to empty without blanking the others', () => {
    translate.use('vi').subscribe();

    for (const ns of I18N_NAMESPACES) {
      const req = httpMock.expectOne(`/i18n/vi/${ns}.json`);
      if (ns === 'reports') {
        req.flush('missing', { status: 404, statusText: 'Not Found' });
      } else {
        req.flush(ns === 'auth' ? { login: { title: 'Đăng nhập' } } : {});
      }
    }

    expect(translate.instant('auth.login.title')).toBe('Đăng nhập');
  });
});
