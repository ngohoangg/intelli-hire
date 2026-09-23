import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { cvParseMockInterceptor } from './core/mocks/cv-parse-mock.interceptor';
import { interviewMockInterceptor } from './core/mocks/interview-mock.interceptor';
import { MockInterviewWebSocketService } from './core/mocks/mock-interview-websocket.service';
import { InterviewWebSocketService } from './features/interviews/services/interview-websocket.service';
import { environment } from '../environments/environment';

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
    }
  ]
};
