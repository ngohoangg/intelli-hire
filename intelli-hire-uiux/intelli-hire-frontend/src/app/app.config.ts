import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { provideTranslateLoader, provideTranslateService } from '@ngx-translate/core';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { cvParseMockInterceptor } from './core/mocks/cv-parse-mock.interceptor';
import { interviewMockInterceptor } from './core/mocks/interview-mock.interceptor';
import { MockInterviewWebSocketService } from './core/mocks/mock-interview-websocket.service';
import { InterviewWebSocketService } from './features/interviews/services/interview-websocket.service';
import { I18nTranslateLoader } from './core/i18n/i18n.loader';
import { environment } from '../environments/environment';

// Vietnamese is the app's default/only shipped locale today — registered once
// at bootstrap so DatePipe/CurrencyPipe/DecimalPipe (and Material's date
// adapter, via MAT_DATE_LOCALE below) format using vi-VN rules.
registerLocaleData(localeVi);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([
      authInterceptor,
      ...(environment.mockCvParse ? [cvParseMockInterceptor] : []),
      ...(environment.mockInterview ? [interviewMockInterceptor] : []),
      errorInterceptor
    ])),
    ...(environment.mockInterview
      ? [{ provide: InterviewWebSocketService, useClass: MockInterviewWebSocketService }]
      : []),
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'outline',
        floatLabel: 'always'
      }
    },
    { provide: LOCALE_ID, useValue: 'vi' },
    { provide: MAT_DATE_LOCALE, useValue: 'vi' },
    provideTranslateService({
      lang: 'vi',
      fallbackLang: 'vi',
      loader: provideTranslateLoader(I18nTranslateLoader)
    })
  ]
};
