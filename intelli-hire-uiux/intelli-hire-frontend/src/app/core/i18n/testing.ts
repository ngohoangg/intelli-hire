import { Provider } from '@angular/core';
import { Observable, of } from 'rxjs';
import { TranslateLoader, provideTranslateLoader, provideTranslateService } from '@ngx-translate/core';

import { I18nNamespace } from './i18n-namespaces';

// Static imports of the REAL vi namespace files. As lanes fill these files in
// with actual keys, every spec using `provideI18nTesting()` picks up the new
// Vietnamese copy automatically — no test-fixture duplication to keep in
// sync. Requires `resolveJsonModule: true` (set in tsconfig.json).
import viCommon from '../../../../public/i18n/vi/common.json';
import viAuth from '../../../../public/i18n/vi/auth.json';
import viAccount from '../../../../public/i18n/vi/account.json';
import viAdmin from '../../../../public/i18n/vi/admin.json';
import viResume from '../../../../public/i18n/vi/resume.json';
import viQuestions from '../../../../public/i18n/vi/questions.json';
import viInterviews from '../../../../public/i18n/vi/interviews.json';
import viReports from '../../../../public/i18n/vi/reports.json';

const VI_NAMESPACE_CONTENT: Record<I18nNamespace, unknown> = {
  common: viCommon,
  auth: viAuth,
  account: viAccount,
  admin: viAdmin,
  resume: viResume,
  questions: viQuestions,
  interviews: viInterviews,
  reports: viReports,
};

/**
 * A TranslateLoader that resolves SYNCHRONOUSLY (via `of()`) instead of
 * hitting HTTP, using the statically-imported real `vi` namespace content
 * above. Synchronous resolution means a spec can assert translated text
 * immediately after `fixture.detectChanges()` — no `fakeAsync`/`tick()`,
 * no `HttpTestingController` flushing.
 */
class SyncViTranslateLoader extends TranslateLoader {
  getTranslation(): Observable<any> {
    return of(VI_NAMESPACE_CONTENT);
  }
}

/**
 * TestBed providers that make the `translate` pipe / `TranslateService` work
 * in Karma specs, synchronously, against the real `vi` JSON content.
 *
 * Usage:
 * ```ts
 * await TestBed.configureTestingModule({
 *   imports: [MyComponent],
 *   providers: [provideI18nTesting()],
 * }).compileComponents();
 * ```
 */
export function provideI18nTesting(): Provider[] {
  return provideTranslateService({
    lang: 'vi',
    fallbackLang: 'vi',
    loader: provideTranslateLoader(SyncViTranslateLoader),
  });
}
